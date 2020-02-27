#!/usr/bin/env node
import 'source-map-support/register';
import { AppStack } from '../lib/app-stack';
import { AppEnv, loadConfig } from '../lib/cdk-native';
import { ClusterStack } from '../lib/cluster-stack';
import { DbBootstrapStack } from '../lib/db-bootstrap-stack';
import { DbDataStack } from '../lib/db-data-stack';
import { DbStack } from '../lib/db-stack';
import { DevPipelineStack } from '../lib/dev-pipeline-stack';
import { EcrStack } from '../lib/ecr-stack';
import { LbStack } from '../lib/lb-stack';
import { SecGrpStack } from '../lib/secgrp-stack';
import { StagingProdPipelineStack } from '../lib/staging-prod-pipeline-stack';
import { VpcStack } from '../lib/vpc-stack';
import cdk = require('@aws-cdk/core');

const app = new cdk.App();

/**
 * The expected name of the required cdk app config json file.
 *
 * This JSON object (assumed to adhere to an expected structure)
 * provides the needed input to all instantiated cdk stacks herein.
 */
const appConfigFilename = 'mcorpus-cdk-app-config.json';

/**
 * The S3 bucket name in the default AWS account holding a cached copy of
 * the app config file.
 *
 * This is used when no local app config file is found.
 *
 * This bucket's life-cycle is **not managed** by these CDK stacks.
 * That is, it is assumed to *pre-exist*.
 */
const appConfigCacheS3BucketName = 'mcorpus-app-config';

/**
 * Generate the CDK stacks.
 *
 * @param appConfig json obj of config to use
 */
function createStacks(appConfig: any) {
  const gitRepoRef = appConfig.sharedConfig.gitRepoRef;

  // common aws env
  const awsEnvCommon: cdk.Environment = {
    account: appConfig.sharedConfig.awsAccountId,
    region: appConfig.sharedConfig.awsRegion,
  };

  // ecr-specific aws env
  const awsEnvEcr: cdk.Environment = {
    account: appConfig.sharedConfig.ecrConfig.awsAccountId,
    region: appConfig.sharedConfig.ecrConfig.awsRegion,
  };

  // app env stack tags
  const awsStackTagsCommon = {
    AppName: appConfig.appName,
    AppEnv: AppEnv.COMMON,
  };
  const awsStackTagsDev = {
    AppName: appConfig.appName,
    AppEnv: AppEnv.DEV,
  };
  const awsStackTagsPrd = {
    AppName: appConfig.appName,
    AppEnv: AppEnv.PRD,
  };

  // isolate app env dependent config
  const devWebAppContainerConfig = appConfig.devConfig.webAppContainerConfig;
  const prdWebAppContainerConfig = appConfig.prdConfig.webAppContainerConfig;
  const devCicdConfig = appConfig.devConfig.cicdConfig;
  const prdCicdConfig = appConfig.prdConfig.cicdConfig;

  // common ECR
  const ecrStack = new EcrStack(app, 'mcorpusEcrRepo', {
    appName: appConfig.appName,
    appEnv: AppEnv.COMMON,
    env: awsEnvEcr,
    tags: awsStackTagsCommon,
    ecrName: appConfig.sharedConfig.ecrConfig.name,
  });

  const mcorpusVpcStackName = 'mcorpusVpc';
  const mcorpusSecGrpStackName = 'mcorpusSecGrp';
  const mcorpusDevLbStackName = 'mcorpusDevLb';
  const mcorpusPrdLbStackName = 'mcorpusPrdLb';
  const mcorpusDevAppStackName = 'mcorpusDevApp';
  const mcorpusPrdAppStackName = 'mcorpusPrdApp';

  // common VPC
  const vpcStack = new VpcStack(app, mcorpusVpcStackName, {
    appName: appConfig.appName,
    appEnv: AppEnv.COMMON,
    env: awsEnvCommon,
    tags: awsStackTagsCommon,
    maxAzs: appConfig.sharedConfig.networkConfig.maxAzs,
    cidr: appConfig.sharedConfig.networkConfig.cidr,
  });
  const secGrpStack = new SecGrpStack(app, mcorpusSecGrpStackName, {
    appName: appConfig.appName,
    appEnv: AppEnv.COMMON,
    env: awsEnvCommon,
    tags: awsStackTagsCommon,
    vpc: vpcStack.vpc,
  });

  // common RDS
  const dbStack = new DbStack(app, 'mcorpusDb', {
    appName: appConfig.appName,
    appEnv: AppEnv.COMMON,
    env: awsEnvCommon,
    tags: awsStackTagsCommon,
    vpc: vpcStack.vpc,
    // dbSecGrp: secGrpStack.dbSecGrp,
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp,
    ecsSecGrp: secGrpStack.ecsSecGrp,
    codebuildSecGrp: secGrpStack.codebuildSecGrp,
    dbName: appConfig.sharedConfig.dbConfig.dbName,
    dbMasterUsername: appConfig.sharedConfig.dbConfig.dbMasterUsername,
  });
  const dbBootstrapStack = new DbBootstrapStack(app, 'mcorpusDbSchema', {
    appName: appConfig.appName,
    appEnv: AppEnv.COMMON,
    env: awsEnvCommon,
    tags: awsStackTagsCommon,
    vpc: vpcStack.vpc,
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp,
    dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn,
    targetRegion: appConfig.sharedConfig.awsRegion,
  });
  const dbDataStack = new DbDataStack(app, 'mcorpusDbData', {
    appName: appConfig.appName,
    appEnv: AppEnv.COMMON,
    env: awsEnvCommon,
    tags: awsStackTagsCommon,
    vpc: vpcStack.vpc,
    dbDataSecGrp: secGrpStack.dbBootstrapSecGrp,
    dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn,
    // s3KmsEncKeyArn: appConfig.ssmKmsArn
  });
  dbDataStack.addDependency(dbBootstrapStack);

  // dev cluster
  const devClusterStack = new ClusterStack(app, 'mcorpusDevCluster', {
    appName: appConfig.appName,
    appEnv: AppEnv.DEV,
    env: awsEnvCommon,
    tags: awsStackTagsDev,
    vpc: vpcStack.vpc,
  });
  devClusterStack.addDependency(secGrpStack);
  // prd cluster
  const prdClusterStack = new ClusterStack(app, 'mcorpusPrdCluster', {
    appName: appConfig.appName,
    appEnv: AppEnv.PRD,
    env: awsEnvCommon,
    tags: awsStackTagsPrd,
    vpc: vpcStack.vpc,
  });
  prdClusterStack.addDependency(secGrpStack);

  const devPipelineStack = new DevPipelineStack(app, 'mcorpusDevPipeline', {
    appName: appConfig.appName,
    appEnv: AppEnv.DEV,
    env: awsEnvCommon,
    tags: awsStackTagsDev,
    appRepository: ecrStack.appRepository,
    vpc: vpcStack.vpc,
    codebuildSecGrp: secGrpStack.codebuildSecGrp,
    appConfigCacheS3BucketName: appConfigCacheS3BucketName,
    appConfigFilename: appConfigFilename,
    ssmImageTagParamName: devCicdConfig.ssmImageTagParamName,
    githubOwner: gitRepoRef.githubOwner,
    githubRepo: gitRepoRef.githubRepo,
    githubOauthTokenSecretArn: gitRepoRef.githubOauthTokenSecretArn,
    githubOauthTokenSecretJsonFieldName: gitRepoRef.githubOauthTokenSecretJsonFieldName,
    gitBranchName: devCicdConfig.gitBranchName,
    triggerOnCommit: devCicdConfig.triggerOnCommit,
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl,
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl,
    appDeployApprovalEmails: devCicdConfig.appDeployApprovalEmails,
    onBuildFailureEmails: devCicdConfig.onBuildFailureEmails,
    cdkDevLbStackName: mcorpusDevLbStackName,
    cdkDevAppStackName: mcorpusDevAppStackName,
  });
  devPipelineStack.addDependency(ecrStack);
  devPipelineStack.addDependency(secGrpStack);
  devPipelineStack.addDependency(devClusterStack);

  const prodPipelineStack = new StagingProdPipelineStack(app, 'mcorpusPrdPipeline', {
    appName: appConfig.appName,
    appEnv: AppEnv.PRD,
    env: awsEnvCommon,
    tags: awsStackTagsPrd,
    appRepository: ecrStack.appRepository,
    appConfigCacheS3BucketName: appConfigCacheS3BucketName,
    appConfigFilename: appConfigFilename,
    githubOwner: gitRepoRef.githubOwner,
    githubRepo: gitRepoRef.githubRepo,
    githubOauthTokenSecretArn: gitRepoRef.githubOauthTokenSecretArn,
    githubOauthTokenSecretJsonFieldName: gitRepoRef.githubOauthTokenSecretJsonFieldName,
    gitBranchName: devPipelineStack.gitBranchName, // use DEV git branch
    triggerOnCommit: devCicdConfig.triggerOnCommit,
    ssmImageTagParamName: devCicdConfig.ssmImageTagParamName,
    prodDeployApprovalEmails: prdCicdConfig.appDeployApprovalEmails,
    cdkPrdLbStackName: mcorpusPrdLbStackName,
    cdkPrdAppStackName: mcorpusPrdAppStackName,
  });
  prodPipelineStack.addDependency(devPipelineStack);
  prodPipelineStack.addDependency(prdClusterStack);

  const devLbStack = new LbStack(app, mcorpusDevLbStackName, {
    appName: appConfig.appName,
    appEnv: AppEnv.DEV,
    env: awsEnvCommon,
    tags: awsStackTagsDev,
    vpc: vpcStack.vpc,
    lbSecGrp: secGrpStack.lbSecGrp,
    sslCertArn: devWebAppContainerConfig.tlsCertArn,
    awsHostedZoneId: devWebAppContainerConfig.dnsConfig.awsHostedZoneId,
    publicDomainName: devWebAppContainerConfig.dnsConfig.publicDomainName,
  });
  const prdLbStack = new LbStack(app, mcorpusPrdLbStackName, {
    appName: appConfig.appName,
    appEnv: AppEnv.PRD,
    env: awsEnvCommon,
    tags: awsStackTagsPrd,
    vpc: vpcStack.vpc,
    lbSecGrp: secGrpStack.lbSecGrp,
    sslCertArn: prdWebAppContainerConfig.tlsCertArn,
    awsHostedZoneId: prdWebAppContainerConfig.dnsConfig.awsHostedZoneId,
    publicDomainName: prdWebAppContainerConfig.dnsConfig.publicDomainName,
  });

  const devAppStack = new AppStack(app, mcorpusDevAppStackName, {
    appName: appConfig.appName,
    appEnv: AppEnv.DEV,
    env: awsEnvCommon,
    tags: awsStackTagsDev,
    vpc: vpcStack.vpc,
    cluster: devClusterStack.cluster,
    appImage: devPipelineStack.appBuiltImage,
    lbListener: devLbStack.lbListener,
    taskdefCpu: devWebAppContainerConfig.taskdefCpu,
    taskdefMemoryLimitMiB: devWebAppContainerConfig.taskdefMemoryLimitMiB,
    containerDefMemoryLimitMiB: devWebAppContainerConfig.containerDefMemoryLimitMiB,
    containerDefMemoryReservationMiB: devWebAppContainerConfig.containerDefMemoryReservationMiB,
    lbToEcsPort: devWebAppContainerConfig.lbToAppPort,
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl,
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl,
    ecsSecGrp: secGrpStack.ecsSecGrp,
    lbSecGrp: secGrpStack.lbSecGrp,
    webAppUrl: devWebAppContainerConfig.webAppUrl,
    javaOpts: devWebAppContainerConfig.javaOpts,
  });
  devAppStack.addDependency(devLbStack);
  devAppStack.addDependency(devPipelineStack);

  const prdAppStack = new AppStack(app, mcorpusPrdAppStackName, {
    appName: appConfig.appName,
    appEnv: AppEnv.PRD,
    env: awsEnvCommon,
    tags: awsStackTagsPrd,
    vpc: vpcStack.vpc,
    cluster: prdClusterStack.cluster,
    appImage: prodPipelineStack.appBuiltImageProd,
    lbListener: prdLbStack.lbListener,
    taskdefCpu: prdWebAppContainerConfig.taskdefCpu,
    taskdefMemoryLimitMiB: prdWebAppContainerConfig.taskdefMemoryLimitMiB,
    containerDefMemoryLimitMiB: prdWebAppContainerConfig.containerDefMemoryLimitMiB,
    containerDefMemoryReservationMiB: prdWebAppContainerConfig.containerDefMemoryReservationMiB,
    lbToEcsPort: prdWebAppContainerConfig.lbToAppPort,
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl,
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl,
    ecsSecGrp: secGrpStack.ecsSecGrp,
    lbSecGrp: secGrpStack.lbSecGrp,
    webAppUrl: prdWebAppContainerConfig.webAppUrl,
    javaOpts: prdWebAppContainerConfig.javaOpts,
  });
  prdAppStack.addDependency(prdLbStack);
  prdAppStack.addDependency(prodPipelineStack);
}

// run the trap
loadConfig(appConfigFilename, appConfigCacheS3BucketName)
  .then(config => createStacks(config))
  .catch(err => console.log(err));
