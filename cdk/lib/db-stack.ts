import cdk = require('@aws-cdk/core');
import { ISecurityGroup, IVpc, SecurityGroup, SubnetType } from '@aws-cdk/aws-ec2';
import { RetentionDays } from '@aws-cdk/aws-logs';
import { DatabaseInstance } from '@aws-cdk/aws-rds';
import { BaseStack, iname, IStackProps } from './cdk-native';
import ec2 = require('@aws-cdk/aws-ec2');
import rds = require('@aws-cdk/aws-rds');
import secrets = require('@aws-cdk/aws-secretsmanager');

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
  public readonly dbSecGrp: ec2.ISecurityGroup;

  /**
   * The db master user secret.
   */
  public readonly dbInstanceJsonSecret: secrets.ISecret;

  /**
   * The db this.dbInstance.
   */
  public readonly dbInstance: DatabaseInstance;

  constructor(scope: cdk.Construct, id: string, props: IDbProps) {
    super(scope, id, props);

    // db security group
    const sgDbInstNme = iname('db-sec-grp', props);
    this.dbSecGrp = new SecurityGroup(this, sgDbInstNme, {
      vpc: props.vpc,
      description: 'Db security group.',
      allowAllOutbound: true,
      securityGroupName: sgDbInstNme,
    });
    this.dbSecGrp.node.applyAspect(new cdk.Tag('Name', sgDbInstNme));

    const optionGroup = rds.OptionGroup.fromOptionGroupName(this, 'dbOptionGroup', 'default:postgres-11');
    const parameterGroup = rds.ParameterGroup.fromParameterGroupName(this, 'dbParameterGroup', 'default.postgres11');

    const dbInstNme = iname('db', props);
    this.dbInstance = new DatabaseInstance(this, dbInstNme, {
      vpc: props.vpc,
      instanceIdentifier: dbInstNme,
      databaseName: props.dbName,
      masterUsername: props.dbMasterUsername,
      engine: rds.DatabaseInstanceEngine.POSTGRES,
      instanceClass: ec2.InstanceType.of(ec2.InstanceClass.T2, ec2.InstanceSize.SMALL),
      optionGroup: optionGroup,
      parameterGroup: parameterGroup,
      enablePerformanceInsights: false,
      multiAz: false,
      autoMinorVersionUpgrade: true,
      // cloudwatchLogsExports: ['alert'],
      cloudwatchLogsRetention: RetentionDays.ONE_MONTH,
      monitoringInterval: cdk.Duration.seconds(60), // default is 1 min
      storageEncrypted: true,
      backupRetention: cdk.Duration.days(0), // i.e. do not do backups
      vpcPlacement: { subnetType: SubnetType.PRIVATE }, // private
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
    const rdsSecretRotation = this.dbInstance.addRotationSingleUser(cdk.Duration.days(30));

    this.dbInstanceJsonSecret = this.dbInstance.secret!;

    // stack output
    new cdk.CfnOutput(this, 'dbEndpoint', {
      value: this.dbInstance.dbInstanceEndpointAddress + ':' + this.dbInstance.dbInstanceEndpointPort,
    });
    new cdk.CfnOutput(this, 'dbInstanceJsonSecretArn', { value: this.dbInstanceJsonSecret!.secretArn });
  }
}
