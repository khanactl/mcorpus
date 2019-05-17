#!/bin/bash
set -e

# This script creates the mcorpus postgres db from scratch 
# then populates it with randomly generated baseline 2000 member data 
# gotten from the mcorpus-*-2000.sql data files.

echo 'creating mcorpus db..'
createdb mcorpus -E UTF8

echo 'creating mcorpus db schema..'
psql mcorpus < mcorpus-schema.ddl 2>&1 >/dev/null

echo 'creating mcorpus users (roles)..'
psql mcorpus < mcorpus-roles.ddl 2>&1 >/dev/null

echo 'adding default mcuser records..'
psql mcorpus < mcorpus-mcuser.sql 2>&1 >/dev/null

echo 'mcorpus db created successfully!'

sleep 1
echo ' '

echo 'importing baseline 2000 rando member data..'
psql mcorpus < mcorpus-member-2000.sql 2>&1 >/dev/null
psql mcorpus < mcorpus-mauth-2000.sql 2>&1 >/dev/null
psql mcorpus < mcorpus-maddress-2000.sql 2>&1 >/dev/null

echo ' '
echo 'mcorpus db stubbed.'
