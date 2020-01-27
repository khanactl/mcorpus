#!/usr/bin/env node
import 'source-map-support/register';

import fs = require('fs');
import os = require('os');

import { AppEnv, IAppBuildInfo } from '../lib/cdk-native';

import aws = require('aws-sdk');

import cdk = require('@aws-cdk/core');

import { InfraPipelineStack } from '../lib/infra-pipeline-stack';
import { VpcStack } from '../lib/vpc-stack';
import { SecGrpStack } from '../lib/secgrp-stack';
import { DbStack } from '../lib/db-stack';
import { DbBootstrapStack } from '../lib/db-bootstrap-stack';
import { DbDataStack } from '../lib/db-data-stack';
import { ECRStack } from '../lib/ecr-stack';
import { ECSStack } from '../lib/ecs-stack';
import { WafStack } from '../lib/waf-stack';
import { CICDStack } from '../lib/cicd-stack';

const app = new cdk.App();

const currentGitBranch = resolveCurrentGitBranch();
const currentAppEnv = resolveAppEnv(currentGitBranch);
// console.log(`gitBranch: ${currentGitBranch}, currentAppEnv: ${currentAppEnv}`);
const appBuild = resolveAppBuild();

/**
 * The expected name of the required cdk app config json file.
 *
 * This JSON object (assumed to adhere to an expected structure)
 * provides the needed input to all instantiated cdk stacks herein.
 */
const appConfigFilename = "mcorpus-cdk-app-config.json";

function resolveCurrentGitBranch(): string {
  // are we in codebuild env (there is no .git dir there)?
  let branch = process.env.GIT_BRANCH_NAME;  // rely on custom buildspec env var
  if(!branch) {
    // assume a .git project dir present and use a 3rd party dep to resolve current git branch
    branch = require('git-branch');
  }
  if(!branch) throw Error("Fatal: Unable to determine the current Git branch.");
  return branch;
}

function resolveAppEnv(gitBranchName: string): AppEnv {
  switch(gitBranchName) {
    case 'master': // the master branch is always production
      return AppEnv.PRD;
    default:  // always default to DEV
      return AppEnv.DEV;
  }
}

function resolveAppBuild(): IAppBuildInfo {
    // load app props file from local build output
    try {
      const arrAppProps =
        fs.readFileSync(
          "../mcorpus-gql/target/classes/app.properties", "utf-8"
        ).split("\n");
      let appVersion = "";
      let buildTimestamp = 0;
      for(var i = 0; i < arrAppProps.length; i++) {
        const cpl = arrAppProps[i] ? arrAppProps[i].toString().replace("/\s/g", "") : "";
        if(cpl && cpl.length > 3 && cpl.indexOf("=") > 0) {
          if(cpl.startsWith("app.version")) {
            appVersion = cpl.split("=")[1];
          } else if(cpl.startsWith("build-timestamp")) {
            buildTimestamp = parseInt(cpl.split("=")[1].replace(/[^0-9]+/g, ""));
            // console.log(`buildTimestamp: ${buildTimestamp}`);
          }
          if(buildTimestamp > 0 && appVersion.length > 0)
            return {
              appVersion: appVersion,
              appBuildTimestamp: buildTimestamp,
            };
        }
      };
      throw new Error("No app.version and/or build-timestamp properties found in app.properties.");
    } catch(err) {
      throw new Error(`Unable to resolve the app build info: ${err}`);
    }
}

function resolveEcsDockerImageTag(appConfig: any): string {
  const envImgTag = process.env.CDK_ECS_DOCKER_IMAGE_TAG;
  if(envImgTag && envImgTag.length > 0) {
    // env var override case
    return envImgTag;
  } else {
    // fallback on local build info
    // FORMAT: "{appVersion}.{buildTimestamp}"
    return `${appBuild.appVersion}.${appBuild.appBuildTimestamp.toString()}`;
  }
}

/**
 * Generate the CDK stacks.
 *
 * @param appConfig json obj of config to use
 */
function createStacks(appConfig: any) {

  const gitRepoRef = appConfig.sharedConfig.gitRepoRef;

  // single aws account houses all app instances
  const awsEnv: cdk.Environment = {
    account: appConfig.sharedConfig.awsAccountId,
    region: appConfig.sharedConfig.awsRegion,
  };

  // common aws stack tags
  const awsStackTags_Shared = {
    "AppName": appConfig.appName,
    "AppEnv": AppEnv.SHARED,
  };

  // app env specific stack tags
  const awsStackTags_appInstance = {
    "AppName": appConfig.appName,
    "AppEnv": currentAppEnv,
  }

  // isolate app env dependent config
  let webAppContainerConfig: any;
  let cicdConfig: any;
  switch(currentAppEnv) {
    case AppEnv.PRD: {
      webAppContainerConfig = appConfig.prdConfig.webAppContainerConfig;
      cicdConfig = appConfig.prdConfig.cicdConfig;
      break;
    }
    case AppEnv.DEV: {
      webAppContainerConfig = appConfig.devConfig.webAppContainerConfig;
      cicdConfig = appConfig.devConfig.cicdConfig;
      break;
    }
    default:
      throw new Error(`Invalid target app env: ${currentAppEnv}`);
  }

  // get the ECR ref
  const ecrArn = `arn:aws:ecr:${appConfig.sharedConfig.ecrConfig.awsRegion}:${appConfig.sharedConfig.ecrConfig.awsAccountId}:repository/${appConfig.sharedConfig.ecrConfig.name}`;

  // resolve the web app docker image tag
  const ecsDkrImgTag = resolveEcsDockerImageTag(appConfig);
  // console.log(`ecsDkrImgTag: ${ecsDkrImgTag}`);

  // app env dependent infra-bootstrap pipeline (the app env genesis stack)
  const infraPipelineStack = new InfraPipelineStack(app, {
    appName: appConfig.appName,
    appEnv: currentAppEnv,
    env: awsEnv,
    tags: awsStackTags_appInstance,
    githubOwner: gitRepoRef.githubOwner,
    githubRepo: gitRepoRef.githubRepo,
    githubOauthTokenSecretArn: gitRepoRef.githubOauthTokenSecretArn,
    githubOauthTokenSecretJsonFieldName: gitRepoRef.githubOauthTokenSecretJsonFieldName,
    gitBranchName: cicdConfig.gitBranchName,
  });

  // common VPC
  const vpcStack = new VpcStack(app, {
    appName: appConfig.appName,
    appEnv: AppEnv.SHARED,
    env: awsEnv,
    tags: awsStackTags_Shared,
  });
  const secGrpStack = new SecGrpStack(app, {
    appName: appConfig.appName,
    appEnv: AppEnv.SHARED,
    env: awsEnv,
    tags: awsStackTags_Shared,
    vpc: vpcStack.vpc,
  });

  // common RDS instance
  const dbStack = new DbStack(app, {
    appName: appConfig.appName,
    appEnv: AppEnv.SHARED,
    env: awsEnv,
    tags: awsStackTags_Shared,
    vpc: vpcStack.vpc,
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp,
    ecsSecGrp: secGrpStack.ecsSecGrp,
    codebuildSecGrp: secGrpStack.codebuildSecGrp,
    dbName: appConfig.sharedConfig.dbConfig.dbName,
    dbMasterUsername: appConfig.sharedConfig.dbConfig.dbMasterUsername,
  });
  const dbBootstrapStack = new DbBootstrapStack(app, {
    appName: appConfig.appName,
    appEnv: AppEnv.SHARED,
    env: awsEnv,
    tags: awsStackTags_Shared,
    vpc: vpcStack.vpc,
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp,
    dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn,
    targetRegion: appConfig.sharedConfig.awsRegion,
  });
  const dbDataStack = new DbDataStack(app, {
    appName: appConfig.appName,
    appEnv: AppEnv.SHARED,
    env: awsEnv,
    tags: awsStackTags_Shared,
    vpc: vpcStack.vpc,
    dbDataSecGrp: secGrpStack.dbBootstrapSecGrp,
    dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn,
    // s3KmsEncKeyArn: appConfig.ssmKmsArn
  });

  const ecsStack = new ECSStack(app, {
    appName: appConfig.appName,
    appEnv: currentAppEnv,
    env: awsEnv,
    tags: awsStackTags_appInstance,
    vpc: vpcStack.vpc,
    taskdefCpu: webAppContainerConfig.taskdefCpu,
    taskdefMemoryLimitMiB: webAppContainerConfig.taskdefMemoryLimitMiB,
    containerDefMemoryLimitMiB: webAppContainerConfig.containerDefMemoryLimitMiB,
    containerDefMemoryReservationMiB: webAppContainerConfig.containerDefMemoryReservationMiB,
    ecrArn: ecrArn,
    ecrRepoTargetTag: ecsDkrImgTag,
    lbToEcsPort: webAppContainerConfig.lbToAppPort,
    sslCertArn: webAppContainerConfig.tlsCertArn,
    // ssmKmsArn: appappConfig.ssmKmsArn,
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl,
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl,
    ecsSecGrp: secGrpStack.ecsSecGrp,
    lbSecGrp: secGrpStack.lbSecGrp,
    webAppUrl: webAppContainerConfig.webAppUrl,
    javaOpts: webAppContainerConfig.javaOpts,
    publicDomainName: webAppContainerConfig.dnsConfig.publicDomainName,
    awsHostedZoneId: webAppContainerConfig.dnsConfig.awsHostedZoneId,
  });

  const wafStack = new WafStack(app, {
    appName: appConfig.appName,
    appEnv: currentAppEnv,
    env: awsEnv,
    tags: awsStackTags_appInstance,
    appLoadBalancerRef: ecsStack.appLoadBalancer,
  });
  const cicdStack = new CICDStack(app, {
    appName: appConfig.appName,
    appEnv: currentAppEnv,
    env: awsEnv,
    tags: awsStackTags_appInstance,
    githubOwner: gitRepoRef.githubOwner,
    githubRepo: gitRepoRef.githubRepo,
    githubOauthTokenSecretArn: gitRepoRef.githubOauthTokenSecretArn,
    githubOauthTokenSecretJsonFieldName: gitRepoRef.githubOauthTokenSecretJsonFieldName,
    gitBranchName: cicdConfig.gitBranchName,
    vpc: vpcStack.vpc,
    codebuildSecGrp: secGrpStack.codebuildSecGrp,
    ecsTaskDefContainerName: ecsStack.containerName,
    lbToEcsPort: webAppContainerConfig.lbToAppPort,
    ecrArn: ecrArn,
    fargateSvc: ecsStack.fargateSvc,
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl,
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl,
    cicdDeployApprovalEmails: cicdConfig.appDeployApprovalEmails,
  });
  cicdStack.addDependency(wafStack, "CICD pipeline depends on ecs and waf.");
}

async function loadConfig(): Promise<any> {
  let config: any;
  // first try local home dir
  try {
    config = fs.readFileSync(`${os.homedir()}/${appConfigFilename}`, 'utf-8');
    return Promise.resolve(config);
  } catch(e) {
    // try to fetch from known s3
    try {
      const s3 = new aws.S3();
      config = await s3.getObject({
        Bucket: "mcorpus-db-data-bucket-shared",
        Key: appConfigFilename,
      }).promise();
      return config;
    } catch(e) {
      console.log('s3.getObject-err: ' + e);
      throw new Error('unable to get mcorpus cdk app config from s3.');
    }
  }
}

loadConfig().then(configObj => {
  //console.log('configObj: ' + configObj);
  let config: any;
  try {
    // s3 case
    const configStr = configObj.Body.toString();
    config = JSON.parse(configStr);
    if(config) {
      // cache in user home dir only if one not already present
      if(!fs.existsSync(`${os.homedir()}/${appConfigFilename}`)) {
        // cache config file locally
        fs.writeFileSync(`${os.homedir()}/${appConfigFilename}`, configStr, {
          encoding: "utf-8"
        });
        // console.log("local config file created");
      }
    }
  } catch(e) {
    // [assume] loaded from locally cached config file case
    config = JSON.parse(configObj);
  }
  //console.log('config: ' + config);
  if(!config) throw new Error("Unresolved config.");

  createStacks(config);

}).catch(err => {
  console.log(err);
});
