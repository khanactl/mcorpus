import cdk = require('@aws-cdk/core');
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
export interface ICICDProps extends cdk.StackProps {
  readonly githubOwner: string;
  readonly githubRepo: string;
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;
  /**
   * The aws codebuild security group ref.
   */
  readonly codebuildSecGrp: ec2.ISecurityGroup;
  /**
   * The name of the SecretsManager entry holding the GitHub OAuth access token.
   */
  readonly githubOauthTokenSecretName: string;
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
export class CICDStack extends cdk.Stack {

  public readonly pipeline: codepipeline.Pipeline;

  constructor(scope: cdk.Construct, id: string, props: ICICDProps) {
    super(scope, id, props);

    // source (github)
    const sourceOutput = new codepipeline.Artifact();
    const sourceAction = new codepipeline_actions.GitHubSourceAction({
      actionName: 'GitHub_Source',
      owner: props.githubOwner, 
      repo: props.githubRepo, 
      oauthToken: cdk.SecretValue.secretsManager(props.githubOauthTokenSecretName), 
      branch: 'master', // default: 'master'
      trigger: codepipeline_actions.GitHubTrigger.WEBHOOK, 
      output: sourceOutput, 
    });

    // build and test action
    const codebuildProject = new codebuild.PipelineProject(this, 'mcorpus-ecs-cdk', {
      vpc: props.vpc, 
      projectName: 'mcorpus-ecs-cdk', 
      environment: {
        computeType: ComputeType.SMALL, 
        privileged: true, // for Docker to run
        buildImage: LinuxBuildImage.STANDARD_2_0, 
      }, 
      buildSpec: BuildSpec.fromSourceFilename('buildspec-ecs.yml'), 
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

    const buildOutput = new codepipeline.Artifact();
    const buildAction = new codepipeline_actions.CodeBuildAction({
      actionName: 'Build-Test', 
      project: codebuildProject, 
      input: sourceOutput, 
      outputs: [ buildOutput ], 
    });

    // manual approve [deployment] action
    const maa = new codepipeline_actions.ManualApprovalAction({
      actionName: 'manual-approval', 
      notificationTopic: new sns.Topic(this, 'confirm-deployment'), 
      notifyEmails: props.cicdDeployApprovalEmails, 
      additionalInformation: 'Please confirm or reject this change for deployment.'
    });

    // deploy action
    const deployAction = new codepipeline_actions.EcsDeployAction({
      actionName: 'Deploy', 
      service: props.fargateSvc, 
      imageFile: buildOutput.atPath('imageDetail.json'), 
    });

    // dedicated codepipeline artifact bucket
    const pipelineArtifactBucket = new s3.Bucket(this, 'mcorpus-pipeline-bucket', {
      bucketName: 'mcorpus-pipeline-bucket', 
      encryption: s3.BucketEncryption.UNENCRYPTED, // TODO use encryption
      blockPublicAccess: BlockPublicAccess.BLOCK_ALL, 
    })

    const pipeline = new codepipeline.Pipeline(this, 'mcorpus-pipeline', {
      pipelineName: 'mcorpus-pipeline', 
      stages: [
        {
          stageName: 'Source', 
          actions: [ sourceAction ]
        }, 
        {
          stageName: 'Build-Test', 
          actions: [ buildAction ]
        }, 
        {
          stageName: 'confirm-deployment', 
          actions: [ maa ]
        }, 
        {
          stageName: 'Deploy', 
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