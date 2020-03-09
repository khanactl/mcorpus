import cdk = require('@aws-cdk/core');
import { Alarm, AlarmWidget, Dashboard, GraphWidget, Metric } from '@aws-cdk/aws-cloudwatch';
import { Cluster, FargateService } from '@aws-cdk/aws-ecs';
import { ApplicationLoadBalancer } from '@aws-cdk/aws-elasticloadbalancingv2';
import { IDatabaseInstance } from '@aws-cdk/aws-rds';
import { BaseStack, iname, IStackProps } from './cdk-native';
import cwa = require('@aws-cdk/aws-cloudwatch-actions');
import sns = require('@aws-cdk/aws-sns');
import sns_sub = require('@aws-cdk/aws-sns-subscriptions');

export interface IMetricsStackProps extends IStackProps {
  // readonly vpc: ec2.IVpc;

  readonly dbInstanceRef: IDatabaseInstance;

  readonly ecsClusterRef: Cluster;

  readonly fargateSvcRef: FargateService;

  readonly appLoadBalancerRef: ApplicationLoadBalancer;

  readonly onMetricAlarmEmails?: string[];
}

export class MetricsStack extends BaseStack {
  public readonly dbCpuAlarm: Alarm;
  public readonly dbCpuAlarmTopic: sns.Topic;

  public readonly ecsCpuAlarm: Alarm;
  public readonly ecsCpuAlarmTopic: sns.Topic;

  public readonly ecsMemoryAlarm: Alarm;
  public readonly ecsMemoryAlarmTopic: sns.Topic;

  public readonly dashboard: Dashboard;

  constructor(scope: cdk.Construct, id: string, props: IMetricsStackProps) {
    super(scope, id, props);

    // *** metrics ***
    // db
    const metricDbCpuUtilization = props.dbInstanceRef.metricCPUUtilization();
    const metricDbFreeableMemory = props.dbInstanceRef.metricFreeableMemory();
    const metricDbNumConnections = props.dbInstanceRef.metricDatabaseConnections();
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
    this.dbCpuAlarmTopic = new sns.Topic(this, dbCpuAlarmTopicName, {
      topicName: dbCpuAlarmTopicName,
    });
    const alarmDbHighCpuName = iname(`db-high-cpu`, props, false);
    this.dbCpuAlarm = new Alarm(this, alarmDbHighCpuName, {
      metric: metricDbCpuUtilization,
      threshold: 90,
      evaluationPeriods: 1,
      alarmName: alarmDbHighCpuName,
    });
    this.dbCpuAlarm.addAlarmAction(new cwa.SnsAction(this.dbCpuAlarmTopic));

    // ecs high cpu
    const ecsCpuAlarmTopicName = iname('ecs-high-cpu-alarm', props);
    this.ecsCpuAlarmTopic = new sns.Topic(this, ecsCpuAlarmTopicName, {
      topicName: ecsCpuAlarmTopicName,
    });
    const alarmEcsCpuName = iname('ecs-high-cpu', props, false);
    this.ecsCpuAlarm = new Alarm(this, alarmEcsCpuName, {
      metric: metricEcsCpu,
      threshold: 90,
      evaluationPeriods: 1,
      alarmName: alarmEcsCpuName,
    });
    this.ecsCpuAlarm.addAlarmAction(new cwa.SnsAction(this.ecsCpuAlarmTopic));

    // ecs high memory
    const ecsMemoryAlarmTopicName = iname('ecs-high-memory-alarm', props);
    this.ecsMemoryAlarmTopic = new sns.Topic(this, ecsMemoryAlarmTopicName, {
      topicName: ecsMemoryAlarmTopicName,
    });
    const alarmEcsMemoryName = iname('ecs-high-memory', props, false);
    this.ecsMemoryAlarm = new Alarm(this, alarmEcsMemoryName, {
      metric: metricEcsMemory,
      threshold: 90,
      evaluationPeriods: 1,
      alarmName: alarmEcsMemoryName,
    });
    this.ecsMemoryAlarm.addAlarmAction(new cwa.SnsAction(this.ecsMemoryAlarmTopic));

    // lb (NONE at present)
    // *** END alarms ***

    // alarm action emails
    if (props.onMetricAlarmEmails && props.onMetricAlarmEmails.length > 0) {
      props.onMetricAlarmEmails
        .map(email => new sns_sub.EmailSubscription(email))
        .forEach(sub => {
          this.dbCpuAlarmTopic.addSubscription(sub);
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
      this.buildGraphWidget('db-freeable-memory', metricDbFreeableMemory),
      this.buildGraphWidget('db-num-connections', metricDbNumConnections),
      // ecs
      this.buildAlarmWidget('ecs-cpu', this.ecsCpuAlarm),
      this.buildAlarmWidget('ecs-memory', this.ecsMemoryAlarm),
      // lb
      this.buildGraphWidget('lb-request-count', metricsLbRequestCount)
    );
    // *** END cloudwatch dashboard ***

    // stack output
    new cdk.CfnOutput(this, 'dbHighCpuAlarmName', {
      value: this.dbCpuAlarm.alarmName,
    });
    new cdk.CfnOutput(this, 'ecsHighCpuAlarmName', {
      value: this.ecsCpuAlarm.alarmName,
    });
    new cdk.CfnOutput(this, 'ecsHighMemoryAlarmName', {
      value: this.ecsMemoryAlarm.alarmName,
    });
    new cdk.CfnOutput(this, 'dashboard', {
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
