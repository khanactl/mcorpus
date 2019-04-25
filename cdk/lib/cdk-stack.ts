import cdk = require('@aws-cdk/cdk');
import ec2 = require('@aws-cdk/aws-ec2');
import ecs = require('@aws-cdk/aws-ecs');
import ecr = require('@aws-cdk/aws-ecr');

export class CdkStack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // vpc
    const vpc = new ec2.VpcNetwork(this, 'McorpusVpc', {
      maxAZs: 1
    });

    const cluster = new ecs.Cluster(this, 'MyCluster', {
      vpc: vpc
    });

    const fargateService = new ecs.LoadBalancedFargateService(this, 'MyFargateService', {
      cluster: cluster,  // Required
      cpu: '256', // Default is 256
      desiredCount: 1,  // Default is 1
      image: ecs.ContainerImage.fromRegistry("amazon/amazon-ecs-sample"), // Required
      memoryMiB: '512',  // Default is 512
      publicLoadBalancer: true  // Default is false
    });

    // Output the DNS where you can access your service
    new cdk.CfnOutput(this, 'LoadBalancerDNS', { 
      value: fargateService.loadBalancer.dnsName 
    });

    // ecr
    const repository = new ecr.Repository(this, 'Repository');
    repository.addLifecycleRule({ tagPrefixList: ['prod'], maxImageCount: 9999 });
    repository.addLifecycleRule({ maxImageAgeDays: 30 }); }
}
