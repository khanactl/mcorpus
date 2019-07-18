import cdk = require('@aws-cdk/core');
import ec2 = require('@aws-cdk/aws-ec2');
import { SubnetType } from '@aws-cdk/aws-ec2';

const region = 'us-west-2';
const azA = `${region}a`;
const azB = `${region}b`;
const availabilityZones = [azA, azB];
const publicCidrs = ['10.0.0.0/26', '10.0.0.64/26'];
const privateCidrs = ['10.0.1.0/26', '10.0.1.64/26'];

export class VpcStack extends cdk.Stack {
  
  public readonly vpc: ec2.Vpc;
  
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    this.vpc = new ec2.Vpc(this, 'VPC', {
      maxAzs: 2,
      natGateways: 1,
      cidr: '10.0.0.0/23', // 512
      enableDnsHostnames: true,
      enableDnsSupport: true,
      // vpnGateway: true, // export bug: https://github.com/awslabs/aws-cdk/issues/2339 - will remove when fixed
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
        }
      ],
    });

    // add tag(s)
    this.vpc.node.applyAspect(new cdk.Tag('Name', "mcorpus-vpc"));

    const subnetResourceOverrides = (key: number, subnet: ec2.ISubnet, cidrType: string[]) => {
      // This adds the az and CIDR Address
      const subnetSource = subnet.node.findChild('Subnet') as ec2.CfnSubnet;
      subnetSource.addPropertyOverride('CidrBlock', cidrType[key]);
      subnetSource.addPropertyOverride('AvailabilityZone', availabilityZones[key]);
      // TODO: override/set tag 'Name' as the defaults are bad: "CdkStack/VPC/PrivateSubnet2"
    };
    
    const publicSubnetIds: string[] = [];
    const privateSubnetIds: string[] = [];

    // Iterate the public subnets
    for (let [key, subnet] of this.vpc.publicSubnets.entries()) {
      publicSubnetIds.push(subnet.subnetId)
      subnetResourceOverrides(key, subnet, publicCidrs);
    }

    // Iterate the private subnets
    for (let [key, subnet] of this.vpc.privateSubnets.entries()) {
      privateSubnetIds.push(subnet.subnetId);
      subnetResourceOverrides(key, subnet, privateCidrs);
    }

    /* Outputs to be for App Stack.
    * Will be imported with: 
    * const vpcImportProps = {
    *   vpcId: cdk.Fn.importValue(`VpcId-${appName}`),
    *   availabilityZones: cdk.Fn.split(':', cdk.Fn.importValue(`AvailabilityZones-${appName}`)),
    *   publicSubnetIds: cdk.Fn.split(':', cdk.Fn.importValue(`PublicSubnetIds-${appName}`)),
    *   privateSubnetIds: cdk.Fn.split(':', cdk.Fn.importValue(`PrivateSubnetIds-${appName}`))
    * }
    * const vpc =  ec2.VpcNetwork.import(this, 'MyVPC', vpcImportProps);
    * 
    * @see {@link: https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-importvalue.html}
    * @see {@link: https://awslabs.github.io/aws-cdk/refs/_aws-cdk_cdk.html?highlight=importvalue#@aws-cdk/cdk.Fn.importValue}
    */
    new cdk.CfnOutput(this, 'VpcId', { value: this.vpc.vpcId, exportName: "VpcId-mcorpus" });
    new cdk.CfnOutput(this, 'AvailabilityZones', { value: this.vpc.availabilityZones.join(':'), exportName: "AvailabilityZones-mcorpus" });
    new cdk.CfnOutput(this, 'PublicSubnetIds', { value: publicSubnetIds.join(':'), exportName: "PublicSubnetIds-mcorpus" });
    new cdk.CfnOutput(this, 'PrivateSubnetIds', { value: privateSubnetIds.join(':'), exportName: "PrivateSubnetIds-mcorpus" });
  }
}
