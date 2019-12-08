#!/usr/bin/env node
import 'source-map-support/register';

import cdk = require('@aws-cdk/core');

import { ICdkAppConfig } from '../lib/cdk-config-def';
import { devConfig, prdConfig } from './mcorpus-cdk-config'

import { VpcStack } from '../lib/vpc-stack';
import { SecGrpStack } from '../lib/secgrp-stack';
import { DbStack } from '../lib/db-stack';
import { DbBootstrapStack } from '../lib/db-bootstrap-stack';
import { DbDataStack } from '../lib/db-data-stack';
import { ECSStack } from '../lib/ecs-stack';
import { CICDStack } from '../lib/cicd-stack';

function createAppInstance(config: ICdkAppConfig) {
  // console.debug("Generating stacks..")
  const vpcStack = new VpcStack(app, {
    appConfig: config, 
  });
  const secGrpStack = new SecGrpStack(app, {
    appConfig: config, 
    vpc: vpcStack.vpc, 
    lbTrafficPort: config.lbToAppPort, 
  });
  const dbStack = new DbStack(app, {
    appConfig: config, 
    vpc: vpcStack.vpc, 
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp, 
    ecsSecGrp: secGrpStack.ecsSecGrp, 
    codebuildSecGrp: secGrpStack.codebuildSecGrp, 
  });
  const dbBootstrapStack = new DbBootstrapStack(app, {
    appConfig: config, 
    vpc: vpcStack.vpc, 
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp, 
    dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn, 
    targetRegion: config.awsRegion, 
  });
  const dbDataStack = new DbDataStack(app, {
    appConfig: config, 
    vpc: vpcStack.vpc, 
    dbDataSecGrp: secGrpStack.dbBootstrapSecGrp, 
    dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn, 
    // s3KmsEncKeyArn: appConfig.ssmKmsArn
  });
  const ecsStack = new ECSStack(app, {
    appConfig: config, 
    vpc: vpcStack.vpc, 
    lbToEcsPort: config.lbToAppPort, 
    sslCertArn: config.tlsCertArn, 
    // ssmKmsArn: appConfig.ssmKmsArn, 
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl, 
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl, 
    ecsSecGrp: secGrpStack.ecsSecGrp, 
    lbSecGrp: secGrpStack.lbSecGrp, 
    webAppUrl: config.webAppUrl, 
    javaOpts: config.javaOpts, 
    publicDomainName: config.dnsConfig.publicDomainName, 
    awsHostedZoneId: config.dnsConfig.awsHostedZoneId, 
  });
  const cicdStack = new CICDStack(app, {
    appConfig: config, 
    githubOwner: config.gitRepoRef.githubOwner, 
    githubRepo: config.gitRepoRef.githubRepo, 
    githubOauthTokenSecretName: config.gitRepoRef.githubOauthTokenSecretName, 
    gitBranchName: config.gitBranchName, 
    vpc: vpcStack.vpc, 
    codebuildSecGrp: secGrpStack.codebuildSecGrp, 
    buildspecFilename: config.buildspecFilename, 
    fargateSvc: ecsStack.fargateSvc, 
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl, 
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl, 
    cicdDeployApprovalEmails: config.appDeployApprovalEmails, 
  });
  // console.debug("Stacks generated.")
}

const app = new cdk.App();

createAppInstance(devConfig);
createAppInstance(prdConfig);
