import cdk = require('@aws-cdk/core');
import { DockerImageAsset } from '@aws-cdk/aws-ecr-assets';
import { BaseStack, IStackProps } from './cdk-native';
import path = require('path');
import ecr = require('@aws-cdk/aws-ecr');

/**
 * ECR Stack config properties.
 */
export interface IECRProps extends IStackProps {
  /**
   * The name of the ECR repository to use.
   */
  readonly repoName: string;
}

/**
 * ECR Stack.
 *
 * Responsible for creating the shared ECR instance
 * which holds all of the app docker images.
 */
export class ECRStack extends BaseStack {

  /**
   * The target ECR which may have been created or may already exist.
   */
  public readonly ecrRepo: ecr.IRepository;

  /**
   * The resolved name of the image tag uploaded to ECR.
   */
  public readonly targetImageTagName: string;

  constructor(scope: cdk.Construct, props: IECRProps) {
    super(scope, 'ECR', props);

    // build docker image locally and upload to ECR
    const dockerAssetInstNme = this.iname('local-docker-asset');
    const dockerAsset = new DockerImageAsset(this, dockerAssetInstNme, {
      directory: path.join(__dirname, "../../mcorpus-gql/target/awsdockerasset"),
      repositoryName: props.repoName,
    });
    this.ecrRepo = dockerAsset.repository;
    this.targetImageTagName = "latest"; // TODO -HACK ALERT- FIX by extracting the image tag name

    // stack outputs
    new cdk.CfnOutput(this, 'ecrRepoName', { value:
      this.ecrRepo.repositoryName
    });
    new cdk.CfnOutput(this, 'targetImageTagName', { value:
      this.targetImageTagName ? this.targetImageTagName : "-UNSET-"
    });

  }
}

