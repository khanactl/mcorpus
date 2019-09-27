import cfn = require('@aws-cdk/aws-cloudformation');
import lambda = require('@aws-cdk/aws-lambda');
import ssm = require('@aws-cdk/aws-ssm');
import iam = require('@aws-cdk/aws-iam');
import cdk = require('@aws-cdk/core');
import path = require('path');
import { ISecurityGroup, IVpc, SubnetType } from '@aws-cdk/aws-ec2';
import { PolicyStatement } from '@aws-cdk/aws-iam';
import { S3EventSource } from '@aws-cdk/aws-lambda-event-sources';
import { BucketEncryption } from '@aws-cdk/aws-s3';
import s3 = require('@aws-cdk/aws-s3')
import { IKey } from '@aws-cdk/aws-kms';
import kms = require('@aws-cdk/aws-kms');

export interface IDbDataProps extends cdk.StackProps {
  /**
   * The VPC ref.
   */
  readonly vpc: IVpc;
  /**
   * The Db data security group.
   */
  readonly dbDataSecGrp: ISecurityGroup;
  /**
   * The db secret.
   */
  readonly dbJsonSecretArn: string;
  /**
   * The KMS key ARN to use to encrypt the db data zip file 
   * in the generated s3 data bucket.
   */
  // readonly s3KmsEncKeyArn: string;
}

export class DbDataStack extends cdk.Stack {

  public readonly dbDataBucket: s3.Bucket;

  public readonly dbDataRole: iam.Role;

  public readonly dbDataFn: lambda.Function;

  // public readonly s3KmsKey: IKey;

  constructor(scope: cdk.Construct, id: string, props: IDbDataProps) {
    super(scope, id);

    // const s3KmsKey: IKey = kms.Key.fromKeyArn(this, 's3-kms-key', props.s3KmsEncKeyArn);

    // create s3 bucket to hold db data
    this.dbDataBucket = new s3.Bucket(this, 'db-data-bucket', {
      bucketName: 'db-data-bucket', 
      // encryption: BucketEncryption.KMS, // a key will be auto created
      encryption: BucketEncryption.UNENCRYPTED, 
      // encryptionKey: s3KmsKey, 
    });

    // create db data lambda fn exec role
    this.dbDataRole = new iam.Role(this, 'db-data-role', {
      assumedBy: new iam.ServicePrincipal('lambda.amazonaws.com')
    });
    this.dbDataRole.addManagedPolicy({
      managedPolicyArn: 'arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole'
    });
    this.dbDataRole.addManagedPolicy({
      managedPolicyArn: 'arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole'
    });
    this.dbDataRole.addToPolicy(new PolicyStatement({
      actions: ['secretsmanager:GetSecretValue'], 
      resources: [ 
        props.dbJsonSecretArn, 
      ],
    }));
    this.dbDataRole.addToPolicy(new PolicyStatement({
      actions: [
        // "s3:PutObject",
        "s3:GetObject",
        "s3:GetBucketTagging",
        "s3:ListBucket",
        "s3:GetBucketVersioning",
        "s3:GetObjectVersion", 
        "kms:Decrypt", 
      ], 
      resources: [ 
        this.dbDataBucket.bucketArn, 
        this.dbDataBucket.bucketArn + '/*', 
      ],
    }));
    // END db data role

    // lambda fn
    this.dbDataFn = new lambda.Function(this, 'db-data-fn', { 
      vpc: props.vpc, 
      vpcSubnets: { subnetType: SubnetType.PRIVATE }, 
      securityGroup: props.dbDataSecGrp, 
      code: lambda.Code.fromAsset(
        path.join(__dirname, "../lambda/dbdata") // dir ref
      ), 
      handler: 'dbdata.main', 
      runtime: lambda.Runtime.PYTHON_3_7, 
      // functionName: 'db-data', 
      memorySize: 512, 
      timeout: cdk.Duration.seconds(3 * 60), 
      role: this.dbDataRole, 
      environment: {
        "DbJsonSecretArn": props.dbJsonSecretArn, 
      }, 
    });

    // s3 object created event
    this.dbDataFn.addEventSource(new S3EventSource(this.dbDataBucket, {
      events: [ s3.EventType.OBJECT_CREATED ], 
      /*
      filters: [
        {
          prefix: '', 
          suffix: '', 
        }
      ]
      */
    }));

    // stack output
    new cdk.CfnOutput(this, 'dbDataBucketName', { value: 
      this.dbDataBucket.bucketName
    });
    /*
    new cdk.CfnOutput(this, 'dbDataBucketEncryptionKeyArn', { value: 
      this.dbDataBucket.encryptionKey!.keyArn
    });
    */
    new cdk.CfnOutput(this, 'dbDataRoleName', { value: 
      this.dbDataRole.roleName
    });
    new cdk.CfnOutput(this, 'dbDataFnName', { value: 
      this.dbDataFn.functionName
    });

  }

}