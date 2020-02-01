#!/bin/bash

# archive previous jmeter test run if present
if [ -d "bm-fetch-member-local" ]; then
  # prev jmeter test run exists so archive it
  timestamp=$(date "+%Y-%m-%d_%H:%M:%S")
  sdir="archive/bm-fetch-member-local-$timestamp"
  mkdir -p $sdir
  mv bm-fetch-member-local $sdir
  mv *.log $sdir
elif ls *.log > /dev/null 2>&1; then
  rm *.log
fi

# run the jmeter script
jmeter -n -t bm-fetch-member-local.jmx -e -o bm-fetch-member-local -l bm-fetch-member-local.log
