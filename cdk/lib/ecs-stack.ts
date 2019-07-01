import cdk = require('@aws-cdk/core');
import iam = require('@aws-cdk/aws-iam');
import ec2 = require('@aws-cdk/aws-ec2');
import ecs = require('@aws-cdk/aws-ecs');
import ecs_patterns = require("@aws-cdk/aws-ecs-patterns");
import { Duration } from '@aws-cdk/core';
import { SubnetSelection } from '@aws-cdk/aws-ec2';
import { FargatePlatformVersion } from '@aws-cdk/aws-ecs';

/**
 * ECS Stack config properties.
 */
export interface IECSProps extends cdk.StackProps {
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;

  readonly ecsTaskExecutionRole: iam.Role;
}

/**
 * ECS Stack.
 */
export class ECSStack extends cdk.Stack {
  
  // private readonly vpc: ec2.IVpc;
  
  constructor(scope: cdk.Construct, id: string, props: IECSProps) {
    super(scope, id, props);

    // this.vpc = props.vpc;

    // task def
    const taskDef = new ecs.FargateTaskDefinition(this, 'mcorpus-gql', {
      cpu: 256,
      memoryLimitMiB: 1024,
      taskRole: props.ecsTaskExecutionRole,
    });
    taskDef.addContainer('mcorpus-fargate-container', {
      image: ecs.ContainerImage.fromAsset('../mcorpus-gql/target')
    });

    // cluster
    const cluster = new ecs.Cluster(this, 'mcorpus-docker-dev', {
      vpc: props.vpc
    });

    const ecsClusterSubnets: SubnetSelection = {
      subnetName: 'Private'
    };

    const service = new ecs.FargateService(this, 'mcorpus-fargate-service', {
      cluster: cluster,
      taskDefinition: taskDef, 
      desiredCount: 1,
      assignPublicIp: false,
      healthCheckGracePeriod: Duration.seconds(15),
      vpcSubnets: ecsClusterSubnets,
      serviceName: 'mcorpus-fargate-service',
      platformVersion: FargatePlatformVersion.LATEST
    });

    /*
    const fargateSvc = new ecs_patterns.LoadBalancedFargateService(this, 'mcorpus-fargate-service', {
      cluster: cluster,
      cpu: 512,
      desiredCount: 1,
      image: ecs.ContainerImage.fromAsset('../mcorpus-gql/target')
    });
    */


  }
}
