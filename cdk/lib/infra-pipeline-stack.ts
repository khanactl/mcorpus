import cdk = require('@aws-cdk/core');
import { BaseStack, IStackProps } from './cdk-native';
import s3 = require('@aws-cdk/aws-s3');
import iam = require('@aws-cdk/aws-iam');
import codebuild = require('@aws-cdk/aws-codebuild');
import codepipeline = require('@aws-cdk/aws-codepipeline');
import codepipeline_actions = require('@aws-cdk/aws-codepipeline-actions');
import { removeListener } from 'cluster';

/**
 * Infra Pipeline stack properties.
 */
export interface IInfraPipelineProps extends IStackProps {
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
   * The Git branch name to associate to the CICD pipeline.
   */
  readonly gitBranchName: string;
}

/**
 * The Infra-Pipeline stack.
 *
 * Responsible for instantiating and updating all needed things
 * in AWS for the app to run by way of automating CDK synth and deploy operations
 * for all the defined CDK stacks save for this one!
 */
export class InfraPipelineStack extends BaseStack {

  constructor(scope: cdk.Construct, props: IInfraPipelineProps) {
    super(scope, "InfraPipeline", props);

    const cdkBuild = new codebuild.PipelineProject(this, "CdkBuild", {
      environment: {
        computeType: codebuild.ComputeType.SMALL,
        privileged: true, // for Docker to run
        buildImage: codebuild.LinuxBuildImage.STANDARD_2_0,
      },
      buildSpec: codebuild.BuildSpec.fromObject({
        version: "0.2",
        env: {
          variables: {
            "GIT_BRANCH_NAME": props.gitBranchName,
          },
        },
        phases: {
          install: {
            "runtime-versions": {
              java: "openjdk8",
              docker: 18
            },
            commands: [
              "pip install --upgrade awscli",
            ],
          },
          pre_build: {
            commands: [
              "mvn -DskipTests package",
              "cd cdk",
              "npm install",
            ],
          },
          build: {
            commands: [
              // note: we're still in cdk subdir
              "npm run build",
              "npm run cdk synth -- -o dist",
            ],
          },
        },
        artifacts: {
          "base-directory": "cdk/dist",
          files: [
            "**/*"
            /*
            `${props.appName}-VPC-SHARED.template.json`,
            `${props.appName}-SecGrp-SHARED.template.json`,
            `${props.appName}-Db-SHARED.template.json`,
            `${props.appName}-DbBootstrap-SHARED.template.json`,
            `${props.appName}-DbData-SHARED.template.json`,
            `${props.appName}-ECS-${props.appEnv}.template.json`,
            `${props.appName}-WAF-${props.appEnv}.template.json`,
            `${props.appName}-CICD-${props.appEnv}.template.json`,
            */
          ],
        },
      }),
    });

    const sourceOutput = new codepipeline.Artifact();
    const cdkBuildOutput = new codepipeline.Artifact(this.iname('CdkBuildOutput', props));

    // source (github)
    const sourceAction = new codepipeline_actions.GitHubSourceAction({
      actionName: `GitHub-Source-${props.appEnv}`,
      owner: props.githubOwner,
      repo: props.githubRepo,
      oauthToken: cdk.SecretValue.secretsManager(props.githubOauthTokenSecretArn, {
        jsonField: props.githubOauthTokenSecretJsonFieldName,
      }),
      branch: props.gitBranchName,
      trigger: codepipeline_actions.GitHubTrigger.WEBHOOK,
      output: sourceOutput,
    });

    /*
    const vpcBuildOutput = new codepipeline.Artifact("VPCBuildOutput");
    const secGrpBuildOutput = new codepipeline.Artifact("SecGrpBuildOutput");
    const dbBuildOutput = new codepipeline.Artifact("DbBuildOutput");
    const dbBootstrapBuildOutput = new codepipeline.Artifact("DbBootstrapBuildOutput");
    const dbDataBuildOutput = new codepipeline.Artifact("DbDataBuildOutput");
    const ecsBuildOutput = new codepipeline.Artifact(this.iname("ECSBuildOutput"));
    const wafBuildOutput = new codepipeline.Artifact(this.iname("WAFBuildOutput"));
    const cicdBuildOutput = new codepipeline.Artifact(this.iname("CICDBuildOutput"));
    */

    const arnS3ConfigObj = "arn:aws:s3:::mcorpus-db-data-bucket-shared/mcorpus-cdk-app-config.json";

    const cdkBuildAction = new codepipeline_actions.CodeBuildAction({
      actionName: this.iname("CDK_Build", props),
      project: cdkBuild,
      input: sourceOutput,
      outputs: [ cdkBuildOutput ],
    });
    cdkBuild.grantPrincipal.addToPolicy(new iam.PolicyStatement({
      actions: [
        "s3:GetObject",
      ],
      resources: [
        arnS3ConfigObj,
      ],
    }));

    const infraPipeline = new codepipeline.Pipeline(this, this.iname('InfraPipeline', props), {
      pipelineName: this.iname('InfraPipeline', props),
      stages: [
        {
          stageName: `Source-${props.appEnv}`,
          actions: [ sourceAction ],
        },
        {
          stageName: `Build-${props.appEnv}`,
          actions: [
            cdkBuildAction,
          ],
        },
        {
          stageName: "Deploy-VPC-SHARED",
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: "VPC_CFN_Deploy",
              templatePath: cdkBuildOutput.atPath(`${props.appName}-VPC-SHARED.template.json`),
              stackName: `${props.appName}-VPC-SHARED`,
              adminPermissions: true,
              // extraInputs: [ vpcBuildOutput ],
            }),
          ],
        },
        {
          stageName: "Deploy-SecGrp-SHARED",
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: "SecGrp_CFN_Deploy",
              templatePath: cdkBuildOutput.atPath(`${props.appName}-SecGrp-SHARED.template.json`),
              stackName: `${props.appName}-SecGrp-SHARED`,
              adminPermissions: true,
              // extraInputs: [ secGrpBuildOutput ],
            }),
          ],
        },
        {
          stageName: "Deploy-Db-SHARED",
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: "Db_CFN_Deploy",
              templatePath: cdkBuildOutput.atPath(`${props.appName}-Db-SHARED.template.json`),
              stackName: `${props.appName}-Db-SHARED`,
              adminPermissions: true,
              // extraInputs: [ dbBuildOutput ],
            }),
          ],
        },
        {
          stageName: "Deploy-DbBootstrap-SHARED",
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: "DbBootstrap_CFN_Deploy",
              templatePath: cdkBuildOutput.atPath(`${props.appName}-DbBootstrap-SHARED.template.json`),
              stackName: `${props.appName}-DbBootstrap-SHARED`,
              adminPermissions: true,
              // extraInputs: [ dbBootstrapBuildOutput ],
            }),
          ],
        },
        {
          stageName: "Deploy-DbData-SHARED",
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: "DbData_CFN_Deploy",
              templatePath: cdkBuildOutput.atPath(`${props.appName}-DbData-SHARED.template.json`),
              stackName: `${props.appName}-DbData-SHARED`,
              adminPermissions: true,
              // extraInputs: [ dbDataBuildOutput ],
            }),
          ],
        },
        {
          stageName: `Deploy-ECS-${props.appEnv}`,
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: `ECS_${props.appEnv}_CFN_Deploy`,
              templatePath: cdkBuildOutput.atPath(`${props.appName}-ECS-${props.appEnv}.template.json`),
              stackName: `${props.appName}-ECS-${props.appEnv}`,
              adminPermissions: true,
              // extraInputs: [ ecsBuildOutput ],
            }),
          ],
        },
        {
          stageName: `Deploy-WAF-${props.appEnv}`,
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: `WAF_${props.appEnv}_CFN_Deploy`,
              templatePath: cdkBuildOutput.atPath(`${props.appName}-WAF-${props.appEnv}.template.json`),
              stackName: `${props.appName}-WAF-${props.appEnv}`,
              adminPermissions: true,
              // extraInputs: [ wafBuildOutput ],
            }),
          ],
        },
        {
          stageName: `Deploy-CICD-${props.appEnv}`,
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: `CICD_${props.appEnv}_CFN_Deploy`,
              templatePath: cdkBuildOutput.atPath(`${props.appName}-CICD-${props.appEnv}.template.json`),
              stackName: `${props.appName}-CICD-${props.appEnv}`,
              adminPermissions: true,
              // extraInputs: [ cicdBuildOutput ],
            }),
          ],
        },
      ],
    });

    // stack output
    new cdk.CfnOutput(this, 'InfraPipelineName', { value: infraPipeline.pipelineName });
  }
}