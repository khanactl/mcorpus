import cdk = require('@aws-cdk/core');
import iam = require('@aws-cdk/aws-iam');
import ec2 = require('@aws-cdk/aws-ec2');
import ecs = require('@aws-cdk/aws-ecs');
import elb = require('@aws-cdk/aws-elasticloadbalancingv2');
import ssm = require('@aws-cdk/aws-ssm');
import { ISecurityGroup, SubnetType } from '@aws-cdk/aws-ec2';
import { IStringParameter } from '@aws-cdk/aws-ssm';
import { FargatePlatformVersion, FargateService, EcrImage } from '@aws-cdk/aws-ecs';
import { ApplicationProtocol, SslPolicy, TargetType } from '@aws-cdk/aws-elasticloadbalancingv2';
import { Duration } from '@aws-cdk/core';
import path = require('path');
import ecr = require('@aws-cdk/aws-ecr');
import * as crypto from 'crypto';
import { DockerImageAsset } from '@aws-cdk/aws-ecr-assets';

/**
 * ECS Stack config properties.
 */
export interface IECSProps extends cdk.StackProps {
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
  
  readonly ssmJdbcUrl: IStringParameter;

  readonly ssmJdbcTestUrl: IStringParameter;
  
  readonly lbSecGrp: ISecurityGroup;

  readonly ecsSecGrp: ISecurityGroup;

  readonly webAppUrl: string;
  /**
   * The JAVA_OPTS docker container env var value to use.
   */
  readonly javaOpts: string;
}

/**
 * ECS Stack.
 */
export class ECSStack extends cdk.Stack {

  public readonly ecrRepo: ecr.IRepository;

  public readonly ecsTaskExecutionRole: iam.Role;
  
  public readonly fargateSvc: FargateService;

  constructor(scope: cdk.Construct, id: string, props: IECSProps) {
    super(scope, id, props);

    // generate JWT salt ssm param
    const rhs = crypto.randomBytes(32).toString('hex');
    const jwtSalt:ssm.IParameter = new ssm.StringParameter(this, 'jwtSalt', {
      parameterName: '/jwtSalt', 
      stringValue: rhs, 
    });

    // ECS/Fargate task execution role
    this.ecsTaskExecutionRole = new iam.Role(this, 'ecsTaskExecutionRole', {
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
    const dockerAsset = new DockerImageAsset(this, 'mcorpus-docker-asset', {
      directory: path.join(__dirname, "../../mcorpus-gql/target"), 
      repositoryName: 'mcorpus', 
    });
    this.ecrRepo = dockerAsset.repository;

    // task def
    const taskDef = new ecs.FargateTaskDefinition(this, 'mcorpus-gql', {
      cpu: 256,
      memoryLimitMiB: 1024,
      taskRole: this.ecsTaskExecutionRole, 
      executionRole: this.ecsTaskExecutionRole, 
    });

    const containerDef = taskDef.addContainer('mcorpus-fargate-container', {
      /*
      image: ecs.ContainerImage.fromAsset(
        path.join(__dirname, "../../mcorpus-gql/target")
      ),
      */
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
      logging: new ecs.AwsLogDriver({ streamPrefix: 'mcorpus-fargate-webapp' }), 
    });
    containerDef.addPortMappings({ 
      containerPort: props.lbToEcsPort, 
    });

    // cluster
    const cluster = new ecs.Cluster(this, 'mcorpus-ecs-cluster', {
      vpc: props.vpc, 
      clusterName: 'mcorpus-ecs-cluster', 
    });

    this.fargateSvc = new ecs.FargateService(this, 'mcorpus-fargate-service', {
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
    const alb = new elb.ApplicationLoadBalancer(this, 'app-load-balancer', {
      vpc: props.vpc,
      internetFacing: true,
      securityGroup: props.lbSecGrp, 
    });

    const listener = alb.addListener('alb-tls-listener', {
      protocol: ApplicationProtocol.HTTPS, 
      port: 443,
      certificateArns: [ props.sslCertArn ],
      sslPolicy: SslPolicy.RECOMMENDED,
      // defaultTargetGroups: []
    });

    /*
    const albTargetGroup = new elb.ApplicationTargetGroup(this, 'alb-target-group', {
      vpc: props.vpc,
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
      port: props.lbToEcsPort,
      protocol: ApplicationProtocol.HTTP,
      targetType: TargetType.IP
    });
    */
    /*
    listener.addTargetGroups('app-lb-tgtrp', {
      targetGroups: [ albTargetGroup ], 
    });
    */
    // bind load balancing target to lb group
    // this.fargateSvc.attachToApplicationTargetGroup(albTargetGroup);
    const albTargetGroup = listener.addTargets('mcorpus-fargate-target', {
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
    
    // stack output
    new cdk.CfnOutput(this, 'loadBalancerDnsName', { value: 
      alb.loadBalancerDnsName
    });
    new cdk.CfnOutput(this, 'fargateTaskExecRoleName', { value: 
      this.ecsTaskExecutionRole.roleName
    });
    new cdk.CfnOutput(this, 'fargateTaskDefArn', { value: 
      taskDef.taskDefinitionArn
    });
    new cdk.CfnOutput(this, 'fargateServiceName', { value: 
      this.fargateSvc.serviceName
    });
    
  }
}
