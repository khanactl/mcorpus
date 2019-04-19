#!/bin/bash

dbUrl=$(aws ssm get-parameters --region us-west-2 --names mcorpusDbUrl --with-decryption --query Parameters[0].Value)
dbUrl=`echo $dbUrl | sed -e 's/^"//' -e 's/"$//'`

jwtSalt=$(aws ssm get-parameters --region us-west-2 --names jwtSalt --with-decryption --query Parameters[0].Value)
jwtSalt=`echo $jwtSalt | sed -e 's/^"//' -e 's/"$//'`

export MCORPUS_DB_DATA_SOURCE_CLASS_NAME=org.postgresql.ds.PGSimpleDataSource
export MCORPUS_DB_URL=$dbUrl
export MCORPUS_JWT_SALT=$jwtSalt
export MCORPUS_JWT_TTL_IN_MILLIS=172800000
export MCORPUS_JWT_STATUS_CACHE_TIMEOUT_IN_MINUTES=10
export MCORPUS_JWT_STATUS_CACHE_MAX_SIZE=60
export MCORPUS_COOKIE_SECURE=true
export RATPACK_SERVER__DEVELOPMENT=false
export RATPACK_SERVER__PORT=5150
export RATPACK_SERVER__PUBLIC_ADDRESS=https://www.mcorpus-aws.net

cwd=$(pwd)
cd /home/ec2-user/webapp
$cwd/run.sh >/dev/null 2>&1 &
