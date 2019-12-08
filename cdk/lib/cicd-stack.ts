import cdk = require('@aws-cdk/core');
import { IStackProps, BaseStack } from './cdk-native'
import ec2 = require('@aws-cdk/aws-ec2');
import codebuild = require('@aws-cdk/aws-codebuild');
import codepipeline = require('@aws-cdk/aws-codepipeline');
import codepipeline_actions = require('@aws-cdk/aws-codepipeline-actions');
import ecs = require('@aws-cdk/aws-ecs');
import s3 = require('@aws-cdk/aws-s3');
import { BuildSpec, ComputeType, LinuxBuildImage } from '@aws-cdk/aws-codebuild';
import { SubnetType } from '@aws-cdk/aws-ec2';
import { IStringParameter } from '@aws-cdk/aws-ssm';
import iam = require('@aws-cdk/aws-iam');
import sns = require('@aws-cdk/aws-sns');
import { BlockPublicAccess } from '@aws-cdk/aws-s3';

/**
 * CICD stack config properties.
 */
export interface ICICDProps extends IStackProps {
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;
  /**
   * The GitHub user of the target repo.
   */
  readonly githubOwner: string;
  /**
   * The GitHub repository name.
   */
  readonly githubRepo: string;
  /**
   * The name of the SecretsManager entry holding the GitHub OAuth access token.
   */
  readonly githubOauthTokenSecretName: string;
  /**
   * The Git branch name to associate to the CICD pipeline.
   */
  readonly gitBranchName: string;
  /**
   * The aws codebuild security group ref.
   */
  readonly codebuildSecGrp: ec2.ISecurityGroup;
  /**
   * The non-path name of the buildspec file to use in codebuild.
   */
  readonly buildspecFilename: string;
  /**
   * The aws ecs fargate service ref.
   */
  readonly fargateSvc: ecs.FargateService;
  /**
   * The SSM secure param of the web app jdbc url to the backend db.
   */
  readonly ssmJdbcUrl: IStringParameter;
  /**
   * The SSM secure param of the web app jdbc url to the backend db.
   */
  readonly ssmJdbcTestUrl: IStringParameter;
  /**
   * The CICD manual-approval stage notification emails.
   */
  readonly cicdDeployApprovalEmails: string[];
}

/**
 * CICD Stack.
 */
export class CICDStack extends BaseStack {

  public readonly pipeline: codepipeline.Pipeline;

  constructor(scope: cdk.Construct, props: ICICDProps) {
    super(scope, 'CICD', props);

    // source (github)
    const sourceOutput = new codepipeline.Artifact();
    const sourceAction = new codepipeline_actions.GitHubSourceAction({
      actionName: `GitHub-Source-${props.appConfig.appEnv}`,
      owner: props.githubOwner, 
      repo: props.githubRepo, 
      oauthToken: cdk.SecretValue.secretsManager(props.githubOauthTokenSecretName), 
      branch: props.gitBranchName, 
      trigger: codepipeline_actions.GitHubTrigger.WEBHOOK, 
      output: sourceOutput, 
    });

    // manual approve [post-source] action
    const maaPostSource = new codepipeline_actions.ManualApprovalAction({
      actionName: this.iname('manual-approval-post-source'), 
      notificationTopic: new sns.Topic(this, this.iname('confirm-deployment-post-source')), 
      notifyEmails: props.cicdDeployApprovalEmails, 
      additionalInformation: `Please confirm or reject this change for ${props.appConfig.appEnv} build/test.`
    });

    // build and test action
    const codebuildInstNme = this.iname('ecs-cdk');
    const codebuildProject = new codebuild.PipelineProject(this, codebuildInstNme, {
      vpc: props.vpc, 
      projectName: codebuildInstNme, 
      environment: {
        computeType: ComputeType.SMALL, 
        privileged: true, // for Docker to run
        buildImage: LinuxBuildImage.STANDARD_2_0, 
      }, 
      buildSpec: BuildSpec.fromSourceFilename(props.buildspecFilename), 
      // role: codebuildServiceRole, 
      securityGroups: [ props.codebuildSecGrp ], 
      subnetSelection: { subnetType: SubnetType.PRIVATE }, 
    });
    
    // codebuild/pipeline ssm access
    codebuildProject.addToRolePolicy(new iam.PolicyStatement({
      actions: [
        "ssm:GetParameters",
        "ssm:GetParameter", 
      ], 
      resources: [
        props.ssmJdbcUrl.parameterArn, 
        props.ssmJdbcTestUrl.parameterArn, 
      ], 
    }));
    // AmazonEC2ContainerRegistryPowerUser managed role privs
    codebuildProject.addToRolePolicy(new iam.PolicyStatement({
      actions: [
        "ecs:ListTaskDefinitions", 
        
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:GetRepositoryPolicy",
        "ecr:DescribeRepositories",
        "ecr:ListImages",
        "ecr:DescribeImages",
        "ecr:BatchGetImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecr:PutImage", 
      ], 
      resources: [
        "*" // TODO limit scope!
      ], 
    }));
    codebuildProject.addToRolePolicy(new iam.PolicyStatement({
      actions: [
        "s3:PutObject",
        "s3:GetObject",
        "logs:CreateLogStream",
        "s3:GetBucketAcl",
        "s3:GetBucketLocation",
        "logs:CreateLogGroup",
        "logs:PutLogEvents",
        "s3:GetObjectVersion"
      ], 
      resources: [
        "*" // TODO limit scope!
      ], 
    }));
    codebuildProject.addToRolePolicy(new iam.PolicyStatement({
      actions: [
        "ec2:CreateNetworkInterface",
        "ec2:DescribeDhcpOptions",
        "ec2:DescribeNetworkInterfaces",
        "ec2:DeleteNetworkInterface",
        "ec2:DescribeSubnets",
        "ec2:DescribeSecurityGroups",
        "ec2:DescribeVpcs", 
        "ec2:CreateNetworkInterface",
      ], 
      resources: [
        "*" // TODO limit scope!
      ], 
    }));
    codebuildProject.addToRolePolicy(new iam.PolicyStatement({
      actions: [
        "ec2:CreateNetworkInterfacePermission"
      ], 
      resources: [
        "arn:aws:ec2:us-west-2:524006177124:network-interface/*"
      ],
      /*
      conditions: [
        {
          "StringEquals": {
            "ec2:Subnet": [
                "arn:aws:ec2:us-west-2:524006177124:subnet/*"
            ],
            "ec2:AuthorizedService": "codebuild.amazonaws.com"
          }
        }
      ] 
      */
    }));
    codebuildProject.addToRolePolicy(new iam.PolicyStatement({
      actions: [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ], 
      resources: [
        "*" // TODO limit scope!
      ], 
    }));

    const buildActionInstNme = this.iname('build-test');
    const buildOutput = new codepipeline.Artifact();
    const buildAction = new codepipeline_actions.CodeBuildAction({
      actionName: buildActionInstNme, 
      project: codebuildProject, 
      input: sourceOutput, 
      outputs: [ buildOutput ], 
    });

    // manual approve [deployment] action
    const maaDeploy = new codepipeline_actions.ManualApprovalAction({
      actionName: this.iname('manual-approval-deployment'), 
      notificationTopic: new sns.Topic(this, this.iname('confirm-deployment')), 
      notifyEmails: props.cicdDeployApprovalEmails, 
      additionalInformation: `Please confirm or reject this change for ${props.appConfig.appEnv} deployment.`
    });

    // deploy action
    const deployAction = new codepipeline_actions.EcsDeployAction({
      actionName: this.iname('deploy'), 
      service: props.fargateSvc, 
      imageFile: buildOutput.atPath('imageDetail.json'), 
    });

    // dedicated codepipeline artifact bucket
    const pipelineArtifactBucketInstNme = this.iname('pipeline-bucket');
    const pipelineArtifactBucket = new s3.Bucket(this, pipelineArtifactBucketInstNme, {
      bucketName: pipelineArtifactBucketInstNme, 
      encryption: s3.BucketEncryption.UNENCRYPTED, // TODO use encryption
      blockPublicAccess: BlockPublicAccess.BLOCK_ALL, 
    })

    const pipelineInstNme = this.iname('cicd-pipeline');
    const pipeline = new codepipeline.Pipeline(this, pipelineInstNme, {
      pipelineName: pipelineInstNme, 
      stages: [
        {
          stageName: this.iname('Source'), 
          actions: [ sourceAction ]
        }, 
        {
          stageName: this.iname('confirm-post-source'), 
          actions: [ maaPostSource ]
        }, 
        {
          stageName: this.iname('Build-Test'), 
          actions: [ buildAction ]
        }, 
        {
          stageName: this.iname('confirm-deployment'), 
          actions: [ maaDeploy ]
        }, 
        {
          stageName: this.iname('Deploy'), 
          actions: [ deployAction ]
        }, 
      ], 
      artifactBucket: pipelineArtifactBucket, 
    });
    pipeline.addToRolePolicy(new iam.PolicyStatement({
      actions: [
        "iam:PassRole"
      ], 
      resources: [
        "*" // TODO limit scope!
      ], 
    }));
    pipeline.addToRolePolicy(new iam.PolicyStatement({
      actions: [
        "codedeploy:CreateDeployment",
        "codedeploy:GetApplication",
        "codedeploy:GetApplicationRevision",
        "codedeploy:GetDeployment",
        "codedeploy:GetDeploymentConfig",
        "codedeploy:RegisterApplicationRevision", 
      ], 
      resources: [
        "*" // TODO limit scope!
      ], 
    }));
    pipeline.addToRolePolicy(new iam.PolicyStatement({
      actions: [
        "elasticbeanstalk:*",
        "ec2:*",
        "elasticloadbalancing:*",
        "autoscaling:*",
        "cloudwatch:*",
        "s3:*",
        "sns:*",
        "cloudformation:*",
        "rds:*",
        "sqs:*",
        "ecs:*"
      ], 
      resources: [
        "*" // TODO limit scope!
      ], 
    }));
    pipeline.addToRolePolicy(new iam.PolicyStatement({
      actions: [
        "lambda:InvokeFunction",
        "lambda:ListFunctions", 
      ], 
      resources: [
        "*" // TODO limit scope!
      ], 
    }));

    // stack output
    new cdk.CfnOutput(this, 'CICDPipelineName', { value: pipeline.pipelineName });
    new cdk.CfnOutput(this, 'codebuildRoleName', { value: codebuildProject.role!.roleName });

  }
}