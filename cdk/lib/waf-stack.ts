import cdk = require('@aws-cdk/core');
import waf = require("@aws-cdk/aws-wafregional");
import elb = require("@aws-cdk/aws-elasticloadbalancingv2");

import { IStackProps, BaseStack } from './cdk-native'

/**
 * WAF stack config properties.
 */
export interface IWafProps extends IStackProps {
  /**
   * The App Load Balancer reference.
   */
  readonly appLoadBalancerRef: elb.IApplicationLoadBalancer;
};

/**
 * WAF (Web App Firewall) stack.
 */
export class WafStack extends BaseStack {

  public readonly webAcl: waf.CfnWebACL;

  constructor(scope: cdk.Construct, props: IWafProps) {
    super(scope, 'WAF', props);

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
    const webAclName = this.inameCml("webAcl");
    this.webAcl = new waf.CfnWebACL(this, webAclName, {
      name: webAclName,
      metricName: `${webAclName}Metrics`,
      defaultAction: { type: "ALLOW" },
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
    const wafToAlb = new waf.CfnWebACLAssociation(this, this.inameCml("Waf2Alb"), {
      resourceArn: props.appLoadBalancerRef.loadBalancerArn,
      webAclId: this.webAcl.ref,
    });

    // stack output
    new cdk.CfnOutput(this, "WebAclName", { value: this.webAcl.name });

  }
}
