#!/usr/bin/env node
import 'source-map-support/register';
import { devConfig } from './mcorpus-cdk-config'
import cdk = require('@aws-cdk/core');
import { VpcStack } from '../lib/vpc-stack';
import { SecGrpStack } from '../lib/secgrp-stack';
import { DbStack } from '../lib/db-stack';
import { DbBootstrapStack } from '../lib/db-bootstrap-stack';
import { ECSStack } from '../lib/ecs-stack';
import { CICDStack } from '../lib/cicd-stack';

const app = new cdk.App();

DbBootstrapStack.generateLambdaZipFile(() => {
  // console.debug("Generating stacks..")
  const vpcStack = new VpcStack(app, 'VpcStack', {
    tags: devConfig.instanceAttrs, 
  });
  const secGrpStack = new SecGrpStack(app, 'SecGrpStack', {
    tags: devConfig.instanceAttrs, 
    vpc: vpcStack.vpc, 
    lbTrafficPort: devConfig.lbToAppPort, 
  });
  const dbStack = new DbStack(app, 'DbStack', {
    tags: devConfig.instanceAttrs, 
    vpc: vpcStack.vpc, 
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp, 
    ecsSecGrp: secGrpStack.ecsSecGrp, 
  });
  const dbBootstrapStack = new DbBootstrapStack(app, 'DbBootstrapStack', {
    tags: devConfig.instanceAttrs, 
    vpc: vpcStack.vpc, 
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp, 
    dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn, 
    targetRegion: devConfig.awsRegion, 
  });
  const ecsStack = new ECSStack(app, 'ECSStack', {
    tags: devConfig.instanceAttrs, 
    vpc: vpcStack.vpc, 
    lbToEcsPort: devConfig.lbToAppPort, 
    sslCertArn: devConfig.tlsCertArn, 
    ssmKmsArn: devConfig.ssmKmsArn, 
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl, 
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl, 
    ecsSecGrp: secGrpStack.ecsSecGrp, 
    lbSecGrp: secGrpStack.lbSecGrp
  });
  const cicdStack = new CICDStack(app, 'CICDStack', {
    tags: devConfig.instanceAttrs, 
    vpc: vpcStack.vpc, 
    codebuildSecGrp: secGrpStack.codebuildSecGrp, 
    fargateSvc: ecsStack.fargateSvc, 
  });
  // console.debug("Stacks generated.")
});
