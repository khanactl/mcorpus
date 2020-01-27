#!/usr/bin/env node

import cdk = require('@aws-cdk/core');
import cml = require('camelcase');

/**
 * The supported application environments.
 */
export enum AppEnv {
  /** development (staging / non-production) */
  DEV = 'DEV',
  /** production */
  PRD = 'PRD',
  /** shared (common to all concrete application environments) */
  SHARED = 'SHARED',
}

/**
 * The app build info definition meant to capture
 * needed properties of the underlying app build operation.
 */
export interface IAppBuildInfo {
  /**
   * The resolved app version (usu. gotten from maven build)
   */
  readonly appVersion: string;
  /**
   * The resolved app build timestamp relative to GMT (usu. gotten from maven build)
   *
   * FORMAT: "yyyymmddHHmmss"
   */
  readonly appBuildTimestamp: number;
}

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

  /**
   * Constructor.
   *
   * @param scope
   * @param rootStackName the root name of this stack (e.g.: 'VPC')
   * @param props the stack properties
   */
  constructor(scope: cdk.Construct, rootStackName: string, props: IStackProps) {
    super(scope, `${props.appName}-${rootStackName}-${props.appEnv}`, props);
  }

  /**
   * Generate an 'instance' name given a root name per the held appConfig state.
   *
   * This method delegates to the static method BaseStack.iname.
   *
   * @see BaseStack.iname
   *
   * @param rootName the root name
   * @param props the stack properties object
   */
  public iname(rootName: string, props: IStackProps): string {
    return BaseStack.iname(props.appName, rootName, props.appEnv);
  }

  /**
   * Generate an 'instance' name given a root name per the held appConfig state
   * in **camelCaseForm**.
   *
   * @param rootName the root name
   */
  public inameCml(rootName: string, props: IStackProps) {
    return cml(this.iname(rootName, props));
  }

} // BaseStack class

