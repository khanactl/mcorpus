import { ISecurityGroup, IVpc, SubnetType } from '@aws-cdk/aws-ec2';
import { PolicyStatement, Role, ServicePrincipal } from '@aws-cdk/aws-iam';
import { Code, Function, Runtime } from '@aws-cdk/aws-lambda';
import { S3EventSource } from '@aws-cdk/aws-lambda-event-sources';
import { BlockPublicAccess, Bucket, BucketEncryption, EventType } from '@aws-cdk/aws-s3';
import { CfnOutput, Construct, Duration, RemovalPolicy } from '@aws-cdk/core';
import { join as pjoin } from 'path';
import { BaseStack, iname, IStackProps } from './cdk-native';

export interface IDbDataProps extends IStackProps {
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

export class DbDataStack extends BaseStack {
  public readonly dbDataBucket: Bucket;

  public readonly dbDataRole: Role;

  public readonly dbDataFn: Function;

  // public readonly s3KmsKey: IKey;

  constructor(scope: Construct, id: string, props: IDbDataProps) {
    super(scope, id, props);

    // const s3KmsKey: IKey = kms.Key.fromKeyArn(this, 's3-kms-key', props.s3KmsEncKeyArn);

    // create s3 bucket to hold db data
    const dbDataBucketInstNme = iname('db-data-bucket', props);
    this.dbDataBucket = new Bucket(this, dbDataBucketInstNme, {
      bucketName: dbDataBucketInstNme,
      // encryption: BucketEncryption.KMS, // a key will be auto created
      encryption: BucketEncryption.S3_MANAGED,
      // encryptionKey: s3KmsKey,
      blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
      removalPolicy: RemovalPolicy.DESTROY,
    });

    // create db data lambda fn exec role
    const dbDataRoleInstNme = iname('db-data-role', props);
    this.dbDataRole = new Role(this, dbDataRoleInstNme, {
      assumedBy: new ServicePrincipal('lambda.amazonaws.com'),
    });
    this.dbDataRole.addManagedPolicy({
      managedPolicyArn: 'arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole',
    });
    this.dbDataRole.addManagedPolicy({
      managedPolicyArn: 'arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole',
    });
    this.dbDataRole.addToPolicy(
      new PolicyStatement({
        actions: ['secretsmanager:GetSecretValue'],
        resources: [props.dbJsonSecretArn],
      })
    );
    this.dbDataRole.addToPolicy(
      new PolicyStatement({
        actions: [
          // "s3:PutObject",
          's3:GetObject',
          's3:GetBucketTagging',
          's3:ListBucket',
          's3:GetBucketVersioning',
          's3:GetObjectVersion',
          'kms:Decrypt',
        ],
        resources: [this.dbDataBucket.bucketArn, this.dbDataBucket.bucketArn + '/*'],
      })
    );
    // END db data role

    // lambda fn
    const dbDataFnInstNme = iname('db-data-fn', props);
    this.dbDataFn = new Function(this, dbDataFnInstNme, {
      vpc: props.vpc,
      vpcSubnets: { subnetType: SubnetType.PRIVATE },
      securityGroup: props.dbDataSecGrp,
      code: Code.fromAsset(
        pjoin(__dirname, '../lambda/dbdata') // dir ref
      ),
      handler: 'dbdata.main',
      runtime: Runtime.PYTHON_3_7,
      // functionName: 'db-data',
      memorySize: 512,
      timeout: Duration.seconds(3 * 60),
      role: this.dbDataRole,
      environment: {
        DbJsonSecretArn: props.dbJsonSecretArn,
      },
    });

    // s3 object created event
    this.dbDataFn.addEventSource(
      new S3EventSource(this.dbDataBucket, {
        events: [EventType.OBJECT_CREATED],
      })
    );

    // stack output
    new CfnOutput(this, 'dbDataBucketName', { value: this.dbDataBucket.bucketName });
    /*
    new CfnOutput(this, 'dbDataBucketEncryptionKeyArn', { value:
      this.dbDataBucket.encryptionKey!.keyArn
    });
    */
    new CfnOutput(this, 'dbDataRoleName', { value: this.dbDataRole.roleName });
    new CfnOutput(this, 'dbDataFnName', { value: this.dbDataFn.functionName });
  }
}
