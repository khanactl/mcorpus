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
  readonly blacklistedIps: string[];
}

export interface IIpBlacklistProps extends NestedStackProps {
  readonly blacklistedIps: string[];
}

export class IpBlacklistStack extends NestedStack {
  public readonly ipBlacklistSet: CfnIPSet;

  constructor(scope: Construct, blacklistedIps: string[]) {
    super(scope, 'IPBlacklistSet', {});

    // ip blacklist
    this.ipBlacklistSet = new CfnIPSet(this, 'IpBlacklist', {
      scope: 'REGIONAL',
      ipAddressVersion: 'IPV4',
      addresses: blacklistedIps,
      // name: ipBlacklistName,
    });

    // output
    new CfnOutput(this, 'IpSetBlacklistSetArn', { value: this.ipBlacklistSet.attrArn });
  }
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

    const ipBlacklistStack = new IpBlacklistStack(this, props.blacklistedIps);

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
              arn: ipBlacklistStack.ipBlacklistSet.attrArn,
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