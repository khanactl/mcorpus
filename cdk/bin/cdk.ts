#!/usr/bin/env node
import 'source-map-support/register';

import cdk = require('@aws-cdk/core');

import { VpcStack } from '../lib/vpc-stack';
import { SecGrpStack } from '../lib/secgrp-stack';
import { DbStack } from '../lib/db-stack';
import { DbBootstrapStack } from '../lib/db-bootstrap-stack';
import { DbDataStack } from '../lib/db-data-stack';
import { ECSStack } from '../lib/ecs-stack';
import { CICDStack } from '../lib/cicd-stack';

import { appConfig } from './mcorpus-cdk-config';

const app = new cdk.App();

createStacks();

function createStacks() {
  // console.debug("Generating stacks..")
  const vpcStack = new VpcStack(app, 'VpcStack', {
    tags: appConfig.instanceAttrs, 
  });
  const secGrpStack = new SecGrpStack(app, 'SecGrpStack', {
    tags: appConfig.instanceAttrs, 
    vpc: vpcStack.vpc, 
    lbTrafficPort: appConfig.lbToAppPort, 
  });
  const dbStack = new DbStack(app, 'DbStack', {
    tags: appConfig.instanceAttrs, 
    vpc: vpcStack.vpc, 
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp, 
    ecsSecGrp: secGrpStack.ecsSecGrp, 
    codebuildSecGrp: secGrpStack.codebuildSecGrp, 
  });
  const dbBootstrapStack = new DbBootstrapStack(app, 'DbBootstrapStack', {
    tags: appConfig.instanceAttrs, 
    vpc: vpcStack.vpc, 
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp, 
    dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn, 
    targetRegion: appConfig.awsRegion, 
  });
  const dbDataStack = new DbDataStack(app, 'DbDataStack', {
    tags: appConfig.instanceAttrs, 
    vpc: vpcStack.vpc, 
    dbDataSecGrp: secGrpStack.dbBootstrapSecGrp, 
    dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn, 
    // s3KmsEncKeyArn: appConfig.ssmKmsArn
  });
  const ecsStack = new ECSStack(app, 'ECSStack', {
    tags: appConfig.instanceAttrs, 
    vpc: vpcStack.vpc, 
    lbToEcsPort: appConfig.lbToAppPort, 
    sslCertArn: appConfig.tlsCertArn, 
    // ssmKmsArn: appConfig.ssmKmsArn, 
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl, 
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl, 
    ecsSecGrp: secGrpStack.ecsSecGrp, 
    lbSecGrp: secGrpStack.lbSecGrp, 
    webAppUrl: appConfig.webAppUrl, 
    javaOpts: appConfig.javaOpts, 
    publicDomainName: appConfig.publicDomainName, 
    awsHostedZoneId: appConfig.awsHostedZoneId, 
  });
  const cicdStack = new CICDStack(app, 'CICDStack', {
    githubOwner: appConfig.githubOwner, 
    githubRepo: appConfig.githubRepo, 
    githubOauthTokenSecretName: appConfig.githubOauthTokenSecretName, 
    tags: appConfig.instanceAttrs, 
    vpc: vpcStack.vpc, 
    codebuildSecGrp: secGrpStack.codebuildSecGrp, 
    fargateSvc: ecsStack.fargateSvc, 
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl, 
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl, 
    cicdDeployApprovalEmails: appConfig.cicdDeployApprovalEmails, 
  });
  // console.debug("Stacks generated.")
}
