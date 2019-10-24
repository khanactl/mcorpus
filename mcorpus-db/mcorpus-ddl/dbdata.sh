#!/bin/bash
set -e

# UNZIP mcorpus mock data file (member.csv, mauth.csv, maddress.csv)
cd /tmp
unzip mock-mcorpus-data.zip

psql mcorpus -c "COPY member FROM '/tmp/member.csv' DELIMITER ',' CSV HEADER NULL '\N';"
psql mcorpus -c "COPY mauth FROM '/tmp/mauth.csv' DELIMITER ',' CSV HEADER NULL '\N';"
psql mcorpus -c "COPY maddress FROM '/tmp/maddress.csv' DELIMITER ',' CSV HEADER NULL '\N';"
