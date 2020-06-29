#!/bin/bash
set -e

REPOSITORY_NAME="mcorpus-gql"
REPOSITORY_URI="524006177124.dkr.ecr.${AWS_DEFAULT_REGION:=us-west-2}.amazonaws.com/$REPOSITORY_NAME"
MCORPUS_VERSION="${project.version}"
MCORPUS_BUILD_TIMESTAMP=$(echo "${timestamp}" | sed "s/[^0-9]*//g")
DOCKER_TAG=${MCORPUS_VERSION}.${MCORPUS_BUILD_TIMESTAMP}

# docker build image
echo "Building docker image with tag: $DOCKER_TAG.."
docker build -t $REPOSITORY_URI:$DOCKER_TAG .

# docker push to AWS
# echo "Pushing docker image to AWS.."
# $(aws ecr get-login --region ${AWS_DEFAULT_REGION:=us-west-2} --no-include-email)
# docker push $REPOSITORY_URI:$DOCKER_TAG

echo "Done."
