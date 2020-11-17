#!/usr/bin/env node
import { Construct, Environment, Stack, StackProps } from "@aws-cdk/core";
import { S3 } from "aws-sdk";
import { existsSync, readFileSync, writeFileSync } from "fs";
import { homedir } from "os";
import cml = require("camelcase");

/**
 * The supported application environments.
 */
export enum AppEnv {
  /** local development environment */
  LOCAL = "LOCAL",
  /** development environment */
  DEV = "DEV",
  /** staging (QA) environment */
  STAGING = "STAGING",
  /** production environment */
  PROD = "PROD",
  /** common (Resource(s) shared among one or more 'concrete' application environments.) */
  COMMON = "COMMON",
}

/**
 * Native stack properties definition.
 */
export interface IAppNameAndEnv {
  /**
   * The application name.
   */
  readonly appName: string;
  /**
   * The ascribed application environment.
   */
  readonly appEnv: AppEnv;
}

export interface IStackProps extends StackProps, IAppNameAndEnv {}

export interface IGitHubRepoRef {
  readonly owner: string;
  readonly repo: string;
  readonly oauthTokenSecretArn: string;
  readonly oauthTokenSecretJsonFieldName: string;
}

export interface INetworkConfig {
  readonly maxAzs: number;
  readonly cidr: string;
}

export interface IDbConfig {
  readonly dbName: string;
  readonly dbMasterUsername: string;
}

export interface IEcrConfig extends Environment {
  readonly name: string;
}

export interface IDnsConfig {
  readonly awsHostedZoneId: string;
  readonly publicDomainName: string;
}
export interface IAppConfig {
  readonly javaOpts: string;
  readonly devFlag: boolean;
  readonly publicAddress: string;
  readonly dbDataSourceClassName: string;
  readonly rstTtlInMinutes: number;
  readonly jwtTtlInMinutes: number;
  readonly jwtRefreshTokenTtlInMinutes: number;
  readonly jwtStatusTimeoutInMinutes: number;
  readonly jwtStatusCacheMaxSize: number;
  readonly metricsOn: boolean;
  readonly graphiql: boolean;
  readonly cookieSecure: boolean;
  readonly httpClientOrigin: string;
}

export interface IWebAppConfig {}
export interface IWebAppContainerConfig {
  readonly lbToAppPort: number;
  readonly taskdefCpu: number;
  readonly taskdefMemLimitInMb: number;
  readonly containerDefMemoryLimitInMb: number;
  readonly containerDefMemoryReservationInMb: number;
  readonly webappUrl: string;
  readonly tlsCertArn: string;
  readonly dnsConfig: IDnsConfig;
}

export interface ICicdConfig {
  readonly gitBranchName: string;
  readonly triggerOnCommit: boolean;
  readonly ssmImageTagParamName: string;
  readonly appDeployApprovalEmails: [string];
  readonly onBuildFailureEmails: [string];
}

export interface IMetricsConfig {
  readonly onAlarmEmailList: [string];
}

/**
 * The cdk app config definition loaded from the cdk app config json file.
 */
export interface ICdkAppConfig extends IAppNameAndEnv {
  readonly cdkAppConfigFilename: string;
  readonly cdkAppConfigCacheS3BucketName: string;
  readonly awsEnv: Environment;
  readonly appEnvStackTags: { [key: string]: string };
  readonly commonEnvStackTags: { [key: string]: string };
  readonly gitHubRepoRef: IGitHubRepoRef;
  readonly ecrConfig: IEcrConfig;
  readonly networkConfig: INetworkConfig;
  readonly dbConfig: IDbConfig;
  readonly appConfig: IAppConfig;
  readonly webAppContainerConfig: IWebAppContainerConfig;
  readonly metricsConfig: IMetricsConfig;
  readonly cicdConfig: ICicdConfig;
}

/**
 * Convert a cdk json app config object to an app env specific ICdkAppConfig instance.
 *
 * @param cdkAppConfigFilename
 * @param cdkAppConfigCacheS3BucketName
 * @param appEnv
 * @param jsonAppConfig
 */
export function cdkAppConfig(
  cdkAppConfigFilename: string,
  cdkAppConfigCacheS3BucketName: string,
  appEnv: AppEnv,
  jsonAppConfig: any
): ICdkAppConfig {
  return {
    cdkAppConfigFilename: cdkAppConfigFilename,
    cdkAppConfigCacheS3BucketName: cdkAppConfigCacheS3BucketName,
    appEnv: appEnv,
    appName: jsonAppConfig.appName,
    awsEnv: env(appEnv, jsonAppConfig),
    appEnvStackTags: tags(appEnv, jsonAppConfig),
    commonEnvStackTags: tags(AppEnv.COMMON, jsonAppConfig),
    gitHubRepoRef: gitHubRepoRef(AppEnv.COMMON, jsonAppConfig),
    ecrConfig: ecrConfig(AppEnv.COMMON, jsonAppConfig),
    networkConfig: networkConfig(AppEnv.DEV, jsonAppConfig),
    dbConfig: dbConfig(AppEnv.DEV, jsonAppConfig),
    appConfig: appConfig(AppEnv.DEV, jsonAppConfig),
    webAppContainerConfig: webappContainerConfig(appEnv, jsonAppConfig),
    metricsConfig: metricsConfig(appEnv, jsonAppConfig),
    cicdConfig: cicdConfig(appEnv, jsonAppConfig)
  }
}

export function gitHubRepoRef(appEnv: AppEnv, jsonAppConfig: any): IGitHubRepoRef {
  const key = `${appEnv.toLowerCase()}Config`;
  const subobj = jsonAppConfig[key].gitRepoRef;
  return {
    repo: subobj.githubRepo,
    owner: subobj.githubOwner,
    oauthTokenSecretArn: subobj.githubOauthTokenSecretArn,
    oauthTokenSecretJsonFieldName: subobj.githubOauthTokenSecretJsonFieldName,
  };
}

export function ecrConfig(appEnv: AppEnv, jsonAppConfig: any): IEcrConfig {
  const key = `${appEnv.toLowerCase()}Config`;
  const subobj = jsonAppConfig[key].ecrConfig;
  return {
    name: subobj.name,
    account: subobj.awsAccountId,
    region: subobj.awsRegion,
  };
}

export function networkConfig(appEnv: AppEnv, jsonAppConfig: any): INetworkConfig {
  const key = `${appEnv.toLowerCase()}Config`;
  const subobj = jsonAppConfig[key].networkConfig;
  return {
    maxAzs: subobj.maxAzs,
    cidr: subobj.cidr,
  };
}

export function dbConfig(appEnv: AppEnv, jsonAppConfig: any): IDbConfig {
  const key = `${appEnv.toLowerCase()}Config`;
  const subobj = jsonAppConfig[key].dbConfig;
  return {
    dbName: subobj.dbName,
    dbMasterUsername: subobj.dbMasterUsername,
  };
}

export function appConfig(appEnv: AppEnv, jsonAppConfig: any): IAppConfig {
  const key = `${appEnv.toLowerCase()}Config`;
  const subobj = jsonAppConfig[key].appConfig;
  return {
    javaOpts: subobj.javaOpts,
    devFlag: subobj.devFlag,
    publicAddress: subobj.publicAddress,
    dbDataSourceClassName: subobj.dbDataSourceClassName,
    rstTtlInMinutes: subobj.rstTtlInMinutes,
    jwtTtlInMinutes: subobj.jwtTtlInMinutes,
    jwtRefreshTokenTtlInMinutes: subobj.jwtRefreshTokenTtlInMinutes,
    jwtStatusTimeoutInMinutes: subobj.jwtStatusTimeoutInMinutes,
    jwtStatusCacheMaxSize: subobj.jwtStatusCacheMaxSize,
    metricsOn: subobj.metricsOn,
    graphiql: subobj.graphiql,
    cookieSecure: subobj.cookieSecure,
    httpClientOrigin: subobj.httpClientOrigin,
  };
}

export function dnsConfig(appEnv: AppEnv, jsonAppConfig: any): IDnsConfig {
  const key = `${appEnv.toLowerCase()}Config`;
  const subobj = jsonAppConfig[key].webAppContainerConfig.dnsConfig;
  return {
    awsHostedZoneId: subobj.awsHostedZoneId,
    publicDomainName: subobj.publicDomainName,
  };
}

export function webappContainerConfig(appEnv: AppEnv, jsonAppConfig: any): IWebAppContainerConfig {
  const key = `${appEnv.toLowerCase()}Config`;
  const subobj = jsonAppConfig[key].webAppContainerConfig;
  return {
    lbToAppPort: subobj.lbToAppPort,
    taskdefCpu: subobj.taskdefCpu,
    taskdefMemLimitInMb: subobj.taskdefMemoryLimitInMb,
    containerDefMemoryLimitInMb: subobj.containerDefMemoryLimitInMb,
    containerDefMemoryReservationInMb: subobj.containerDefMemoryReservationInMb,
    webappUrl: subobj.webAppUrl,
    tlsCertArn: subobj.tlsCertArn,
    dnsConfig: dnsConfig(appEnv, jsonAppConfig),
  };
}

export function cicdConfig(appEnv: AppEnv, jsonAppConfig: any): ICicdConfig {
  const key = `${appEnv.toLowerCase()}Config`;
  const subobj = jsonAppConfig[key].cicdConfig;
  return {
    gitBranchName: subobj.gitBranchName,
    triggerOnCommit: subobj.triggerOnCommit,
    ssmImageTagParamName: subobj.ssmImageTagParamName,
    appDeployApprovalEmails: subobj.appDeployApprovalEmails,
    onBuildFailureEmails: subobj.onBuildFailureEmails,
  };
}

export function metricsConfig(appEnv: AppEnv, jsonAppConfig: any): IMetricsConfig {
  const key = `${appEnv.toLowerCase()}Config`;
  return {
    onAlarmEmailList: jsonAppConfig[key].onMetricAlarmEmails,
  };
}

/**
 * Extract the aws environment from the json config given the app env.
 *
 * @param appEnv the desired app env
 * @param jsonAppConfig the json app config object
 * @returns new Environment instance
 */
export function env(appEnv: AppEnv, jsonAppConfig: any): Environment {
  const key = `${appEnv.toLowerCase()}Config`;
  return {
    account: jsonAppConfig[key].awsAccountId,
    region: jsonAppConfig[key].awsRegion,
  };
}

/**
 * Generate tags for use in cdk stacks or stages.
 *
 * @param appEnv the ascribed app env
 * @param jsonAppConfig the json app config object
 * @returns map of string types keys to string typed values
 */
export function tags(appEnv: AppEnv, jsonAppConfig: any): { [key: string]: string } {
  return {
    AppName: jsonAppConfig.appName,
    AppEnv: appEnv,
  };
}

/**
 * Native base CDK stack class.
 */
export abstract class BaseStack extends Stack {
  /**
   * Constructor.
   *
   * @param scope
   * @param rootStackName the root name of the stack
   * @param props the stack properties
   */
  constructor(scope: Construct, rootStackName: string, props: IStackProps) {
    super(scope, iname(rootStackName, props), props);
  }
} // BaseStack class

/**
 * Load the CDK app config JSON file either from local user dir
 * or, when no local one exists, from a known S3 bucket.
 *
 * @param appConfigFilename the CDK app config JSON file name
 *
 * The expected name of the required cdk app config json file.
 *
 * This JSON object (assumed to adhere to an expected structure)
 * provides the needed input to all instantiated cdk stacks herein.
 *
 * @param s3ConfigCacheBucketName Optional name of an S3 bucket containing the cached CDK app config JSON file
 *
 * The S3 bucket name in the default AWS account holding a cached copy of
 * the app config file.
 *
 * This is used when no local app config file is found.
 *
 * This bucket's life-cycle is **not managed** by these CDK stacks.
 * That is, it is assumed to *pre-exist*.
 *
 * @returns the resolved CDK app config JSON **object**
 */
export async function loadConfig(appConfigFilename: string, s3ConfigCacheBucketName?: string): Promise<any> {
  // first try local home dir
  if (existsSync(`${homedir()}/${appConfigFilename}`)) {
    try {
      const config = readFileSync(`${homedir()}/${appConfigFilename}`, "utf-8");
      return Promise.resolve(JSON.parse(config));
    } catch (e) {
      throw new Error("Unable to get cdk app config from local user dir: " + e);
    }
  } else if (s3ConfigCacheBucketName && s3ConfigCacheBucketName.length > 0) {
    // try to fetch from known s3
    try {
      const s3 = new S3();
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
        // cache config file locally
        writeFileSync(`${homedir()}/${appConfigFilename}`, configStr, {
          encoding: "utf-8",
        });
      }
      return Promise.resolve(config);
    } catch (e) {
      throw new Error("Unable to get cdk app config from s3: " + e);
    }
  }
  throw new Error(
    "Unable to get cdk ap config: No local config file found and no cache config s3 bucket name provided."
  );
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
 * @param appNameAndEnv the app name and app env
 * @returns the generated iname
 */
export function iname(rootName: string, appNameAndEnv: IAppNameAndEnv): string {
  return `${appNameAndEnv.appName.toLowerCase()}-${rootName}-${appNameAndEnv.appEnv.toLowerCase()}`;
}

/**
 * Generate an "env" name given a root name.
 *
 * FORMAT
 * ```
 * "{rootName}-{lower(appEnv)}"
 * ```
 * @param rootName the root name
 * @param appEnv the app env
 */
export function ename(rootName: string, appEnv: AppEnv): string {
  return `${rootName}-${appEnv.toLowerCase()}`;
}

/*
export function inamec(rootName: string, appEnv: AppEnv, appConfig: any): string {
  return `${appConfig.appName.toLowerCase()}-${rootName}-${appEnv.toLowerCase()}`
}
*/

/**
 * Generate an *instance name* given a root name in **camelCaseForm**.
 *
 * @param rootName the root name
 * @param props the app name and env object
 * @returns the generated iname in camelCase
 */
export function inameCml(rootName: string, props: IAppNameAndEnv) {
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
 * @param props the app name and env object
 * @returns the generated ipath
 */
export function ipath(rootName: string, props: IAppNameAndEnv): string {
  return `${props.appName.toLowerCase()}/${rootName.toLowerCase()}/${props.appEnv.toLowerCase()}`;
}
