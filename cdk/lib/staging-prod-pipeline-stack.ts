import { BuildSpec, LinuxBuildImage, PipelineProject } from '@aws-cdk/aws-codebuild';
import { Artifact, Pipeline } from '@aws-cdk/aws-codepipeline';
import {
	CloudFormationCreateUpdateStackAction,
	CodeBuildAction,
	GitHubSourceAction,
	GitHubTrigger,
	ManualApprovalAction,
} from '@aws-cdk/aws-codepipeline-actions';
import { Repository } from '@aws-cdk/aws-ecr';
import { PolicyStatement } from '@aws-cdk/aws-iam';
import { Topic } from '@aws-cdk/aws-sns';
import { Construct, SecretValue } from '@aws-cdk/core';
import { BaseStack, iname, IStackProps } from './cdk-native';
import { PipelineContainerImage } from './pipeline-container-image';

export interface IStagingProdPipelineStackProps extends IStackProps {
	/**
	 * The ECR repo from which docker web app images are pulled
	 * for deployment into QA and PROD app environments.
	 */
	readonly appRepository: Repository;

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
	 * The JSON field name holding the GitHub Oauth token
	 * in the {@link githubOauthTokenSecretArn} value.
	 */
	readonly githubOauthTokenSecretJsonFieldName: string;
	/**
	 * The Git branch name to associate to the CICD pipeline.
	 */
	readonly gitBranchName: string;
	/**
	 * Trigger CICD build on commit OR trigger manually.
	 */
	readonly triggerOnCommit: boolean;

	readonly ssmImageTagParamName: string;

	readonly prodDeployApprovalEmails: string[];

	readonly cdkPrdLbStackName: string;
	readonly cdkPrdAppStackName: string;
	readonly cdkPrdMetricsStackName: string;
}

export class StagingProdPipelineStack extends BaseStack {
	// public readonly appBuiltImageStaging: PipelineContainerImage;
	public readonly appBuiltImageProd: PipelineContainerImage;

	public readonly cfnDeployConfirmTopic?: Topic;

	constructor(scope: Construct, id: string, props: IStagingProdPipelineStackProps) {
		super(scope, id, props);

		this.appBuiltImageProd = new PipelineContainerImage(props.appRepository);

		const sourceOutput = new Artifact();

		const sourceAction = new GitHubSourceAction({
			actionName: `GitHub-${props.gitBranchName}-branch`,
			owner: props.githubOwner,
			repo: props.githubRepo,
			oauthToken: SecretValue.secretsManager(props.githubOauthTokenSecretArn, {
				jsonField: props.githubOauthTokenSecretJsonFieldName,
			}),
			branch: props.gitBranchName,
			trigger: props.triggerOnCommit ? GitHubTrigger.WEBHOOK : GitHubTrigger.NONE,
			output: sourceOutput,
		});

		const cdkBuild = new PipelineProject(this, 'CdkBuildProject', {
			environment: {
				buildImage: LinuxBuildImage.STANDARD_2_0,
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
					files: [
						`cdk/dist/${props.cdkPrdLbStackName}.template.json`,
						`cdk/dist/${props.cdkPrdAppStackName}.template.json`,
						`cdk/dist/${props.cdkPrdMetricsStackName}.template.json`,
						'cdk/imageTag.json',
					],
					'discard-paths': 'yes',
				},
			}),
		});
		cdkBuild.addToRolePolicy(
			new PolicyStatement({
				resources: [`arn:aws:ssm:*:*:parameter${props.ssmImageTagParamName}`],
				actions: ['ssm:GetParameter'],
			})
		);
		cdkBuild.addToRolePolicy(
			new PolicyStatement({
				resources: ['*'],
				actions: ['ec2:DescribeAvailabilityZones'],
			})
		);
		// allow codebuild to pull cdk config file from s3
		cdkBuild.addToRolePolicy(
			new PolicyStatement({
				resources: [`arn:aws:s3:::${props.appConfigCacheS3BucketName}/${props.appConfigFilename}`],
				actions: ['s3:GetObject'],
			})
		);

		const cdkBuildOutput = new Artifact();

		const topicName = iname('confirm-cfn-deployment', props);
		this.cfnDeployConfirmTopic = new Topic(this, topicName, {
			topicName: topicName,
		});

		new Pipeline(this, 'Pipeline', {
			pipelineName: iname('pipeline', props),
			stages: [
				{
					stageName: 'Source',
					actions: [sourceAction],
				},
				{
					stageName: 'Build',
					actions: [
						new CodeBuildAction({
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
					stageName: 'Validation',
					actions: [
						new ManualApprovalAction({
							actionName: 'Confirm_Production_Deployment',
							runOrder: 1,
							notificationTopic: this.cfnDeployConfirmTopic,
							notifyEmails: props.prodDeployApprovalEmails,
						}),
					],
				},
				{
					stageName: 'Deploy_Production',
					actions: [
						new CloudFormationCreateUpdateStackAction({
							actionName: 'CFN_Deploy_Lb',
							stackName: props.cdkPrdLbStackName,
							templatePath: cdkBuildOutput.atPath(`${props.cdkPrdLbStackName}.template.json`),
							adminPermissions: true,
							runOrder: 1,
						}),
						new CloudFormationCreateUpdateStackAction({
							actionName: 'CFN_Deploy_App',
							stackName: props.cdkPrdAppStackName,
							runOrder: 2,
							templatePath: cdkBuildOutput.atPath(`${props.cdkPrdAppStackName}.template.json`),
							adminPermissions: true,
							parameterOverrides: {
								[this.appBuiltImageProd.paramName]: cdkBuildOutput.getParam('imageTag.json', 'imageTag'),
							},
							extraInputs: [cdkBuildOutput],
						}),
						new CloudFormationCreateUpdateStackAction({
							actionName: 'CFN_Deploy_Metrics',
							stackName: props.cdkPrdMetricsStackName,
							templatePath: cdkBuildOutput.atPath(`${props.cdkPrdMetricsStackName}.template.json`),
							adminPermissions: true,
							runOrder: 3,
						}),
					],
				},
			],
		});
	}
}
