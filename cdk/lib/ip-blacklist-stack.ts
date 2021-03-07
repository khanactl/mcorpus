import { CfnIPSet } from '@aws-cdk/aws-wafv2';
import { CfnOutput, Construct } from '@aws-cdk/core';
import { BaseStack, iname, IStackProps } from './cdk-native';

export const IPBlacklistStackRootProps = {
  rootStackName: 'ipblacklist',
  description: 'IP Blacklist stack used by WAF stack.',
};

export interface IIPBlacklistProps extends IStackProps {
  readonly blacklistedIps: string[];
}

export class IPBlacklistStack extends BaseStack {
  public readonly ipBlacklistSet: CfnIPSet;

  constructor(scope: Construct, props: IIPBlacklistProps) {
    super(scope, IPBlacklistStackRootProps.rootStackName, {
      ...props,  ...{ description: IPBlacklistStackRootProps.description }
    });

    // ip blacklist
    this.ipBlacklistSet = new CfnIPSet(this, 'IPBlacklist', {
      scope: 'REGIONAL',
      ipAddressVersion: 'IPV4',
      addresses: props.blacklistedIps,
      name: iname('ipblacklist', props),
    });

    // output
    new CfnOutput(this, 'IpSetBlacklistSetArn', { value: this.ipBlacklistSet.attrArn });
  }
}