#!/usr/bin/env node
import 'source-map-support/register';

import cdk = require('@aws-cdk/core');

import { ICdkAppConfig } from '../lib/cdk-config-def';
import { sharedConfig, devConfig, prdConfig } from './mcorpus-cdk-config'

import { VpcStack } from '../lib/vpc-stack';
import { SecGrpStack } from '../lib/secgrp-stack';
import { DbStack } from '../lib/db-stack';
import { DbBootstrapStack } from '../lib/db-bootstrap-stack';
import { DbDataStack } from '../lib/db-data-stack';
import { ECSStack } from '../lib/ecs-stack';
import { CICDStack } from '../lib/cicd-stack';

const app = new cdk.App();

// common VPC 
const vpcStack = new VpcStack(app, {
  appConfig: sharedConfig, 
});
const secGrpStack = new SecGrpStack(app, {
  appConfig: sharedConfig, 
  vpc: vpcStack.vpc, 
  lbTrafficPort: devConfig.webAppContainerConfig!.lbToAppPort, 
});

// common RDS instance
const dbStack = new DbStack(app, {
  appConfig: sharedConfig, 
  vpc: vpcStack.vpc, 
  dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp, 
  ecsSecGrp: secGrpStack.ecsSecGrp, 
  codebuildSecGrp: secGrpStack.codebuildSecGrp, 
});
const dbBootstrapStack = new DbBootstrapStack(app, {
  appConfig: sharedConfig, 
  vpc: vpcStack.vpc, 
  dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp, 
  dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn, 
  targetRegion: sharedConfig.awsRegion, 
});
const dbDataStack = new DbDataStack(app, {
  appConfig: sharedConfig, 
  vpc: vpcStack.vpc, 
  dbDataSecGrp: secGrpStack.dbBootstrapSecGrp, 
  dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn, 
  // s3KmsEncKeyArn: appConfig.ssmKmsArn
});

createAppInstance(devConfig);
createAppInstance(prdConfig);

function createAppInstance(config: ICdkAppConfig) {
  const ecsStack = new ECSStack(app, {
    appConfig: config, 
    vpc: vpcStack.vpc, 
    lbToEcsPort: config.webAppContainerConfig!.lbToAppPort, 
    sslCertArn: config.webAppContainerConfig!.tlsCertArn, 
    // ssmKmsArn: appConfig.ssmKmsArn, 
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl, 
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl, 
    ecsSecGrp: secGrpStack.ecsSecGrp, 
    lbSecGrp: secGrpStack.lbSecGrp, 
    webAppUrl: config.webAppContainerConfig!.webAppUrl, 
    javaOpts: config.webAppContainerConfig!.javaOpts, 
    publicDomainName: config.webAppContainerConfig!.dnsConfig.publicDomainName, 
    awsHostedZoneId: config.webAppContainerConfig!.dnsConfig.awsHostedZoneId, 
  });
  const cicdStack = new CICDStack(app, {
    appConfig: config, 
    githubOwner: config.cicdConfig!.gitRepoRef.githubOwner, 
    githubRepo: config.cicdConfig!.gitRepoRef.githubRepo, 
    githubOauthTokenSecretName: config.cicdConfig!.gitRepoRef.githubOauthTokenSecretName, 
    gitBranchName: config.cicdConfig!.gitBranchName, 
    vpc: vpcStack.vpc, 
    codebuildSecGrp: secGrpStack.codebuildSecGrp, 
    buildspecFilename: config.cicdConfig!.buildspecFilename, 
    fargateSvc: ecsStack.fargateSvc, 
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl, 
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl, 
    cicdDeployApprovalEmails: config.cicdConfig!.appDeployApprovalEmails, 
  });
}
