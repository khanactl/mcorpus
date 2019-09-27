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
  /**
   * The load balancer to app ecs container (traffic) port.
   */
  readonly lbTrafficPort: number;
}

/**
 * Security Group Stack.
 */
export class SecGrpStack extends cdk.Stack {

  public readonly dbBootstrapSecGrp: SecurityGroup;
  
  public readonly lbSecGrp: SecurityGroup;

  public readonly ecsSecGrp: SecurityGroup;

  public readonly codebuildSecGrp: SecurityGroup;

  constructor(scope: cdk.Construct, id: string, props: ISecGrpProps) {
    super(scope, id, props);

    // db bootstrap security group
    this.dbBootstrapSecGrp = new SecurityGroup(this, 'sg-dbbootstrap', {
      vpc: props.vpc,
      description: 'Db bootstrap security group.',
      allowAllOutbound: true, 
      securityGroupName: 'db-bootstrap-sec-grp', 
    });

    // load balancer security group
    this.lbSecGrp = new SecurityGroup(this, 'sg-alb', {
      vpc: props.vpc,
      description: 'App load balancer security group.',
      allowAllOutbound: true, 
      securityGroupName: 'load-balancer-sec-grp', 
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
      description: 'ECS container security group',
      allowAllOutbound: true, 
      securityGroupName: 'ecs-container-sec-grp', 
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
      description: 'Codebuild security group',
      allowAllOutbound: true, 
      securityGroupName: 'codebuild-sec-grp', 
    });

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
