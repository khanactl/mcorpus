import { CfnIPSet, CfnWebACL } from '@aws-cdk/aws-wafv2';
import { CfnOutput, Construct, Fn, Lazy, NestedStack, NestedStackProps } from '@aws-cdk/core';
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
          visibilityConfig: {
            sampledRequestsEnabled: true,
            cloudWatchMetricsEnabled: true,
            metricName: "IPBlacklist",
          },
          overrideAction: { count: {} },
          name: "IPBlacklist",
          statement: {
            ipSetReferenceStatement: {
              arn: "arn:aws:wafv2:us-west-2:524006177124:regional/ipset/mcorpus-ipblacklist-dev/cb1ff704-8f8f-42ea-af50-8e3e06ca74e7",
              ipSetForwardedIpConfig: {
                headerName: 'X-Forwarded-For',
                fallbackBehavior: 'NO_MATCH',
                position: 'ANY',
              }
            },
          }
        },
        {
          priority: 2,
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
          priority: 3,
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
          priority: 4,
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

    // stack output
    new CfnOutput(this, 'WafAclArn', { value: webAcl.attrArn });
  }
}