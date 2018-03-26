#!/bin/bash

cd target
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar mcorpus-gql-server.jar -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
