#!/bin/bash

# create the mcorpus-gql ECR repo.

# create ecr
aws ecr create-repository --repository-name mcorpus-gql

# upload docker image from maven generated local asset dir
cd ../mcorpus-gql/target
chmod u+x dockerbuild.sh
./dockerbuild.sh
