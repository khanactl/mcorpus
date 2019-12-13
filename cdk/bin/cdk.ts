#!/usr/bin/env node
import 'source-map-support/register';

import cdk = require('@aws-cdk/core');

import { AppEnv } from '../lib/app-env';

import { VpcStack } from '../lib/vpc-stack';
import { SecGrpStack } from '../lib/secgrp-stack';
import { DbStack } from '../lib/db-stack';
import { DbBootstrapStack } from '../lib/db-bootstrap-stack';
import { DbDataStack } from '../lib/db-data-stack';
import { ECSStack } from '../lib/ecs-stack';
import { CICDStack } from '../lib/cicd-stack';

// import { sharedConfig, devConfig, prdConfig } from './mcorpus-cdk-config'
const config = require("../app.config.json");

function resolveAppEnv(): AppEnv {
  // are we in codebuild env (there is no .git dir there)?
  let branch = process.env.GIT_BRANCH_NAME;  // rely on custom buildspec env var
  if(!branch) {
    branch = require('git-branch');
  }
  switch(branch) {
    case 'master':  return AppEnv.PRD;
    default:        return AppEnv.DEV;
  }
}

const appEnv = resolveAppEnv();
// console.log(`appEnv: ${appEnv}`);

const app = new cdk.App();

const env: cdk.Environment = {
  account: config.sharedConfig.awsAccountId,
  region: config.sharedConfig.awsRegion, 
};

const tags = {
  "AppName": config.appName,
  "AppEnv": config.appEnv, 
};

// common VPC 
const vpcStack = new VpcStack(app, {
  appEnv: AppEnv.SHARED, 
  appName: config.appName, 
  env: env, 
  tags: tags, 
});
const secGrpStack = new SecGrpStack(app, {
  appEnv: AppEnv.SHARED, 
  appName: config.appName, 
  env: env, 
  tags: tags, 
  vpc: vpcStack.vpc, 
  lbTrafficPort: config.devConfig.webAppContainerConfig.lbToAppPort, 
});

// common RDS instance
const dbStack = new DbStack(app, {
  appEnv: AppEnv.SHARED, 
  appName: config.appName, 
  env: env, 
  tags: tags, 
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
  env: env, 
  tags: tags, 
  vpc: vpcStack.vpc, 
  dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp, 
  dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn, 
  targetRegion: config.sharedConfig.awsRegion, 
});
const dbDataStack = new DbDataStack(app, {
  appEnv: AppEnv.SHARED, 
  appName: config.appName, 
  env: env, 
  tags: tags, 
  vpc: vpcStack.vpc, 
  dbDataSecGrp: secGrpStack.dbBootstrapSecGrp, 
  dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn, 
  // s3KmsEncKeyArn: appConfig.ssmKmsArn
});

var webAppContainerConfig;
var cicdConfig;
switch(appEnv) {
  case AppEnv.PRD: {
    webAppContainerConfig = config.prdConfig.webAppContainerConfig;
    cicdConfig = config.prdConfig.cicdConfig;
  }
  default:
  case AppEnv.DEV: {
    webAppContainerConfig = config.prdConfig.webAppContainerConfig;
    cicdConfig = config.prdConfig.cicdConfig;
  }
}

const ecsStack = new ECSStack(app, {
  appEnv: appEnv, 
  appName: config.appName, 
  env: env, 
  tags: tags, 
  vpc: vpcStack.vpc, 
  lbToEcsPort: webAppContainerConfig.lbToAppPort, 
  sslCertArn: webAppContainerConfig.tlsCertArn, 
  // ssmKmsArn: appConfig.ssmKmsArn, 
  ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl, 
  ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl, 
  ecsSecGrp: secGrpStack.ecsSecGrp, 
  lbSecGrp: secGrpStack.lbSecGrp, 
  webAppUrl: webAppContainerConfig.webAppUrl, 
  javaOpts: webAppContainerConfig.javaOpts, 
  publicDomainName: webAppContainerConfig.dnsConfig.publicDomainName, 
  awsHostedZoneId: webAppContainerConfig.dnsConfig.awsHostedZoneId, 
});
const cicdStack = new CICDStack(app, {
  appEnv: appEnv, 
  appName: config.appName, 
  env: env, 
  tags: tags, 
  githubOwner: config.sharedConfig.gitRepoRef.githubOwner, 
  githubRepo: config.sharedConfig.gitRepoRef.githubRepo, 
  githubOauthTokenSecretName: config.sharedConfig.gitRepoRef.githubOauthTokenSecretName, 
  gitBranchName: config.prdConfig.cicdConfig.gitBranchName, 
  vpc: vpcStack.vpc, 
  codebuildSecGrp: secGrpStack.codebuildSecGrp, 
  buildspecFilename: cicdConfig.buildspecFilename, 
  fargateSvc: ecsStack.fargateSvc, 
  ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl, 
  ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl, 
  cicdDeployApprovalEmails: cicdConfig.appDeployApprovalEmails, 
}); 
