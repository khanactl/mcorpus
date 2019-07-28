import cdk = require('@aws-cdk/core');

export class SecretsStack extends cdk.Stack {

  public readonly kmsArn: string;
  public readonly mcorpusDbUrlArn: string;
  public readonly jwtSaltArn: string;

  constructor(scope: cdk.Construct, id: string) {
    super(scope, id);

    this.kmsArn = 'arn:aws:kms:us-west-2:524006177124:key/c66b3f26-8480-40f1-95a1-6abf58f2aedd';
    this.mcorpusDbUrlArn = 'arn:aws:ssm:us-west-2:524006177124:parameter/mcorpusDbUrl';
    this.jwtSaltArn = 'arn:aws:ssm:us-west-2:524006177124:parameter/jwtSalt';
  }
}