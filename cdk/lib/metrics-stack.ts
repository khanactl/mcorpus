import cdk = require('@aws-cdk/core');
import { Dashboard, GraphWidget, Metric } from '@aws-cdk/aws-cloudwatch';
import { Cluster, FargateService } from '@aws-cdk/aws-ecs';
import { ApplicationLoadBalancer } from '@aws-cdk/aws-elasticloadbalancingv2';
import { IDatabaseInstance } from '@aws-cdk/aws-rds';
import { BaseStack, iname, IStackProps } from './cdk-native';
import ec2 = require('@aws-cdk/aws-ec2');
import cloudwatch = require('@aws-cdk/aws-cloudwatch');
import lambda = require('@aws-cdk/aws-lambda');

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
    const alarmDbHighCpu = new cloudwatch.Alarm(this, 'db-high-cpu', {
      metric: metricDbCpuUtilization,
      threshold: 90,
      evaluationPeriods: 1,
      alarmName: 'db-high-cpu',
    });
    // ecs
    const alarmEcsCpu = new cloudwatch.Alarm(this, 'ecs-high-cpu', {
      metric: metricEcsCpu,
      threshold: 90,
      evaluationPeriods: 1,
      alarmName: 'ecs-high-cpu',
    });
    const alarmEcsMemory = new cloudwatch.Alarm(this, 'ecs-high-memory', {
      metric: metricEcsMemory,
      threshold: 90,
      evaluationPeriods: 1,
      alarmName: 'ecs-high-memory',
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
      this.buildGraphWidget('db-cpu', metricDbCpuUtilization),
      this.buildGraphWidget('db-freeable-memory', metricDbFreeableMemory),
      this.buildGraphWidget('db-num-connections', metricDbNumConnections),
      // ecs
      this.buildGraphWidget('ecs-cpu', metricEcsCpu),
      this.buildGraphWidget('ecs-memory', metricEcsMemory),
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
}
