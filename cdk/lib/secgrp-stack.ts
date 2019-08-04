import cdk = require('@aws-cdk/core');
import ec2 = require('@aws-cdk/aws-ec2');
import { SubnetType, IConnectable } from '@aws-cdk/aws-ec2';

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

  public readonly ecsSecGrp: ec2.SecurityGroup;

  public readonly lbSecGrp: ec2.SecurityGroup;
  
  constructor(scope: cdk.Construct, id: string, props: ISecGrpProps) {
    super(scope, id, props);

    // load balancer security group
    this.lbSecGrp = new ec2.SecurityGroup(this, 'sg-alb', {
      vpc: props.vpc,
      description: 'App load balancer security group.',
      allowAllOutbound: true   // Can be set to false
    });
    // rule: outside internet access only by TLS on 443
    this.lbSecGrp.addIngressRule(
      ec2.Peer.anyIpv4(), 
      ec2.Port.tcp(443), 
      'TLS/443 access from internet'
    );
    
    // ecs container security group
    this.ecsSecGrp = new ec2.SecurityGroup(this, 'ecs-container-seg-grp', {
      vpc: props.vpc, 
      securityGroupName: 'ecs-container-seg-grp', 
      description: 'ECS container security group',
      allowAllOutbound: true, 
    });    
    // rule: lb to ecs container traffic
    this.ecsSecGrp.addIngressRule(
      this.lbSecGrp, 
      ec2.Port.tcp(props.lbTrafficPort), 
      'lb to ecs container traffic'
    );
  }
}
