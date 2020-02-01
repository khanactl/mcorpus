#!/bin/bash

# archive previous jmeter test run if present
if [ -d "loadtest-local" ]; then
  # prev jmeter test run exists so archive it
  timestamp=$(date "+%Y-%m-%d_%H:%M:%S")
  sdir="archive/loadtest-local-$timestamp"
  mkdir -p $sdir
  mv loadtest-local $sdir
  mv *.log $sdir
elif ls *.log > /dev/null 2>&1; then
  rm *.log
fi

# run the jmeter script
jmeter -n -t loadtest-local.jmx -e -o loadtest-local -l loadtest-local.log
