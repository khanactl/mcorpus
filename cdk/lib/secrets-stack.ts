import cdk = require('@aws-cdk/core');
import ssm = require('@aws-cdk/aws-ssm');

export class SecretsStack extends cdk.Stack {

  public readonly kmsArn: string;
  /*
  public readonly ssmMcorpusDbUrl: ssm.IParameter;
  public readonly ssmMcorpusTestDbUrl: ssm.IParameter;
  public readonly ssmJwtSalt: ssm.IParameter;
  */
 
  constructor(scope: cdk.Construct, id: string) {
    super(scope, id);

    this.kmsArn = 'arn:aws:kms:us-west-2:524006177124:key/c66b3f26-8480-40f1-95a1-6abf58f2aedd';
    /*
    this.mcorpusDbUrlArn = 'arn:aws:ssm:us-west-2:524006177124:parameter/mcorpusDbUrl';
    this.jwtSaltArn = 'arn:aws:ssm:us-west-2:524006177124:parameter/jwtSalt';
    */

    /*
    this.ssmMcorpusDbUrl = ssm.StringParameter.fromSecureStringParameterAttributes(this, 'mcorpusDbUrl', {
      parameterName: 'mcorpusDbUrl',
      version: 2
    });
    this.ssmMcorpusTestDbUrl = ssm.StringParameter.fromSecureStringParameterAttributes(this, 'mcorpusTestDbUrl', {
      parameterName: 'mcorpusTestDbUrl',
      version: 2
    });
    this.ssmJwtSalt = ssm.StringParameter.fromSecureStringParameterAttributes(this, 'jwtSalt', {
      parameterName: 'jwtSalt',
      version: 1
    });
    */
  }
}