import cdk = require('@aws-cdk/core');
import { Dashboard, GraphWidget, Metric } from '@aws-cdk/aws-cloudwatch';
import { Cluster } from '@aws-cdk/aws-ecs';
import { ApplicationLoadBalancer } from '@aws-cdk/aws-elasticloadbalancingv2';
import { IDatabaseInstance } from '@aws-cdk/aws-rds';
import { BaseStack, iname, IStackProps } from './cdk-native';
import ec2 = require('@aws-cdk/aws-ec2');
import cloudwatch = require('@aws-cdk/aws-cloudwatch');
import lambda = require('@aws-cdk/aws-lambda');

export interface IMetricsStackProps extends IStackProps {
  // readonly vpc: ec2.IVpc;

  readonly dbInstance: IDatabaseInstance;

  readonly ecsCluster: Cluster;

  readonly alb: ApplicationLoadBalancer;
}

export class MetricsStack extends BaseStack {
  public readonly dashboard: Dashboard;

  constructor(scope: cdk.Construct, id: string, props: IMetricsStackProps) {
    super(scope, id, props);

    // *** RDS metrics ***
    // Add alarm for high db CPU
    const metricDbCpuUtilization = props.dbInstance.metricCPUUtilization();
    const metricDbFreeableMemory = props.dbInstance.metricFreeableMemory();
    const metricDbNumConnections = props.dbInstance.metricDatabaseConnections();
    const alarmDbHighCpu = new cloudwatch.Alarm(this, 'db-high-cpu', {
      metric: metricDbCpuUtilization,
      threshold: 90,
      evaluationPeriods: 1,
      alarmName: 'db-high-cpu',
    });
    // *** END RDS metrics ***

    // *** ECS metrics ***
    const metricEcsCpu = props.ecsCluster.metric('CPUUtilization', {});
    const metricEcsMemory = props.ecsCluster.metric('MemoryUtilization', {});
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
    // *** END ECS metrics ***

    // *** load balancer metrics ***
    const metricsLbRequestCount = props.alb.metricRequestCount({});
    const metricsLbAuthSuccess = props.alb.metricElbAuthSuccess({});
    // END load balancer metrics ***

    // *** cloudwatch dashboard ***
    const dashboardName = iname('metrics', props);
    this.dashboard = new Dashboard(this, 'metrics', {
      dashboardName: dashboardName,
    });
    this.dashboard.addWidgets(
      // db
      this.buildGraphWidget(metricDbCpuUtilization),
      this.buildGraphWidget(metricDbFreeableMemory),
      this.buildGraphWidget(metricDbNumConnections),
      // ecs
      this.buildGraphWidget(metricEcsCpu),
      this.buildGraphWidget(metricEcsMemory),
      // lb
      this.buildGraphWidget(metricsLbRequestCount),
      this.buildGraphWidget(metricsLbAuthSuccess)
    );
    // *** END cloudwatch dashboard ***

    // stack output
    new cdk.CfnOutput(this, 'dashboard', {
      value: this.dashboard.toString(),
    });
  }

  private buildGraphWidget(metric: Metric): GraphWidget {
    return new GraphWidget({
      title: metric.metricName,
      left: [metric],
    });
  }
}
