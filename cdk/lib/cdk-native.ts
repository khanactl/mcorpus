#!/usr/bin/env node

import cdk = require('@aws-cdk/core');
import { AppEnv } from './app-env';
import cml = require('camelcase');

/**
 * Native stack properties definition.
 */
export interface IStackProps extends cdk.StackProps {
  /**
   * The application name.
   */
  readonly appName: string;
  /**
   * The ascribed application environment.
   */
  readonly appEnv: AppEnv;
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
   * @param appName
   * @param rootName
   * @param AppEnv
   */
  public static iname(appName: string, rootName: string, appEnv: AppEnv): string {
    return `${appName.toLowerCase()}-${rootName}-${appEnv.toLowerCase()}`;
  }

  protected readonly appEnv: AppEnv;

  protected readonly appName: string;

  /**
   * Constructor.
   *
   * @param scope
   * @param rootStackName the root name of this stack (e.g.: 'VPC')
   * @param props the stack properties
   */
  constructor(scope: cdk.Construct, rootStackName: string, props: IStackProps) {
    super(scope, `${props.appName}-${rootStackName}-${props.appEnv}`, props);
    this.appEnv = props.appEnv;
    this.appName = props.appName;
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
    return BaseStack.iname(this.appName, rootName, this.appEnv);
  }

  /**
   * Generate an 'instance' name given a root name per the held appConfig state
   * in **camelCaseForm**.
   *
   * @param rootName the root name
   */
  public inameCml(rootName: string) {
    return cml(rootName);
  }

} // BaseStack class

