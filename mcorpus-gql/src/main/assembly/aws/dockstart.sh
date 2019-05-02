#!/bin/bash
set -e

# mcorpus Docker entrypoint bash shell script.

export MCORPUS_DB_DATA_SOURCE_CLASS_NAME=org.postgresql.ds.PGSimpleDataSource
# export MCORPUS_DB_URL=$(aws ssm get-parameters --region us-west-2 --names mcorpusDbUrl --with-decryption --query Parameters[0].Value)
# export MCORPUS_JWT_SALT=$(aws ssm get-parameters --region us-west-2 --names jwtSalt --with-decryption --query Parameters[0].Value)
export MCORPUS_DB_URL='jdbc:postgresql://localbox:5432/mcorpus?user=mcweb&password=aPvumfTgxv2A/butD/9WrApiF93swos890X5YFOKwQFZ&ssl=false'
export MCORPUS_JWT_SALT='4E521B8861508F17C423162845BBC5E916C4FDE82A73B6E2BE292DDA7AEE6FF6'
export MCORPUS_JWT_TTL_IN_MILLIS=172800000
export MCORPUS_JWT_STATUS_CACHE_TIMEOUT_IN_MINUTES=10
export MCORPUS_JWT_STATUS_CACHE_MAX_SIZE=60
export MCORPUS_COOKIE_SECURE=true
export RATPACK_SERVER__DEVELOPMENT=false
export RATPACK_SERVER__PORT=5150
export RATPACK_SERVER__PUBLIC_ADDRESS=https://www.mcorpus-aws.net

JAVA_OPTS=(
  '-Xms100M'
  '-Xmx1000M'
  '-Djava.net.preferIPv4Stack=true'
  '-Dlog4j.configurationFile=log4j2-aws.xml'
  '-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector'
  '-cp log4j-api-2.11.1.jar:log4j-core-2.11.1.jar:log4j-slf4j-impl-2.11.1.jar:disruptor-3.4.2.jar'
  '-jar '
)

cd /webapp \
  && exec java ${JAVA_OPTS[*]} mcorpus-gql-server.jar
