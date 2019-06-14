#!/usr/bin/env node
import 'source-map-support/register';
import cdk = require('@aws-cdk/cdk');
import { McorpusVpc } from '../lib/mcorpus-vpc';

const app = new cdk.App();
new McorpusVpc(app, 'McorpusVpc');
