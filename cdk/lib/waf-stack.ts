import { CfnIPSet, CfnWebACL } from '@aws-cdk/aws-wafv2';
import { Construct } from '@aws-cdk/core';
import { BaseStack, iname, inameCml, IStackProps } from './cdk-native';

export const WafStackRootProps = {
  rootStackName: 'waf',
  description: 'Web Application Firewall stack based on cfn wafv2.',
};

/**
 * WAF stack config properties.
 */
export interface IWafProps extends IStackProps {
  readonly blacklistedIps: string[];
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

    // ip blacklist
    const ipBlacklistName = inameCml('ipBlacklist', props);
    const ipBlacklistSet = new CfnIPSet(this, ipBlacklistName, {
      scope: 'REGIONAL',
      ipAddressVersion: 'IPV4',
      addresses: props.blacklistedIps,
      name: ipBlacklistName,
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
              arn: ipBlacklistSet.attrArn,
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
  }
}