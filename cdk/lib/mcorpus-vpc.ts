import cdk = require('@aws-cdk/cdk');
import ec2 = require('@aws-cdk/aws-ec2');
import elb = require('@aws-cdk/aws-elasticloadbalancingv2');
import { SubnetType } from '@aws-cdk/aws-ec2';

export class McorpusVpc extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // The code that defines your stack goes here
    // *** VPC ***

    const vpc = new ec2.Vpc(this, 'McorpusVpc', { 
      cidr: '10.0.0.0/16',
      natGateways: 1,
      subnetConfiguration: [
        {
          cidrMask: 26,
          name: 'Public',
          subnetType: SubnetType.Public
        },
        {
          cidrMask: 26,
          name: 'Private',
          subnetType: SubnetType.Private
        }
      ]
    });

    // *** EC2 instance ***

    // AMI (Amazon Linux - amzn2-ami-hvm-2.0.20190508-x86_64-gp2)
    const amiId = 'ami-0cb72367e98845d43';

    // ec2 instance
    new ec2.CfnInstance(this, 'mcorpus-ec2-inst', {
      imageId: amiId,
      instanceType: 't2.micro',
      keyName: 'mcorpus-ec2-kp',   // TODO: generate ssh key rather than reference existing key?
      monitoring: false,
      networkInterfaces: [
        {
          deviceIndex: '0',
          associatePublicIpAddress: true,
          subnetId: vpc.publicSubnets[0].subnetId
        }
      ]
    });

    // *** load balancer ***
    
    const lb = new elb.NetworkLoadBalancer(this, 'mcorpus-network-lb', {
      vpc,
      internetFacing: true,
      
    });
    const lbListener = lb.addListener('TLSListener', {
      port: 443,
      protocol: elb.Protocol.Tls,
      sslPolicy: elb.SslPolicy.Recommended,
      certificates: [
        {
          // TODO parameterize load balancer cert - this one is the DEV wildcard cert
          certificateArn: 'arn:aws:acm:us-west-2:524006177124:certificate/8c7ea4bb-f2fd-4cdb-b85c-184d2a864b0a'
        }
      ]
    });
    const lbTargets = new elb.NetworkTargetGroup(this, 'lbTargetGroup', {
      vpc: vpc,
      port: 5150,
      targetGroupName: 'mcorpus-ec2-tgtgrp',
      targetType: elb.TargetType.Ip,
      healthCheck: {
        path: '/health',
        protocol: elb.Protocol.Http,
        intervalSecs: 30
      }
    });
    lbListener.addTargetGroups('mcorpusEc2TargetGrp', lbTargets);

    // *** security groups ***
    
    const cidrHome = new ec2.CidrIPv4('73.162.90.1/23');
    const cidrWork = new ec2.CidrIPv4('128.48.24.0/21');

    const sshPort = new ec2.TcpPort(22);
    const dbPort = new ec2.TcpPort(5432);
    
    const sgWebSrvr = new ec2.SecurityGroup(this, 'mcorpus-websrvr-sg', {
      vpc,
      groupName: 'mcorpus-websrvr-sg',
      description: 'Mcorpus web server security group',
      allowAllOutbound: true
    });
    // TODO add ingress rule from network load balancer by private ip address of matching network interface
    // sgWebSrvr.addIngressRule()
    // ssh access
    sgWebSrvr.addIngressRule(cidrHome, sshPort, 'ssh from home');
    sgWebSrvr.addIngressRule(cidrWork, sshPort, 'ssh from work');

    // db SG
    const sgDb = new ec2.SecurityGroup(this, 'mcorpus-db-sg', {
      vpc,
      groupName: 'mcorpus-db-sg',
      description: 'Mcorpus db security group',
      allowAllOutbound: true
    });
    sgDb.addIngressRule(cidrHome, dbPort, 'from home');
    sgDb.addIngressRule(cidrWork, dbPort, 'from work');
    // web srvr to db connection
    sgDb.connections.allowFrom(sgWebSrvr, dbPort, 'from web server');
  }
}
