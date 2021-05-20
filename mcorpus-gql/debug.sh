#!/bin/bash

cd target
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005  -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=log4j2-localdev.xml -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -cp log4j-api-2.14.1.jar:log4j-core-2.14.1.jar:log4j-slf4j-impl-2.14.1.jar:disruptor-3.4.4.jar -jar mcorpus-gql-server.jar
