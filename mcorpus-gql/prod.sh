#!/bin/bash

cd target
java -Dlog4j.configurationFile=log4j2-prod.xml -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -cp log4j-api.jar:log4j-core.jar:log4j-slf4j-impl.jar:disruptor.jar -jar mcorpus-gql-server.jar
