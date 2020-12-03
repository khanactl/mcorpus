import { ISecurityGroup, IVpc, SubnetType } from '@aws-cdk/aws-ec2';
import { PolicyStatement, Role, ServicePrincipal } from '@aws-cdk/aws-iam';
import { Code, Runtime, SingletonFunction } from '@aws-cdk/aws-lambda';
import { RetentionDays } from '@aws-cdk/aws-logs';
import { IStringParameter, StringParameter } from '@aws-cdk/aws-ssm';
import { CfnOutput, Construct, CustomResource, Duration } from '@aws-cdk/core';
import { RemovalPolicy } from "@aws-cdk/core/lib/removal-policy";
import { Provider } from '@aws-cdk/custom-resources';
import { copyFileSync, copySync, mkdirSync } from 'fs-extra';
import { join as pjoin } from 'path';
import { v4 as uuidv4 } from 'uuid';
import { BaseStack, iname, IStackProps } from './cdk-native';

export const DbBootstrapStackRootProps = {
  rootStackName: 'db-bootstrap',
  description: 'Creates the db schema and required roles using a stack embedded CustomResource.',
};

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
  public readonly dbBootstrapRole: Role;

  public readonly ssmNameJdbcAdminUrl: string;
  public readonly ssmVersionJdbcAdminUrl: number;

  public readonly ssmNameJdbcUrl: string;
  public readonly ssmVersionJdbcUrl: number;

  public readonly ssmNameJdbcTestUrl: string;
  public readonly ssmVersionJdbcTestUrl: number;

  public readonly responseMessage: string;

  public readonly ssmJdbcAdminUrl: IStringParameter;
  public readonly ssmJdbcUrl: IStringParameter;
  public readonly ssmJdbcTestUrl: IStringParameter;

  constructor(scope: Construct, props: IDbBootstrapProps) {
    super(scope, DbBootstrapStackRootProps.rootStackName, {
      ...props, ...{ description: DbBootstrapStackRootProps.description }
    });

    // db dbootstrap role
    const dbBootstrapRoleInstNme = iname('db-bootstrap-role', props);
    this.dbBootstrapRole = new Role(this, dbBootstrapRoleInstNme, {
      assumedBy: new ServicePrincipal('lambda.amazonaws.com'),
    });
    this.dbBootstrapRole.addManagedPolicy({
      managedPolicyArn: 'arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole',
    });
    this.dbBootstrapRole.addManagedPolicy({
      managedPolicyArn: 'arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole',
    });
    this.dbBootstrapRole.addToPolicy(
      new PolicyStatement({
        actions: ['secretsmanager:GetSecretValue'],
        resources: [props.dbJsonSecretArn],
      })
    );
    this.dbBootstrapRole.addToPolicy(
      new PolicyStatement({
        actions: ['ssm:PutParameter'],
        resources: [
          '*', // TODO narrow scope
        ],
      })
    );
    // END db bootstrap role

    // prepare asset under /tmp dir as cdk does NOT honor symlinks in codepipeline/pipelines
    const dpath = pjoin(__dirname, '../lambda/dbbootstrap');
    const dbgenpath = pjoin(__dirname, '../../mcorpus-db/gen');
    const flambdafn = 'dbbootstrap.py';
    const fschemadef = 'mcorpus-schema.ddl';
    const froles = 'mcorpus-roles.ddl';
    const fdbdatastub = 'mcorpus-mcuser.csv';
    const psycopg2dirname = 'psycopg2';
    const tmpAssetDir = `/tmp/${uuidv4()}`;
    mkdirSync(tmpAssetDir);
    copyFileSync(`${dpath}/${flambdafn}`, `${tmpAssetDir}/${flambdafn}`);
    copyFileSync(`${dbgenpath}/${fschemadef}`, `${tmpAssetDir}/${fschemadef}`);
    copyFileSync(`${dbgenpath}/${froles}`, `${tmpAssetDir}/${froles}`);
    copyFileSync(`${dbgenpath}/${fdbdatastub}`, `${tmpAssetDir}/${fdbdatastub}`);
    copySync(`${dpath}/../${psycopg2dirname}`, `${tmpAssetDir}/${psycopg2dirname}`);

    const lambdaProviderInstNme = iname('db-bootstrap-lambda-fn', props);
    const lambdaFn = new SingletonFunction(this, lambdaProviderInstNme, {
      vpc: props.vpc,
      vpcSubnets: { subnetType: SubnetType.PRIVATE },
      securityGroup: props.dbBootstrapSecGrp,
      uuid: 'f8dfc6d4-d864-4f4f-8d65-63c2ea54f2ac', // one-time globaly unique
      runtime: Runtime.PYTHON_3_8,
      // functionName: 'DbBootstrapLambda',
      memorySize: 128,
      timeout: Duration.seconds(60),
      code: Code.fromAsset(pjoin(tmpAssetDir)),
      handler: 'dbbootstrap.main',
      role: this.dbBootstrapRole,
      logRetention: RetentionDays.ONE_DAY,
    });

    const lambdaFnProviderInstNme = iname('db-bootstrap-lambda-fn-provider', props);
    const lambdaFnProvider = new Provider(this, lambdaFnProviderInstNme, {
      onEventHandler: lambdaFn,
      logRetention: RetentionDays.ONE_DAY,
    });

    const resourceInstNme = iname('db-bootstrap', props);
    const resource = new CustomResource(this, resourceInstNme, {
      serviceToken: lambdaFnProvider.serviceToken,
      // provider: CustomResourceProvider.lambda(lambdaFn),
      properties: {
        AppEnv: props.appEnv,
        DbJsonSecretArn: props.dbJsonSecretArn, // NOTE: python lambda input params are capitalized!
        TargetRegion: props.targetRegion,
        SsmNameJdbcAdminUrl: `/mcorpusDbAdminUrl/${props.appEnv}`,
        SsmNameJdbcUrl: `/mcorpusDbUrl/${props.appEnv}`, // NOTE: must use '/pname' (not 'pname') format!
        SsmNameJdbcTestUrl: `/mcorpusTestDbUrl/${props.appEnv}`,
      },
      removalPolicy: RemovalPolicy.DESTROY,
    });

    this.ssmNameJdbcAdminUrl = resource.getAttString('SsmNameJdbcAdminUrl');
    this.ssmVersionJdbcAdminUrl = parseInt(resource.getAttString('SsmVersionJdbcAdminUrl'));

    this.ssmNameJdbcUrl = resource.getAttString('SsmNameJdbcUrl');
    this.ssmVersionJdbcUrl = parseInt(resource.getAttString('SsmVersionJdbcUrl'));

    this.ssmNameJdbcTestUrl = resource.getAttString('SsmNameJdbcTestUrl');
    this.ssmVersionJdbcTestUrl = parseInt(resource.getAttString('SsmVersionJdbcTestUrl'));

    this.responseMessage = resource.getAttString('Message');

    // obtain the just generated SSM jdbc url param refs
    // mcadmin
    const ssmJdbcAdminUrlInstNme = iname('db-admin-url', props);
    this.ssmJdbcAdminUrl = StringParameter.fromSecureStringParameterAttributes(this, ssmJdbcAdminUrlInstNme, {
      parameterName: this.ssmNameJdbcAdminUrl,
      version: this.ssmVersionJdbcAdminUrl,
      simpleName: false,
    });
    // mcweb
    const ssmJdbcUrlInstNme = iname('db-url', props);
    this.ssmJdbcUrl = StringParameter.fromSecureStringParameterAttributes(this, ssmJdbcUrlInstNme, {
      parameterName: this.ssmNameJdbcUrl,
      version: this.ssmVersionJdbcUrl,
      simpleName: false,
    });
    // mewebtest
    const ssmJdbcTestUrlInstNme = iname('test-db-url', props);
    this.ssmJdbcTestUrl = StringParameter.fromSecureStringParameterAttributes(this, ssmJdbcTestUrlInstNme, {
      parameterName: this.ssmNameJdbcTestUrl,
      version: this.ssmVersionJdbcTestUrl,
      simpleName: false,
    });

    // stack output
    // new CfnOutput(this, 'CustomResourceRef', { value: resource.ref });
    new CfnOutput(this, 'dbBootstrapRoleArn', { value: this.dbBootstrapRole.roleArn });
    new CfnOutput(this, 'dbBootstrapResponseMessage', { value: this.responseMessage });
    new CfnOutput(this, 'ssmJdbcAdminUrlArn', { value: this.ssmJdbcAdminUrl.parameterArn });
    new CfnOutput(this, 'ssmJdbcUrlArn', { value: this.ssmJdbcUrl.parameterArn });
    new CfnOutput(this, 'ssmJdbcTestUrlArn', { value: this.ssmJdbcTestUrl.parameterArn });
  }
}
