import cdk = require('@aws-cdk/core');
import ec2 = require('@aws-cdk/aws-ec2');

/**
 * Db Stack config properties.
 */
export interface IDbProps extends cdk.StackProps {
  /**
   * The VPC ref
   */
  readonly vpc: ec2.IVpc;
}

/**
 * Db Stack.
 */
export class DbStack extends cdk.Stack {
  
  // private readonly vpc: ec2.IVpc;
  
  constructor(scope: cdk.Construct, id: string, props: IDbProps) {
    super(scope, id, props);

    // this.vpc = props.vpc;


  }
}
