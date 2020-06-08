import { ISecurityGroup, IVpc, Port, SubnetType } from '@aws-cdk/aws-ec2';
import {
  AssetImage,
  AwsLogDriver,
  Cluster,
  ContainerImage,
  FargatePlatformVersion,
  FargateService,
  FargateTaskDefinition,
  Secret,
} from '@aws-cdk/aws-ecs';
import {
  ApplicationListener,
  ApplicationProtocol,
  ApplicationTargetGroup,
  Protocol,
} from '@aws-cdk/aws-elasticloadbalancingv2';
import { PolicyStatement } from '@aws-cdk/aws-iam';
import { LogGroup, RetentionDays } from '@aws-cdk/aws-logs';
import { IParameter, IStringParameter, StringParameter } from '@aws-cdk/aws-ssm';
import { CfnOutput, Construct, Duration, RemovalPolicy } from '@aws-cdk/core';
import { randomBytes } from 'crypto';
import { BaseStack, iname, IStackProps } from './cdk-native';
import path = require('path');

/**
 * AppStack config properties.
 */
export interface IAppStackProps extends IStackProps {
  /**
   * The VPC ref
   */
  readonly vpc: IVpc;

  readonly cluster: Cluster;

  readonly appImage?: ContainerImage;

  readonly lbListener: ApplicationListener;

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
 *
 * Houses the web app in a Docker container on the AWS Fargate platform
 * and binds the generated web app container to an already created
 * app load balancer listener.
 */
export class AppStack extends BaseStack {
  /**
   * The container name to use in CICD when referencing the container in the buidlspec.
   */
  public readonly containerName: string;

  public readonly fargateSvc: FargateService;

  public readonly webContainerLogGrp: LogGroup;

  public readonly albTargetGroup: ApplicationTargetGroup;

  constructor(scope: Construct, id: string, props: IAppStackProps) {
    super(scope, id, props);

    // generate JWT salt ssm param
    const rhs = randomBytes(32).toString('hex');
    const jwtSaltInstNme = iname('jwtSalt', props);
    const jwtSalt: IParameter = new StringParameter(this, jwtSaltInstNme, {
      parameterName: `/${jwtSaltInstNme}`,
      stringValue: rhs,
    });

    // task def
    const taskDefInstNme = iname('fargate-taskdef', props);
    const taskDef = new FargateTaskDefinition(this, taskDefInstNme, {
      cpu: props.taskdefCpu,
      memoryLimitMiB: props.taskdefMemoryLimitMiB,
    });
    taskDef.addToExecutionRolePolicy(
      new PolicyStatement({
        actions: ['ssm:GetParameter'],
        resources: [props.ssmJdbcUrl.parameterArn, props.ssmJdbcTestUrl.parameterArn, jwtSalt.parameterArn],
      })
    );

    // web app container log group
    this.webContainerLogGrp = new LogGroup(this, iname('webapp', props), {
      retention: RetentionDays.ONE_WEEK,
      logGroupName: `webapp-${props.appEnv}`,
      removalPolicy: RemovalPolicy.DESTROY,
    });

    const appImage = props.appImage || new AssetImage(path.join(__dirname, '../mcorpus-gql/target/awsdockerasset'));

    this.containerName = iname('gql', props);
    const containerDef = taskDef.addContainer(this.containerName, {
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

        MCORPUS_JWT_STATUS_CACHE_MAX_SIZE: '5',
        MCORPUS_JWT_STATUS_CACHE_TIMEOUT_IN_MINUTES: '5',
        MCORPUS_JWT_TTL_IN_SECONDS: '172800',

        MCORPUS_METRICS_ON: 'true',
        MCORPUS_GRAPHIQL: 'true',
        MCORPUS_HTTP_CLIENT_ORIGIN: '',

        MCORPUS_SERVER__DEVELOPMENT: 'false',
        MCORPUS_SERVER__PORT: `${props.lbToEcsPort}`,
        MCORPUS_SERVER__PUBLIC_ADDRESS: props.webAppUrl,
      },
      secrets: {
        MCORPUS_DB_URL: Secret.fromSsmParameter(props.ssmJdbcUrl),
        MCORPUS_TEST_DB_URL: Secret.fromSsmParameter(props.ssmJdbcTestUrl),
        MCORPUS_JWT_SALT: Secret.fromSsmParameter(jwtSalt),
      },
      logging: new AwsLogDriver({
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
    this.fargateSvc = new FargateService(this, fargateSvcInstNme, {
      cluster: props.cluster,
      taskDefinition: taskDef,
      desiredCount: 1,
      assignPublicIp: false,
      healthCheckGracePeriod: Duration.seconds(15),
      vpcSubnets: { subnetType: SubnetType.PRIVATE },
      serviceName: fargateSvcInstNme,
      platformVersion: FargatePlatformVersion.LATEST,
      securityGroup: props.ecsSecGrp,
    });

    // bind load balancing target to lb group
    const albTargetGroupInstNme = iname('fargate-target', props);
    this.albTargetGroup = props.lbListener.addTargets(albTargetGroupInstNme, {
      targetGroupName: albTargetGroupInstNme,
      port: props.lbToEcsPort,
      targets: [this.fargateSvc],
      healthCheck: {
        protocol: Protocol.HTTP,
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
    new CfnOutput(this, 'fargateTaskDefArn', {
      value: taskDef.taskDefinitionArn,
    });
    new CfnOutput(this, 'fargateServiceName', {
      value: this.fargateSvc.serviceName,
    });
    new CfnOutput(this, 'webAppLogGroupName', {
      value: this.webContainerLogGrp.logGroupName,
    });
    new CfnOutput(this, 'containerName', {
      value: this.containerName,
    });
    new CfnOutput(this, 'appLoadBalancerTargetGroupName', {
      value: this.albTargetGroup.targetGroupName,
    });
  }
}
