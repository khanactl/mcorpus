import cdk = require('@aws-cdk/core');
import { IStackProps, BaseStack } from './cdk-native';
import ec2 = require('@aws-cdk/aws-ec2');
import ecs = require('@aws-cdk/aws-ecs');

export interface IClusterStackProps extends IStackProps {
  /**
   * The VPC ref.
   */
  readonly vpc: ec2.IVpc;
}

export class ClusterStack extends BaseStack {
  public readonly cluster: ecs.Cluster;

  constructor(scope: cdk.Construct, props: IClusterStackProps) {
    super(scope, 'Cluster', props);

    this.cluster = new ecs.Cluster(this, 'FargateCluster', {
      vpc: props.vpc,
    });

    // stack output
    new cdk.CfnOutput(this, 'FargateClusterName', {
      value: this.cluster.clusterName,
    });
  }
}
