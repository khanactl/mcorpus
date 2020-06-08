#!/bin/bash
set -e

# mcorpus Docker startup script for local dev env.

# expected input args:
#  1:  <dockerImageId>

JAVA_OPTS=(
  '-server'
  '-Xms100M'
  '-Xmx1000M'
  '-Djava.net.preferIPv4Stack=true'
  '-Dlog4j.configurationFile=log4j2-aws.xml'
  '-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector'
  '-cp log4j-api-2.13.3.jar:log4j-core-2.13.3.jar:log4j-slf4j-impl-2.13.3.jar:disruptor-3.4.2.jar'
)

docker run \
  -p 5150:5150/tcp -p 5432/tcp \
  -e JAVA_OPTS="${JAVA_OPTS[*]}" \
  -e MCORPUS_DB_DATA_SOURCE_CLASS_NAME='org.postgresql.ds.PGSimpleDataSource' \
  -e MCORPUS_DB_URL='jdbc:postgresql://host.docker.internal:5432/mcorpus?user=mcweb&password=mcweb&ssl=false' \
  -e MCORPUS_JWT_SALT='4E521B8861508F17C423162845BBC5E916C4FDE82A73B6E2BE292DDA7AEE6FF6' \
  -e MCORPUS_JWT_TTL_IN_MILLIS='172800000' \
  -e MCORPUS_JWT_STATUS_CACHE_TIMEOUT_IN_MINUTES='11' \
  -e MCORPUS_JWT_STATUS_CACHE_MAX_SIZE='60' \
  -e MCORPUS_COOKIE_SECURE='true' \
  -e RATPACK_SERVER__DEVELOPMENT='false' \
  -e RATPACK_SERVER__PORT='5150' \
  -e RATPACK_SERVER__PUBLIC_ADDRESS='https://mcorpus.d2d' \
  "$@"
