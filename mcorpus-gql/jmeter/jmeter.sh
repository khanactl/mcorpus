#!/bin/bash

# archive previous jmeter test run if present
if [ -d "testoutput" ]; then
  # prev jmeter test run exists so archive it
  timestamp=$(date "+%Y-%m-%d_%H:%M:%S")
  sdir="archive/run-$timestamp"
  mkdir -p $sdir
  mv testoutput $sdir
  mv *.log $sdir
elif ls *.log > /dev/null 2>&1; then 
  rm *.log
fi

# run the jmeter script
jmeter -n -t MCorpusLoadTest.jmx -e -o testoutput -l mcorpus-jmeter.log
