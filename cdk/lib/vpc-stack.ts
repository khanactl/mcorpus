import { Vpc } from '@aws-cdk/aws-ec2';
import { CfnOutput, Construct } from '@aws-cdk/core';
import { BaseStack, iname, IStackProps } from './cdk-native';

export const VpcStackRootProps = {
  rootStackName: 'vpc',
  description: 'The required VPC',
};

/**
 * VPC stack config properties.
 */
export interface IVpcProps extends IStackProps {
  readonly maxAzs: number;
  readonly cidr: string;
}

/**
 * VPC stack.
 */
export class VpcStack extends BaseStack {
  public readonly vpc: Vpc;

  constructor(scope: Construct, props: IVpcProps) {
    super(scope, VpcStackRootProps.rootStackName, {
      ...props,  ...{ description: VpcStackRootProps.description }
    });

    this.vpc = new Vpc(this, iname('vpc', props), {
      maxAzs: props.maxAzs,
      // natGateways: 1,
      cidr: props.cidr,
      enableDnsHostnames: true,
      enableDnsSupport: true,
    });

    const publicSubnetIds: string[] = [];
    const privateSubnetIds: string[] = [];

    // Iterate the public subnets
    for (let [key, subnet] of this.vpc.publicSubnets.entries()) {
      publicSubnetIds.push(subnet.subnetId);
    }

    // Iterate the private subnets
    for (let [key, subnet] of this.vpc.privateSubnets.entries()) {
      privateSubnetIds.push(subnet.subnetId);
    }

    // stack output
    new CfnOutput(this, 'VpcId', { value: this.vpc.vpcId });
    new CfnOutput(this, 'AvailabilityZones', { value: this.vpc.availabilityZones.join(':') });
    new CfnOutput(this, 'PublicSubnetIds', { value: publicSubnetIds.join(':') });
    new CfnOutput(this, 'PrivateSubnetIds', { value: privateSubnetIds.join(':') });
  }
}
