#!/usr/bin/env node
import { App } from '@aws-cdk/core';
import 'source-map-support/register';
import { AppStack, AppStackRootProps } from '../lib/app-stack';
import { AppEnv, cdkAppConfig, ICdkAppConfig, iname, loadConfig } from '../lib/cdk-native';
import { ClusterStack } from '../lib/cluster-stack';
import { DbBootstrapStack } from '../lib/db-bootstrap-stack';
import { DbDataStack } from '../lib/db-data-stack';
import { DbStack } from '../lib/db-stack';
import { DevPipelineStack } from '../lib/dev-pipeline-stack';
import { LbStack, LbStackRootProps } from '../lib/lb-stack';
import { MetricsStack, MetricsStackRootProps } from '../lib/metrics-stack';
import { SecGrpStack } from '../lib/secgrp-stack';
import { VpcStack } from '../lib/vpc-stack';

const app = new App();

/**
 * The expected name of the required cdk app config json file.
 *
 * This JSON object (assumed to adhere to an expected structure)
 * provides the needed input to all instantiated cdk stacks herein.
 */
const cdkAppConfigFilename = 'mcorpus-cdk-app-config.json';

/**
 * The S3 bucket name in the default AWS account holding a cached copy of
 * the app config file.
 *
 * This is used when no local app config file is found.
 *
 * This bucket's life-cycle is **not managed** by these CDK stacks.
 * That is, it is assumed to *pre-exist*.
 */
const cdkAppConfigCacheS3BucketName = 'mcorpus-app-config';

async function configTransform(jsonConfig: any): Promise<ICdkAppConfig> {
  // DEV
  return cdkAppConfig(
    AppEnv.DEV,
    cdkAppConfigFilename,
    cdkAppConfigCacheS3BucketName,
    jsonConfig
  );
}

async function generate(cdkAppConfig: ICdkAppConfig): Promise<void> {
  // VPC
  const vpcStack = new VpcStack(app, {
    appName: cdkAppConfig.appName,
    appEnv: cdkAppConfig.appEnv,
    env: cdkAppConfig.awsEnv,
    tags: cdkAppConfig.appEnvStackTags,
    maxAzs: cdkAppConfig.networkConfig.maxAzs,
    cidr: cdkAppConfig.networkConfig.cidr,
  });
  const secGrpStack = new SecGrpStack(app, {
    appName: cdkAppConfig.appName,
    appEnv: cdkAppConfig.appEnv,
    env: cdkAppConfig.awsEnv,
    tags: cdkAppConfig.appEnvStackTags,
    vpc: vpcStack.vpc,
  });

  // RDS
  const dbStack = new DbStack(app, {
    appName: cdkAppConfig.appName,
    appEnv: cdkAppConfig.appEnv,
    env: cdkAppConfig.awsEnv,
    tags: cdkAppConfig.appEnvStackTags,
    vpc: vpcStack.vpc,
    // dbSecGrp: secGrpStack.dbSecGrp,
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp,
    ecsSecGrp: secGrpStack.ecsSecGrp,
    codebuildSecGrp: secGrpStack.codebuildSecGrp,
    dbName: cdkAppConfig.dbConfig.dbName,
    dbMasterUsername: cdkAppConfig.dbConfig.dbMasterUsername,
  });
  const dbBootstrapStack = new DbBootstrapStack(app, {
    appName: cdkAppConfig.appName,
    appEnv: cdkAppConfig.appEnv,
    env: cdkAppConfig.awsEnv,
    tags: cdkAppConfig.appEnvStackTags,
    vpc: vpcStack.vpc,
    dbBootstrapSecGrp: secGrpStack.dbBootstrapSecGrp,
    dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn,
    targetRegion: dbStack.region,
  });
  const dbDataStack = new DbDataStack(app, {
    appName: cdkAppConfig.appName,
    appEnv: cdkAppConfig.appEnv,
    env: cdkAppConfig.awsEnv,
    tags: cdkAppConfig.appEnvStackTags,
    vpc: vpcStack.vpc,
    dbDataSecGrp: secGrpStack.dbBootstrapSecGrp,
    dbJsonSecretArn: dbStack.dbInstanceJsonSecret.secretArn,
    // s3KmsEncKeyArn: appConfig.ssmKmsArn
  });
  dbDataStack.addDependency(dbBootstrapStack);

  // ECS cluster
  const clusterStack = new ClusterStack(app, {
    appName: cdkAppConfig.appName,
    appEnv: cdkAppConfig.appEnv,
    env: cdkAppConfig.awsEnv,
    tags: cdkAppConfig.appEnvStackTags,
    vpc: vpcStack.vpc,
  });
  clusterStack.addDependency(secGrpStack);

  const devPipelineStack = new DevPipelineStack(app, {
    ecrRepoName: cdkAppConfig.ecrConfig.name,
    appName: cdkAppConfig.appName,
    appEnv: cdkAppConfig.appEnv,
    env: cdkAppConfig.awsEnv,
    tags: cdkAppConfig.appEnvStackTags,
    vpc: vpcStack.vpc,
    // appRepository: ecrStack.appRepository,
    codebuildSecGrp: secGrpStack.codebuildSecGrp,
    appConfigCacheS3BucketName: cdkAppConfig.cdkAppConfigCacheS3BucketName,
    appConfigFilename: cdkAppConfig.cdkAppConfigFilename,
    ssmImageTagParamName: cdkAppConfig.cicdConfig.ssmImageTagParamName,
    githubOwner: cdkAppConfig.gitHubRepoRef.owner,
    githubRepo: cdkAppConfig.gitHubRepoRef.repo,
    githubOauthTokenSecretArn: cdkAppConfig.gitHubRepoRef.oauthTokenSecretArn,
    githubOauthTokenSecretJsonFieldName: cdkAppConfig.gitHubRepoRef.oauthTokenSecretJsonFieldName,
    gitBranchName: cdkAppConfig.cicdConfig.gitBranchName,
    triggerOnCommit: cdkAppConfig.cicdConfig.triggerOnCommit,
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl,
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl,
    appDeployApprovalEmails: cdkAppConfig.cicdConfig.appDeployApprovalEmails,
    onBuildFailureEmails: cdkAppConfig.cicdConfig.onBuildFailureEmails,
    cdkDevLbStackName: iname(LbStackRootProps.rootStackName, cdkAppConfig),
    cdkDevAppStackName: iname(AppStackRootProps.rootStackName, cdkAppConfig),
    cdkDevMetricsStackName: iname(MetricsStackRootProps.rootStackName, cdkAppConfig),
  });
  // devPipelineStack.addDependency(ecrStack);
  devPipelineStack.addDependency(secGrpStack);
  devPipelineStack.addDependency(clusterStack);

  const lbStack = new LbStack(app, {
    appName: cdkAppConfig.appName,
    appEnv: cdkAppConfig.appEnv,
    env: cdkAppConfig.awsEnv,
    tags: cdkAppConfig.appEnvStackTags,
    vpc: vpcStack.vpc,
    lbSecGrp: secGrpStack.lbSecGrp,
    sslCertArn: cdkAppConfig.webAppContainerConfig.tlsCertArn,
    awsHostedZoneId: cdkAppConfig.webAppContainerConfig.dnsConfig.awsHostedZoneId,
    publicDomainName: cdkAppConfig.webAppContainerConfig.dnsConfig.publicDomainName,
  });

  const appStack = new AppStack(app, {
    appName: cdkAppConfig.appName,
    appEnv: cdkAppConfig.appEnv,
    env: cdkAppConfig.awsEnv,
    tags: cdkAppConfig.appEnvStackTags,
    vpc: vpcStack.vpc,
    cluster: clusterStack.cluster,
    // ecrRepoName: cdkAppConfig.ecrConfig.name,
    appImage: devPipelineStack.appBuiltImage,
    lbListener: lbStack.lbListener,
    taskdefCpu: cdkAppConfig.webAppContainerConfig.taskdefCpu,
    taskdefMemoryLimitMiB: cdkAppConfig.webAppContainerConfig.taskdefMemLimitInMb,
    containerDefMemoryLimitMiB: cdkAppConfig.webAppContainerConfig.containerDefMemoryLimitInMb,
    containerDefMemoryReservationMiB: cdkAppConfig.webAppContainerConfig.containerDefMemoryReservationInMb,
    lbToEcsPort: cdkAppConfig.webAppContainerConfig.lbToAppPort,
    ssmJdbcUrl: dbBootstrapStack.ssmJdbcUrl,
    ssmJdbcTestUrl: dbBootstrapStack.ssmJdbcTestUrl,
    ecsSecGrp: secGrpStack.ecsSecGrp,
    lbSecGrp: secGrpStack.lbSecGrp,
    javaOpts: cdkAppConfig.appConfig.javaOpts,
    devFlag: cdkAppConfig.appConfig.devFlag,
    publicAddress: cdkAppConfig.appConfig.publicAddress,
    dbDataSourceClassName: cdkAppConfig.appConfig.dbDataSourceClassName,
    rstTtlInMinutes: cdkAppConfig.appConfig.rstTtlInMinutes,
    jwtTtlInMinutes: cdkAppConfig.appConfig.jwtTtlInMinutes,
    jwtRefreshTokenTtlInMinutes: cdkAppConfig.appConfig.jwtRefreshTokenTtlInMinutes,
    jwtStatusTimeoutInMinutes: cdkAppConfig.appConfig.jwtStatusTimeoutInMinutes,
    jwtStatusCacheMaxSize: cdkAppConfig.appConfig.jwtStatusCacheMaxSize,
    metricsOn: cdkAppConfig.appConfig.metricsOn,
    graphiql: cdkAppConfig.appConfig.graphiql,
    cookieSecure: cdkAppConfig.appConfig.cookieSecure,
    httpClientOrigin: cdkAppConfig.appConfig.httpClientOrigin,
  });
  appStack.addDependency(lbStack);
  appStack.addDependency(devPipelineStack);

  const metricsStack = new MetricsStack(app, {
    appName: cdkAppConfig.appName,
    appEnv: cdkAppConfig.appEnv,
    env: cdkAppConfig.awsEnv,
    tags: cdkAppConfig.appEnvStackTags,
    dbInstanceRef: dbStack.dbInstance,
    ecsClusterRef: clusterStack.cluster,
    fargateSvcRef: appStack.fargateSvc,
    appLoadBalancerRef: lbStack.appLoadBalancer,
    onMetricAlarmEmails: cdkAppConfig.metricsConfig.onAlarmEmailList,
  });
  metricsStack.addDependency(devPipelineStack);
  metricsStack.addDependency(appStack);
}

// run the trap
loadConfig(cdkAppConfigFilename, cdkAppConfigCacheS3BucketName)
  .then(json => configTransform(json))
  .then(config => generate(config))
  .catch(err => console.log(err));
