import cdk = require('@aws-cdk/core');
import ec2 = require('@aws-cdk/aws-ec2');
import iam = require('@aws-cdk/aws-iam');

/**
 * IAM Stack config properties.
 */
export interface IIAMProps extends cdk.StackProps {
  /**
   * The VPC ref
   */
  // readonly vpc: ec2.IVpc;
}

/**
 * IAM Stack.
 */
export class IAMStack extends cdk.Stack {
  
  // private readonly vpc: ec2.IVpc;
  public readonly ecsTaskExecutionRole: iam.Role;
  
  constructor(scope: cdk.Construct, id: string, props: IIAMProps) {
    super(scope, id, props);

    // this.vpc = props.vpc;

    // ECS/Fargate task execution role
    const ssmAccess = new iam.PolicyStatement({
      actions: [ 
        'kms:Decript', 
        'kms:DescribeKey',
        'kms:DescribeParamters',
        'kms:GetParamters',
        'ssm:GetParameter',
        'ssm:GetParameters'
      ],
      resources: [
        "arn:aws:ssm:us-west-2:524006177124:parameter/mcorpusDbUrl",
        "arn:aws:ssm:us-west-2:524006177124:parameter/jwtSalt",
        "arn:aws:kms:us-west-2:524006177124:key/c66b3f26-8480-40f1-95a1-6abf58f2aedd"
      ],
    });
    this.ecsTaskExecutionRole = new iam.Role(this, 'ecsTaskExecutionRole', {
      assumedBy: new iam.ServicePrincipal('ecs-tasks.amazonaws.com')
    });
    this.ecsTaskExecutionRole.addManagedPolicy({
      managedPolicyArn: 'arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy'
    });
    ssmAccess.effect = iam.Effect.ALLOW;
    this.ecsTaskExecutionRole.addToPolicy(ssmAccess);

  }
}
