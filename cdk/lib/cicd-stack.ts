import cdk = require('@aws-cdk/core');
import ec2 = require('@aws-cdk/aws-ec2');
import codebuild = require('@aws-cdk/aws-codebuild');
import codepipeline = require('@aws-cdk/aws-codepipeline');
import codepipeline_actions = require('@aws-cdk/aws-codepipeline-actions');
import ecs = require('@aws-cdk/aws-ecs');
import { EcsDeployAction } from '@aws-cdk/aws-codepipeline-actions';

/**
 * CICD stack config properties.
 */
export interface ICICDProps extends cdk.StackProps {
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;
  /**
   * The aws codebuild security group ref.
   */
  readonly codebuildSecGrp: ec2.ISecurityGroup;
  /**
   * The aws ecs fargate service ref.
   */
  readonly fargateSvc: ecs.FargateService;
}

/**
 * CICD Stack.
 */
export class CICDStack extends cdk.Stack {

  constructor(scope: cdk.Construct, id: string, props: ICICDProps) {
    super(scope, id, props);

    // TODO finish

    // stack output
    // TODO
  }
}