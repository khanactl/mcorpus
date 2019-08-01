#!/usr/bin/env node
import 'source-map-support/register';
import cdk = require('@aws-cdk/core');
import { SecretsStack } from '../lib/secrets-stack';
import { VpcStack } from '../lib/vpc-stack';
import { DbStack } from '../lib/db-stack';
import { ECSStack } from '../lib/ecs-stack';
import { SecGrpStack } from '../lib/secgrp-stack';

const app = new cdk.App();

const secretsStack = new SecretsStack(app, 'SecretsStack');
const vpcStack = new VpcStack(app, 'VpcStack');
const secGrpStack = new SecGrpStack(app, 'SecGrpStack', {
  vpc: vpcStack.vpc, 
  lbTrafficPort: 5150, 
});
const dbStack = new DbStack(app, 'DbStack', {
 vpc: vpcStack.vpc, 
 ecsSecGrp: secGrpStack.ecsSecGrp, 
});
const ecsStack = new ECSStack(app, 'ECSStack', {
  vpc: vpcStack.vpc, 
  lbToEcsPort: 5150, 
  sslCertArn: 'arn:aws:acm:us-west-2:524006177124:certificate/8c7ea4bb-f2fd-4cdb-b85c-184d2a864b0a', 
  ssmKmsArn: secretsStack.kmsArn, 
  ssmMcorpusDbUrlArn: secretsStack.mcorpusDbUrlArn, 
  ssmJwtSaltArn: secretsStack.jwtSaltArn, 
  ecsSecGrp: secGrpStack.ecsSecGrp, 
  lbSecGrp: secGrpStack.lbSecGrp
});
