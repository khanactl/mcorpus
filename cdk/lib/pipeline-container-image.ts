import cdk = require("@aws-cdk/core");
import ecs = require("@aws-cdk/aws-ecs");
import ecr = require("@aws-cdk/aws-ecr");

export class PipelineContainerImage extends ecs.ContainerImage {
  public readonly imageName: string;
  private readonly repository: ecr.IRepository;
  private parameter?: cdk.CfnParameter;

  constructor(repository: ecr.IRepository) {
    super();
    // this.imageName = repository.repositoryUriForTag(new cdk.Token(() => this.parameter!.stringValue).toString());
    this.imageName = repository.repositoryUriForTag(
      cdk.Lazy.stringValue({ produce: () => this.parameter!.valueAsString })
    );
    this.repository = repository;
  }

  public bind(containerDefinition: ecs.ContainerDefinition): ecs.ContainerImageConfig {
    this.repository.grantPull(
      containerDefinition.taskDefinition.obtainExecutionRole()
    );
    this.parameter = new cdk.CfnParameter(
      containerDefinition,
      "PipelineParam",
      {
        type: "String"
      }
    );
    return {
      imageName: this.imageName
    };
  }

  public get paramName(): string {
    // return cdk.Token.asString(this.parameter!.logicalId).toString();
    return cdk.Lazy.stringValue({ produce: () => this.parameter!.logicalId });
  }

  public toRepositoryCredentialsJson():
    | ecs.CfnTaskDefinition.RepositoryCredentialsProperty
    | undefined {
    return undefined;
  }
}
