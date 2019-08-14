import cdk = require('@aws-cdk/core');
import iam = require('@aws-cdk/aws-iam');
import ec2 = require('@aws-cdk/aws-ec2');
import ecs = require('@aws-cdk/aws-ecs');
import elb = require('@aws-cdk/aws-elasticloadbalancingv2');
import ssm = require('@aws-cdk/aws-ssm');
import secrets = require('@aws-cdk/aws-secretsmanager');
import { ISecurityGroup } from '@aws-cdk/aws-ec2';
import { FargatePlatformVersion, FargateService } from '@aws-cdk/aws-ecs';
import { ApplicationProtocol, SslPolicy, TargetType } from '@aws-cdk/aws-elasticloadbalancingv2';
import { Duration } from '@aws-cdk/core';
import path = require('path');

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

  readonly ssmKmsArn: string;
  
  readonly ssmMcorpusDbUrl: ssm.IParameter;
  
  readonly lbSecGrp: ISecurityGroup;

  readonly ecsSecGrp: ISecurityGroup;
}

/**
 * ECS Stack.
 */
export class ECSStack extends cdk.Stack {

  public readonly ecsTaskExecutionRole: iam.Role;
  
  public readonly fargateSvc: FargateService;

  constructor(scope: cdk.Construct, id: string, props: IECSProps) {
    super(scope, id, props);

     const jwtSalt:ssm.IParameter = new ssm.StringParameter(this, 'jwtSalt', {
       stringValue: 'TODO'
     });

    // ECS/Fargate task execution role
    const ssmAccess = new iam.PolicyStatement({
      actions: [ 
        'kms:Decript', 
        'kms:DescribeKey',
        'kms:DescribeParamters',
        'kms:GetParamters',
        'ssm:GetParameter',
        'ssm:GetParameters',
        // TODO add for secrets manager
      ],
      resources: [
        props.ssmMcorpusDbUrl.parameterArn,
        jwtSalt.parameterArn, 
        props.ssmKmsArn
      ],
    });
    this.ecsTaskExecutionRole = new iam.Role(this, 'ecsTaskExecutionRole', {
      assumedBy: new iam.ServicePrincipal('ecs-tasks.amazonaws.com')
    });
    this.ecsTaskExecutionRole.addManagedPolicy({
      managedPolicyArn: 'arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy'
    });
    ssmAccess.effect = iam.Effect.ALLOW;
    this.ecsTaskExecutionRole.addToPolicy(ssmAccess);
    
    // task def
    const taskDef = new ecs.FargateTaskDefinition(this, 'mcorpus-gql', {
      cpu: 256,
      memoryLimitMiB: 1024,
      taskRole: this.ecsTaskExecutionRole, 
      executionRole: this.ecsTaskExecutionRole, 
    });

    const containerDef = taskDef.addContainer('mcorpus-fargate-container', {
      image: ecs.ContainerImage.fromAsset(
        path.join(__dirname, "..", "..", "mcorpus-gql", "target")
        // '../mcorpus-gql/target'
      ), 
      healthCheck: {
        command: [`curl -f -s http://localhost:${props.lbToEcsPort}/health/ || exit 1`],
        interval: Duration.seconds(120), 
        timeout: Duration.seconds(5), 
        startPeriod: Duration.seconds(15), 
        retries: 3, 
      },
      memoryLimitMiB: 900,  
      memoryReservationMiB: 800, 
      essential: true, 
      environment: { 
        'JAVA_OPTS' : '-server -Xms100M -Xmx1000M -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=log4j2-aws.xml -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -cp log4j-api-2.11.2.jar:log4j-core-2.11.2.jar:log4j-slf4j-impl-2.11.2.jar:disruptor-3.4.2.jar', 
        'MCORPUS_COOKIE_SECURE' : 'true', 
        'MCORPUS_DB_DATA_SOURCE_CLASS_NAME' : 'org.postgresql.ds.PGSimpleDataSource', 
        'MCORPUS_JWT_STATUS_CACHE_MAX_SIZE' : '60', 
        'MCORPUS_JWT_STATUS_CACHE_TIMEOUT_IN_MINUTES' : '10', 
        'MCORPUS_JWT_TTL_IN_SECONDS' : '172800', 
        'MCORPUS_SERVER__DEVELOPMENT' : 'false', 
        'MCORPUS_SERVER__PORT' : `${props.lbToEcsPort}`, 
        'MCORPUS_SERVER__PUBLIC_ADDRESS' : 'https://www.mcorpus-aws.net', 
      }, 
      secrets: {
        'MCORPUS_DB_URL' : ecs.Secret.fromSsmParameter(props.ssmMcorpusDbUrl),  
        'MCORPUS_JWT_SALT' : ecs.Secret.fromSsmParameter(jwtSalt), 
      }
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
      vpcSubnets: {
        subnetName: 'Private'
      },
      serviceName: 'mcorpus-fargate-service',
      platformVersion: FargatePlatformVersion.LATEST, 
      securityGroup: props.ecsSecGrp
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

    listener.addTargetGroups('app-lb-tgtrp', {
      targetGroups: [ albTargetGroup ]
    });
    // ****************************
    // *** END inline load balancer ***
    // ****************************
    
    // bind load balancing target to lb group
    this.fargateSvc.attachToApplicationTargetGroup(albTargetGroup);

    // stack output
    // TODO
  }
}
