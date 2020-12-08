#!/bin/bash
set -e

OPTIND=1

del_cdk_stacks=false
del_s3_buckets=false
del_cloudwatch_logs=false

show_help() {
  echo "-c  delete cdk stacks"
  echo "-s  delete s3 buckets"
  echo "-l  delete cloudwatch logs"
  echo "-a  delete all things"
}

# grab input args
while getopts "h?csla" opt; do
  case "$opt" in
    h|\?)
      show_help
      exit 0
      ;;
    c) del_cdk_stacks=true ;;
    s) del_s3_buckets=true ;;
    l) del_cloudwatch_logs=true; ;;
    a)
      del_cdk_stacks=true;
      del_s3_buckets=true;
      del_cloudwatch_logs=true;
      ;;
    *) echo "Unknown parameter passed: $1"; exit 1 ;;
  esac
done

# DEV env destroy
if [ $del_cdk_stacks = true ]
then
  cdk destroy mcorpus-pipeline-dev --force;
  cdk destroy mcorpus-lb-dev --force;
  cdk destroy mcorpus-waf-dev --force;
  cdk destroy mcorpus-ecs-cluster-dev --force;
  cdk destroy mcorpus-db-bootstrap-dev --force;
  cdk destroy mcorpus-db-dev --force;
  cdk destroy mcorpus-vpc-dev --force;
fi

# empty any cdk generated s3 data and buckets (warning)
if [ $del_s3_buckets = true ]
then
  echo 'nixing S3 buckets..';
  S3_DEL_STACKS=(`aws s3 ls | grep -v 'cdk-\|mcorpus-app-config\|mcorpus-db-data\|elasticbeanstalk' | cut -c 21-`);
  for i in "${S3_DEL_STACKS[@]}"
  do
    echo "emptying $i";
    # aws s3 rm --dryrun --recursive s3://$i
    aws s3 rm --recursive s3://$i;
    echo "deleting $i";
    aws s3api delete-bucket --bucket $i;
  done;
  echo 's3 buckets deleted'
fi

# clear out CloudWatch logs
if [ $del_cloudwatch_logs = true ]
then
  echo 'nixing CloudWatch logs groups..';
  CW_LOGS_JSON=(`aws logs describe-log-groups | jq -r .logGroups[].logGroupName`);
  for i in "${CW_LOGS_JSON[@]}"
  do
    echo "deleting $i";
    aws logs delete-log-group --log-group-name $i;
  done;
fi