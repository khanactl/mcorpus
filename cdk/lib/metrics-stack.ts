import cdk = require('@aws-cdk/core');
import { Alarm, AlarmWidget, Dashboard, GraphWidget, Metric } from '@aws-cdk/aws-cloudwatch';
import { Cluster, FargateService } from '@aws-cdk/aws-ecs';
import { ApplicationLoadBalancer } from '@aws-cdk/aws-elasticloadbalancingv2';
import { IDatabaseInstance } from '@aws-cdk/aws-rds';
import { BaseStack, iname, IStackProps } from './cdk-native';

export interface IMetricsStackProps extends IStackProps {
  // readonly vpc: ec2.IVpc;

  readonly dbInstanceRef: IDatabaseInstance;

  readonly ecsClusterRef: Cluster;

  readonly fargateSvcRef: FargateService;

  readonly appLoadBalancerRef: ApplicationLoadBalancer;
}

export class MetricsStack extends BaseStack {
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
    // db
    const alarmDbHighCpuName = iname(`db-high-cpu`, props, false);
    const alarmDbHighCpu = new Alarm(this, alarmDbHighCpuName, {
      metric: metricDbCpuUtilization,
      threshold: 90,
      evaluationPeriods: 1,
      alarmName: alarmDbHighCpuName,
    });
    // ecs
    const alarmEcsCpuName = iname('ecs-high-cpu', props, false);
    const alarmEcsCpu = new Alarm(this, alarmEcsCpuName, {
      metric: metricEcsCpu,
      threshold: 90,
      evaluationPeriods: 1,
      alarmName: alarmEcsCpuName,
    });
    const alarmEcsMemoryName = iname('ecs-high-memory', props, false);
    const alarmEcsMemory = new Alarm(this, alarmEcsMemoryName, {
      metric: metricEcsMemory,
      threshold: 90,
      evaluationPeriods: 1,
      alarmName: alarmEcsMemoryName,
    });
    // lb
    // NONE at present
    // *** END alarms ***

    // *** cloudwatch dashboard ***
    const dashboardName = iname('metrics', props);
    this.dashboard = new Dashboard(this, 'metrics', {
      dashboardName: dashboardName,
    });
    this.dashboard.addWidgets(
      // db
      this.buildAlarmWidget('db-cpu', alarmDbHighCpu),
      this.buildGraphWidget('db-freeable-memory', metricDbFreeableMemory),
      this.buildGraphWidget('db-num-connections', metricDbNumConnections),
      // ecs
      this.buildAlarmWidget('ecs-cpu', alarmEcsCpu),
      this.buildAlarmWidget('ecs-memory', alarmEcsMemory),
      // lb
      this.buildGraphWidget('lb-request-count', metricsLbRequestCount)
    );
    // *** END cloudwatch dashboard ***

    // stack output
    new cdk.CfnOutput(this, 'dbHighCpuAlarmName', {
      value: alarmDbHighCpu.alarmName,
    });
    new cdk.CfnOutput(this, 'ecsHighCpuAlarmName', {
      value: alarmEcsCpu.alarmName,
    });
    new cdk.CfnOutput(this, 'ecsHighMemoryAlarmName', {
      value: alarmEcsMemory.alarmName,
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
