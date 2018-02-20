---------------------------------
---     mcorpus db roles      ---
---------------------------------
-- Author               jkirton
-- mcorpus Version:     0.7.0
-- Created:             10/15/17
-- Modified:            12/09/17
-- Description:         Prototype UC member corpus db written in PostgreSQL.
-- PostgreSQL Version   10.1
---------------------------------

-- Role: mcweb
-- Desc: Able to issue /graphql requests against the mcorpus db's public schema.
create role mcweb with 
  login 
  encrypted password '' 
  connection limit 500 
  valid until '2020-01-01';
grant connect on database mcorpus to mcweb;
grant select, insert, update, delete on member, mauth, maddress, mbenefits to mcweb;
grant select, insert on mcuser, mcuser_audit to mcweb;

-- Role: mcadmin
-- Desc: mcorpus db architect privileges - ddl create/drop
create role mcadmin with 
  login 
  encrypted password '' 
  connection limit 1 
  valid until '2020-01-01';
grant connect on database mcorpus to mcadmin;
