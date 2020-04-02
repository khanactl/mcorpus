import { Alarm, AlarmWidget, ComparisonOperator, Dashboard, GraphWidget, Metric } from '@aws-cdk/aws-cloudwatch';
import { SnsAction } from '@aws-cdk/aws-cloudwatch-actions';
import { Cluster, FargateService } from '@aws-cdk/aws-ecs';
import { ApplicationLoadBalancer } from '@aws-cdk/aws-elasticloadbalancingv2';
import { IDatabaseInstance } from '@aws-cdk/aws-rds';
import { Topic } from '@aws-cdk/aws-sns';
import { EmailSubscription } from '@aws-cdk/aws-sns-subscriptions';
import { CfnOutput, Construct } from '@aws-cdk/core';
import { BaseStack, iname, IStackProps } from './cdk-native';

export interface IMetricsStackProps extends IStackProps {
  readonly dbInstanceRef: IDatabaseInstance;

  readonly ecsClusterRef: Cluster;

  readonly fargateSvcRef: FargateService;

  readonly appLoadBalancerRef: ApplicationLoadBalancer;

  readonly onMetricAlarmEmails?: string[];
}

export class MetricsStack extends BaseStack {
  public readonly dbCpuAlarm: Alarm;
  public readonly dbCpuAlarmTopic: Topic;

  public readonly dbFreeStorageSpaceAlarm: Alarm;
  public readonly dbFresStorageSpaceAlarmTopic: Topic;

  public readonly ecsCpuAlarm: Alarm;
  public readonly ecsCpuAlarmTopic: Topic;

  public readonly ecsMemoryAlarm: Alarm;
  public readonly ecsMemoryAlarmTopic: Topic;

  public readonly dashboard: Dashboard;

  constructor(scope: Construct, id: string, props: IMetricsStackProps) {
    super(scope, id, props);

    // *** metrics ***
    // db
    const metricDbCpuUtilization = props.dbInstanceRef.metricCPUUtilization();
    const metricDbNumConnections = props.dbInstanceRef.metricDatabaseConnections();
    const metricDbFreeStorageSpace = props.dbInstanceRef.metricFreeStorageSpace();
    // ecs
    const metricEcsCpu = props.ecsClusterRef.metric('CPUUtilization', {
      dimensions: {
        ClusterName: props.ecsClusterRef.clusterName,
        ServiceName: props.fargateSvcRef.serviceName,
      },
    });
    const metricEcsMemory = props.ecsClusterRef.metric('MemoryUtilization', {
      dimensions: {
        ClusterName: props.ecsClusterRef.clusterName,
        ServiceName: props.fargateSvcRef.serviceName,
      },
    });
    // load balancer
    const metricsLbRequestCount = props.appLoadBalancerRef.metricRequestCount({});
    // *** END metrics ***

    // *** alarms ***
    // db high cpu
    const dbCpuAlarmTopicName = iname('db-high-cpu-alarm', props);
    this.dbCpuAlarmTopic = new Topic(this, dbCpuAlarmTopicName, {
      topicName: dbCpuAlarmTopicName,
    });
    const alarmDbHighCpuName = iname('db-high-cpu', props, false);
    this.dbCpuAlarm = new Alarm(this, alarmDbHighCpuName, {
      metric: metricDbCpuUtilization,
      threshold: 90,
      evaluationPeriods: 1,
      alarmName: alarmDbHighCpuName,
    });
    this.dbCpuAlarm.addAlarmAction(new SnsAction(this.dbCpuAlarmTopic));

    // db free storage space
    const dbFreeStoreageSpaceAlarmTopicName = iname('db-low-disk-space-alarm', props);
    this.dbFresStorageSpaceAlarmTopic = new Topic(this, dbFreeStoreageSpaceAlarmTopicName, {
      topicName: dbFreeStoreageSpaceAlarmTopicName,
    });
    const alarmDbFreeStorageSpaceName = iname('db-low-disk-space', props, false);
    this.dbFreeStorageSpaceAlarm = new Alarm(this, alarmDbFreeStorageSpaceName, {
      metric: metricDbFreeStorageSpace,
      comparisonOperator: ComparisonOperator.LESS_THAN_OR_EQUAL_TO_THRESHOLD,
      threshold: 500 * 1000 * 1000, // 500MB
      evaluationPeriods: 1,
      alarmName: alarmDbFreeStorageSpaceName,
    });
    this.dbFreeStorageSpaceAlarm.addAlarmAction(new SnsAction(this.dbFresStorageSpaceAlarmTopic));

    // ecs high cpu
    const ecsCpuAlarmTopicName = iname('ecs-high-cpu-alarm', props);
    this.ecsCpuAlarmTopic = new Topic(this, ecsCpuAlarmTopicName, {
      topicName: ecsCpuAlarmTopicName,
    });
    const alarmEcsCpuName = iname('ecs-high-cpu', props, false);
    this.ecsCpuAlarm = new Alarm(this, alarmEcsCpuName, {
      metric: metricEcsCpu,
      threshold: 90,
      evaluationPeriods: 1,
      alarmName: alarmEcsCpuName,
    });
    this.ecsCpuAlarm.addAlarmAction(new SnsAction(this.ecsCpuAlarmTopic));

    // ecs high memory
    const ecsMemoryAlarmTopicName = iname('ecs-high-memory-alarm', props);
    this.ecsMemoryAlarmTopic = new Topic(this, ecsMemoryAlarmTopicName, {
      topicName: ecsMemoryAlarmTopicName,
    });
    const alarmEcsMemoryName = iname('ecs-high-memory', props, false);
    this.ecsMemoryAlarm = new Alarm(this, alarmEcsMemoryName, {
      metric: metricEcsMemory,
      threshold: 90,
      evaluationPeriods: 1,
      alarmName: alarmEcsMemoryName,
    });
    this.ecsMemoryAlarm.addAlarmAction(new SnsAction(this.ecsMemoryAlarmTopic));

    // lb (NONE at present)
    // *** END alarms ***

    // alarm action emails
    if (props.onMetricAlarmEmails && props.onMetricAlarmEmails.length > 0) {
      props.onMetricAlarmEmails
        .map(email => new EmailSubscription(email))
        .forEach(sub => {
          this.dbCpuAlarmTopic.addSubscription(sub);
          this.dbFresStorageSpaceAlarmTopic.addSubscription(sub);
          this.ecsCpuAlarmTopic.addSubscription(sub);
          this.ecsMemoryAlarmTopic.addSubscription(sub);
        });
    }

    // *** cloudwatch dashboard ***
    const dashboardName = iname('metrics', props);
    this.dashboard = new Dashboard(this, 'metrics', {
      dashboardName: dashboardName,
    });
    this.dashboard.addWidgets(
      // db
      this.buildAlarmWidget('db-cpu', this.dbCpuAlarm),
      this.buildAlarmWidget('db-disk-space', this.dbFreeStorageSpaceAlarm),
      this.buildGraphWidget('db-num-connections', metricDbNumConnections),
      // ecs
      this.buildAlarmWidget('ecs-cpu', this.ecsCpuAlarm),
      this.buildAlarmWidget('ecs-memory', this.ecsMemoryAlarm),
      // lb
      this.buildGraphWidget('lb-request-count', metricsLbRequestCount)
    );
    // *** END cloudwatch dashboard ***

    // stack output
    new CfnOutput(this, 'dbHighCpuAlarmName', {
      value: this.dbCpuAlarm.alarmName,
    });
    new CfnOutput(this, 'dbFreeStorageSpaceAlarmName', {
      value: this.dbFreeStorageSpaceAlarm.alarmName,
    });
    new CfnOutput(this, 'ecsHighCpuAlarmName', {
      value: this.ecsCpuAlarm.alarmName,
    });
    new CfnOutput(this, 'ecsHighMemoryAlarmName', {
      value: this.ecsMemoryAlarm.alarmName,
    });
    new CfnOutput(this, 'dashboard', {
      value: this.dashboard.toString(),
    });
  }

  private buildGraphWidget(title: string, metric: Metric): GraphWidget {
    return new GraphWidget({
      title: title,
      left: [metric],
    });
  }

  private buildAlarmWidget(title: string, alarm: Alarm): AlarmWidget {
    return new AlarmWidget({
      title: title,
      alarm: alarm,
    });
  }
}
