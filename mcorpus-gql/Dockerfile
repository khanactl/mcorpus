FROM openjdk:8-jre-alpine

ENV AWSCLI_VERSION "${awscli.version}"

RUN apk add --update \
    python \
    python-dev \
    py-pip \
    build-base \
    && pip install awscli==$AWSCLI_VERSION --upgrade --user \
    && apk --purge -v del py-pip \
    && rm -rf /var/cache/apk/*

EXPOSE 5150/tcp 5432/tcp

WORKDIR /webapp

COPY mcorpus-gql-server.jar /webapp/
COPY log4j2-aws.xml /webapp/
COPY disruptor-3.4.2.jar /webapp/
COPY log4j-api-2.11.1.jar /webapp/
COPY log4j-core-2.11.1.jar /webapp/
COPY log4j-slf4j-impl-2.11.1.jar /webapp/
COPY dockstart.sh /webapp/

RUN chmod +x /webapp/dockstart.sh

ENTRYPOINT /webapp/dockstart.sh
