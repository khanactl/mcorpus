FROM openjdk:8-jre-alpine

RUN apk --no-cache add curl

EXPOSE 5150/tcp 5432/tcp

WORKDIR /webapp

COPY mcorpus-gql-server.jar .
COPY log4j2-aws.xml .
COPY disruptor-3.4.2.jar .
COPY log4j-api-2.13.1.jar .
COPY log4j-core-2.13.1.jar .
COPY log4j-slf4j-impl-2.13.1.jar .

HEALTHCHECK --interval=120s --timeout=5s --start-period=15s --retries=3 CMD curl -f -s http://localhost:5150/health/ || exit 1

ENTRYPOINT exec java $JAVA_OPTS -jar mcorpus-gql-server.jar
