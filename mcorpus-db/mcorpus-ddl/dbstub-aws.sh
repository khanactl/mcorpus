#!/bin/bash
set -e

# *** AWS RDS psql sepcific ***

# expected input args:
# -h <dbhost>
# -U <dbusername>

# This script performs the following:
# - create mcorpus schema to an already existing mcorpus db
# - adds the mcorpus db users (db roles)
# - inserts the requisite mcuser records

# (already created by AWS RDS)
# echo 'creating mcorpus db..'
# createdb mcorpus -E UTF8

echo 'creating mcorpus db schema..'
psql $@ mcorpus < mcorpus-schema.ddl 2>&1 >/dev/null

echo 'creating mcorpus users (roles)..'
psql $@ mcorpus < mcorpus-roles.ddl 2>&1 >/dev/null

echo 'adding default mcuser records..'
psql $@ mcorpus < mcorpus-mcuser.sql 2>&1 >/dev/null

echo 'mcorpus db created successfully!'
