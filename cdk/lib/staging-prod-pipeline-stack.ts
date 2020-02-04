import cdk = require('@aws-cdk/core');
import iam = require('@aws-cdk/aws-iam');
import ecr = require('@aws-cdk/aws-ecr');
import sns = require('@aws-cdk/aws-sns');
import codebuild = require('@aws-cdk/aws-codebuild');
import codepipeline = require('@aws-cdk/aws-codepipeline');
import codepipeline_actions = require('@aws-cdk/aws-codepipeline-actions');
import { PipelineContainerImage } from './pipeline-container-image';
import { IStackProps, BaseStack, iname } from './cdk-native';
import { BuildSpec, ComputeType, LinuxBuildImage } from '@aws-cdk/aws-codebuild';

export interface IStagingProdPipelineStackProps extends IStackProps {
  /**
   * The ECR repo from which docker web app images are pulled
   * for deployment into QA and PROD app environments.
   */
  readonly appRepository: ecr.Repository;

  /**
   * The name of the S3 bucket holding the CDK JSON config.
   */
  readonly appConfigCacheS3BucketName: string;

  /**
   * The filename of the CDK JSON config.
   */
  readonly appConfigFilename: string;

  /**
   * The GitHub user of the target repo.
   */
  readonly githubOwner: string;
  /**
   * The GitHub repository name.
   */
  readonly githubRepo: string;
  /**
   * The ARN of the SecretsManager entry holding the GitHub OAuth access token.
   */
  readonly githubOauthTokenSecretArn: string;
  /**
   * The JSON field name holding the GitHub Oauth token in the {@link githubOauthTokenSecretArn} value.
   */
  readonly githubOauthTokenSecretJsonFieldName: string;
  /**
   * The Git branch corresponding to production.
   */
  readonly gitProdBranch: string;

  readonly ssmImageTagParamName: string;

  readonly prodDeployApprovalEmails: string[];

  /**
   * The CDK stack name of the **PRD** app instance.
   */
  readonly cdkPrdAppStackName: string;
}

export class StagingProdPipelineStack extends BaseStack {
  public readonly appRepository: ecr.Repository;
  public readonly appBuiltImageStaging: PipelineContainerImage;
  public readonly appBuiltImageProd: PipelineContainerImage;

  constructor(scope: cdk.Construct, id: string, props: IStagingProdPipelineStackProps) {
    super(scope, id, props);

    this.appRepository = props.appRepository;
    // this.appBuiltImageStaging = new PipelineContainerImage(this.appRepository);
    this.appBuiltImageProd = new PipelineContainerImage(this.appRepository);

    const sourceOutput = new codepipeline.Artifact();

    const sourceAction = new codepipeline_actions.GitHubSourceAction({
      actionName: 'GitHub',
      owner: props.githubOwner,
      repo: props.githubRepo,
      oauthToken: cdk.SecretValue.secretsManager(props.githubOauthTokenSecretArn, {
        jsonField: props.githubOauthTokenSecretJsonFieldName,
      }),
      branch: props.gitProdBranch,
      trigger: codepipeline_actions.GitHubTrigger.NONE,
      output: sourceOutput,
    });

    const cdkBuild = new codebuild.PipelineProject(this, 'CdkBuildProject', {
      environment: {
        buildImage: codebuild.LinuxBuildImage.UBUNTU_14_04_NODEJS_10_14_1,
      },
      buildSpec: BuildSpec.fromObject({
        version: '0.2',
        phases: {
          install: {
            commands: ['cd cdk', 'npm install'],
          },
          build: {
            commands: [
              'npm run build',
              'npm run cdk synth -- -o dist',
              `IMAGE_TAG=$(aws ssm get-parameter --name "${props.ssmImageTagParamName}" --output text --query Parameter.Value)`,
              `printf '{ "imageTag": "'$IMAGE_TAG'" }' > imageTag.json`,
            ],
          },
        },
        artifacts: {
          files: [`cdk/dist/${props.cdkPrdAppStackName}.template.json`, 'cdk/imageTag.json'],
          'discard-paths': 'yes',
        },
      }),
    });
    cdkBuild.addToRolePolicy(
      new iam.PolicyStatement({
        resources: [`arn:aws:ssm:*:*:parameter${props.ssmImageTagParamName}`],
        actions: ['ssm:GetParameter'],
      })
    );
    cdkBuild.addToRolePolicy(
      new iam.PolicyStatement({
        resources: ['*'],
        actions: ['ec2:DescribeAvailabilityZones'],
      })
    );
    // allow codebuild to pull cdk config file from s3
    cdkBuild.addToRolePolicy(
      new iam.PolicyStatement({
        resources: [`arn:aws:s3:::${props.appConfigCacheS3BucketName}/${props.appConfigFilename}`],
        actions: ['s3:GetObject'],
      })
    );

    const cdkBuildOutput = new codepipeline.Artifact();

    new codepipeline.Pipeline(this, 'Pipeline', {
      pipelineName: iname('pipeline', props),
      stages: [
        {
          stageName: 'Source',
          actions: [sourceAction],
        },
        {
          stageName: 'Build',
          actions: [
            new codepipeline_actions.CodeBuildAction({
              actionName: 'CdkBuild',
              project: cdkBuild,
              input: sourceOutput,
              outputs: [cdkBuildOutput],
            }),
          ],
        },
        /*
        {
          stageName: "DeployStaging",
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: "CFN_Deploy",
              stackName: "mcorpus-App-QA",
              templatePath: cdkBuildOutput.atPath("mcorpus-App-QA.template.json"),
              adminPermissions: true,
              runOrder: 1,
              parameterOverrides: {
                [this.appBuiltImageStaging.paramName]: cdkBuildOutput.getParam(
                  "imageTag.json",
                  "imageTag"
                )
              },
              extraInputs: [cdkBuildOutput]
            }),
            new codepipeline_actions.ManualApprovalAction({
              actionName: "Validation",
              runOrder: 2,
              notifyEmails: [props.stagingValidationEmail]
            })
          ]
        },
        */
        {
          stageName: 'DeployProd',
          actions: [
            new codepipeline_actions.ManualApprovalAction({
              actionName: 'Validation',
              runOrder: 1,
              notificationTopic: new sns.Topic(this, 'confirm-production-deployment', {
                topicName: 'confirm-production-deployment',
              }),
              notifyEmails: props.prodDeployApprovalEmails,
            }),
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: 'CFN_Deploy',
              stackName: props.cdkPrdAppStackName,
              runOrder: 2,
              templatePath: cdkBuildOutput.atPath(`${props.cdkPrdAppStackName}.template.json`),
              adminPermissions: true,
              parameterOverrides: {
                [this.appBuiltImageProd.paramName]: cdkBuildOutput.getParam('imageTag.json', 'imageTag'),
              },
              extraInputs: [cdkBuildOutput],
            }),
          ],
        },
      ],
    });
  }
}
