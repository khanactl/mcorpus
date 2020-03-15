#!/usr/bin/env node
import { Construct, Stack, StackProps } from '@aws-cdk/core';
import { S3 } from 'aws-sdk';
import { existsSync, readFileSync, writeFileSync } from 'fs';
import { homedir } from 'os';
import cml = require('camelcase');

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
 * Native stack properties definition.
 */
export interface IStackProps extends StackProps {
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
export abstract class BaseStack extends Stack {
  /**
   * Constructor.
   *
   * @param scope
   * @param id the name of this stack
   * @param props the stack properties
   */
  constructor(scope: Construct, id: string, props: IStackProps) {
    super(scope, id, props);
  }
} // BaseStack class

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
    const config = readFileSync(`${homedir()}/${appConfigFilename}`, 'utf-8');
    return Promise.resolve(JSON.parse(config));
  } catch (e) {
    if (s3ConfigCacheBucketName && s3ConfigCacheBucketName.length > 0) {
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
          // cache in user home dir only if one not already present
          if (!existsSync(`${homedir()}/${appConfigFilename}`)) {
            // cache config file locally
            writeFileSync(`${homedir()}/${appConfigFilename}`, configStr, {
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
 * @param useAppName Use the app name in the generated iname?  Defaults to true.
 * @returns the generated iname
 */
export function iname(rootName: string, props: IStackProps, useAppName: boolean = true): string {
  return useAppName
    ? `${props.appName.toLowerCase()}-${rootName}-${props.appEnv.toLowerCase()}`
    : `${rootName}-${props.appEnv.toLowerCase()}`;
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
