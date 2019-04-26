FROM openjdk:8-jre-alpine

WORKDIR /webapp

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
COPY target/mcorpus-gql-server.jar mcorpus-gql-server.jar
COPY target/log4j2-aws.xml log4j2-aws.xml
COPY target/disruptor-3.4.2.jar disruptor-3.4.2.jar
COPY target/log4j-api-2.11.1.jar log4j-api-2.11.1.jar
COPY target/log4j-core-2.11.1.jar log4j-core-2.11.1.jar
COPY target/log4j-slf4j-impl-2.11.1.jar log4j-slf4j-impl-2.11.1.jar

# port
EXPOSE 5150/tcp
EXPOSE 5432/tcp

# run
#CMD ["/usr/bin/java", "-Dlog4j.configurationFile=log4j2-aws.xml", "-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector", "-cp", "log4j-api-2.11.1.jar:log4j-core-2.11.1.jar:log4j-slf4j-impl-2.11.1.jar:disruptor-3.4.2.jar", "-jar", "mcorpus-gql-server.jar"]

ENTRYPOINT [ "exec target/aws/appStart.sh" ]
