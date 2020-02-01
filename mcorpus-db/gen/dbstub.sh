#!/bin/bash
set -e

# This script creates the mcorpus postgres db from scratch 
# then adds the requisite default mcuser records.

echo 'creating mcorpus db..'
createdb mcorpus -E UTF8

echo 'creating mcorpus db schema..'
psql mcorpus < mcorpus-schema.ddl 2>&1 >/dev/null

echo 'creating mcorpus users (roles)..'
psql mcorpus < mcorpus-roles.ddl 2>&1 >/dev/null

echo 'adding default mcuser records..'
# we copy to /tmp for permissions reasons
cp mcorpus-mcuser.csv /tmp
psql -c "COPY mcuser FROM '/tmp/mcorpus-mcuser.csv' CSV HEADER NULL '\N';" mcorpus

echo 'mcorpus db created successfully!'
