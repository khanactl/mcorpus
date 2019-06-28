#!/usr/bin/env node
import 'source-map-support/register';
import cdk = require('@aws-cdk/core');
import { VpcStack } from '../lib/vpc-stack';
import { LbStack } from '../lib/lb-stack';

const app = new cdk.App();
const vpcStack = new VpcStack(app, 'VpcStack');
const lbStack = new LbStack(app, 'LbStack', {
  vpc: vpcStack.vpc,
  innerPort: 5150,
  listenerPort: 443,
  sslCertId: 'TODO'
});
