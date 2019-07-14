#!/bin/bash
set -e

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

JAVA_OPTS=(
  '-server'
  '-Xms64M'
  '-Xmx768M'
  '-Djava.net.preferIPv4Stack=true'
  '-Dlog4j.configurationFile=log4j2-aws.xml'
  '-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector'
  '-cp log4j-api-2.12.0.jar:log4j-core-2.12.0.jar:log4j-slf4j-impl-2.12.0.jar:disruptor-3.4.2.jar'
)

cd /home/ec2-user/webapp

exec java ${JAVA_OPTS[*]} -jar mcorpus-gql-server.jar >/dev/null 2>&1 &

