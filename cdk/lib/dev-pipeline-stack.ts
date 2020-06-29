import { BuildSpec, ComputeType, LinuxBuildImage, PipelineProject } from '@aws-cdk/aws-codebuild';
import { Artifact, Pipeline } from '@aws-cdk/aws-codepipeline';
import {
  CloudFormationCreateUpdateStackAction,
  CodeBuildAction,
  GitHubSourceAction,
  GitHubTrigger,
} from '@aws-cdk/aws-codepipeline-actions';
import { ISecurityGroup, IVpc, SubnetType } from '@aws-cdk/aws-ec2';
import { IRepository } from '@aws-cdk/aws-ecr';
import { SnsTopic } from '@aws-cdk/aws-events-targets';
import { PolicyStatement } from '@aws-cdk/aws-iam';
import { Topic } from '@aws-cdk/aws-sns';
import { EmailSubscription } from '@aws-cdk/aws-sns-subscriptions';
import { IStringParameter } from '@aws-cdk/aws-ssm';
import { CfnOutput, Construct, SecretValue } from '@aws-cdk/core';
import { BaseStack, iname, IStackProps } from './cdk-native';
import { PipelineContainerImage } from './pipeline-container-image';

export interface IDevPipelineStackProps extends IStackProps {
  readonly appRepository: IRepository;
  /**
   * The VPC ref
   */
  readonly vpc: IVpc;

  /**
   * The aws codebuild security group ref.
   */
  readonly codebuildSecGrp: ISecurityGroup;

  /**
   * The name of the S3 bucket holding the CDK JSON config.
   */
  readonly appConfigCacheS3BucketName: string;

  /**
   * The filename of the CDK JSON config.
   */
  readonly appConfigFilename: string;

  /**
   * The SSM param name holding the target docker image tag.
   */
  readonly ssmImageTagParamName: string;

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
  /**
   * Trigger CICD build on commit OR trigger manually.
   */
  readonly triggerOnCommit: boolean;

  /**
   * The SSM secure param of the web app jdbc url to the backend db.
   */
  readonly ssmJdbcUrl: IStringParameter;
  /**
   * The SSM secure param of the web app jdbc url to the backend db.
   */
  readonly ssmJdbcTestUrl: IStringParameter;

  readonly appDeployApprovalEmails?: string[];

  readonly onBuildFailureEmails?: string[];

  readonly cdkDevLbStackName: string;
  readonly cdkDevAppStackName: string;
  readonly cdkDevMetricsStackName: string;
}

export class DevPipelineStack extends BaseStack {
  public readonly appBuiltImage: PipelineContainerImage;
  public readonly imageTag: string;
  public readonly gitBranchName: string;

  public readonly dockerBuildFailureEventTopic?: Topic;
  public readonly cdkBuildFailureEventTopic?: Topic;

  constructor(scope: Construct, id: string, props: IDevPipelineStackProps) {
    super(scope, id, props);

    this.appBuiltImage = new PipelineContainerImage(props.appRepository);

    // source (github)
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
    this.gitBranchName = props.gitBranchName;

    // *** Docker (web app container) build ***

    const dockerBuild = new PipelineProject(this, 'DockerCodeBuildProject', {
      vpc: props.vpc,
      // projectName: codebuildInstNme,
      environment: {
        computeType: ComputeType.SMALL,
        privileged: true, // for Docker to run
        buildImage: LinuxBuildImage.STANDARD_2_0,
      },
      securityGroups: [props.codebuildSecGrp],
      subnetSelection: { subnetType: SubnetType.PRIVATE },
      buildSpec: BuildSpec.fromObject({
        version: '0.2',
        env: {
          variables: {
            REPOSITORY_URI: props.appRepository.repositoryUri,
          },
          'parameter-store': {
            MCORPUS_DB_URL: props.ssmJdbcUrl.parameterName,
            MCORPUS_TEST_DB_URL: props.ssmJdbcTestUrl.parameterName,
          },
        },
        phases: {
          install: {
            'runtime-versions': {
              java: 'openjdk11',
              docker: 18,
            },
            commands: ['pip install --upgrade awscli'],
          },
          pre_build: {
            commands: [
              // '$(aws ecr get-login --no-include-email --region $AWS_DEFAULT_REGION)',
              'echo Test started on `date`',
              'aws --version',
              'docker --version',
              'mvn --version',

              'echo $(env)', // output the env vars

              'mvn clean test',

              'echo Logging in to Amazon ECR...',
              'echo $AWS_DEFAULT_REGION',
              '$(aws ecr get-login --region $AWS_DEFAULT_REGION --no-include-email)',
              'COMMIT_HASH=$(echo $CODEBUILD_RESOLVED_SOURCE_VERSION | cut -c 1-7)',
              "APP_VERSION=$(cat mcorpus-gql/target/classes/app.properties | grep app.version | cut -d'=' -f2)",
              "BUILD_TIMESTAMP=$(cat mcorpus-gql/target/classes/app.properties | grep build.timestamp | cut -d'=' -f2)",
              "BUILD_TIMESTAMP_DIGITS=$(echo $BUILD_TIMESTAMP | sed 's/[^0-9]//g')",
              'BUILD_VERSION_TAG=${APP_VERSION}.${BUILD_TIMESTAMP_DIGITS}',
            ],
          },
          build: {
            commands: [
              'echo Build started on `date`',
              'mvn -DskipTests package',

              'echo Building the Docker image...',
              'cd mcorpus-gql/target/awsdockerasset',
              'docker build -t $REPOSITORY_URI:$COMMIT_HASH .',
              'docker tag $REPOSITORY_URI:$COMMIT_HASH $REPOSITORY_URI:$BUILD_VERSION_TAG',
            ],
          },
          post_build: {
            commands: [
              'echo Pushing the Docker images...',
              'docker push $REPOSITORY_URI:$COMMIT_HASH',
              'docker push $REPOSITORY_URI:$BUILD_VERSION_TAG',
              'echo Writing image detail file...',
              `printf '{ "imageTag": "'$COMMIT_HASH'" }' > imageTag.json`,
              `aws ssm put-parameter --name "${props.ssmImageTagParamName}" --value $COMMIT_HASH --type String --overwrite`,
              'echo Build completed on `date`',
            ],
          },
        },
        artifacts: {
          files: ['mcorpus-gql/target/awsdockerasset/imageTag.json'],
          'discard-paths': 'yes',
        },
      }),
    });
    // codebuild/pipeline ssm access
    dockerBuild.addToRolePolicy(
      new PolicyStatement({
        actions: ['ssm:GetParameters', 'ssm:GetParameter'],
        resources: [props.ssmJdbcUrl.parameterArn, props.ssmJdbcTestUrl.parameterArn],
      })
    );
    dockerBuild.addToRolePolicy(
      new PolicyStatement({
        resources: ['arn:aws:ssm:*:*:parameter' + props.ssmImageTagParamName],
        actions: ['ssm:PutParameter'],
      })
    );
    props.appRepository.grantPullPush(dockerBuild);

    // docker build failure email dispatch
    if (props.onBuildFailureEmails && props.onBuildFailureEmails.length > 0) {
      const topicName = iname('docker-build-failure', props);
      const displayName = `${props.appName} ${props.appEnv} Docker/app build failure.`;
      this.dockerBuildFailureEventTopic = new Topic(this, topicName, {
        topicName: topicName,
        displayName: displayName,
      });
      props.onBuildFailureEmails.forEach((email) =>
        this.dockerBuildFailureEventTopic!.addSubscription(new EmailSubscription(email))
      );
      dockerBuild.onBuildFailed(topicName, {
        target: new SnsTopic(this.dockerBuildFailureEventTopic),
        description: displayName,
      });
    }

    // *** CDK build ***

    const cdkBuild = new PipelineProject(this, 'CdkBuildProject', {
      environment: {
        buildImage: LinuxBuildImage.UBUNTU_14_04_NODEJS_10_14_1,
      },
      buildSpec: BuildSpec.fromObject({
        version: '0.2',
        phases: {
          install: {
            commands: ['cd cdk', 'npm install'],
          },
          build: {
            commands: ['npm run build', 'npm run cdk synth -- -o dist'],
          },
        },
        artifacts: {
          'base-directory': 'cdk/dist',
          files: [
            `${props.cdkDevLbStackName}.template.json`,
            `${props.cdkDevAppStackName}.template.json`,
            `${props.cdkDevMetricsStackName}.template.json`,
          ],
        },
      }),
    });
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
        actions: ['s3:GetObject', 'kms:Decrypt'],
      })
    );

    // cdk build failure email dispatch
    if (props.onBuildFailureEmails && props.onBuildFailureEmails.length > 0) {
      const topicName = iname('cdk-build-failure', props);
      const displayName = `${props.appName} ${props.appEnv} CDK build failure.`;
      this.cdkBuildFailureEventTopic = new Topic(this, topicName, {
        topicName: topicName,
        displayName: displayName,
      });
      props.onBuildFailureEmails.forEach((email) =>
        this.cdkBuildFailureEventTopic!.addSubscription(new EmailSubscription(email))
      );
      cdkBuild.onBuildFailed(topicName, {
        target: new SnsTopic(this.cdkBuildFailureEventTopic),
        description: displayName,
      });
    }

    const dockerBuildOutput = new Artifact('DockerBuildOutput');
    const cdkBuildOutput = new Artifact();

    const devCicdPipeline = new Pipeline(this, 'Pipeline', {
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
              actionName: 'DockerBuild',
              project: dockerBuild,
              input: sourceOutput,
              outputs: [dockerBuildOutput],
            }),
            new CodeBuildAction({
              actionName: 'CdkBuild',
              project: cdkBuild,
              input: sourceOutput,
              outputs: [cdkBuildOutput],
            }),
          ],
        },
        {
          stageName: 'Deploy',
          actions: [
            new CloudFormationCreateUpdateStackAction({
              actionName: 'CFN_Deploy_Lb',
              stackName: props.cdkDevLbStackName,
              templatePath: cdkBuildOutput.atPath(`${props.cdkDevLbStackName}.template.json`),
              adminPermissions: true,
              runOrder: 1,
            }),
            new CloudFormationCreateUpdateStackAction({
              actionName: 'CFN_Deploy_App',
              stackName: props.cdkDevAppStackName,
              templatePath: cdkBuildOutput.atPath(`${props.cdkDevAppStackName}.template.json`),
              adminPermissions: true,
              parameterOverrides: {
                [this.appBuiltImage.paramName]: dockerBuildOutput.getParam('imageTag.json', 'imageTag'),
              },
              extraInputs: [dockerBuildOutput],
              runOrder: 2,
            }),
            new CloudFormationCreateUpdateStackAction({
              actionName: 'CFN_Deploy_Metrics',
              stackName: props.cdkDevMetricsStackName,
              templatePath: cdkBuildOutput.atPath(`${props.cdkDevMetricsStackName}.template.json`),
              adminPermissions: true,
              runOrder: 3,
            }),
          ],
        },
      ],
    });

    this.imageTag = dockerBuildOutput.getParam('imageTag.json', 'imageTag');

    // stack output
    new CfnOutput(this, 'DevCicdDockerBuildFailedEvent', { value: this.dockerBuildFailureEventTopic!.topicName });
    new CfnOutput(this, 'DevCicdCdkBuildFailedEvent', { value: this.cdkBuildFailureEventTopic!.topicName });
    // new cdk.CfnOutput(this, 'DevCicdImageTag', { value: this.imageTag }); // WONT WORK!
  }
}
