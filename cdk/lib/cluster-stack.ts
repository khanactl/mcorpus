import { IVpc } from '@aws-cdk/aws-ec2';
import { Cluster } from '@aws-cdk/aws-ecs';
import { CfnOutput, Construct } from '@aws-cdk/core';
import { BaseStack, iname, IStackProps } from './cdk-native';

export const ClusterStackRootProps = {
  rootStackName: 'ecs-cluster',
  description: 'Creates the ECS cluster the app container in which the app docker container resides.',
};

export interface IClusterStackProps extends IStackProps {
  /**
   * The VPC ref.
   */
  readonly vpc: IVpc;
}

export class ClusterStack extends BaseStack {
  public readonly cluster: Cluster;

  constructor(scope: Construct, props: IClusterStackProps) {
    super(scope, ClusterStackRootProps.rootStackName, {
      ...props, ...{ description: ClusterStackRootProps.description }
    });

    this.cluster = new Cluster(this, 'FargateCluster', {
      vpc: props.vpc,
      clusterName: iname('cluster', props),
      containerInsights: true,
    });

    // stack output
    new CfnOutput(this, 'FargateClusterName', {
      value: this.cluster.clusterName,
    });
  }
}
