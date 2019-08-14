import cdk = require('@aws-cdk/core');
import ec2 = require('@aws-cdk/aws-ec2');
import { SecurityGroup, Peer, Port } from '@aws-cdk/aws-ec2';

/**
 * Security Group config properties.
 */
export interface ISecGrpProps extends cdk.StackProps {
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;

  readonly lbTrafficPort: number;
}

/**
 * Security Group Stack.
 */
export class SecGrpStack extends cdk.Stack {

  public readonly ecsSecGrp: SecurityGroup;

  public readonly lbSecGrp: SecurityGroup;

  public readonly codebuildSecGrp: SecurityGroup;
  
  constructor(scope: cdk.Construct, id: string, props: ISecGrpProps) {
    super(scope, id, props);

    // load balancer security group
    this.lbSecGrp = new SecurityGroup(this, 'sg-alb', {
      vpc: props.vpc,
      description: 'App load balancer security group.',
      allowAllOutbound: true   // Can be set to false
    });
    // rule: outside internet access only by TLS on 443
    this.lbSecGrp.addIngressRule(
      Peer.anyIpv4(), 
      Port.tcp(443), 
      'TLS/443 access from internet'
    );
    
    // ecs container security group
    this.ecsSecGrp = new SecurityGroup(this, 'ecs-container-sec-grp', {
      vpc: props.vpc, 
      securityGroupName: 'ecs-container-sec-grp', 
      description: 'ECS container security group',
      allowAllOutbound: true, 
    });    
    // rule: lb to ecs container traffic
    this.ecsSecGrp.addIngressRule(
      this.lbSecGrp, 
      Port.tcp(props.lbTrafficPort), 
      'lb to ecs container traffic'
    );

    // codebuild security group
    this.codebuildSecGrp = new SecurityGroup(this, 'codebuild-sec-grp', {
      vpc: props.vpc, 
      securityGroupName: 'codebuild-sec-grp', 
      description: 'Codebuild security group',
      allowAllOutbound: true, 
    });

    // stack output
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
