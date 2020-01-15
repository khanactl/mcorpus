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
            `${this.appName}-VPC-SHARED.template.json`,
            `${this.appName}-SecGrp-SHARED.template.json`,
            `${this.appName}-Db-SHARED.template.json`,
            `${this.appName}-DbBootstrap-SHARED.template.json`,
            `${this.appName}-DbData-SHARED.template.json`,
            `${this.appName}-ECS-${this.appEnv}.template.json`,
            `${this.appName}-WAF-${this.appEnv}.template.json`,
            `${this.appName}-CICD-${this.appEnv}.template.json`,
            */
          ],
        },
      }),
    });

    const sourceOutput = new codepipeline.Artifact();
    const cdkBuildOutput = new codepipeline.Artifact(this.iname('CdkBuildOutput'));

    // source (github)
    const sourceAction = new codepipeline_actions.GitHubSourceAction({
      actionName: `GitHub-Source-${this.appEnv}`,
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
      actionName: this.iname("CDK_Build"),
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

    const infraPipeline = new codepipeline.Pipeline(this, this.iname('InfraPipeline'), {
      pipelineName: this.iname('InfraPipeline'),
      stages: [
        {
          stageName: `Source-${this.appEnv}`,
          actions: [ sourceAction ],
        },
        {
          stageName: `Build-${this.appEnv}`,
          actions: [
            cdkBuildAction,
          ],
        },
        {
          stageName: "Deploy-VPC-SHARED",
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: "VPC_CFN_Deploy",
              templatePath: cdkBuildOutput.atPath(`${this.appName}-VPC-SHARED.template.json`),
              stackName: `${this.appName}-VPC-SHARED`,
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
              templatePath: cdkBuildOutput.atPath(`${this.appName}-SecGrp-SHARED.template.json`),
              stackName: `${this.appName}-SecGrp-SHARED`,
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
              templatePath: cdkBuildOutput.atPath(`${this.appName}-Db-SHARED.template.json`),
              stackName: `${this.appName}-Db-SHARED`,
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
              templatePath: cdkBuildOutput.atPath(`${this.appName}-DbBootstrap-SHARED.template.json`),
              stackName: `${this.appName}-DbBootstrap-SHARED`,
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
              templatePath: cdkBuildOutput.atPath(`${this.appName}-DbData-SHARED.template.json`),
              stackName: `${this.appName}-DbData-SHARED`,
              adminPermissions: true,
              // extraInputs: [ dbDataBuildOutput ],
            }),
          ],
        },
        {
          stageName: `Deploy-ECS-${this.appEnv}`,
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: `ECS_${this.appEnv}_CFN_Deploy`,
              templatePath: cdkBuildOutput.atPath(`${this.appName}-ECS-${this.appEnv}.template.json`),
              stackName: `${this.appName}-ECS-${this.appEnv}`,
              adminPermissions: true,
              // extraInputs: [ ecsBuildOutput ],
            }),
          ],
        },
        {
          stageName: `Deploy-WAF-${this.appEnv}`,
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: `WAF_${this.appEnv}_CFN_Deploy`,
              templatePath: cdkBuildOutput.atPath(`${this.appName}-WAF-${this.appEnv}.template.json`),
              stackName: `${this.appName}-WAF-${this.appEnv}`,
              adminPermissions: true,
              // extraInputs: [ wafBuildOutput ],
            }),
          ],
        },
        {
          stageName: `Deploy-CICD-${this.appEnv}`,
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: `CICD_${this.appEnv}_CFN_Deploy`,
              templatePath: cdkBuildOutput.atPath(`${this.appName}-CICD-${this.appEnv}.template.json`),
              stackName: `${this.appName}-CICD-${this.appEnv}`,
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