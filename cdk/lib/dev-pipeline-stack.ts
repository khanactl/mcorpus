import cdk = require('@aws-cdk/core');
import iam = require('@aws-cdk/aws-iam');
import ecr = require('@aws-cdk/aws-ecr');
import ec2 = require('@aws-cdk/aws-ec2');
import sns = require('@aws-cdk/aws-sns');
import sns_sub = require('@aws-cdk/aws-sns-subscriptions');
import event_targets = require('@aws-cdk/aws-events-targets');
import codebuild = require('@aws-cdk/aws-codebuild');
import codepipeline = require('@aws-cdk/aws-codepipeline');
import codepipeline_actions = require('@aws-cdk/aws-codepipeline-actions');
import { BuildSpec, ComputeType, LinuxBuildImage } from '@aws-cdk/aws-codebuild';
import { SubnetType } from '@aws-cdk/aws-ec2';
import { IStringParameter } from '@aws-cdk/aws-ssm';
import { IStackProps, BaseStack, iname } from './cdk-native';
import { PipelineContainerImage } from './pipeline-container-image';

export interface IDevPipelineStackProps extends IStackProps {
  readonly appRepository: ecr.IRepository;
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;

  /**
   * The aws codebuild security group ref.
   */
  readonly codebuildSecGrp: ec2.ISecurityGroup;

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
   * The SSM secure param of the web app jdbc url to the backend db.
   */
  readonly ssmJdbcUrl: IStringParameter;
  /**
   * The SSM secure param of the web app jdbc url to the backend db.
   */
  readonly ssmJdbcTestUrl: IStringParameter;

  readonly appDeployApprovalEmails?: string[];

  readonly onBuildFailureEmails?: string[];

  /**
   * The CDK stack name of the **DEV** app instance.
   */
  readonly cdkDevAppStackName: string;
}

export class DevPipelineStack extends BaseStack {
  public readonly appBuiltImage: PipelineContainerImage;
  public readonly imageTag: string;

  public readonly dockerBuildFailureEventTopic?: sns.Topic;
  public readonly cdkBuildFailureEventTopic?: sns.Topic;

  constructor(scope: cdk.Construct, id: string, props: IDevPipelineStackProps) {
    super(scope, id, props);

    this.appBuiltImage = new PipelineContainerImage(props.appRepository);

    // source (github)
    const sourceOutput = new codepipeline.Artifact();
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

    // *** Docker (web app container) build ***

    const dockerBuild = new codebuild.PipelineProject(this, 'DockerCodeBuildProject', {
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
              java: 'openjdk8',
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
              // `REPOSITORY_URI=${props.env!.account}.dkr.ecr.${props.env!.region}.amazonaws.com/${props.ecrRepo.repositoryName}`,
              // `REPOSITORY_URI=${ecrRef.repositoryUri}`,
              'COMMIT_HASH=$(echo $CODEBUILD_RESOLVED_SOURCE_VERSION | cut -c 1-7)',
              "APP_VERSION=$(cat mcorpus-gql/target/classes/app.properties | grep app.version | cut -d'=' -f2)",
              "BUILD_TIMESTAMP=$(cat mcorpus-gql/target/classes/app.properties | grep build.timestamp | cut -d'=' -f2)",
              "BUILD_TIMESTAMP_DIGITS=$(echo $BUILD_TIMESTAMP | sed 's/[^0-9]//g')",
              'BUILD_VERSION_TAG=${APP_VERSION}.${BUILD_TIMESTAMP_DIGITS}',
            ],
          },
          build: {
            commands: [
              // 'docker build -t $APP_REPOSITORY_URI:$CODEBUILD_RESOLVED_SOURCE_VERSION app',
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
              /* TODO mimic
                'docker push $APP_REPOSITORY_URI:$CODEBUILD_RESOLVED_SOURCE_VERSION',
                'docker push $NGINX_REPOSITORY_URI:$CODEBUILD_RESOLVED_SOURCE_VERSION',
                `printf '{ "imageTag": "'$CODEBUILD_RESOLVED_SOURCE_VERSION'" }' > imageTag.json`,
                `aws ssm put-parameter --name "${props.ssmImageTagParamName}" --value $CODEBUILD_RESOLVED_SOURCE_VERSION --type String --overwrite`
                */

              'echo Pushing the Docker images...',
              'docker push $REPOSITORY_URI:$COMMIT_HASH',
              'docker push $REPOSITORY_URI:$BUILD_VERSION_TAG',

              /* TODO needed?
                "TASKDEF_ARN=$(aws ecs list-task-definitions | jq -r '.taskDefinitionArns[-1]')",
                `CONTAINER_NAME=${props.ecsTaskDefContainerName}`,
                `CONTAINER_PORT=${props.lbToEcsPort}`,
                */

              'echo Writing image detail file...',
              // "printf '[{\"name\":\"%s\",\"imageUri\":\"%s\"}]' $CONTAINER_NAME $REPOSITORY_URI:$COMMIT_HASH > imageDetail.json",
              `printf '{ "imageTag": "'$COMMIT_HASH'" }' > imageTag.json`,

              /*
                "echo Generating appspec.yaml file...",
                "cat ../../appspec-template.yaml | sed -e \"s%\\\${taskDefArn}%$TASKDEF_ARN%\" -e \"s%\\\${containerName}%$CONTAINER_NAME%\" -e \"s%\\\${containerPort}%$CONTAINER_PORT%\" > appspec.yaml",
                */
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
      new iam.PolicyStatement({
        actions: ['ssm:GetParameters', 'ssm:GetParameter'],
        resources: [props.ssmJdbcUrl.parameterArn, props.ssmJdbcTestUrl.parameterArn],
      })
    );
    dockerBuild.addToRolePolicy(
      new iam.PolicyStatement({
        resources: ['arn:aws:ssm:*:*:parameter' + props.ssmImageTagParamName],
        actions: ['ssm:PutParameter'],
      })
    );
    props.appRepository.grantPullPush(dockerBuild);

    // docker build failure email dispatch
    if (props.onBuildFailureEmails && props.onBuildFailureEmails.length > 0) {
      this.dockerBuildFailureEventTopic = new sns.Topic(this, 'dev-cicd-docker-build-failure', {
        topicName: 'dev-cicd-docker-build-failure',
        displayName: 'DEV CICD Docker Build Failure',
      });
      props.onBuildFailureEmails.forEach(email =>
        this.dockerBuildFailureEventTopic!.addSubscription(new sns_sub.EmailSubscription(email))
      );
      dockerBuild.onBuildFailed('dev-cicd-docker-build-failure', {
        target: new event_targets.SnsTopic(this.dockerBuildFailureEventTopic),
        description: 'On DEV CICD Docker web app container build failure.',
      });
    }

    // *** CDK build ***

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
            commands: ['npm run build', 'npm run cdk synth -- -o dist'],
          },
        },
        artifacts: {
          'base-directory': 'cdk/dist',
          files: [`${props.cdkDevAppStackName}.template.json`],
        },
      }),
    });
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

    // cdk build failure email dispatch
    if (props.onBuildFailureEmails && props.onBuildFailureEmails.length > 0) {
      this.cdkBuildFailureEventTopic = new sns.Topic(this, 'dev-cicd-cdk-build-failure', {
        topicName: 'dev-cicd-cdk-build-failure',
        displayName: 'DEV CICD CDK Synth Build Failure',
      });
      props.onBuildFailureEmails.forEach(email =>
        this.cdkBuildFailureEventTopic!.addSubscription(new sns_sub.EmailSubscription(email))
      );
      dockerBuild.onBuildFailed('dev-cicd-cdk-build-failure', {
        target: new event_targets.SnsTopic(this.cdkBuildFailureEventTopic),
        description: 'On DEV CICD CDK Synth build failure.',
      });
    }

    const dockerBuildOutput = new codepipeline.Artifact('DockerBuildOutput');
    const cdkBuildOutput = new codepipeline.Artifact();

    const devCicdPipeline = new codepipeline.Pipeline(this, 'Pipeline', {
      stages: [
        {
          stageName: 'Source',
          actions: [sourceAction],
        },
        {
          stageName: 'Build',
          actions: [
            new codepipeline_actions.CodeBuildAction({
              actionName: 'DockerBuild',
              project: dockerBuild,
              input: sourceOutput,
              outputs: [dockerBuildOutput],
            }),
            new codepipeline_actions.CodeBuildAction({
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
            new codepipeline_actions.CloudFormationCreateUpdateStackAction({
              actionName: 'CFN_Deploy',
              stackName: props.cdkDevAppStackName,
              templatePath: cdkBuildOutput.atPath(`${props.cdkDevAppStackName}.template.json`),
              adminPermissions: true,
              parameterOverrides: {
                [this.appBuiltImage.paramName]: dockerBuildOutput.getParam('imageTag.json', 'imageTag'),
              },
              extraInputs: [dockerBuildOutput],
            }),
          ],
        },
      ],
    });

    this.imageTag = dockerBuildOutput.getParam('imageTag.json', 'imageTag');

    // stack output
    new cdk.CfnOutput(this, 'DevCicdDockerBuildFailedEvent', { value: this.dockerBuildFailureEventTopic!.topicName });
    new cdk.CfnOutput(this, 'DevCicdCdkBuildFailedEvent', { value: this.cdkBuildFailureEventTopic!.topicName });
    // new cdk.CfnOutput(this, 'DevCicdImageTag', { value: this.imageTag }); // WONT WORK!
  }
}
