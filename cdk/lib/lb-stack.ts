import cdk = require('@aws-cdk/core');
import { ISecurityGroup, Peer, Port, SubnetType } from '@aws-cdk/aws-ec2';
import { FargatePlatformVersion, FargateService, LogDrivers } from '@aws-cdk/aws-ecs';
import { ApplicationProtocol, SslPolicy } from '@aws-cdk/aws-elasticloadbalancingv2';
import { IStringParameter } from '@aws-cdk/aws-ssm';
import { Duration } from '@aws-cdk/core';
import { randomBytes } from 'crypto';
import { BaseStack, IStackProps, iname, inameCml } from './cdk-native';
import iam = require('@aws-cdk/aws-iam');
import ec2 = require('@aws-cdk/aws-ec2');
import ecs = require('@aws-cdk/aws-ecs');
import elb = require('@aws-cdk/aws-elasticloadbalancingv2');
import ssm = require('@aws-cdk/aws-ssm');
import r53 = require('@aws-cdk/aws-route53');
import path = require('path');
import alias = require('@aws-cdk/aws-route53-targets');
import logs = require('@aws-cdk/aws-logs');
import waf = require('@aws-cdk/aws-wafregional');

export interface ILbStackProps extends IStackProps {
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;

  // readonly fargateSvc: FargateService;

  /**
   * The inner or traffic port used to send traffic
   * from the load balancer to the ecs/fargate service.
   */
  readonly lbToEcsPort: number;

  /**
   * The load balancer security group ref.
   */
  readonly lbSecGrp: ISecurityGroup;
  /**
   * The domain name registered in AWS Route53 and the one used for this web app.
   *
   * This will connect the public to this app!
   */
  readonly publicDomainName?: string;
  /**
   * The AWS Route53 Hosted Zone Id.
   */
  readonly awsHostedZoneId?: string;
  /**
   * SSL Certificate Arn
   */
  readonly sslCertArn: string;
}

export class LbStack extends BaseStack {
  public readonly appLoadBalancer: elb.ApplicationLoadBalancer;
  public readonly lbListener: elb.ApplicationListener;

  public readonly webAcl: waf.CfnWebACL;

  constructor(scope: cdk.Construct, id: string, props: ILbStackProps) {
    super(scope, id, props);

    // ****************************
    // *** inline load balancer ***
    // ****************************
    // application load balancer
    const albInstNme = iname('app-loadbalancer', props);
    this.appLoadBalancer = new elb.ApplicationLoadBalancer(this, albInstNme, {
      vpc: props.vpc,
      internetFacing: true,
      securityGroup: props.lbSecGrp,
      loadBalancerName: albInstNme,
    });

    // sec grp rule: outside internet access only by TLS on 443
    props.lbSecGrp.addIngressRule(Peer.anyIpv4(), Port.tcp(443), 'TLS/443 access from internet');

    const listenerInstNme = iname('alb-tls-listener', props);
    this.lbListener = this.appLoadBalancer.addListener(listenerInstNme, {
      protocol: ApplicationProtocol.HTTPS,
      port: 443,
      certificateArns: [props.sslCertArn],
      sslPolicy: SslPolicy.RECOMMENDED,
      // defaultTargetGroups: []
    });

    // ****************************
    // *** END inline load balancer ***
    // ****************************

    // DNS bind load balancer to domain name record
    if (props.awsHostedZoneId && props.publicDomainName) {
      // console.log('Load balancer DNS will be bound in Route53.');
      const hostedZone = r53.HostedZone.fromHostedZoneAttributes(this, iname('hostedzone', props), {
        hostedZoneId: props.awsHostedZoneId,
        zoneName: props.publicDomainName,
      });
      // NOTE: arecord creation will fail if it already exists
      const arecordInstNme = iname('arecord', props);
      const arecord = new r53.ARecord(this, arecordInstNme, {
        recordName: props.publicDomainName,
        zone: hostedZone,
        target: r53.RecordTarget.fromAlias(new alias.LoadBalancerTarget(this.appLoadBalancer)),
      });
      // dns specific stack output
      new cdk.CfnOutput(this, 'HostedZone', {
        value: hostedZone.hostedZoneId,
      });
      new cdk.CfnOutput(this, 'ARecord', {
        value: arecord.domainName,
      });
    }

    // ***************
    // ***** WAF *****
    // ***************
    // *** firewall rules ***

    // rule: rate limiter
    /*
    const ruleRateLimit = new waf.CfnRateBasedRule(this, "rateLimitByIP", {
      name: "rateLimitByIP",
      metricName: "myRateLimitByIP",
      rateKey: "IP", // i.e. rate limiting based on a sourcing IP address (only available option currently per CloudFormation docs)
      rateLimit: 2000, // the minimum allowed
      matchPredicates: [
        {
          dataId: 'IPSetId',
          negated: false,
          type: 'IPMatch',
        },
      ],
    });
    */

    // *** END firewall rules ***

    // acl (groups rules)
    const webAclName = inameCml('webAcl', props);
    this.webAcl = new waf.CfnWebACL(this, webAclName, {
      name: webAclName,
      metricName: `${webAclName}Metrics`,
      defaultAction: { type: 'ALLOW' },
      /*
      rules: [
        {
          action: { type: "BLOCK" },
          creationStack: [],
          priority: 1,
          ruleId: ruleRateLimit.ref,
        },
      ],
      */
    });

    // bind waf to alb
    const wafToAlb = new waf.CfnWebACLAssociation(this, inameCml('Waf2Alb', props), {
      resourceArn: this.appLoadBalancer.loadBalancerArn,
      webAclId: this.webAcl.ref,
    });
    // ***************
    // *** END WAF ***
    // ***************

    // stack output
    new cdk.CfnOutput(this, 'loadBalancerDnsName', {
      value: this.appLoadBalancer.loadBalancerDnsName,
    });
    new cdk.CfnOutput(this, 'WebAclName', { value: this.webAcl.name });
  }
}
