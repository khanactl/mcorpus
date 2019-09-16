---------------------------------
---     mcorpus db roles      ---
---------------------------------
-- Author               jkirton
-- mcorpus Version:     0.9.0-SNAPSHOT
-- Created:             10/15/17
-- Modified:            02/26/19
-- Description:         Prototype UC member corpus db written in PostgreSQL.
-- PostgreSQL Version   11.2
---------------------------------

-- Role: mcweb
-- Desc: Able to issue /graphql requests against the mcorpus db's public schema
--       as well as api login and logout.
create role mcweb with 
  login 
  encrypted password '{mcweb}' 
  connection limit 500 
  valid until '2021-01-01';
grant connect on database mcorpus to mcweb;
-- mcorpus related
grant select, insert, update, delete on member, mauth, maddress, mbenefits to mcweb;
grant select, insert on member_audit to mcweb;
-- mcuser related
grant select on mcuser, mcuser_audit to mcweb;
grant insert, update, delete on mcuser to mcweb;
grant insert on mcuser_audit to mcweb;

-- Role: mcwebtest
-- Desc: mcorpus db test role with distinct priviliges 
--			  beyond mcweb in order to accommodate db cleanup in audit tables.
create role mcwebtest with 
  login 
  encrypted password '{mcwebtest}' 
  connection limit 5 
  valid until '2021-01-01';
grant connect on database mcorpus to mcwebtest;
grant select, insert, update, delete on member, mauth, maddress, mbenefits to mcwebtest;
grant select, insert, update, delete on mcuser, mcuser_audit, member_audit to mcwebtest;

-- Role: mcadmin
-- Desc: mcorpus db architect privileges - ddl create/drop
/*
create role mcadmin with 
  login 
  encrypted password '{mcadmin}' 
  connection limit 1 
  valid until '2021-01-01';
grant connect on database mcorpus to mcadmin;
*/