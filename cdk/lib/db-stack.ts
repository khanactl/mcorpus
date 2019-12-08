import cdk = require('@aws-cdk/core');
import { IStackProps, BaseStack } from './cdk-native'
import ec2 = require('@aws-cdk/aws-ec2');
import rds = require('@aws-cdk/aws-rds');
import secrets = require('@aws-cdk/aws-secretsmanager');
import cloudwatch = require('@aws-cdk/aws-cloudwatch');
import { DatabaseInstance } from '@aws-cdk/aws-rds'
import { SubnetType, IVpc, ISecurityGroup } from '@aws-cdk/aws-ec2';

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
   * The master db instance json db instance secret.
   */
  public readonly dbInstanceJsonSecret: secrets.ISecret;

  constructor(scope: cdk.Construct, props: IDbProps) {
    super(scope, 'Db', props);

    // const parameterGroup = rds.ParameterGroup.fromParameterGroupName(this, 'dbParamGroup', 'default.postgres11');
    // const optionGroup = rds.OptionGroup.fromOptionGroupName(this, 'dbOptionGroup', 'default:postgres-11');

    const dbInstNme = this.iname('db');
    const instance = new DatabaseInstance(this, dbInstNme, {
      vpc: props.vpc, 
      instanceIdentifier: dbInstNme, 
      databaseName: 'mcorpus', 
      masterUsername: 'mcadmin', 
      engine: rds.DatabaseInstanceEngine.POSTGRES, 
      instanceClass: ec2.InstanceType.of(
        ec2.InstanceClass.T2,  
        ec2.InstanceSize.SMALL
      ),
      // parameterGroup: parameterGroup, 
      // optionGroup: optionGroup, 
      enablePerformanceInsights: false, 
      multiAz: false, 
      autoMinorVersionUpgrade: true, 
      /*
      cloudwatchLogsExports: [
        'trace',
        'audit',
        'alert',
        'listener'
      ], 
      cloudwatchLogsRetention: logs.RetentionDays.ONE_WEEK, 
      */
      monitoringInterval: cdk.Duration.seconds(60), // default is 1 min
      storageEncrypted: true, 
      backupRetention: cdk.Duration.days(0), // i.e. do not do backups
      vpcPlacement: { subnetType: SubnetType.PRIVATE }, // private
      deletionProtection: false, 
    });

    // allow db bootstrap lambda fn to connect to db
    instance.connections.allowDefaultPortFrom(
      props.dbBootstrapSecGrp, 
      'from db bootstrap', 
    );
    
    // allow ecs container traffic to db
    instance.connections.allowDefaultPortFrom(
      props.ecsSecGrp, 
      'from ecs container', // NOTE: connections construct resolves to a db specific sec grp!
    );

    // allow codebuild traffic to db
    instance.connections.allowDefaultPortFrom(
      props.codebuildSecGrp, 
      'from codebuild', 
    );

    // Rotate the master user password every 30 days
    const rdsSecretRotation = instance.addRotationSingleUser('Rotation');

    // Add alarm for high CPU
    const cloudWatchAlarmInstNme = this.iname('high-cpu');
    const cloudWatchAlarm = new cloudwatch.Alarm(this, cloudWatchAlarmInstNme, {
      metric: instance.metricCPUUtilization(),
      threshold: 90,
      evaluationPeriods: 1
    });

    // Trigger Lambda function on instance availability events
    /*
    const fn = new lambda.Function(this, 'Function', {
      code: lambda.Code.inline('exports.handler = (event) => console.log(event);'),
      handler: 'index.handler',
      runtime: lambda.Runtime.NODEJS_8_10
    });
    const availabilityRule = instance.onEvent('Availability', { target: new targets.LambdaFunction(fn) });
    availabilityRule.addEventPattern({
      detail: {
        EventCategories: [
          'availability'
        ]
      }
    });
    */

    this.dbInstanceJsonSecret = instance.secret!;

    // stack output
    new cdk.CfnOutput(this, 'dbEndpoint', { value: 
      instance.dbInstanceEndpointAddress + ':' + instance.dbInstanceEndpointPort
    });
    new cdk.CfnOutput(this, 'dbInstanceJsonSecretArn', { value: 
      this.dbInstanceJsonSecret!.secretArn
    });
    new cdk.CfnOutput(this, 'dbCloudWatchAlarmArn', { value: 
      cloudWatchAlarm.alarmArn
    });

  }
}
