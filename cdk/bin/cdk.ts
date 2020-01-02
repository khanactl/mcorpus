#!/usr/bin/env node
import 'source-map-support/register';

import fs = require('fs');
import os = require('os');

import cdk = require('@aws-cdk/core');

import { AppEnv } from '../lib/app-env';

import { VpcStack } from '../lib/vpc-stack';
import { SecGrpStack } from '../lib/secgrp-stack';
import { DbStack } from '../lib/db-stack';
import { DbBootstrapStack } from '../lib/db-bootstrap-stack';
import { DbDataStack } from '../lib/db-data-stack';
import { ECSStack } from '../lib/ecs-stack';
import { WafStack } from '../lib/waf-stack';
import { CICDStack } from '../lib/cicd-stack';

function resolveCurrentGitBranch(): string {
  // are we in codebuild env (there is no .git dir there)?
  let branch = process.env.GIT_BRANCH_NAME;  // rely on custom buildspec env var
  if(!branch) {
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

function createAppInstance(appEnv: AppEnv, appConfig: any): void {
  var webAppContainerConfig;
  var cicdConfig;
  switch(appEnv) {
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
      throw new Error(`Invalid target app env: ${appEnv}`);
  }

  const awsStackTags_appInstance = {
    "AppName": appConfig.appName,
    "AppEnv": appEnv,
  }

  const gitRepoRef = appConfig.sharedConfig.gitRepoRef;

  const ecsStack = new ECSStack(app, {
    appEnv: appEnv,
    appName: appConfig.appName,
    env: awsEnv,
    tags: awsStackTags_appInstance,
    vpc: vpcStack.vpc,
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
    appEnv: appEnv,
    appName: appConfig.appName,
    env: awsEnv,
    tags: awsStackTags_appInstance,
    appLoadBalancerRef: ecsStack.appLoadBalancer,
  });
  const cicdStack = new CICDStack(app, {
    appEnv: appEnv,
    appName: appConfig.appName,
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
    ecrRepo: ecsStack.ecrRepo,
    fargateSvc: ecsStack.fargateSvc,
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl,
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl,
    cicdDeployApprovalEmails: cicdConfig.appDeployApprovalEmails,
  });
  cicdStack.addDependency(wafStack, "CICD is always the last stack.");
}

const currentGitBranch = resolveCurrentGitBranch();
const currentAppEnv = resolveAppEnv(currentGitBranch);
// console.log(`gitBranch: ${currentGitBranch}, currentAppEnv: ${currentAppEnv}`);

const appConfigFilename = 'mcorpus-cdk-app-config.json';
const config = JSON.parse(fs.readFileSync(`${os.homedir()}/${appConfigFilename}`, 'utf-8'));

const app = new cdk.App();

const awsEnv: cdk.Environment = {
  account: config.sharedConfig.awsAccountId,
  region: config.sharedConfig.awsRegion,
};

const awsStackTags_Shared = {
  "AppName": config.appName,
  "AppEnv": AppEnv.SHARED,
};

// common VPC
const vpcStack = new VpcStack(app, {
  appEnv: AppEnv.SHARED,
  appName: config.appName,
  env: awsEnv,
  tags: awsStackTags_Shared,
});
const secGrpStack = new SecGrpStack(app, {
  appEnv: AppEnv.SHARED,
  appName: config.appName,
  env: awsEnv,
  tags: awsStackTags_Shared,
  vpc: vpcStack.vpc,
});

// common RDS instance
const dbStack = new DbStack(app, {
  appEnv: AppEnv.SHARED,
  appName: config.appName,
  env: awsEnv,
  tags: awsStackTags_Shared,
  vpc: vpcStack.vpc,
  dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp,
  ecsSecGrp: secGrpStack.ecsSecGrp,
  codebuildSecGrp: secGrpStack.codebuildSecGrp,
  dbName: config.sharedConfig.dbConfig.dbName,
  dbMasterUsername: config.sharedConfig.dbConfig.dbMasterUsername,
});
const dbBootstrapStack = new DbBootstrapStack(app, {
  appEnv: AppEnv.SHARED,
  appName: config.appName,
  env: awsEnv,
  tags: awsStackTags_Shared,
  vpc: vpcStack.vpc,
  dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp,
  dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn,
  targetRegion: config.sharedConfig.awsRegion,
});
const dbDataStack = new DbDataStack(app, {
  appEnv: AppEnv.SHARED,
  appName: config.appName,
  env: awsEnv,
  tags: awsStackTags_Shared,
  vpc: vpcStack.vpc,
  dbDataSecGrp: secGrpStack.dbBootstrapSecGrp,
  dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn,
  // s3KmsEncKeyArn: appConfig.ssmKmsArn
});

// constrain stack output by current git branch
// createAppInstance(currentAppEnv, config);

createAppInstance(AppEnv.DEV, config);
createAppInstance(AppEnv.PRD, config);
