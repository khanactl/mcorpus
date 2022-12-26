#!/bin/bash
set -e

log4j2v="2.19.0"
disruptorv="3.4.4"

cd target
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005  -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=log4j2-localdev.xml -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -cp log4j-api-$log4j2v.jar:log4j-core-$log4j2v.jar:log4j-slf4j-impl-$log4j2v.jar:disruptor-$disruptorv.jar -jar mcorpus-gql-server.jar
