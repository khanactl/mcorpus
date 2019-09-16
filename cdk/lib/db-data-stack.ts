import cfn = require('@aws-cdk/aws-cloudformation');
import lambda = require('@aws-cdk/aws-lambda');
import ssm = require('@aws-cdk/aws-ssm');
import iam = require('@aws-cdk/aws-iam');
import cdk = require('@aws-cdk/core');
import path = require('path');
import { IVpc, SubnetType, ISecurityGroup } from '@aws-cdk/aws-ec2';
import { IStringParameter } from '@aws-cdk/aws-ssm';
import { PolicyStatement } from '@aws-cdk/aws-iam';
import fs = require('fs');
import archiver = require('archiver');

export interface IDbDataProps extends cdk.StackProps {
  /**
   * The VPC ref.
   */
  readonly vpc: IVpc;
  /**
   * The Db bootstrap security group.
   */
  readonly dbBootstrapSecGrp: ISecurityGroup;
  /**
   * rds db instance secrets
   */
  readonly dbJsonSecretArn: string;
}

export class DbDataStack extends cdk.Stack {

  constructor(scope: cdk.Construct, id: string, props: IDbDataProps) {
    super(scope, id);

    
  }

}