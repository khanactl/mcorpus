import cdk = require('@aws-cdk/core');
import { ISecurityGroup, Port, SubnetType } from '@aws-cdk/aws-ec2';
import { DockerImageAsset } from '@aws-cdk/aws-ecr-assets';
import { FargatePlatformVersion, FargateService, LogDrivers } from '@aws-cdk/aws-ecs';
import { ApplicationProtocol, SslPolicy } from '@aws-cdk/aws-elasticloadbalancingv2';
import { IStringParameter } from '@aws-cdk/aws-ssm';
import { Duration } from '@aws-cdk/core';
import { randomBytes } from 'crypto';
import { BaseStack, IStackProps } from './cdk-native';
import iam = require('@aws-cdk/aws-iam');
import ec2 = require('@aws-cdk/aws-ec2');
import ecs = require('@aws-cdk/aws-ecs');
import elb = require('@aws-cdk/aws-elasticloadbalancingv2');
import ssm = require('@aws-cdk/aws-ssm');
import r53 = require('@aws-cdk/aws-route53');
import path = require('path');
import ecr = require('@aws-cdk/aws-ecr');
import alias = require('@aws-cdk/aws-route53-targets');
import logs = require('@aws-cdk/aws-logs');

/**
 * ECS Stack config properties.
 */
export interface IECSProps extends IStackProps {
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;
  /**
   * SSL Certificate Arn
   */
  readonly sslCertArn: string;
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
  /**
   * The domain name registered in AWS Route53 and the one used for this web app.
   *
   * This will connect the public to this app!
   */
  readonly publicDomainName?: string;
  /**
   * The AWS Route53 Hosted Zone Id.
   */
  readonly awsHostedZoneId?: string;
}

/**
 * ECS Stack.
 */
export class ECSStack extends BaseStack {

  public readonly ecrRepo: ecr.IRepository;

  public readonly ecsTaskExecutionRole: iam.Role;

  public readonly fargateSvc: FargateService;

  public readonly webContainerLogGrp: logs.LogGroup;

  public readonly appLoadBalancer: elb.ApplicationLoadBalancer;

  /**
   * The container name to use in CICD when referencing the container in the buidlspec.
   */
  public readonly containerName: string;

  constructor(scope: cdk.Construct, props: IECSProps) {
    super(scope, 'ECS', props);

    // generate JWT salt ssm param
    const rhs = randomBytes(32).toString('hex');
    const jwtSaltInstNme = this.iname('jwtSalt');
    const jwtSalt:ssm.IParameter = new ssm.StringParameter(this, jwtSaltInstNme, {
      parameterName: `/${jwtSaltInstNme}`,
      stringValue: rhs,
    });

    // ECS/Fargate task execution role
    const escTskExecInstNme = this.iname('ecs-tsk-exec-role');
    this.ecsTaskExecutionRole = new iam.Role(this, escTskExecInstNme, {
      assumedBy: new iam.ServicePrincipal('ecs-tasks.amazonaws.com')
    });
    this.ecsTaskExecutionRole.addManagedPolicy({
      managedPolicyArn: 'arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy'
    });
    this.ecsTaskExecutionRole.addToPolicy(new iam.PolicyStatement({
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
    }));

    // ecr repo
    const dockerAssetInstNme = this.iname('docker-asset');
    const dockerAsset = new DockerImageAsset(this, dockerAssetInstNme, {
      directory: path.join(__dirname, "../../mcorpus-gql/target"),
      repositoryName: 'mcorpus-gql',
    });
    this.ecrRepo = dockerAsset.repository;

    // task def
    const taskDefInstNme = this.iname('fargate-taskdef');
    const taskDef = new ecs.FargateTaskDefinition(this, taskDefInstNme, {
      cpu: 256,
      memoryLimitMiB: 1024,
      taskRole: this.ecsTaskExecutionRole,
      executionRole: this.ecsTaskExecutionRole,
    });

    // web app container log group
    this.webContainerLogGrp = new logs.LogGroup(this, this.iname("webapp"), {
      retention: logs.RetentionDays.ONE_WEEK,
      logGroupName: `webapp-${this.appEnv}`,
      removalPolicy: cdk.RemovalPolicy.DESTROY,
    });

    this.containerName = this.iname('gql');
    const containerDef = taskDef.addContainer(this.containerName, {
     image: ecs.ContainerImage.fromEcrRepository(this.ecrRepo),
      healthCheck: {
        command: [`curl -f -s http://localhost:${props.lbToEcsPort}/health/ || exit 1`],
        interval: Duration.seconds(120),
        timeout: Duration.seconds(5),
        startPeriod: Duration.seconds(15),
        retries: 3,
      },
      memoryLimitMiB: 900,
      memoryReservationMiB: 500,
      essential: true,
      environment: {
        'JAVA_OPTS' : props.javaOpts,
        'MCORPUS_COOKIE_SECURE' : 'true',
        'MCORPUS_DB_DATA_SOURCE_CLASS_NAME' : 'org.postgresql.ds.PGSimpleDataSource',
        'MCORPUS_RST_TTL_IN_SECONDS' : '1800',
        'MCORPUS_JWT_STATUS_CACHE_MAX_SIZE' : '60',
        'MCORPUS_JWT_STATUS_CACHE_TIMEOUT_IN_MINUTES' : '10',
        'MCORPUS_JWT_TTL_IN_SECONDS' : '172800',
        'MCORPUS_SERVER__DEVELOPMENT' : 'false',
        'MCORPUS_SERVER__PORT' : `${props.lbToEcsPort}`,
        'MCORPUS_SERVER__PUBLIC_ADDRESS' : props.webAppUrl,
      },
      secrets: {
        'MCORPUS_DB_URL' : ecs.Secret.fromSsmParameter(props.ssmJdbcUrl),
        'MCORPUS_TEST_DB_URL' : ecs.Secret.fromSsmParameter(props.ssmJdbcTestUrl),
        'MCORPUS_JWT_SALT' : ecs.Secret.fromSsmParameter(jwtSalt),
      },
      logging: new ecs.AwsLogDriver({
        streamPrefix: this.iname('webapplogs'),
        logGroup: this.webContainerLogGrp,
      }),
    });
    containerDef.addPortMappings({
      containerPort: props.lbToEcsPort,
    });

    // cluster
    const ecsClusterInstNme = this.iname('ecs-cluster');
    const cluster = new ecs.Cluster(this, ecsClusterInstNme, {
      vpc: props.vpc,
      clusterName: ecsClusterInstNme,
    });

    // sec grp rule: lb to ecs container traffic
    props.ecsSecGrp.addIngressRule(
      props.lbSecGrp,
      Port.tcp(props.lbToEcsPort),
      'lb to ecs container traffic'
    );

    const fargateSvcInstNme = this.iname('fargate-svc');
    this.fargateSvc = new ecs.FargateService(this, fargateSvcInstNme, {
      cluster: cluster,
      taskDefinition: taskDef,
      desiredCount: 1,
      assignPublicIp: false,
      healthCheckGracePeriod: Duration.seconds(15),
      vpcSubnets: { subnetType: SubnetType.PRIVATE },
      // serviceName: 'mcorpus-fargate-service',
      platformVersion: FargatePlatformVersion.LATEST,
      securityGroup: props.ecsSecGrp,
    });

    // ****************************
    // *** inline load balancer ***
    // ****************************
    // application load balancer
    const albInstNme = this.iname('app-loadbalancer');
    this.appLoadBalancer = new elb.ApplicationLoadBalancer(this, albInstNme, {
      vpc: props.vpc,
      internetFacing: true,
      securityGroup: props.lbSecGrp,
    });

    const listenerInstNme = this.iname('alb-tls-listener');
    const listener = this.appLoadBalancer.addListener(listenerInstNme, {
      protocol: ApplicationProtocol.HTTPS,
      port: 443,
      certificateArns: [ props.sslCertArn ],
      sslPolicy: SslPolicy.RECOMMENDED,
      // defaultTargetGroups: []
    });

    // bind load balancing target to lb group
    // this.fargateSvc.attachToApplicationTargetGroup(albTargetGroup);
    const albTargetGroupInstNme = this.iname('fargate-target');
    const albTargetGroup = listener.addTargets(albTargetGroupInstNme, {
      // targetGroupName: '',
      port: props.lbToEcsPort,
      targets: [
        this.fargateSvc,
      ],
      healthCheck: {
        // port: String(props.innerPort),
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
    // ****************************
    // *** END inline load balancer ***
    // ****************************

    // DNS bind load balancer to domain name record
    if(props.awsHostedZoneId && props.publicDomainName) {
      // console.log('Load balancer DNS will be bound in Route53.');
      const hostedZone = r53.HostedZone.fromHostedZoneAttributes(
        this, this.iname('hostedzone'), {
          hostedZoneId: props.awsHostedZoneId,
          zoneName: props.publicDomainName,
        }
      );
      // NOTE: arecord creation will fail if it already exists
      const arecordInstNme = this.iname('arecord');
      const arecord = new r53.ARecord(this, arecordInstNme, {
        recordName: props.publicDomainName,
        zone: hostedZone,
        target: r53.RecordTarget.fromAlias(new alias.LoadBalancerTarget(this.appLoadBalancer)),
      });
      // dns specific stack output
      new cdk.CfnOutput(this, 'HostedZone', { value:
        hostedZone.hostedZoneId
      });
      new cdk.CfnOutput(this, 'ARecord', { value:
        arecord.domainName
      });
    }

    // stack output
    new cdk.CfnOutput(this, 'fargateTaskExecRoleName', { value:
      this.ecsTaskExecutionRole.roleName
    });
    new cdk.CfnOutput(this, 'fargateTaskDefArn', { value:
      taskDef.taskDefinitionArn
    });
    new cdk.CfnOutput(this, 'fargateServiceName', { value:
      this.fargateSvc.serviceName
    });
    new cdk.CfnOutput(this, 'webAppLogGroupName', { value:
      this.webContainerLogGrp.logGroupName
    });
    new cdk.CfnOutput(this, 'containerName', { value:
      this.containerName
    });
    new cdk.CfnOutput(this, 'loadBalancerDnsName', { value:
      this.appLoadBalancer.loadBalancerDnsName
    });

  }
}
