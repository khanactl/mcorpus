cd target
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005  -Dlog4j.configurationFile=log4j2-localdev.xml -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -cp log4j-api-2.19.0.jar;log4j-core-2.19.0.jar;log4j-slf4j-impl-2.19.0.jar;disruptor-3.4.4.jar -jar mcorpus-gql-server.jar
