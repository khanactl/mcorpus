#!/bin/bash

# WARNING: This script will probably need to run in cloud9
#          in order to have network access to the RDS db instance
#          when it is held in a private subnet.

# archive previous jmeter test run if present
if [ -d "loadtest-aws" ]; then
  # prev jmeter test run exists so archive it
  timestamp=$(date "+%Y-%m-%d_%H:%M:%S")
  sdir="archive/loadtest-aws-$timestamp"
  mkdir -p $sdir
  mv loadtest-aws $sdir
  mv *.log $sdir
elif ls *.log > /dev/null 2>&1; then
  rm *.log
fi

# run the jmeter script
jmeter -n -t loadtest-aws.jmx -e -o loadtest-aws -l loadtest-aws.log
