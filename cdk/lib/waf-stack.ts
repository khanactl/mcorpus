import cdk = require('@aws-cdk/core');
import { IStackProps, BaseStack } from './cdk-native'

/**
 * WAF stack config properties.
 */
export interface IWafProps extends IStackProps {

};

/**
 * WAF (Web App Firewall) stack.
 */
export class WafStack extends BaseStack {

  constructor(scope: cdk.Construct, props: IWafProps) {
    super(scope, 'WAF', props);

    
  }

}
