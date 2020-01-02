import cdk = require('@aws-cdk/core');
import { BaseStack, IStackProps } from './cdk-native';
import s3 = require('@aws-cdk/aws-s3');
import codebuild = require('@aws-cdk/aws-codebuild');
import codecommit = require('@aws-cdk/aws-codecommit');
import codepipeline = require('@aws-cdk/aws-codepipeline');
import codepipeline_actions = require('@aws-cdk/aws-codepipeline-actions');

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
    super(scope, 'InfraPipeline', props);

    const cdkBuild = new codebuild.PipelineProject(this, 'CdkBuild', {
      environment: {
        computeType: codebuild.ComputeType.SMALL,
        privileged: true, // for Docker to run
        buildImage: codebuild.LinuxBuildImage.STANDARD_2_0,
      },
      buildSpec: codebuild.BuildSpec.fromObject({
        version: '0.2',
        phases: {
          install: {
            "runtime-versions": {
              java: "openjdk8",
              docker: 18
            },
            commands: [
              "pip install --upgrade awscli",
              "npm install",
            ],
          },
          pre_build: {
            commands: [
              "mvn -DskipTests package",
            ],
          },
          build: {
            commands: [
              "npm run build",
              "npm run cdk synth -- -o dist",
            ],
          },
        },
        artifacts: {
          "base-directory": "dist",
          files: [
            "LambdaStack.template.json",
          ],
        },
      }),
    });

  }

}