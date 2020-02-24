import cdk = require('@aws-cdk/core');
import { ISecurityGroup, Port, SubnetType } from '@aws-cdk/aws-ec2';
import { FargatePlatformVersion, FargateService } from '@aws-cdk/aws-ecs';
import { ApplicationProtocol } from '@aws-cdk/aws-elasticloadbalancingv2';
import { IStringParameter } from '@aws-cdk/aws-ssm';
import { Duration } from '@aws-cdk/core';
import { randomBytes } from 'crypto';
import { BaseStack, iname, IStackProps } from './cdk-native';
import iam = require('@aws-cdk/aws-iam');
import ec2 = require('@aws-cdk/aws-ec2');
import ecs = require('@aws-cdk/aws-ecs');
import elb = require('@aws-cdk/aws-elasticloadbalancingv2');
import ssm = require('@aws-cdk/aws-ssm');
import path = require('path');
import logs = require('@aws-cdk/aws-logs');

/**
 * ECS Stack config properties.
 */
export interface IAppStackProps extends IStackProps {
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;

  readonly cluster: ecs.Cluster;

  readonly appImage?: ecs.ContainerImage;

  readonly lbListener: elb.ApplicationListener;

  /**
   * The [docker] container task def cpu setting.
   */
  readonly taskdefCpu: number;
  /**
   * The [docker] container task def memory in MB setting.
   */
  readonly taskdefMemoryLimitMiB: number;
  /**
   * The [docker] container def memory *limit* in MB.
   */
  readonly containerDefMemoryLimitMiB: number;
  /**
   * The [docker] container def memory *reservation* in MB.
   */
  readonly containerDefMemoryReservationMiB: number;

  /**
   * The inner or traffic port used to send traffic
   * from the load balancer to the ecs/fargate service.
   */
  readonly lbToEcsPort: number;

  // readonly ssmKmsArn: string;
  /**
   * The SSM secure param of the web app jdbc url to the backend db.
   */
  readonly ssmJdbcUrl: IStringParameter;
  /**
   * The SSM secure param of the web app jdbc url to the backend db.
   */
  readonly ssmJdbcTestUrl: IStringParameter;
  /**
   * The load balancer security group ref.
   */
  readonly lbSecGrp: ISecurityGroup;
  /**
   * The ECS/Fargate container security group ref.
   */
  readonly ecsSecGrp: ISecurityGroup;
  /**
   * The web app domain name URL used for server-issued http 302 redirects
   * and cookie/jwt verifications.
   */
  readonly webAppUrl: string;
  /**
   * The JAVA_OPTS docker container env var value to use.
   */
  readonly javaOpts: string;
}

/**
 * AppStack.
 */
export class AppStack extends BaseStack {
  /**
   * The container name to use in CICD when referencing the container in the buidlspec.
   */
  public readonly containerName: string;

  public readonly ecsTaskExecutionRole: iam.Role;

  public readonly fargateSvc: FargateService;

  public readonly webContainerLogGrp: logs.LogGroup;

  constructor(scope: cdk.Construct, id: string, props: IAppStackProps) {
    super(scope, id, props);

    // get the ECR handle
    // const ecrRef = ecr.Repository.fromRepositoryArn(this, "ecr-repo-ref", props.ecrArn);

    // generate JWT salt ssm param
    const rhs = randomBytes(32).toString('hex');
    const jwtSaltInstNme = iname('jwtSalt', props);
    const jwtSalt: ssm.IParameter = new ssm.StringParameter(this, jwtSaltInstNme, {
      parameterName: `/${jwtSaltInstNme}`,
      stringValue: rhs,
    });

    // ECS/Fargate task execution role
    const escTskExecInstNme = iname('ecs-tsk-exec-role', props);
    this.ecsTaskExecutionRole = new iam.Role(this, escTskExecInstNme, {
      assumedBy: new iam.ServicePrincipal('ecs-tasks.amazonaws.com'),
    });
    this.ecsTaskExecutionRole.addManagedPolicy({
      managedPolicyArn: 'arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy',
    });
    this.ecsTaskExecutionRole.addToPolicy(
      new iam.PolicyStatement({
        actions: [
          /*
          'kms:Decrypt',
          'kms:DescribeKey',
          'kms:DescribeParamters',
          'kms:GetParamters',
          */
          'ssm:GetParameter',
        ],
        resources: [
          props.ssmJdbcUrl.parameterArn,
          props.ssmJdbcTestUrl.parameterArn,
          jwtSalt.parameterArn,
          // props.ssmKmsArn, // TODO fix
        ],
      })
    );

    // task def
    const taskDefInstNme = iname('fargate-taskdef', props);
    const taskDef = new ecs.FargateTaskDefinition(this, taskDefInstNme, {
      cpu: props.taskdefCpu,
      memoryLimitMiB: props.taskdefMemoryLimitMiB,
      taskRole: this.ecsTaskExecutionRole,
      executionRole: this.ecsTaskExecutionRole,
    });

    // web app container log group
    this.webContainerLogGrp = new logs.LogGroup(this, iname('webapp', props), {
      retention: logs.RetentionDays.ONE_WEEK,
      logGroupName: `webapp-${props.appEnv}`,
      removalPolicy: cdk.RemovalPolicy.DESTROY,
    });

    const appImage = props.appImage || new ecs.AssetImage(path.join(__dirname, '../mcorpus-gql/target/awsdockerasset'));

    this.containerName = iname('gql', props);
    const containerDef = taskDef.addContainer(this.containerName, {
      // image: ecs.ContainerImage.fromEcrRepository(ecrRef, props.ecrRepoTargetTag),
      image: appImage,
      healthCheck: {
        command: [`curl -f -s http://localhost:${props.lbToEcsPort}/health/ || exit 1`],
        interval: Duration.seconds(120),
        timeout: Duration.seconds(5),
        startPeriod: Duration.seconds(15),
        retries: 3,
      },
      memoryLimitMiB: props.containerDefMemoryLimitMiB,
      memoryReservationMiB: props.containerDefMemoryReservationMiB,
      essential: true,
      environment: {
        JAVA_OPTS: props.javaOpts,
        MCORPUS_COOKIE_SECURE: 'true',
        MCORPUS_DB_DATA_SOURCE_CLASS_NAME: 'org.postgresql.ds.PGSimpleDataSource',
        MCORPUS_RST_TTL_IN_SECONDS: '1800',
        MCORPUS_JWT_STATUS_CACHE_MAX_SIZE: '10',
        MCORPUS_JWT_STATUS_CACHE_TIMEOUT_IN_MINUTES: '5',
        MCORPUS_JWT_TTL_IN_SECONDS: '172800',
        MCORPUS_SERVER__DEVELOPMENT: 'false',
        MCORPUS_SERVER__PORT: `${props.lbToEcsPort}`,
        MCORPUS_SERVER__PUBLIC_ADDRESS: props.webAppUrl,
      },
      secrets: {
        MCORPUS_DB_URL: ecs.Secret.fromSsmParameter(props.ssmJdbcUrl),
        MCORPUS_TEST_DB_URL: ecs.Secret.fromSsmParameter(props.ssmJdbcTestUrl),
        MCORPUS_JWT_SALT: ecs.Secret.fromSsmParameter(jwtSalt),
      },
      logging: new ecs.AwsLogDriver({
        streamPrefix: iname('webapplogs', props),
        logGroup: this.webContainerLogGrp,
      }),
    });
    containerDef.addPortMappings({
      containerPort: props.lbToEcsPort,
    });

    // sec grp rule: lb to ecs container traffic
    props.ecsSecGrp.addIngressRule(props.lbSecGrp, Port.tcp(props.lbToEcsPort), 'lb to ecs container traffic');

    const fargateSvcInstNme = iname('fargate-svc', props);
    this.fargateSvc = new ecs.FargateService(this, fargateSvcInstNme, {
      cluster: props.cluster,
      taskDefinition: taskDef,
      desiredCount: 1,
      assignPublicIp: false,
      healthCheckGracePeriod: Duration.seconds(15),
      vpcSubnets: { subnetType: SubnetType.PRIVATE },
      // serviceName: 'mcorpus-fargate-service',
      platformVersion: FargatePlatformVersion.LATEST,
      securityGroup: props.ecsSecGrp,
    });

    // bind load balancing target to lb group
    const albTargetGroupInstNme = iname('fargate-target', props);
    const albTargetGroup = props.lbListener.addTargets(albTargetGroupInstNme, {
      targetGroupName: albTargetGroupInstNme,
      port: props.lbToEcsPort,
      targets: [this.fargateSvc],
      healthCheck: {
        protocol: elb.Protocol.HTTP,
        path: '/health',
        port: 'traffic-port',
        healthyThresholdCount: 5,
        unhealthyThresholdCount: 2,
        timeout: Duration.seconds(20),
        interval: Duration.seconds(120),
      },
      protocol: ApplicationProtocol.HTTP,
    });

    // stack output
    new cdk.CfnOutput(this, 'fargateTaskExecRoleName', {
      value: this.ecsTaskExecutionRole.roleName,
    });
    new cdk.CfnOutput(this, 'fargateTaskDefArn', {
      value: taskDef.taskDefinitionArn,
    });
    new cdk.CfnOutput(this, 'fargateServiceName', {
      value: this.fargateSvc.serviceName,
    });
    new cdk.CfnOutput(this, 'webAppLogGroupName', {
      value: this.webContainerLogGrp.logGroupName,
    });
    new cdk.CfnOutput(this, 'containerName', {
      value: this.containerName,
    });
  }
}
