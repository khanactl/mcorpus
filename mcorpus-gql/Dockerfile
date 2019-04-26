FROM openjdk:8-jre-alpine

LABEL key=maintainer="khanactl@gmail.com"

# aws cli
RUN apt-get update -y && \
    apt-get install -y python python-pip python-virtualenv dos2unix && \
    mkdir aws && \
    virtualenv aws/env && \
    ./aws/env/bin/pip install awscli && \
    apt-get autoremove --purge -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /webapp

# artifacts
COPY mcorpus-gql-server.jar mcorpus-gql-server.jar
COPY log4j2-aws.xml log4j2-aws.xml
COPY disruptor-3.4.2.jar disruptor-3.4.2.jar
COPY log4j-api-2.11.1.jar log4j-api-2.11.1.jar
COPY log4j-core-2.11.1.jar log4j-core-2.11.1.jar
COPY log4j-slf4j-impl-2.11.1.jar log4j-slf4j-impl-2.11.1.jar
COPY aws/appStart.sh appStart.sh
COPY aws/run.sh run.sh

# port
EXPOSE 5150/tcp
# EXPOSE 5432/tcp  wont work.  instead: docker run -p 5432:5432 your-sql-container

# run
#CMD ["/usr/bin/java", "-Dlog4j.configurationFile=log4j2-aws.xml", "-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector", "-cp", "log4j-api-2.11.1.jar:log4j-core-2.11.1.jar:log4j-slf4j-impl-2.11.1.jar:disruptor-3.4.2.jar", "-jar", "mcorpus-gql-server.jar"]

# startup
ENTRYPOINT [ "exec appStart.sh" ]
