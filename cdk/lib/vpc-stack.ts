import cdk = require('@aws-cdk/core');
import ec2 = require('@aws-cdk/aws-ec2');
import { SubnetType } from '@aws-cdk/aws-ec2';

/**
 * VPC stack config properties.
 */
export interface IVpcProps extends cdk.StackProps {

};

/**
 * VPC stack.
 */
export class VpcStack extends cdk.Stack {
  
  public readonly vpc: ec2.Vpc;
  
  constructor(scope: cdk.Construct, id: string, props: IVpcProps) {
    super(scope, id, props);

    this.vpc = new ec2.Vpc(this, 'VPC', {
      maxAzs: 2,
      natGateways: 1,
      cidr: '10.0.0.0/23', // 512
      enableDnsHostnames: true,
      enableDnsSupport: true,
      subnetConfiguration: [
        {
          cidrMask: 26, // 64
          name: 'Public',
          subnetType: SubnetType.PUBLIC,
        },
        {
          cidrMask: 26, // 64
          name: 'Private',
          subnetType: SubnetType.PRIVATE,
        }, 
      ],
    });

    // add VPC Name tag
    this.vpc.node.applyAspect(new cdk.Tag('Name', "mcorpus-vpc"));

    const publicSubnetIds: string[] = [];
    const privateSubnetIds: string[] = [];

    // Iterate the public subnets
    for (let [key, subnet] of this.vpc.publicSubnets.entries()) {
      publicSubnetIds.push(subnet.subnetId)
    }

    // Iterate the private subnets
    for (let [key, subnet] of this.vpc.privateSubnets.entries()) {
      privateSubnetIds.push(subnet.subnetId);
    }

    // stack output
    new cdk.CfnOutput(this, 'VpcId', { value: this.vpc.vpcId });
    new cdk.CfnOutput(this, 'AvailabilityZones', { value: this.vpc.availabilityZones.join(':') });
    new cdk.CfnOutput(this, 'PublicSubnetIds', { value: publicSubnetIds.join(':') });
    new cdk.CfnOutput(this, 'PrivateSubnetIds', { value: privateSubnetIds.join(':') });
  }
}
