# mcorpus aws infrastructure definition

Houses the [CDK](https://docs.aws.amazon.com/cdk/api/latest/) stacks defining the infrastructure used to run the mcorpus graphql server in aws.

## CDK app config file
A file named **mcorpus-cdk-app-config.json** must be present in the user's home directory.
This file is external to the project as it may contain security-sensitive information.
