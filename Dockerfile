FROM openjdk:8-jre-alpine

# env
ENV MCORPUS_DB_DATA_SOURCE_CLASS_NAME=org.postgresql.ds.PGSimpleDataSource
ENV MCORPUS_DB_URL=$dbUrl
ENV MCORPUS_JWT_SALT=$jwtSalt
ENV MCORPUS_JWT_TTL_IN_MILLIS=172800000
ENV MCORPUS_JWT_STATUS_CACHE_TIMEOUT_IN_MINUTES=10
ENV MCORPUS_JWT_STATUS_CACHE_MAX_SIZE=60
ENV MCORPUS_COOKIE_SECURE=true
ENV RATPACK_SERVER__DEVELOPMENT=false
ENV RATPACK_SERVER__PORT=5150
ENV RATPACK_SERVER__PUBLIC_ADDRESS=https://www.mcorpus-aws.net

# artifacts
COPY mcorpus-gql/target/mcorpus-gql-server.jar /webapp/mcorpus-gql-server.jar
COPY mcorpus-gql/target/log4j2-aws.xml /webapp/log4j2-aws.xml
COPY mcorpus-gql/target/disruptor-3.4.2.jar /webapp/disruptor-3.4.2.jar
COPY mcorpus-gql/target/log4j-api-2.11.1.jar /webapp/log4j-api-2.11.1.jar
COPY mcorpus-gql/target/log4j-core-2.11.1.jar /webapp/log4j-core-2.11.1.jar
COPY mcorpus-gql/target/log4j-slf4j-impl-2.11.1.jar /webapp/log4j-slf4j-impl-2.11.1.jar

# port
EXPOSE 5150/tcp

# run
CMD ["/usr/bin/java", "-Dlog4j.configurationFile=/webapp/log4j2-aws.xml", "-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector", "-cp", "/webapp/log4j-api-2.11.1.jar:/webapp/log4j-core-2.11.1.jar:/webapp/log4j-slf4j-impl-2.11.1.jar:/webapp/disruptor-3.4.2.jar", "-jar", "/webapp/mcorpus-gql-server.jar"]
