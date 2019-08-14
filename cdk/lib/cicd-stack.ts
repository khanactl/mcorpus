import cdk = require('@aws-cdk/core');
import ec2 = require('@aws-cdk/aws-ec2');
import codebuild = require('@aws-cdk/aws-codebuild');
import codepipeline = require('@aws-cdk/aws-codepipeline');
import codepipeline_actions = require('@aws-cdk/aws-codepipeline-actions');
import ecs = require('@aws-cdk/aws-ecs');
import { EcsDeployAction } from '@aws-cdk/aws-codepipeline-actions';

export interface ICicdProps extends cdk.StackProps {
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;

  readonly codebuildSecGrp: ec2.ISecurityGroup;

  readonly fargateSvc: ecs.FargateService;
}

/**
 * Cicd Stack.
 */
export class CicdStack extends cdk.Stack {

  constructor(scope: cdk.Construct, id: string, props: ICicdProps) {
    super(scope, id, props);


  }
}