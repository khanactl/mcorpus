#!/usr/bin/env node
import 'source-map-support/register';
import {
  AppEnv,
  resolveAppBuild,
  resolveAppEnv,
  resolveAppImageTag,
  resolveCurrentGitBranch,
  loadConfig
} from '../lib/cdk-native';
import { CICDStack } from '../lib/cicd-stack';
import { DbBootstrapStack } from '../lib/db-bootstrap-stack';
import { DbDataStack } from '../lib/db-data-stack';
import { DbStack } from '../lib/db-stack';
import { ECSStack } from '../lib/ecs-stack';
import { ClusterStack } from '../lib/cluster-stack';
import { SecGrpStack } from '../lib/secgrp-stack';
import { VpcStack } from '../lib/vpc-stack';
import { WafStack } from '../lib/waf-stack';
import { AppStack } from '../lib/app-stack';

import fs = require('fs');
import os = require('os');
import path = require('path');

import cdk = require('@aws-cdk/core');
import { DevPipelineStack } from '../lib/dev-pipeline-stack';

const app = new cdk.App();

/**
 * The expected name of the required cdk app config json file.
 *
 * This JSON object (assumed to adhere to an expected structure)
 * provides the needed input to all instantiated cdk stacks herein.
 */
const appConfigFilename = "mcorpus-cdk-app-config.json";

/**
 * The S3 bucket name in the default AWS account holding a cached copy of
 * the app config file.
 *
 * This is used when no local app config file is found.
 */
const appConfigCacheS3BucketName = "mcorpus-db-data-bucket-shared";

/**
 * Generate the CDK stacks.
 *
 * @param appConfig json obj of config to use
 */
function createStacks(appConfig: any) {
  const gitRepoRef = appConfig.sharedConfig.gitRepoRef;

  // const currentGitBranch = resolveCurrentGitBranch();
  // const currentAppEnv = resolveAppEnv(currentGitBranch);
  // console.log(`gitBranch: ${currentGitBranch}, currentAppEnv: ${currentAppEnv}`);

  // const appBuild = resolveAppBuild("../mcorpus-gql/target/classes/app.properties");

  // single aws account houses all app instances
  const awsEnv: cdk.Environment = {
    account: appConfig.sharedConfig.awsAccountId,
    region: appConfig.sharedConfig.awsRegion,
  };

  // common aws stack tags
  const awsStackTags_Shared = {
    AppName: appConfig.appName,
    AppEnv: AppEnv.SHARED,
  };

  const awsStackTags_DEV = {
    AppName: appConfig.appName,
    AppEnv: AppEnv.DEV,
  };
  const awsStackTags_PRD = {
    AppName: appConfig.appName,
    AppEnv: AppEnv.PRD,
  };

  // isolate app env dependent config
  const devWebAppContainerConfig = appConfig.devConfig.webAppContainerConfig;
  const prdWebAppContainerConfig = appConfig.prdConfig.webAppContainerConfig;
  const devCicdConfig = appConfig.devConfig.cicdConfig;
  const prdCicdConfig = appConfig.prdConfig.cicdConfig;

  /*
  let webAppContainerConfig: any;
  let cicdConfig: any;
  switch (currentAppEnv) {
    case AppEnv.PRD: {
      webAppContainerConfig = appConfig.prdConfig.webAppContainerConfig;
      cicdConfig = appConfig.prdConfig.cicdConfig;
      break;
    }
    case AppEnv.DEV: {
      webAppContainerConfig = appConfig.devConfig.webAppContainerConfig;
      cicdConfig = appConfig.devConfig.cicdConfig;
      break;
    }
    default:
      throw new Error(`Invalid target app env: ${currentAppEnv}`);
  }
  */

  // get the ECR ref
  // const ecrArn = `arn:aws:ecr:${appConfig.sharedConfig.ecrConfig.awsRegion}:${appConfig.sharedConfig.ecrConfig.awsAccountId}:repository/${appConfig.sharedConfig.ecrConfig.name}`;

  // resolve the web app docker image tag
  // const ecsDkrImgTag = resolveAppImageTag(appConfig, appBuild);
  // console.log(`ecsDkrImgTag: ${ecsDkrImgTag}`);

  /*
  // app env dependent infra-bootstrap pipeline (the app env genesis stack)
  const infraPipelineStack = new InfraPipelineStack(app, {
    appName: appConfig.appName,
    appEnv: currentAppEnv,
    env: awsEnv,
    tags: awsStackTags_appInstance,
    githubOwner: gitRepoRef.githubOwner,
    githubRepo: gitRepoRef.githubRepo,
    githubOauthTokenSecretArn: gitRepoRef.githubOauthTokenSecretArn,
    githubOauthTokenSecretJsonFieldName:
      gitRepoRef.githubOauthTokenSecretJsonFieldName,
    gitBranchName: cicdConfig.gitBranchName,
  });
  */

  // common VPC
  const vpcStack = new VpcStack(app, {
    appName: appConfig.appName,
    appEnv: AppEnv.SHARED,
    env: awsEnv,
    tags: awsStackTags_Shared,
    maxAzs: appConfig.sharedConfig.networkConfig.maxAzs,
    cidr: appConfig.sharedConfig.networkConfig.cidr,
  });
  const secGrpStack = new SecGrpStack(app, {
    appName: appConfig.appName,
    appEnv: AppEnv.SHARED,
    env: awsEnv,
    tags: awsStackTags_Shared,
    vpc: vpcStack.vpc,
  });

  // common RDS instance
  const dbStack = new DbStack(app, {
    appName: appConfig.appName,
    appEnv: AppEnv.SHARED,
    env: awsEnv,
    tags: awsStackTags_Shared,
    vpc: vpcStack.vpc,
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp,
    ecsSecGrp: secGrpStack.ecsSecGrp,
    codebuildSecGrp: secGrpStack.codebuildSecGrp,
    dbName: appConfig.sharedConfig.dbConfig.dbName,
    dbMasterUsername: appConfig.sharedConfig.dbConfig.dbMasterUsername,
  });
  const dbBootstrapStack = new DbBootstrapStack(app, {
    appName: appConfig.appName,
    appEnv: AppEnv.SHARED,
    env: awsEnv,
    tags: awsStackTags_Shared,
    vpc: vpcStack.vpc,
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp,
    dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn,
    targetRegion: appConfig.sharedConfig.awsRegion,
  });
  const dbDataStack = new DbDataStack(app, {
    appName: appConfig.appName,
    appEnv: AppEnv.SHARED,
    env: awsEnv,
    tags: awsStackTags_Shared,
    vpc: vpcStack.vpc,
    dbDataSecGrp: secGrpStack.dbBootstrapSecGrp,
    dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn,
    // s3KmsEncKeyArn: appConfig.ssmKmsArn
  });

  // dev cluster
  const devClusterStack = new ClusterStack(app, {
    appName: appConfig.appName,
    appEnv: AppEnv.DEV,
    env: awsEnv,
    tags: awsStackTags_DEV,
    vpc: vpcStack.vpc,
  });
  devClusterStack.addDependency(secGrpStack);

  const devPipelineStack = new DevPipelineStack(app, {
    appName: appConfig.appName,
    appEnv: AppEnv.DEV,
    env: awsEnv,
    tags: awsStackTags_DEV,
    vpc: vpcStack.vpc,
    codebuildSecGrp: secGrpStack.codebuildSecGrp,
    appConfigCacheS3BucketName: appConfigCacheS3BucketName,
    appConfigFilename: appConfigFilename,
    ssmImageTagParamName: devCicdConfig.ssmImageTagParamName,
    githubOwner: gitRepoRef.githubOwner,
    githubRepo: gitRepoRef.githubRepo,
    githubOauthTokenSecretArn: gitRepoRef.githubOauthTokenSecretArn,
    githubOauthTokenSecretJsonFieldName:
      gitRepoRef.githubOauthTokenSecretJsonFieldName,
    gitBranchName: devCicdConfig.gitBranchName,
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl,
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl,
  });

  const devAppStack = new AppStack(app, {
    appName: appConfig.appName,
    appEnv: AppEnv.DEV,
    env: awsEnv,
    tags: awsStackTags_DEV,
    vpc: vpcStack.vpc,
    cluster: devClusterStack.cluster,
    appImage: devPipelineStack.appBuiltImage,
    taskdefCpu: devWebAppContainerConfig.taskdefCpu,
    taskdefMemoryLimitMiB: devWebAppContainerConfig.taskdefMemoryLimitMiB,
    containerDefMemoryLimitMiB:
      devWebAppContainerConfig.containerDefMemoryLimitMiB,
    containerDefMemoryReservationMiB:
      devWebAppContainerConfig.containerDefMemoryReservationMiB,
    lbToEcsPort: devWebAppContainerConfig.lbToAppPort,
    sslCertArn: devWebAppContainerConfig.tlsCertArn,
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl,
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl,
    ecsSecGrp: secGrpStack.ecsSecGrp,
    lbSecGrp: secGrpStack.lbSecGrp,
    webAppUrl: devWebAppContainerConfig.webAppUrl,
    javaOpts: devWebAppContainerConfig.javaOpts,
    publicDomainName: devWebAppContainerConfig.dnsConfig.publicDomainName,
    awsHostedZoneId: devWebAppContainerConfig.dnsConfig.awsHostedZoneId,
  });

  /*
  const wafStack = new WafStack(app, {
    appName: appConfig.appName,
    appEnv: currentAppEnv,
    env: awsEnv,
    tags: awsStackTags_appInstance,
    appLoadBalancerRef: ecsStack.appLoadBalancer,
  });
  */

  /*
  const cicdStack = new CICDStack(app, {
    appName: appConfig.appName,
    appEnv: currentAppEnv,
    env: awsEnv,
    tags: awsStackTags_appInstance,
    githubOwner: gitRepoRef.githubOwner,
    githubRepo: gitRepoRef.githubRepo,
    githubOauthTokenSecretArn: gitRepoRef.githubOauthTokenSecretArn,
    githubOauthTokenSecretJsonFieldName:
      gitRepoRef.githubOauthTokenSecretJsonFieldName,
    gitBranchName: cicdConfig.gitBranchName,
    vpc: vpcStack.vpc,
    codebuildSecGrp: secGrpStack.codebuildSecGrp,
    ecsTaskDefContainerName: ecsStack.containerName,
    lbToEcsPort: webAppContainerConfig.lbToAppPort,
    ecrArn: ecrArn,
    fargateSvc: ecsStack.fargateSvc,
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl,
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl,
    cicdDeployApprovalEmails: cicdConfig.appDeployApprovalEmails,
  });
  cicdStack.addDependency(wafStack, 'CICD pipeline depends on ecs and waf.');
  */
}

loadConfig(appConfigFilename, appConfigCacheS3BucketName)
  .then(config => createStacks(config))
  .catch(err => console.log(err));
