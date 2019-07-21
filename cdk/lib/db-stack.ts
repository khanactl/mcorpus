import cdk = require('@aws-cdk/core');
import ec2 = require('@aws-cdk/aws-ec2');
import rds = require('@aws-cdk/aws-rds');
import logs = require('@aws-cdk/aws-logs');
import cloudwatch = require('@aws-cdk/aws-cloudwatch');
import { DatabaseInstance } from '@aws-cdk/aws-rds'

/**
 * Db Stack config properties.
 */
export interface IDbProps extends cdk.StackProps {
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;
}

/**
 * Db Stack.
 */
export class DbStack extends cdk.Stack {
  
  constructor(scope: cdk.Construct, id: string, props: IDbProps) {
    super(scope, id, props);

    const parameterGroup = rds.ParameterGroup.fromParameterGroupName(this, 'dbParamGroup', 'default.postgres11');

    const optionGroup = rds.OptionGroup.fromOptionGroupName(this, 'dbOptionGroup', 'default:postgres-11');

    const instance = new DatabaseInstance(this, 'McorpusDbInstance', {
      vpc: props.vpc, 
      engine: rds.DatabaseInstanceEngine.POSTGRES, 
      instanceClass: ec2.InstanceType.of(
        ec2.InstanceClass.T2,  
        ec2.InstanceSize.SMALL
      ),
      parameterGroup: parameterGroup, 
      optionGroup: optionGroup, 
      masterUsername: 'mcadmin', 
      databaseName: 'mcorpus', 
      enablePerformanceInsights: false, 
      multiAz: true, 
      autoMinorVersionUpgrade: true, 
      cloudwatchLogsExports: [
        'trace',
        'audit',
        'alert',
        'listener'
      ], 
      cloudwatchLogsRetention: logs.RetentionDays.ONE_WEEK, 
      monitoringInterval: cdk.Duration.seconds(60), 
      storageEncrypted: true, 
      backupRetention: cdk.Duration.days(0), // i.e. do not do backups
    });

    // Rotate the master user password every 30 days
    instance.addRotationSingleUser('Rotation');

    // Add alarm for high CPU
    new cloudwatch.Alarm(this, 'HighCPU', {
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

  }
}
