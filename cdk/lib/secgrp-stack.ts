import cdk = require('@aws-cdk/core');
import { IStackProps, BaseStack } from './cdk-native'
import ec2 = require('@aws-cdk/aws-ec2');
import { SecurityGroup, Peer, Port } from '@aws-cdk/aws-ec2';

/**
 * Security Group config properties.
 */
export interface ISecGrpProps extends IStackProps {
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;
}

/**
 * Security Group Stack.
 */
export class SecGrpStack extends BaseStack {

  public readonly dbBootstrapSecGrp: SecurityGroup;

  public readonly lbSecGrp: SecurityGroup;

  public readonly ecsSecGrp: SecurityGroup;

  public readonly codebuildSecGrp: SecurityGroup;

  constructor(scope: cdk.Construct, props: ISecGrpProps) {
    super(scope, 'SecGrp', props);

    // db bootstrap security group
    const sgDbBootstrapInstNme = this.iname('dbbootstrap-sec-grp', props);
    this.dbBootstrapSecGrp = new SecurityGroup(this, sgDbBootstrapInstNme, {
      vpc: props.vpc,
      description: 'Db bootstrap security group.',
      allowAllOutbound: true,
      securityGroupName: sgDbBootstrapInstNme,
    });
    this.dbBootstrapSecGrp.node.applyAspect(new cdk.Tag('Name', sgDbBootstrapInstNme));

    // load balancer security group
    const sgLbInstNme = this.iname('lb-sec-grp', props);
    this.lbSecGrp = new SecurityGroup(this, sgLbInstNme, {
      vpc: props.vpc,
      description: 'App load balancer security group.',
      allowAllOutbound: true,
      securityGroupName: sgLbInstNme,
    });
    this.lbSecGrp.node.applyAspect(new cdk.Tag('Name', sgLbInstNme));

    // ecs container security group
    const sgEcsInstNme = this.iname('ecs-container-sec-grp', props);
    this.ecsSecGrp = new SecurityGroup(this, sgEcsInstNme, {
      vpc: props.vpc,
      description: 'ECS container security group',
      allowAllOutbound: true,
      securityGroupName: sgEcsInstNme,
    });
    this.ecsSecGrp.node.applyAspect(new cdk.Tag('Name', sgEcsInstNme));

    // codebuild security group
    const sgCodebuildInstNme = this.iname('codebuild-sec-grp', props);
    this.codebuildSecGrp = new SecurityGroup(this, sgCodebuildInstNme, {
      vpc: props.vpc,
      description: 'Codebuild security group',
      allowAllOutbound: true,
      securityGroupName: sgCodebuildInstNme,
    });
    this.codebuildSecGrp.node.applyAspect(new cdk.Tag('Name', sgCodebuildInstNme));

    // stack output
    new cdk.CfnOutput(this, 'DbBootstrapSecurityGroup', { value:
      this.dbBootstrapSecGrp.securityGroupName
    });
    new cdk.CfnOutput(this, 'LoadBalancerSecurityGroup', { value:
      this.lbSecGrp.securityGroupName
    });
    new cdk.CfnOutput(this, 'ECSContainerSecurityGroup', { value:
      this.ecsSecGrp.securityGroupName
    });
    new cdk.CfnOutput(this, 'CodebuildSecurityGroup', { value:
      this.codebuildSecGrp.securityGroupName
    });
  }
}
