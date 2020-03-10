import { Repository } from '@aws-cdk/aws-ecr';
import { CfnOutput, Construct, RemovalPolicy } from '@aws-cdk/core';
import { BaseStack, IStackProps } from './cdk-native';

export interface IEcrStackProps extends IStackProps {
  readonly ecrName: string;
}

export class EcrStack extends BaseStack {
  public readonly appRepository: Repository;

  constructor(scope: Construct, id: string, props: IEcrStackProps) {
    super(scope, id, props);

    this.appRepository = new Repository(this, 'AppEcrRepo', {
      repositoryName: props.ecrName,
      removalPolicy: RemovalPolicy.DESTROY,
    });

    // stack output
    new CfnOutput(this, 'ECRName', { value: this.appRepository.repositoryName });
  }
}
