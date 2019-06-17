#!/bin/bash
set -e

REPOSITORY_URI='524006177124.dkr.ecr.us-west-2.amazonaws.com/mcorpus-gql'
MCORPUS_VERSION='${project.version}'

# docker build image
echo "Building docker image with version tag: $MCORPUS_VERSION.."
docker build -t $REPOSITORY_URI:$MCORPUS_VERSION -t $REPOSITORY_URI:latest .

# docker push to AWS
echo "Pushing docker image to AWS.."
$(aws ecr get-login --region $AWS_DEFAULT_REGION --no-include-email)
docker push $REPOSITORY_URI:$MCORPUS_VERSION
docker push $REPOSITORY_URI:latest

echo "Done."
