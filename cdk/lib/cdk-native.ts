#!/usr/bin/env node
import os = require('os');
import fs = require('fs');
import cml = require('camelcase');
import aws = require('aws-sdk');
import cdk = require('@aws-cdk/core');

/**
 * The supported application environments.
 */
export enum AppEnv {
  /** development (staging / non-production) */
  DEV = 'DEV',
  /** production */
  PRD = 'PRD',
  /** common (resource(s) shared among all concrete application environments) */
  COMMON = 'COMMON',
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
   * Constructor.
   *
   * @param scope
   * @param id the name of this stack
   * @param props the stack properties
   */
  constructor(scope: cdk.Construct, id: string, props: IStackProps) {
    super(scope, id, props);
  }
} // BaseStack class

export function resolveCurrentGitBranch(): string {
  // are we in codebuild env (there is no .git dir there)?
  let branch = process.env.GIT_BRANCH_NAME; // rely on custom buildspec env var
  if (!branch) {
    // assume a .git project dir present and use a 3rd party dep to resolve current git branch
    branch = require('git-branch');
  }
  if (!branch) throw Error('Fatal: Unable to determine the current Git branch.');
  return branch;
}

export function resolveAppEnv(gitBranchName: string): AppEnv {
  switch (gitBranchName) {
    case 'master': // the master branch is always production
      return AppEnv.PRD;
    default:
      // always default to DEV
      return AppEnv.DEV;
  }
}

/**
 * Load the CDK app config JSON file either from local user dir
 * or, when no local one exists, from a known S3 bucket.
 *
 * @param appConfigFilename the CDK app config JSON file name
 * @param s3ConfigCacheBucketName Optional name of an S3 bucket containing the cached CDK app config JSON file
 * @returns the resolved CDK app config JSON **object**
 */
export async function loadConfig(appConfigFilename: string, s3ConfigCacheBucketName?: string): Promise<any> {
  // first try local home dir
  try {
    const config = fs.readFileSync(`${os.homedir()}/${appConfigFilename}`, 'utf-8');
    return Promise.resolve(JSON.parse(config));
  } catch (e) {
    if (s3ConfigCacheBucketName && s3ConfigCacheBucketName.length > 0) {
      // try to fetch from known s3
      try {
        const s3 = new aws.S3();
        const configObj = await s3
          .getObject({
            Bucket: s3ConfigCacheBucketName,
            Key: appConfigFilename,
          })
          .promise();
        // s3 case
        const configStr = configObj.Body!.toString();
        const config = JSON.parse(configStr);
        if (config) {
          // cache in user home dir only if one not already present
          if (!fs.existsSync(`${os.homedir()}/${appConfigFilename}`)) {
            // cache config file locally
            fs.writeFileSync(`${os.homedir()}/${appConfigFilename}`, configStr, {
              encoding: 'utf-8',
            });
            // console.log("local config file created");
          }
        }
        return Promise.resolve(config);
      } catch (err) {
        throw new Error('Unable to get cdk app config from s3: ' + err);
      }
    } else {
      throw new Error('No local config file found and no cache config s3 bucket name provided.');
    }
  }
}

/**
 * Generate an "instance" name given a root name.
 *
 * FORMAT
 * ```
 * "{lower(appName)}-{rootName}-{lower(appEnv)}"
 * ```
 *
 * @param rootName the root name
 * @param props the stack properties object
 * @returns the generated iname
 */
export function iname(rootName: string, props: IStackProps): string {
  return `${props.appName.toLowerCase()}-${rootName}-${props.appEnv.toLowerCase()}`;
}

/**
 * Generate an *instance name* given a root name in **camelCaseForm**.
 *
 * @param rootName the root name
 * @param props the stack properties object
 * @returns the generated iname in camelCase
 */
export function inameCml(rootName: string, props: IStackProps) {
  return cml(iname(rootName, props));
}

/**
 * Generate an *instance path* given a name.
 *
 * FORMAT
 * ```
 * "{lower(appName)}/{lower(appEnv)}/{name}"
 * ```
 *
 * @param name the name
 * @param props the stack properties object
 * @returns the generated ipath
 */
export function ipath(rootName: string, props: IStackProps) {
  return `${props.appName.toLowerCase()}/${rootName.toLowerCase()}/${props.appEnv.toLowerCase()}`;
}
