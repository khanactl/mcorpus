import { ISecurityGroup, IVpc, Peer, Port } from '@aws-cdk/aws-ec2';
import {
  ApplicationListener,
  ApplicationLoadBalancer,
  ApplicationProtocol,
  SslPolicy,
} from '@aws-cdk/aws-elasticloadbalancingv2';
import { ARecord, HostedZone, RecordTarget } from '@aws-cdk/aws-route53';
import { LoadBalancerTarget } from '@aws-cdk/aws-route53-targets';
import { CfnWebACL, CfnWebACLAssociation } from '@aws-cdk/aws-wafregional';
import { CfnOutput, Construct } from '@aws-cdk/core';
import { BaseStack, iname, inameCml, IStackProps } from './cdk-native';

export interface ILbStackProps extends IStackProps {
  /**
   * The VPC ref
   */
  readonly vpc: IVpc;

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
  public readonly appLoadBalancer: ApplicationLoadBalancer;
  public readonly lbListener: ApplicationListener;

  public readonly webAcl: CfnWebACL;

  constructor(scope: Construct, id: string, props: ILbStackProps) {
    super(scope, id, props);

    // ****************************
    // *** inline load balancer ***
    // ****************************
    // application load balancer
    const albInstNme = iname('app-loadbalancer', props);
    this.appLoadBalancer = new ApplicationLoadBalancer(this, albInstNme, {
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
      const hostedZone = HostedZone.fromHostedZoneAttributes(this, iname('hostedzone', props), {
        hostedZoneId: props.awsHostedZoneId,
        zoneName: props.publicDomainName,
      });
      // NOTE: arecord creation will fail if it already exists
      const arecordInstNme = iname('arecord', props);
      const arecord = new ARecord(this, arecordInstNme, {
        recordName: props.publicDomainName,
        zone: hostedZone,
        target: RecordTarget.fromAlias(new LoadBalancerTarget(this.appLoadBalancer)),
      });
      // dns specific stack output
      new CfnOutput(this, 'HostedZone', {
        value: hostedZone.hostedZoneId,
      });
      new CfnOutput(this, 'ARecord', {
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
    this.webAcl = new CfnWebACL(this, webAclName, {
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
    const wafToAlb = new CfnWebACLAssociation(this, inameCml('Waf2Alb', props), {
      resourceArn: this.appLoadBalancer.loadBalancerArn,
      webAclId: this.webAcl.ref,
    });
    // ***************
    // *** END WAF ***
    // ***************

    // stack output
    new CfnOutput(this, 'AppLoadBalancerName', { value: this.appLoadBalancer.loadBalancerName });
    new CfnOutput(this, 'AppLoadBalancerListenerArn', { value: this.lbListener.listenerArn });
    new CfnOutput(this, 'loadBalancerDnsName', {
      value: this.appLoadBalancer.loadBalancerDnsName,
    });
    new CfnOutput(this, 'WafWebAclName', { value: this.webAcl.name });
  }
}
