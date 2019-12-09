#!/usr/bin/env node

import cdk = require('@aws-cdk/core');
import { ICdkAppConfig } from './cdk-config-def';

/**
 * Native stack properties definition.
 */
export interface IStackProps {
  /**
   * The native CDK application configuration reference.
   */
  readonly appConfig: ICdkAppConfig;
}

/**
 * Native base CDK stack class.
 */
export abstract class BaseStack extends cdk.Stack {

  /**
   * Generate an 'instance' name given a 'root' name.
   * 
   * FORMAT: "{lower(appName)}-{rootName}-{lower(appEnv)}".
   * 
   * @param appConfig
   * @param rootName 
   */
  public static iname(appConfig: ICdkAppConfig, rootName: string): string {
    return `${appConfig.appName.toLowerCase()}-${rootName}-${appConfig.appEnv.toLowerCase()}`;
  }

  protected readonly appConfig: ICdkAppConfig;

  protected readonly stackInstanceName: string;

  /**
   * Constructor.
   * 
   * @param scope 
   * @param rootStackName the root name of this stack (e.g.: 'VPC')
   * @param props the stack properties
   */
  constructor(scope: cdk.Construct, rootStackName: string, props: IStackProps) {
    super(scope, `${props.appConfig.appName}-${rootStackName}-${props.appConfig.appEnv}`, {
      // NOTE: stackName is not necessary since we specify the id in super.constructor
      // TODO verify this
      // stackName: BaseStack.stackName(props.appConfig, rootStackName), 
      env: {
        account: props.appConfig.awsAccountId, 
        region: props.appConfig.awsRegion, 
      }, 
      tags: {
        'AppName': props.appConfig.appName, 
        'AppEnv': props.appConfig.appEnv, 
      }, 
    });
    this.appConfig = props.appConfig;
    this.stackInstanceName = BaseStack.iname(props.appConfig, rootStackName);
    // console.log(`stackInstanceName: ${this.stackInstanceName}, stackId: ${this.stackId}`);
  }

  /**
   * Generate an 'instance' name given a root name per the held appConfig state.
   * 
   * This method delegates to the static method BaseStack.iname.
   * 
   * @see BaseStack.iname
   * @param rootName the root name
   */
  public iname(rootName: string): string {
    return BaseStack.iname(this.appConfig, rootName);
  }

} // BaseStack class

