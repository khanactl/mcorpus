import cdk = require('@aws-cdk/core');
import { IStackProps, BaseStack } from './cdk-native'
import cfn = require('@aws-cdk/aws-cloudformation');
import lambda = require('@aws-cdk/aws-lambda');
import ssm = require('@aws-cdk/aws-ssm');
import iam = require('@aws-cdk/aws-iam');
import path = require('path');
import { IVpc, SubnetType, ISecurityGroup } from '@aws-cdk/aws-ec2';
import { IStringParameter } from '@aws-cdk/aws-ssm';
import { PolicyStatement } from '@aws-cdk/aws-iam';
import fs = require('fs');
import archiver = require('archiver');

export interface IDbBootstrapProps extends IStackProps {
  /**
   * The VPC ref.
   */
  readonly vpc: IVpc;
  /**
   * The Db bootstrap security group.
   */
  readonly dbBootstrapSecGrp: ISecurityGroup;
  /**
   * rds db instance secrets
   */
  readonly dbJsonSecretArn: string;
  /**
   * The AWS Region used by the lambda fn for both:
   * 1. Fetching db rds json secret (SecretsManager)
   * 2. Creating SSM secure param for jdbc urls (SSM SecureParameter)
   */
  readonly targetRegion: string;
}

/**
 * DbBootstrapStack
 * 
 * Wrapper around a Custom Resource that is responsible for:
 * 1. Creating the db schema
 * 2. Creating the db roles/users
 * 3. Creating the needed SSM db/jdbc secure param secrets for downstream use
 */
export class DbBootstrapStack extends BaseStack {

  public readonly dbBootstrapRole: iam.Role;
  
  public readonly ssmNameJdbcUrl: string;
  public readonly ssmVersionJdbcUrl: number;

  public readonly ssmNameJdbcTestUrl: string;
  public readonly ssmVersionJdbcTestUrl: number;
  
  public readonly responseMessage: string;

  public readonly ssmJdbcUrl: IStringParameter;
  public readonly ssmJdbcTestUrl: IStringParameter;

  constructor(scope: cdk.Construct, props: IDbBootstrapProps) {
    super(scope, 'DbBootstrap', props);

    // db dbootstrap role
    const dbBootstrapRoleInstNme = this.iname('db-bootstrap-role');
    this.dbBootstrapRole = new iam.Role(this, dbBootstrapRoleInstNme, {
      assumedBy: new iam.ServicePrincipal('lambda.amazonaws.com')
    });
    this.dbBootstrapRole.addManagedPolicy({
      managedPolicyArn: 'arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole'
    });
    this.dbBootstrapRole.addManagedPolicy({
      managedPolicyArn: 'arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole'
    });
    this.dbBootstrapRole.addToPolicy(new PolicyStatement({
      actions: ['secretsmanager:GetSecretValue'], 
      resources: [ 
        props.dbJsonSecretArn, 
      ],
    }));
    this.dbBootstrapRole.addToPolicy(new PolicyStatement({
      actions: ['ssm:PutParameter'], 
      resources: [ 
        '*' // TODO narrow scope
      ],
    }));
    // END db bootstrap role

    const lambdaProviderInstNme = this.iname('db-bootstrap-lambda');
    const lambdaProvider = new lambda.SingletonFunction(this, lambdaProviderInstNme, {
      vpc: props.vpc, 
      vpcSubnets: { subnetType: SubnetType.PRIVATE }, 
      securityGroup: props.dbBootstrapSecGrp, 
      uuid: 'f8dfc6d4-d864-4f4f-8d65-63c2ea54f2ac', // one-time globaly unique 
      runtime: lambda.Runtime.PYTHON_3_7, 
      // functionName: 'DbBootstrapLambda', 
      memorySize: 128, 
      timeout: cdk.Duration.seconds(60), 
      code: lambda.Code.fromAsset(
        path.join(__dirname, "../lambda/dbbootstrap") // dir ref
      ), 
      handler: 'dbbootstrap.main', 
      role: this.dbBootstrapRole, 
    });
  
    const resourceInstNme = this.iname('db-bootstrap');
    const resource = new cfn.CustomResource(this, resourceInstNme, {
      provider: cfn.CustomResourceProvider.lambda(lambdaProvider),
      properties: {
        'DbJsonSecretArn': props.dbJsonSecretArn, // NOTE: python lambda input params are capitalized!
        'TargetRegion': props.targetRegion, 
      }
    });
  
    this.ssmNameJdbcUrl = resource.getAtt('SsmNameJdbcUrl').toString();
    this.ssmVersionJdbcUrl = parseInt(resource.getAtt('SsmVersionJdbcUrl').toString());
    
    this.ssmNameJdbcTestUrl = resource.getAtt('SsmNameJdbcTestUrl').toString();
    this.ssmVersionJdbcTestUrl = parseInt(resource.getAtt('SsmVersionJdbcTestUrl').toString());
    
    this.responseMessage = resource.getAtt('Message').toString();

    // obtain the just generated SSM jdbc url param refs
    const ssmJdbcUrlInstNme = this.iname('db-url');
    this.ssmJdbcUrl = ssm.StringParameter.fromSecureStringParameterAttributes(this, ssmJdbcUrlInstNme, {
      parameterName: this.ssmNameJdbcUrl, 
      version: this.ssmVersionJdbcUrl, 
      simpleName: false, 
    });
    const ssmJdbcTestUrlInstNme = this.iname('test-db-url');
    this.ssmJdbcTestUrl = ssm.StringParameter.fromSecureStringParameterAttributes(this, ssmJdbcTestUrlInstNme, {
      parameterName: this.ssmNameJdbcTestUrl, 
      version: this.ssmVersionJdbcTestUrl, 
      simpleName: false, 
    });

    // stack output
    new cdk.CfnOutput(this, 'dbBootstrapRoleArn', { value: 
      this.dbBootstrapRole.roleArn
    });
    new cdk.CfnOutput(this, 'dbBootstrapResponseMessage', { value: 
      this.responseMessage
    });
    new cdk.CfnOutput(this, 'ssmJdbcUrlArn', { value: 
      this.ssmJdbcUrl.parameterArn
    });
    new cdk.CfnOutput(this, 'ssmJdbcTestUrlArn', { value: 
      this.ssmJdbcTestUrl.parameterArn
    });

  }
}