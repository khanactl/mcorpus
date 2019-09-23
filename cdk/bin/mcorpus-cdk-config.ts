#!/usr/bin/env node

/**
 * The mcorpus aws cdk app configuration definition.
 * 
 * The application knobs for a specific application instance/deployment.
 * 
 * @author jpk
 */
export interface IMcorpusCdkAppConfig {
  /**
   * The AWS account in which to create the app CDK stacks.
   */
  readonly awsAccountId: string;
  /**
   * The primary AWS Region to which the CDK stacks are deployed.
   */
  readonly awsRegion: string;
  /**
   * Instance attributes.
   * Key/value pairs to globally ascribe to the CDK generated artifacts.
   * These attrs translate to adding stack-level tags.
   */
  readonly instanceAttrs?: { [key: string]: string };
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
   * The public-facing web app url address.
   */
  readonly webAppUrl: string;
  /**
   * JAVA_OPTS to use for the ECS/Fargate docker container.
   */
  readonly javaOpts: string;
  /**
   * The KMS ARN to use for generating SSM secure parameters.
   */
  // readonly ssmKmsArn: string;
  /**
   * The CICD manual approval notification emails.
   */
  readonly cicdDeployApprovalEmails: string[];
}

/**
 * The Mcorpus AWS app configuration.
 * ==================================
 */
export const appConfig: IMcorpusCdkAppConfig = {
  awsAccountId: '524006177124', 
  awsRegion: 'us-west-2', 
  instanceAttrs: {
    'AppEnv': 'DEV', 
  }, 
  lbToAppPort: 5150, 
  // ssmKmsArn: 'arn:aws:kms:us-west-2:524006177124:key/c66b3f26-8480-40f1-95a1-6abf58f2aedd', 
  tlsCertArn: 'arn:aws:acm:us-west-2:524006177124:certificate/8c7ea4bb-f2fd-4cdb-b85c-184d2a864b0a', 
  webAppUrl: 'https://www.mcorpus-aws.net', 
  javaOpts: '-server -Xms100M -Xmx1000M -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=log4j2-aws.xml -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -cp log4j-api-2.12.1.jar:log4j-core-2.12.1.jar:log4j-slf4j-impl-2.12.1.jar:disruptor-3.4.2.jar', 
  cicdDeployApprovalEmails: [
    'jpucop@gmail.com', 
  ]
}
