#!/bin/bash
set -e

# stop current app instance (revision)
# jps -l | grep mcorpus-gql-server.jar | awk '{print $1}' | xargs kill -15
ps xa | grep java | grep mcorpus-gql-server.jar | awk '{print $1}' | xargs kill -15

# wait for webapp to shutdown
# sleep 5s

# delete existing deploy files
# rm -f /home/ec2-user/webapp/*
