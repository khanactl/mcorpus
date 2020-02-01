import cdk = require('@aws-cdk/core');
import { BaseStack, IStackProps } from './cdk-native';
import ecr = require('@aws-cdk/aws-ecr');

export interface IEcrStackProps extends IStackProps {
  readonly ecrName: string;
}

export class EcrStack extends BaseStack {
  public readonly appRepository: ecr.Repository;

  constructor(scope: cdk.Construct, id: string, props: IEcrStackProps) {
    super(scope, id, props);

    this.appRepository = new ecr.Repository(this, 'AppEcrRepo', {
      repositoryName: props.ecrName,
      removalPolicy: cdk.RemovalPolicy.DESTROY,
    });

    // stack output
    new cdk.CfnOutput(this, 'ECRName', { value: this.appRepository.repositoryName });
  }
}
