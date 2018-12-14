#!/bin/bash

cd target
java -Dlog4j.configurationFile=log4j2-prod.xml -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -cp log4j-api-2.11.1.jar:log4j-core-2.11.1.jar:log4j-slf4j-impl-2.11.1.jar:disruptor-3.4.2.jar -jar mcorpus-gql-server.jar
