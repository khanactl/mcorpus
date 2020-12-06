import { CfnWebACL } from '@aws-cdk/aws-wafv2';
import { Construct } from '@aws-cdk/core';
import { Reference } from "@aws-cdk/core/lib/reference";
import { BaseStack, iname, inameCml, IStackProps } from './cdk-native';

export const WafStackRootProps = {
  rootStackName: 'waf',
  description: 'Web Application Firewall stack based on cfn wafv2.',
};

/**
 * WAF stack config properties.
 */
export interface IWafProps extends IStackProps {
}

/**
 * WAF stack.
 */
export class WafStack extends BaseStack {
  public readonly webAclArn: string;

  constructor(scope: Construct, props: IWafProps) {
    super(scope, WafStackRootProps.rootStackName, {
      ...props,  ...{ description: WafStackRootProps.description }
    });

    const webAclName = inameCml('WebAcl', props);
    const webAcl = new CfnWebACL(this, webAclName, {
      defaultAction: { allow: {} },
      rules: [
        {
          priority: 1,
          overrideAction: { none: {} },
          visibilityConfig: {
            sampledRequestsEnabled: true,
            cloudWatchMetricsEnabled: true,
            metricName: "AWS-AWSManagedRulesAmazonIpReputationList",
          },
          name: "AWS-AWSManagedRulesAmazonIpReputationList",
          statement: {
            managedRuleGroupStatement: {
              vendorName: "AWS",
              name: "AWSManagedRulesAmazonIpReputationList",
            },
          },
        },
        {
          priority: 2,
          overrideAction: { none: {} },
          visibilityConfig: {
            sampledRequestsEnabled: true,
            cloudWatchMetricsEnabled: true,
            metricName: "AWS-AWSManagedRulesCommonRuleSet",
          },
          name: "AWS-AWSManagedRulesCommonRuleSet",
          statement: {
            managedRuleGroupStatement: {
              vendorName: "AWS",
              name: "AWSManagedRulesCommonRuleSet",
            },
          },
        },
        {
          priority: 3,
          overrideAction: { none: {} },
          visibilityConfig: {
            sampledRequestsEnabled: true,
            cloudWatchMetricsEnabled: true,
            metricName: "AWS-AWSManagedRulesKnownBadInputsRuleSet",
          },
          name: "AWS-AWSManagedRulesKnownBadInputsRuleSet",
          statement: {
            managedRuleGroupStatement: {
              vendorName: "AWS",
              name: "AWSManagedRulesKnownBadInputsRuleSet",
            },
          },
        },
      ],
      scope: "REGIONAL",
      visibilityConfig: {
        sampledRequestsEnabled: true,
        cloudWatchMetricsEnabled: true,
        metricName: iname("web-acl", props),
      },
    });

    this.webAclArn = webAcl.attrArn;
  }
}