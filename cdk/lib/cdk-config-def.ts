#!/usr/bin/env node

/**
 * The supported application environments.
 */
export enum AppEnv {
  /** development (staging / non-production) */
  DEV = 'DEV', 
  /** production */
  PRD = 'PRD' 
}

/**
 * GitHub repository reference definition.
 */
export interface IGitHubRepoRef {
  /**
   * The GitHub user that owns the target repository.
   */
  readonly githubOwner: string;
  /**
   * The GitHub repository name.
   */
  readonly githubRepo: string;
  /**
   * The name of the SecretsManager entry holding the GitHub OAuth access token.
   */
  readonly githubOauthTokenSecretName: string;
}

/**
 * DNS configuration definition.
 */
export interface IDnsConfig {
  /**
   * DNS domain name (optional).
   * 
   * Connect the load balancer to an AWS Route53 hosted zone registered domain name.
   * 
   * This is for inbound public web app traffic.
   */
  readonly publicDomainName?: string;
  /**
   * The AWS Route53 Hosted Zone Id (optional).
   */
  readonly awsHostedZoneId?: string;
}

/**
 * AWS CDK application configuration definition.
 * 
 * The application knobs for a specific application instance/deployment.
 */
export interface ICdkAppConfig {
  /**
   * The application name.
   */
  readonly appName: string;
  /**
   * The ascribed appliction environment.
   */
  readonly appEnv: AppEnv;

  /**
   * The needed Git source control repository info.
   */
  readonly gitRepoRef: IGitHubRepoRef;
  /**
   * The branch name to associate with the CICD pipeline.
   */
  readonly gitBranchName: string;
  
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
  readonly instanceAttrs: { [key: string]: string };

  /**
   * The 'traffic' port for the front-facing load balancer to the 
   * web application container.
   */
  readonly lbToAppPort: number;
  /**
   * The ARN of the TLS certificate to use at the public-facing app load balancer 
   * to suport https connections.
   */
  readonly tlsCertArn: string;
  /**
   * The public-facing web app url address.
   */
  readonly webAppUrl: string;
  /**
   * JAVA_OPTS to use for the web app docker container.
   */
  readonly javaOpts: string;
  /**
   * The KMS ARN to use for generating SSM secure parameters.
   */
  // ssmKmsArn(): string;
  /**
   * The app DNS configuration.
   */
  readonly dnsConfig: IDnsConfig;
  /**
   * The CodeBuild buildspec non-path file name.
   */
  readonly buildspecFilename: string;
  /**
   * The email addresses of the people that approve application deployments.
   */
  readonly appDeployApprovalEmails: string[];
}

