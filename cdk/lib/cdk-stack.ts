import cdk = require('@aws-cdk/cdk');
import ec2 = require('@aws-cdk/aws-ec2');
import ecs = require('@aws-cdk/aws-ecs');
// import ecr = require('@aws-cdk/aws-ecr');
import cert = require('@aws-cdk/aws-certificatemanager');
// import ssm = require('@aws-cdk/aws-ssm');

export class CdkStack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // vpc
    const vpc = new ec2.VpcNetwork(this, 'McorpusVpc', {
      maxAZs: 1
    });

    const cluster = new ecs.Cluster(this, 'McorpusFargateCluster', {
      vpc: vpc,
      clusterName: "McorpusFargateCluster"
    });

    // ref to existing TLS cert used by the https application load balancer
    const cert443 = cert.Certificate.fromCertificateArn(this, 'loadBalancerCert', 'arn:aws:acm:us-west-2:524006177124:certificate/8c7ea4bb-f2fd-4cdb-b85c-184d2a864b0a');
    
    const javaOpts = [
      '-server',
      '-Xms100M',
      '-Xmx1000M',
      '-Djava.net.preferIPv4Stack=true',
      '-Dlog4j.configurationFile=log4j2-aws.xml',
      '-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector',
      '-cp log4j-api-2.11.2.jar:log4j-core-2.11.2.jar:log4j-slf4j-impl-2.11.2.jar:disruptor-3.4.2.jar'
    ].join(' ');
    
    const fargateService = new ecs.LoadBalancedFargateService(this, 'MyFargateService', {
      cluster: cluster, 
      loadBalancerType: ecs.LoadBalancerType.Application,
      publicLoadBalancer: true, 
      image: ecs.ContainerImage.fromRegistry("524006177124.dkr.ecr.us-west-2.amazonaws.com/mcorpus-gql:0.9.5-20190504203944"), // Required
      certificate: cert443, 
      containerPort: 5150,
      cpu: '256', 
      desiredCount: 1, 
      memoryMiB: '512', 
      environment: {
        "JAVA_OPTS": javaOpts,
        "MCORPUS_DB_DATA_SOURCE_CLASS_NAME": "org.postgresql.ds.PGSimpleDataSource",
        // TODO ssm-ize
        // "MCORPUS_DB_URL": "",
        // "MCORPUS_JWT_SALT": "",
        "MCORPUS_JWT_TTL_IN_MILLIS": "172800000",
        "MCORPUS_JWT_STATUS_CACHE_TIMEOUT_IN_MINUTES": "11",
        "MCORPUS_JWT_STATUS_CACHE_MAX_SIZE": "60",
        "MCORPUS_COOKIE_SECURE": "true",
        "RATPACK_SERVER__DEVELOPMENT": "false",
        "RATPACK_SERVER__PORT": "5150",
        "RATPACK_SERVER__PUBLIC_ADDRESS": "https://www-mcorpus-aws.net"
      }
    });

    // Output the DNS where you can access your service
    new cdk.CfnOutput(this, 'LoadBalancerDNS', { 
      value: fargateService.loadBalancer.dnsName 
    });

    // ecr
    // const repository = new ecr.Repository(this, 'Repository');
    // repository.addLifecycleRule({ tagPrefixList: ['dev'], maxImageCount: 9999 });
    // repository.addLifecycleRule({ maxImageAgeDays: 30 });
  }
}
