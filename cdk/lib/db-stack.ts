import {
  InstanceClass,
  InstanceSize,
  InstanceType,
  ISecurityGroup,
  IVpc,
  SecurityGroup,
  SubnetType
} from '@aws-cdk/aws-ec2';
import { RetentionDays } from '@aws-cdk/aws-logs';
import { DatabaseInstance, DatabaseInstanceEngine, OptionGroup, ParameterGroup, PostgresEngineVersion } from '@aws-cdk/aws-rds';
import { ISecret } from '@aws-cdk/aws-secretsmanager';
import { Aspects, CfnOutput, Construct, Duration, Tag } from '@aws-cdk/core';
import { BaseStack, iname, IStackProps } from './cdk-native';

export const DbStackRootProps = {
  rootStackName: 'db',
  description: 'Creates the app database instance in RDS.',
};

/**
 * Db Stack config properties.
 */
export interface IDbProps extends IStackProps {
  /**
   * The VPC ref
   */
  readonly vpc: IVpc;

  /**
   * The db bootstrap security group ref.
   */
  readonly dbBootstrapSecGrp: ISecurityGroup;
  /**
   * The ECS/Fargate security group ref.
   */
  readonly ecsSecGrp: ISecurityGroup;
  /**
   * The CICD codebuild security group ref.
   */
  readonly codebuildSecGrp: ISecurityGroup;
  /**
   * The database name to use.
   */
  readonly dbName: string;
  /**
   * The database master username to use.
   */
  readonly dbMasterUsername: string;
}

/**
 * Db Stack.
 */
export class DbStack extends BaseStack {
  /**
   * The RDS db security group.
   */
  public readonly dbSecGrp: ISecurityGroup;

  /**
   * The db master user secret.
   */
  public readonly dbInstanceJsonSecret: ISecret;

  /**
   * The db this.dbInstance.
   */
  public readonly dbInstance: DatabaseInstance;

  constructor(scope: Construct, props: IDbProps) {
    super(scope, DbStackRootProps.rootStackName, {
      ...props, ...{ description: DbStackRootProps.description }
    });

    // db security group
    const sgDbInstNme = iname('db-sec-grp', props);
    this.dbSecGrp = new SecurityGroup(this, sgDbInstNme, {
      vpc: props.vpc,
      description: 'Db security group.',
      allowAllOutbound: true,
      securityGroupName: sgDbInstNme,
    });
    Aspects.of(this.dbSecGrp).add(new Tag('Name', sgDbInstNme));

    const optionGroup = OptionGroup.fromOptionGroupName(this, 'dbOptionGroup', 'default:postgres-11');
    const parameterGroup = ParameterGroup.fromParameterGroupName(this, 'dbParameterGroup', 'default.postgres11');

    const dbInstNme = iname('db', props);
    this.dbInstance = new DatabaseInstance(this, dbInstNme, {
      vpc: props.vpc,
      instanceIdentifier: dbInstNme,
      databaseName: props.dbName,
      credentials: {
        username: props.dbMasterUsername
      },
      engine: DatabaseInstanceEngine.postgres({
        version: PostgresEngineVersion.VER_11_8,
      }),
      instanceType: InstanceType.of(InstanceClass.T2, InstanceSize.SMALL),
      optionGroup: optionGroup,
      parameterGroup: parameterGroup,
      enablePerformanceInsights: false,
      multiAz: false,
      autoMinorVersionUpgrade: true,
      // cloudwatchLogsExports: ['alert'],
      cloudwatchLogsRetention: RetentionDays.ONE_MONTH,
      monitoringInterval: Duration.seconds(60), // default is 1 min
      storageEncrypted: true,
      backupRetention: Duration.days(0), // i.e. do not do backups
      vpcSubnets: { subnetType: SubnetType.PRIVATE_WITH_NAT }, // private
      deletionProtection: false,
      securityGroups: [this.dbSecGrp],
    });

    // allow db bootstrap lambda fn to connect to db
    this.dbInstance.connections.allowDefaultPortFrom(props.dbBootstrapSecGrp, 'from db bootstrap');

    // allow ecs container traffic to db
    this.dbInstance.connections.allowDefaultPortFrom(
      props.ecsSecGrp,
      'from ecs container' // NOTE: connections construct resolves to a db specific sec grp!
    );

    // allow codebuild traffic to db
    this.dbInstance.connections.allowDefaultPortFrom(props.codebuildSecGrp, 'from codebuild');

    // Rotate the master user password every 30 days
    this.dbInstance.addRotationSingleUser({
      automaticallyAfter: Duration.days(30)
    });

    this.dbInstanceJsonSecret = this.dbInstance.secret!;

    // stack output
    new CfnOutput(this, 'dbEndpoint', {
      value: this.dbInstance.dbInstanceEndpointAddress + ':' + this.dbInstance.dbInstanceEndpointPort,
    });
    new CfnOutput(this, 'dbInstanceJsonSecretArn', { value: this.dbInstanceJsonSecret.secretArn });
  }
}
