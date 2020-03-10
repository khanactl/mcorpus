import { IVpc, SecurityGroup } from '@aws-cdk/aws-ec2';
import { CfnOutput, Construct, Tag } from '@aws-cdk/core';
import { BaseStack, iname, IStackProps } from './cdk-native';

/**
 * Security Group config properties.
 */
export interface ISecGrpProps extends IStackProps {
  /**
   * The VPC ref
   */
  readonly vpc: IVpc;
}

/**
 * Security Group Stack.
 */
export class SecGrpStack extends BaseStack {
  // public readonly dbSecGrp: SecurityGroup;

  public readonly dbBootstrapSecGrp: SecurityGroup;

  public readonly lbSecGrp: SecurityGroup;

  public readonly ecsSecGrp: SecurityGroup;

  public readonly codebuildSecGrp: SecurityGroup;

  constructor(scope: Construct, id: string, props: ISecGrpProps) {
    super(scope, id, props);

    /* :( NO WORK - error - cyclic reference
    // db security group
    const sgDbInstNme = iname('db-sec-grp', props);
    this.dbSecGrp = new SecurityGroup(this, sgDbInstNme, {
      vpc: props.vpc,
      description: 'Db security group.',
      allowAllOutbound: true,
      securityGroupName: sgDbInstNme,
    });
    this.dbSecGrp.node.applyAspect(new cdk.Tag('Name', sgDbInstNme));
    */

    // db bootstrap security group
    const sgDbBootstrapInstNme = iname('dbbootstrap-sec-grp', props);
    this.dbBootstrapSecGrp = new SecurityGroup(this, sgDbBootstrapInstNme, {
      vpc: props.vpc,
      description: 'Db bootstrap security group.',
      allowAllOutbound: true,
      securityGroupName: sgDbBootstrapInstNme,
    });
    this.dbBootstrapSecGrp.node.applyAspect(new Tag('Name', sgDbBootstrapInstNme));

    // load balancer security group
    const sgLbInstNme = iname('lb-sec-grp', props);
    this.lbSecGrp = new SecurityGroup(this, sgLbInstNme, {
      vpc: props.vpc,
      description: 'App load balancer security group.',
      allowAllOutbound: true,
      securityGroupName: sgLbInstNme,
    });
    this.lbSecGrp.node.applyAspect(new Tag('Name', sgLbInstNme));

    // ecs container security group
    const sgEcsInstNme = iname('ecs-container-sec-grp', props);
    this.ecsSecGrp = new SecurityGroup(this, sgEcsInstNme, {
      vpc: props.vpc,
      description: 'ECS container security group',
      allowAllOutbound: true,
      securityGroupName: sgEcsInstNme,
    });
    this.ecsSecGrp.node.applyAspect(new Tag('Name', sgEcsInstNme));

    // codebuild security group
    const sgCodebuildInstNme = iname('codebuild-sec-grp', props);
    this.codebuildSecGrp = new SecurityGroup(this, sgCodebuildInstNme, {
      vpc: props.vpc,
      description: 'Codebuild security group',
      allowAllOutbound: true,
      securityGroupName: sgCodebuildInstNme,
    });
    this.codebuildSecGrp.node.applyAspect(new Tag('Name', sgCodebuildInstNme));

    // stack output
    // new cdk.CfnOutput(this, 'DbSecurityGroup', { value: this.dbSecGrp.securityGroupName });
    new CfnOutput(this, 'DbBootstrapSecurityGroup', { value: this.dbBootstrapSecGrp.securityGroupName });
    new CfnOutput(this, 'LoadBalancerSecurityGroup', { value: this.lbSecGrp.securityGroupName });
    new CfnOutput(this, 'ECSContainerSecurityGroup', { value: this.ecsSecGrp.securityGroupName });
    new CfnOutput(this, 'CodebuildSecurityGroup', { value: this.codebuildSecGrp.securityGroupName });
  }
}
