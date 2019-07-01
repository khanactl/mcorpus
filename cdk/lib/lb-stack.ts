import cdk = require('@aws-cdk/core');
import ec2 = require('@aws-cdk/aws-ec2');
import elb = require('@aws-cdk/aws-elasticloadbalancingv2');
import { Duration } from '@aws-cdk/core';
import { SslPolicy } from '@aws-cdk/aws-elasticloadbalancingv2';
import { ApplicationProtocol, TargetType } from '@aws-cdk/aws-elasticloadbalancingv2';

/**
 * Load Balancer Stack config properties.
 */
export interface ILbProps extends cdk.StackProps {
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;
  /**
   * The client-side port for which to listen for *inbound* traffic.
   * 
   * @default 443
   */
  readonly listenerPort: number;
  /**
   * The inner (traffic) port used to send traffic to the application web server.
   * 
   * @default 5150
   */
  readonly innerPort: number;
  /**
   * SSL Certificate Id (Arn? verify)
   */
  readonly sslCertId: string;
}

/**
 * Application Load Balancer Stack.
 */
export class LbStack extends cdk.Stack {
  
  // private readonly vpc: ec2.IVpc;
  
  constructor(scope: cdk.Construct, id: string, props: ILbProps) {
    super(scope, id, props);

    // this.vpc = props.vpc;

    // load balancer security group
    const sgAppLoadBalancer = new ec2.SecurityGroup(this, 'sg-alb', {
      vpc: props.vpc,
      description: 'Application Load balancer security group.',
      allowAllOutbound: true   // Can be set to false
    });
    sgAppLoadBalancer.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(443), 'TLS/443 access from internet');
    
    // application load balancer
    const alb = new elb.ApplicationLoadBalancer(this, 'app-load-balancer', {
      vpc: props.vpc,
      internetFacing: true,
      securityGroup: sgAppLoadBalancer,
    });

    const listener = alb.addListener('alb-tls-listener', {
      port: props.listenerPort,
      certificateArns: [ props.sslCertId ],
      sslPolicy: SslPolicy.RECOMMENDED
    });

    const albTargetGroup = new elb.ApplicationTargetGroup(this, 'alb-target-group', {
      vpc: props.vpc,
      healthCheck: {
        // port: String(props.innerPort),
        protocol: elb.Protocol.HTTP,
        path: '/health',
        port: 'traffic-port',
        healthyThresholdCount: 5,
        unhealthyThresholdCount: 2,
        timeout: Duration.seconds(20),
        interval: Duration.seconds(120),
      },
      port: props.innerPort,
      protocol: ApplicationProtocol.HTTP,
      targetType: TargetType.IP
    });

    listener.addTargetGroups('app-lb-tgtrp', {
      targetGroups: [ albTargetGroup ]
    });

  }
}
