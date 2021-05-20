# FROM openjdk:11.0.7-jre
ARG REPO_LOCATION=524006177124.dkr.ecr.us-west-2.amazonaws.com/mcorpus-gql
FROM ${REPO_LOCATION}:amazoncorretto-11

# RUN apk --no-cache add curl

EXPOSE ${env.MCORPUS_SERVER__PORT}/tcp 5432/tcp

WORKDIR /webapp

COPY mcorpus-gql-server.jar .
COPY log4j2-aws.xml .
COPY disruptor-3.4.4.jar .
COPY log4j-api-2.14.1.jar .
COPY log4j-core-2.14.1.jar .
COPY log4j-slf4j-impl-2.14.1.jar .

HEALTHCHECK --interval=120s --timeout=5s --start-period=15s --retries=3 CMD curl -f -s http://localhost:${env.MCORPUS_SERVER__PORT}/health/ || exit 1

ENTRYPOINT exec java $JAVA_OPTS -jar mcorpus-gql-server.jar
