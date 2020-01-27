import cdk = require('@aws-cdk/core');
import { BuildSpec, ComputeType, LinuxBuildImage } from '@aws-cdk/aws-codebuild';
import { SubnetType } from '@aws-cdk/aws-ec2';
import { BlockPublicAccess } from '@aws-cdk/aws-s3';
import { IStringParameter } from '@aws-cdk/aws-ssm';
import { BaseStack, IStackProps } from './cdk-native';
import ec2 = require('@aws-cdk/aws-ec2');
import codebuild = require('@aws-cdk/aws-codebuild');
import codepipeline = require('@aws-cdk/aws-codepipeline');
import codepipeline_actions = require('@aws-cdk/aws-codepipeline-actions');
import ecs = require('@aws-cdk/aws-ecs');
import s3 = require('@aws-cdk/aws-s3');
import iam = require('@aws-cdk/aws-iam');
import sns = require('@aws-cdk/aws-sns');
import ecr = require('@aws-cdk/aws-ecr');

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
   * The aws codebuild security group ref.
   */
  readonly codebuildSecGrp: ec2.ISecurityGroup;
  /**
   * The container name used in the buildspec.
   */
  readonly ecsTaskDefContainerName: string;
  /**
   * The inner or traffic port used to send traffic
   * from the load balancer to the ecs/fargate service.
   */
  readonly lbToEcsPort: number;
  /**
   * The pre-existing ECR ref as an ARN.
   */
  readonly ecrArn: string;
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

  public readonly pipelineArtifactBucket: s3.Bucket;

  constructor(scope: cdk.Construct, props: ICICDProps) {
    super(scope, 'CICD', props);

    // get the ECR handle
    const ecrRef = ecr.Repository.fromRepositoryArn(this, "ecr-repo-ref", props.ecrArn);

    // dedicated codepipeline artifact bucket
    const pipelineArtifactBucketInstNme = this.iname('pipeline-bucket', props);
    this.pipelineArtifactBucket = new s3.Bucket(this, pipelineArtifactBucketInstNme, {
      bucketName: pipelineArtifactBucketInstNme,
      encryption: s3.BucketEncryption.S3_MANAGED,
      blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
      removalPolicy: cdk.RemovalPolicy.DESTROY,
    })

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

    // manual approve [pre-build] action
    const maaPostSource = new codepipeline_actions.ManualApprovalAction({
      actionName: this.iname('manual-approval-build', props),
      notificationTopic: new sns.Topic(this, this.iname('confirm-build', props)),
      notifyEmails: props.cicdDeployApprovalEmails,
      additionalInformation: `Confirm or reject this ${props.appEnv} build?`
    });

    // build and test action
    const codebuildInstNme = this.iname('ecs-cdk', props);
    const codebuildProject = new codebuild.PipelineProject(this, codebuildInstNme, {
      vpc: props.vpc,
      projectName: codebuildInstNme,
      environment: {
        computeType: ComputeType.SMALL,
        privileged: true, // for Docker to run
        buildImage: LinuxBuildImage.STANDARD_2_0,
      },
      // buildSpec: BuildSpec.fromSourceFilename(props.buildspecFilename),
      buildSpec: BuildSpec.fromObject({
        version: "0.2",
        env: {
          "parameter-store": {
            MCORPUS_DB_URL: props.ssmJdbcUrl.parameterName,
            MCORPUS_TEST_DB_URL: props.ssmJdbcTestUrl.parameterName,
          }
        },
        phases: {
          install: {
            "runtime-versions": {
              java: "openjdk8",
              docker: 18
            },
            commands: [
              "pip install --upgrade awscli"
            ],
          },
          pre_build: {
            commands: [
              "echo Test started on `date`",
              "aws --version",
              "docker --version",
              "mvn --version",

              "echo $(env)", // output the env vars

              "mvn clean test",

              "echo Logging in to Amazon ECR...",
              "echo $AWS_DEFAULT_REGION",
              "$(aws ecr get-login --region $AWS_DEFAULT_REGION --no-include-email)",
              // `REPOSITORY_URI=${props.env!.account}.dkr.ecr.${props.env!.region}.amazonaws.com/${props.ecrRepo.repositoryName}`,
              `REPOSITORY_URI=${ecrRef.repositoryUri}`,
              "COMMIT_HASH=$(echo $CODEBUILD_RESOLVED_SOURCE_VERSION | cut -c 1-7)",
              "APP_VERSION=$(cat mcorpus-gql/target/classes/app.properties | grep app.version | cut -d'=' -f2)",
              "BUILD_TIMESTAMP=$(cat mcorpus-gql/target/classes/app.properties | grep build.timestamp | cut -d'=' -f2)",
              "BUILD_TIMESTAMP_DIGITS=$(echo $BUILD_TIMESTAMP | sed 's/[^0-9]//g')",
              "BUILD_VERSION_TAG=${APP_VERSION}.${BUILD_TIMESTAMP_DIGITS}",
            ]
          },
          build: {
            commands: [
              "echo Build started on `date`",
              "mvn -DskipTests package",

              "echo Building the Docker image...",
              "cd mcorpus-gql/target",
              "docker build -t $REPOSITORY_URI:$COMMIT_HASH .",
              "docker tag $REPOSITORY_URI:$COMMIT_HASH $REPOSITORY_URI:$BUILD_VERSION_TAG",
            ]
          },
          post_build: {
            commands: [
              "echo Pushing the Docker images...",
              "docker push $REPOSITORY_URI:$COMMIT_HASH",
              "docker push $REPOSITORY_URI:$BUILD_VERSION_TAG",

              "TASKDEF_ARN=$(aws ecs list-task-definitions | jq -r '.taskDefinitionArns[-1]')",
              `CONTAINER_NAME=${props.ecsTaskDefContainerName}`,
              `CONTAINER_PORT=${props.lbToEcsPort}`,

              "echo Writing image detail file...",
              "printf '[{\"name\":\"%s\",\"imageUri\":\"%s\"}]' $CONTAINER_NAME $REPOSITORY_URI:$COMMIT_HASH > imageDetail.json",

              "echo Generating appspec.yaml file...",
              "cat ../../appspec-template.yaml | sed -e \"s%\\\${taskDefArn}%$TASKDEF_ARN%\" -e \"s%\\\${containerName}%$CONTAINER_NAME%\" -e \"s%\\\${containerPort}%$CONTAINER_PORT%\" > appspec.yaml",

              "echo Build completed on `date`"
            ]
          }
        },
        artifacts: {
          files: [
            "mcorpus-gql/target/appspec.yaml",
            "mcorpus-gql/target/imageDetail.json",
          ],
          "discard-paths": "yes"
        },
      }),
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
        // ECR
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
        // ECS
        "ecs:ListTaskDefinitions",
      ],
      resources: [
        "*"
      ],
    }));
    codebuildProject.addToRolePolicy(new iam.PolicyStatement({
      actions: [
        "s3:GetBucketAcl",
        "s3:GetBucketLocation",
        "s3:GetObjectVersion",
        "s3:GetObject",
        "s3:PutObject",
        "logs:CreateLogStream",
        "logs:CreateLogGroup",
        "logs:PutLogEvents",
      ],
      resources: [
        this.pipelineArtifactBucket.bucketArn,
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
        `arn:aws:ec2:${props.env!.region}:${props.env!.account}:network-interface/*`,
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
        "logs:PutLogEvents",
      ],
      resources: [
        "*" // TODO limit scope!
      ],
    }));

    const buildActionInstNme = this.iname('build-test', props);
    const buildOutput = new codepipeline.Artifact();
    const buildAction = new codepipeline_actions.CodeBuildAction({
      actionName: buildActionInstNme,
      project: codebuildProject,
      input: sourceOutput,
      outputs: [ buildOutput ],
    });

    // manual approve [deployment] action
    const maaDeploy = new codepipeline_actions.ManualApprovalAction({
      actionName: this.iname('manual-approval-deployment', props),
      notificationTopic: new sns.Topic(this, this.iname('confirm-deployment', props)),
      notifyEmails: props.cicdDeployApprovalEmails,
      additionalInformation: `Please confirm or reject this change for ${props.appEnv} deployment.`
    });

    // deploy action
    const deployAction = new codepipeline_actions.EcsDeployAction({
      actionName: this.iname('deploy', props),
      service: props.fargateSvc,
      imageFile: buildOutput.atPath('imageDetail.json'),
    });

    const pipelineInstNme = this.iname('cicd-pipeline', props);
    this.pipeline = new codepipeline.Pipeline(this, pipelineInstNme, {
      pipelineName: pipelineInstNme,
      stages: [
        {
          stageName: `Source-${props.appEnv}`,
          actions: [ sourceAction ]
        },
        {
          stageName: `Confirm-Build-${props.appEnv}`,
          actions: [ maaPostSource ]
        },
        {
          stageName: `Build-Test-${props.appEnv}`,
          actions: [ buildAction ]
        },
        {
          stageName: `Confirm-Deployment-${props.appEnv}`,
          actions: [ maaDeploy ]
        },
        {
          stageName: `Deploy-${props.appEnv}`,
          actions: [ deployAction ]
        },
      ],
      artifactBucket: this.pipelineArtifactBucket,
    });
    this.pipeline.addToRolePolicy(new iam.PolicyStatement({
      actions: [
        "iam:PassRole"
      ],
      resources: [
        codebuildProject.role!.roleArn,
      ],
    }));
    this.pipeline.addToRolePolicy(new iam.PolicyStatement({
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
    this.pipeline.addToRolePolicy(new iam.PolicyStatement({
      actions: [
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
    this.pipeline.addToRolePolicy(new iam.PolicyStatement({
      actions: [
        "lambda:InvokeFunction",
        "lambda:ListFunctions",
      ],
      resources: [
        "*" // TODO limit scope!
      ],
    }));

    // stack output
    new cdk.CfnOutput(this, 'CICDPipelineName', { value: this.pipeline.pipelineName });
    new cdk.CfnOutput(this, 'CICDCodePipelineRoleName', { value: this.pipeline.role!.roleName });
    new cdk.CfnOutput(this, 'CICDPipelineArtifactBucketName', { value: this.pipelineArtifactBucket.bucketName });
    new cdk.CfnOutput(this, 'CICDCodeBuildRoleName', { value: codebuildProject.role!.roleName });
  }
}