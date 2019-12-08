#!/usr/bin/env node

import { AppEnv, IGitHubRepoRef, IDnsConfig, ICdkAppConfig } from '../lib/cdk-config-def'

export const APP_NAME = 'mcorpus';
export const AWS_ACCOUNT_ID = '524006177124';
export const AWS_PRIMARY_REGION = 'us-west-2';

export const gitRepoRef: IGitHubRepoRef = {
  githubOwner: 'khana', 
  githubRepo: 'mcorpus', 
  githubOauthTokenSecretName: 'mcorpus-github-oauth-token', 
}

export const devConfig: ICdkAppConfig = {
  appName: APP_NAME, 
  appEnv: AppEnv.DEV, 
  gitRepoRef: gitRepoRef, 
  gitBranchName: 'infra', // TODO change to dev when ready
  awsAccountId: AWS_ACCOUNT_ID, 
  awsRegion: AWS_PRIMARY_REGION, 
  instanceAttrs: {
    'AppName': APP_NAME, 
    'AppEnv': AppEnv.DEV,
  }, 
  lbToAppPort: 5150, 
  tlsCertArn: 'arn:aws:acm:us-west-2:524006177124:certificate/8c7ea4bb-f2fd-4cdb-b85c-184d2a864b0a', 
  webAppUrl: 'https://www.dev.mcorpus-aws.net', 
  javaOpts: '-server -Xms100M -Xmx1000M -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=log4j2-aws.xml -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -cp log4j-api-2.12.1.jar:log4j-core-2.12.1.jar:log4j-slf4j-impl-2.12.1.jar:disruptor-3.4.2.jar', 
  dnsConfig: {
    awsHostedZoneId: 'Z18ZXNXBFMQKMU', 
    publicDomainName: 'dev.mcorpus-aws.net', 
  }, 
  buildspecFilename: 'buildspec-ecs-dev.yml', 
  appDeployApprovalEmails: [
    'jpucop@gmail.com', 
  ], 
}

export const prdConfig: ICdkAppConfig = {
  appName: APP_NAME, 
  appEnv: AppEnv.PRD, 
  gitRepoRef: gitRepoRef, 
  gitBranchName: 'master', 
  awsAccountId: AWS_ACCOUNT_ID, 
  awsRegion: AWS_PRIMARY_REGION, 
  instanceAttrs: {
    'AppName': APP_NAME, 
    'AppEnv': AppEnv.PRD,
  }, 
  lbToAppPort: 5150, 
  tlsCertArn: 'arn:aws:acm:us-west-2:524006177124:certificate/8c7ea4bb-f2fd-4cdb-b85c-184d2a864b0a', 
  webAppUrl: 'https://www.mcorpus-aws.net', 
  javaOpts: '-server -Xms100M -Xmx1000M -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=log4j2-aws.xml -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -cp log4j-api-2.12.1.jar:log4j-core-2.12.1.jar:log4j-slf4j-impl-2.12.1.jar:disruptor-3.4.2.jar', 
  dnsConfig: {
    awsHostedZoneId: 'Z1FUM5HD37QRP1', 
    publicDomainName: 'mcorpus-aws.net', 
  }, 
  buildspecFilename: 'buildspec-ecs-prd.yml', 
  appDeployApprovalEmails: [
    'jpucop@gmail.com', 
  ], 
}

