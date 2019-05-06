FROM openjdk:8-jre-alpine

EXPOSE 5150/tcp 5432/tcp

WORKDIR /webapp

COPY mcorpus-gql-server.jar .
COPY log4j2-aws.xml .
COPY disruptor-3.4.2.jar .
COPY log4j-api-2.11.1.jar .
COPY log4j-core-2.11.1.jar .
COPY log4j-slf4j-impl-2.11.1.jar .

ENTRYPOINT exec java $JAVA_OPTS -jar mcorpus-gql-server.jar
