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

export function resolveCurrentGitBranch(): string {
  // are we in codebuild env (there is no .git dir there)?
  let branch = process.env.GIT_BRANCH_NAME;  // rely on custom buildspec env var
  if (!branch) {
    // assume a .git project dir present and use a 3rd party dep to resolve current git branch
    branch = require('git-branch');
  }
  if (!branch) throw Error("Fatal: Unable to determine the current Git branch.");
  return branch;
}

export function resolveAppEnv(gitBranchName: string): AppEnv {
  switch (gitBranchName) {
    case 'master': // the master branch is always production
      return AppEnv.PRD;
    default:  // always default to DEV
      return AppEnv.DEV;
  }
}

export function resolveAppBuild(appPropsFilePath: string): IAppBuildInfo {
  // load app props file from local build output
  try {
    const arrAppProps =
      fs.readFileSync(
        // path.join(__dirname, "../mcorpus-gql/target/classes/app.properties"),
        appPropsFilePath,
        "utf-8"
      ).split("\n");
    let appVersion = "";
    let buildTimestamp = 0;
    for (var i = 0; i < arrAppProps.length; i++) {
      const cpl = arrAppProps[i] ? arrAppProps[i].toString().replace(/\s/g, "") : "";
      if (cpl && cpl.length > 3 && cpl.indexOf("=") > 0) {
        if (cpl.startsWith("app.version")) {
          appVersion = cpl.split("=")[1];
        } else if (cpl.startsWith("build-timestamp")) {
          buildTimestamp = parseInt(cpl.split("=")[1].replace(/[^0-9]+/g, ""));
          // console.log(`buildTimestamp: ${buildTimestamp}`);
        }
        if (buildTimestamp > 0 && appVersion.length > 0)
          return {
            appVersion: appVersion,
            appBuildTimestamp: buildTimestamp,
          };
      }
    };
    throw new Error("No app.version and/or build-timestamp properties found in app.properties.");
  } catch (err) {
    throw new Error(`Unable to resolve the app build info: ${err}`);
  }
}

/**
 * Resolve the docker web app image tag.
 *
 * @param appConfig
 * @param appBuild
 */
export function resolveAppImageTag(appConfig: any, appBuild: IAppBuildInfo): string {
  const envImgTag = process.env.CDK_ECS_DOCKER_IMAGE_TAG;
  if (envImgTag && envImgTag.length > 0) {
    // env var override case
    return envImgTag;
  } else {
    // fallback on local build info
    // FORMAT: "{appVersion}.{buildTimestamp}"
    return `${appBuild.appVersion}.${appBuild.appBuildTimestamp.toString()}`;
  }
}

/**
 * Load the CDK app config JSON file either from local user dir
 * or, when no local one exists, from a known S3 bucket.
 *
 * @param appConfigFilename the CDK app config JSON file name
 * @param s3ConfigCacheBucketName the S3 bucket name containing the cached CDK app config JSON file
 * @returns the resolved CDK app config JSON **object**
 */
export async function loadConfig(appConfigFilename: string, s3ConfigCacheBucketName: string): Promise<any> {
  // first try local home dir
  try {
    const config = fs.readFileSync(`${os.homedir()}/${appConfigFilename}`, 'utf-8');
    return Promise.resolve(JSON.parse(config));
  } catch (e) {
    if (s3ConfigCacheBucketName && s3ConfigCacheBucketName.length > 0) {
      // try to fetch from known s3
      try {
        const s3 = new aws.S3();
        const configObj = await s3.getObject({
          Bucket: s3ConfigCacheBucketName,
          Key: appConfigFilename,
        }).promise();
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
        throw new Error("Unable to get cdk app config from s3: " + err);
      }
    } else {
      throw new Error("No local config file found and no cache config s3 bucket name provided.");
    }
  }
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

