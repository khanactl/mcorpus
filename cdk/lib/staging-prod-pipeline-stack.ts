import cdk = require("@aws-cdk/core");
import iam = require("@aws-cdk/aws-iam");
import ecr = require("@aws-cdk/aws-ecr");
import codebuild = require("@aws-cdk/aws-codebuild");
import codepipeline = require("@aws-cdk/aws-codepipeline");
import codepipeline_actions = require("@aws-cdk/aws-codepipeline-actions");
import { PipelineContainerImage } from "./pipeline-container-image";
import { IStackProps } from "./cdk-native";
import {
  BuildSpec,
  ComputeType,
  LinuxBuildImage
} from "@aws-cdk/aws-codebuild";
// import { PolicyStatementEffect } from '@aws-cdk/aws-iam';
// import { githubOwner, repoName, awsSecretsGitHubTokenName, gitProdBranch, ssmImageTagParamName, stagingValidationEmail } from '../config'

export interface IStagingProdPipelineStackProps extends IStackProps {
  readonly appRepository: ecr.Repository;
  readonly imageTag: string;
  readonly githubOwner: string;
  readonly repoName: string;
  readonly awsSecretsGitHubTokenName: string;
  readonly gitProdBranch: string;
  readonly ssmImageTagParamName: string;
  readonly stagingValidationEmail: string;
}

export class StagingProdPipelineStack extends cdk.Stack {
  public readonly appRepository: ecr.Repository;
  public readonly appBuiltImageStaging: PipelineContainerImage;
  public readonly appBuiltImageProd: PipelineContainerImage;

  constructor(
    scope: cdk.Construct,
    id: string,
    props: IStagingProdPipelineStackProps
  ) {
    super(scope, id, props);

    this.appRepository = props.appRepository;
    this.appBuiltImageStaging = new PipelineContainerImage(this.appRepository);
    this.appBuiltImageProd = new PipelineContainerImage(this.appRepository);

    const sourceOutput = new codepipeline.Artifact();

    const sourceAction = new codepipeline_actions.GitHubSourceAction({
      actionName: "GitHub",
      owner: props.githubOwner,
      repo: props.repoName,
      oauthToken: cdk.SecretValue.secretsManager(
        props.awsSecretsGitHubTokenName
      ),
      output: sourceOutput,
      trigger: codepipeline_actions.GitHubTrigger.NONE,
      branch: props.gitProdBranch
    });

    const cdkBuild = new codebuild.PipelineProject(this, "CdkBuildProject", {
      environment: {
        buildImage: codebuild.LinuxBuildImage.UBUNTU_14_04_NODEJS_10_14_1
      },
      buildSpec: BuildSpec.fromObject({
        version: "0.2",
        phases: {
          install: {
            commands: [
              "cd cdk",
              "npm install"
            ]
          },
          build: {
            commands: [
              "npm run build",
              "npm run cdk synth StagingAppStack -- -o .",
              "npm run cdk synth ProdAppStack -- -o .",
              'IMAGE_TAG=`aws ssm get-parameter --name "' +
              props.ssmImageTagParamName +
              '" --output text --query Parameter.Value`',
              `printf '{ "imageTag": "'$IMAGE_TAG'" }' > imageTag.json`,
              "ls"
            ]
          }
        },
        artifacts: {
          "base-directory": "cdk",
          files: [
            "StagingAppStack.template.yaml",
            "ProdAppStack.template.yaml",
            "imageTag.json"
          ]
        }
      })
    });
    cdkBuild.addToRolePolicy(
      new iam.PolicyStatement({
        resources: ["arn:aws:ssm:*:*:parameter/" + props.ssmImageTagParamName],
        actions: ["ssm:GetParameter"]
      })
    );
    cdkBuild.addToRolePolicy(
      new iam.PolicyStatement({
        resources: ["*"],
        actions: ["ec2:DescribeAvailabilityZones"]
      })
    );

    const cdkBuildOutput = new codepipeline.Artifact();

    new codepipeline.Pipeline(this, "Pipeline", {
      stages: [
        {
          stageName: "Source",
          actions: [sourceAction]
        },
        {
          stageName: "Build",
          actions: [
            new codepipeline_actions.CodeBuildAction({
              actionName: "CdkBuild",
              project: cdkBuild,
              input: sourceOutput,
              outputs: [cdkBuildOutput]
            })
          ]
        },
        {
          stageName: "DeployStaging",
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: "CFN_Deploy",
              stackName: "StagingAppStack",
              templatePath: cdkBuildOutput.atPath(
                "StagingAppStack.template.yaml"
              ),
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
        {
          stageName: "DeployProd",
          actions: [
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: "CFN_Deploy",
              stackName: "ProdAppStack",
              templatePath: cdkBuildOutput.atPath("ProdAppStack.template.yaml"),
              adminPermissions: true,
              parameterOverrides: {
                [this.appBuiltImageProd.paramName]: cdkBuildOutput.getParam(
                  "imageTag.json",
                  "imageTag"
                )
              },
              extraInputs: [cdkBuildOutput]
            })
          ]
        }
      ]
    });
  }
}
