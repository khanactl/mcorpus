#!/bin/bash
set -e

l4j2v="2.19.0"
disruptorv="3.4.4"

cd target
java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=log4j2-prod.xml -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -cp log4j-api-$l4j2v.jar:log4j-core-$l4j2v.jar:log4j-slf4j-impl-$l4j2v.jar:disruptor-$disruptorv.jar -jar mcorpus-gql-server.jar
