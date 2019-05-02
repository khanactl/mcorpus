FROM openjdk:8-jre-alpine

ENV AWSCLI_VERSION "${awscli.version}"

# RUN apk update
# RUN apk upgrade
RUN apk add --update \
    bash \
    python \
    python-dev \
    py-pip \
    build-base \
    && pip install awscli==$AWSCLI_VERSION --upgrade --user \
    && apk --purge -v del py-pip \
    && rm -rf /var/cache/apk/*

EXPOSE 5150/tcp 5432/tcp

WORKDIR /webapp

COPY mcorpus-gql-server.jar .
COPY log4j2-aws.xml .
COPY disruptor-3.4.2.jar .
COPY log4j-api-2.11.1.jar .
COPY log4j-core-2.11.1.jar .
COPY log4j-slf4j-impl-2.11.1.jar .
COPY dockstart.sh .

RUN chmod +x dockstart.sh

ENTRYPOINT ./dockstart.sh
