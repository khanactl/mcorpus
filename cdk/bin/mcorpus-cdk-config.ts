#!/usr/bin/env node

/**
 * The mcorpus aws app configuration definition.
 * 
 * @author jpk
 */
export interface IMcorpusConfig {
  /**
   * The AWS account under which to create the stacks.
   */
  readonly awsAccountId: string;
  /**
   * The primary AWS Region to which the CDK stacks are deployed.
   */
  readonly awsRegion: string;
  /**
   * The 'traffic' port for the front-facing load balancer to the 
   * web application container.
   */
  readonly lbToAppPort: number;
  /**
   * The TLS certificate ARN to use at the public-facing app load balancer 
   * to suport https application connections.
   */
  readonly tlsCertArn: string;
  /**
   * The KMS ARN to use for generating SSM secure parameters.
   */
  readonly ssmKmsArn: string;
  /**
   * Instance attributes.
   * Key/value pairs to globally ascribe to the CDK generated artifacts.
   * These attrs translate to adding stack-level tags.
   */
  readonly instanceAttrs?: { [key: string]: string };
}

/**
 * =================================
 * The Mcorpus app DEV config object.
 * =================================
 */
export const devConfig: IMcorpusConfig = {
  awsAccountId: '524006177124', 
  awsRegion: 'us-west-2', 
  lbToAppPort: 5150, 
  ssmKmsArn: 'arn:aws:kms:us-west-2:524006177124:key/c66b3f26-8480-40f1-95a1-6abf58f2aedd', 
  tlsCertArn: 'arn:aws:acm:us-west-2:524006177124:certificate/8c7ea4bb-f2fd-4cdb-b85c-184d2a864b0a', 
  instanceAttrs: {
    'AppEnv': 'DEV', 
  }, 
}
