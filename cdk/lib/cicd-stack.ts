import cdk = require('@aws-cdk/core');
import ec2 = require('@aws-cdk/aws-ec2');
import codebuild = require('@aws-cdk/aws-codebuild');
import codepipeline = require('@aws-cdk/aws-codepipeline');
import codepipeline_actions = require('@aws-cdk/aws-codepipeline-actions');
import ecs = require('@aws-cdk/aws-ecs');
import { EcsDeployAction } from '@aws-cdk/aws-codepipeline-actions';
import path = require('path');
import { BuildEnvironment } from '@aws-cdk/aws-codebuild';

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
  /**
   * The CICD manual-approval stage notification emails.
   */
  readonly cicdDeployApprovalEmails: string[];
}

/**
 * CICD Stack.
 */
export class CICDStack extends cdk.Stack {

  public readonly pipeline: codepipeline.Pipeline;

  constructor(scope: cdk.Construct, id: string, props: ICICDProps) {
    super(scope, id, props);

    // const pipelineProj = new codebuild.PipelineProject(this, 'mcorpus-codepipelin');

    // source (github)
    const sourceOutput = new codepipeline.Artifact();
    const sourceAction = new codepipeline_actions.GitHubSourceAction({
      actionName: 'GitHub_Source',
      owner: 'khanactl',
      repo: 'mcorpus',
      oauthToken: cdk.SecretValue.secretsManager('my-github-token'), // TODO
      branch: 'master', // default: 'master'
      trigger: codepipeline_actions.GitHubTrigger.WEBHOOK, 
      output: sourceOutput, 
    });
    
    // build
    const codebuildProj = new codebuild.Project(this, 'mcorpus-codebuild', {
      vpc: props.vpc, 
      buildSpec: codebuild.BuildSpec.fromSourceFilename(
        path.join(__dirname, "../../buildspec-ecs.yml")
      ),
      environment: {
        buildImage: codebuild.LinuxBuildImage.UBUNTU_14_04_DOCKER_18_09_0, 
        privileged: true, 
      }, 
    });
    const buildOutput = new codepipeline.Artifact();
    const buildAction = new codepipeline_actions.CodeBuildAction({
      actionName: 'CodeBuild',
      project: codebuildProj, 
      input: sourceOutput,
      outputs: [ buildOutput ], 
    });
    
    // manual approval
    const actionApprove = new codepipeline_actions.ManualApprovalAction({
      actionName: 'Approve',
      notifyEmails: props.cicdDeployApprovalEmails, 
    });
    
    // deploy
    const actionDeploy = new codepipeline_actions.EcsDeployAction({
      actionName: 'DeployAction',       
      service: props.fargateSvc, 
      imageFile: buildOutput.atPath('target/imageDetail.json'), 
    })
    
    this.pipeline = new codepipeline.Pipeline(this, 'mcorpus-deploy-pipeline', {
      stages: [
        {
          stageName: 'Source',
          actions: [ sourceAction ], 
        },
        {
          stageName: 'Build-Test',
          actions: [ buildAction ], 
        },
        {
          stageName: 'Approve',
          actions: [ actionApprove ], 
        },
        {
          stageName: 'Deploy',
          actions: [ actionDeploy ], 
        },
      ],
    });

    // stack output
    new cdk.CfnOutput(this, 'pipelineName', { value: 
      this.pipeline.pipelineName, 
    });
  }
}