-----------------------------------------------------
------------- mcorpus schema definition -------------
-----------------------------------------------------
-- Author               jkirton
-- mcorpus Version:     0.1
-- Created:             10/15/17
-- Modified:            2/6/2018
-- Description:         Prototype member corpus db
-- PostgreSQL Version   10.1
-----------------------------------------------------

-- NOTE: a postgres com.tll.mcorpus.db must already exist for this script to work

-- bash> 'createdb mcorpus -E UTF8'
-- bash> 'psql mcorpus < mcorpus.ddl'

-- psql>
--   COPY mcuser(name,email,username,pswd,admin) FROM '/Users/d2d/dev/mcorpus/mcorpus-ddl/mock/mock-user.csv' DELIMITER ',' CSV HEADER;

--   COPY member(mid,emp_id,location,name_first,name_middle,name_last,display_name) FROM '/Users/d2d/dev/mcorpus/mcorpus-ddl/mock/mock-member.csv' DELIMITER ',' CSV HEADER;
--    
--   COPY maddress(mid,address_name,attn,street1,street2,city,state,postal_code,country) FROM '/Users/d2d/dev/mcorpus/mcorpus-ddl/mock/mock-maddress-mid.csv' DELIMITER ',' CSV HEADER;
--   COPY mbenefits(mid,foreign_adrs_flag,beli,mcb,med_plan_code,med_opt_out,den_plan_code,den_opt_out,vis_plan_code,vis_opt_out,leg_plan_code,leg_opt_out) FROM '/Users/d2d/dev/mcorpus/mcorpus-ddl/mock/mock-mbenefits-mid.csv' DELIMITER ',' CSV HEADER;

--   UPDATE ... SET pswhash = crypt('new password', gen_salt('bf'));
--   SELECT (pswhash = crypt('entered password', pswhash)) AS pswmatch FROM ... ;


-- set the connection defaults (https://www.postgresql.org/docs/9.6/static/runtime-config-client.html)
SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

-- functions, stored procedure support
CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;
COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';

-- CRYPTO and UUID support (https://www.postgresql.org/docs/9.6/static/pgcrypto.html)
CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public VERSION "1.3";
COMMENT ON EXTENSION pgcrypto IS 'pgcrypto v1.3';

SET search_path = public, pg_catalog;

---------------------------------------------------------------------------
-- mcorpus user and audit (those who access and mutate this corpus of data)
---------------------------------------------------------------------------

create table mcuser (
  uid                     uuid primary key default gen_random_uuid(),

  created                 timestamp not null default now(),
  modified                timestamp null,

  name                    text not null,
  email                   text not null,
  username                text not null,
  pswd                    text not null,
  admin                   boolean not null,

  unique(username),
  unique(email)
);
comment on type mcuser is 'The user table holding authentication credentials for access to the public mcorpus schema.';

create type mcuser_audit_type as enum (
  'LOGIN',
  'LOGOUT',
  'GRAPHQL_QUERY'
);
comment on type mcuser_audit_type is 'The mcuser audit record/event type.';

create table mcuser_audit (
  uid                     uuid not null, -- i.e. the mcuser.uid
  created                 timestamp not null default now(),
  type                    mcuser_audit_type not null,
  web_session_id          text null,
  remote_addr             text null,
  http_host               text null,
  http_origin             text null,
  http_referer            text null,
  http_forwarded          text null,

  primary key (created, type)
);
comment on type mcuser_audit is 'Log of when users access the api.';

/*
pass_hash()

@return the generated hash of a text password
        with a randomly generated salt.
*/
create or replace function pass_hash(pswd text) returns text as $$
  begin
    -- NOTE: we use the blowfish algo for the salt
    -- SEE: https://www.postgresql.org/docs/9.6/static/pgcrypto.html#PGCRYPTO-CRYPT-ALGORITHMS
    return crypt(pswd, gen_salt('bf'));
  end;
$$ language plpgsql
RETURNS NULL ON NULL INPUT;

/*
login

Call this function to authenticate users
by username/passwoed credentials
along with the sourcing http headers and ip address.

When user authentication is successful,
a LOGIN-type mcuser_audit record is created.

@param 1  username the user specified username
@param 2  password the user specified password
@param 3  user_session_id the unique identifier of the user instigated
                          web session (usu. the JSESSIONID)
@param 4  user_ip the users IP address
@param 5  user_host the users incoming http host header value
@param 6  user_origin the users incoming http origin header value
@param 7  user_referer the users incoming http host header value
@param 8  user_fowarded the users incoming http host header value

@return:
  the matching mcuser record upon successfull login
  -OR-
  NULL when login fails.
*/
CREATE OR REPLACE FUNCTION login(user_username text, user_password text, user_session_id text, user_ip text, user_host text, user_origin text, user_referer text, user_forwarded text)
RETURNS mcuser as $$
  DECLARE passed BOOLEAN;
  DECLARE row_mcuser mcuser%ROWTYPE;
  BEGIN
    passed = false;
    SELECT (pswd = crypt(user_password, pswd)) INTO passed
    FROM mcuser
    WHERE username = $1;

    IF passed THEN
      -- login success
      SELECT uid, created, modified, name, email, username, null, admin from mcuser INTO row_mcuser where username = $1;
      -- add mcuser_audit LOGIN record upon successful login
      INSERT INTO mcuser_audit (uid, type, web_session_id, remote_addr, http_host, http_origin, http_referer, http_forwarded)
        VALUES (row_mcuser.uid, 'LOGIN', user_session_id, user_ip, user_host, user_origin, user_referer, user_forwarded);
      -- return the user id and admin flag of the matched username
      RAISE NOTICE '% logged in', row_mcuser.uid;
      RETURN row_mcuser;
    END IF;

    -- default
    RAISE NOTICE 'login failed';
    RETURN null;
  END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION logout(user_uid UUID, user_session_id text)
RETURNS void as $$
  DECLARE user_exists BOOLEAN;
  BEGIN
    user_exists = false;
    SELECT EXISTS(SELECT uid FROM mcuser WHERE uid = $1) INTO user_exists;
    IF user_exists THEN
      INSERT INTO mcuser_audit (uid, type, web_session_id)
        VALUES (user_uid, 'LOGOUT', user_session_id);
      RAISE NOTICE '% logged out', user_uid;
    ELSE
      -- default
      RAISE NOTICE 'logout failed';
    END IF;
  END
$$ LANGUAGE plpgsql;

-- ***************
-- *** mcorpus ***
-- ***************

create type Location as enum (
  '01',
  '02',
  '03',
  '04',
  '05',
  '06',
  '07',
  '08',
  '09',
  '98',
  '20'
);
comment on type Location is 'Location is the 2-digit campus code.';

create type member_status as enum (
  'ACTIVE',
  'INACTIVE'
);
comment on type Location is 'All possible values of the member status flag which is used to track the state of members in the system.';

create table member (
                          -- NOTE: the member table only generates member ids!
  mid                     uuid primary key default gen_random_uuid(),

  created                 timestamp not null default now(),
  modified                timestamp not null default now(),

  emp_id                  text not null,
  location                Location not null,

  name_first              text not null,
  name_middle             text not null,
  name_last               text not null,
  display_name            text null,

  status                  member_status not null default 'ACTIVE',

  unique (emp_id, location)
);
comment on type member is 'The core member table.';

/**
 * set_modified()
 *
 * Function to set the member.modified column to current timestamp.
 */
CREATE FUNCTION set_modified() RETURNS TRIGGER
    LANGUAGE plpgsql
    AS $$
BEGIN
  NEW.modified := CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$;

/**
 * trigger_member_updated
 */
CREATE TRIGGER trigger_member_updated
  BEFORE UPDATE ON member
  FOR EACH ROW
  EXECUTE PROCEDURE set_modified();

create table mauth (
  mid                     uuid primary key,

  modified                timestamp not null default now(),

  dob                     date not null,
  ssn                     char(9) not null,

  email_personal          text null,
  email_work              text null,

  mobile_phone            text null,
  home_phone              text null,
  work_phone              text null,
  fax                     text null,

  username                text not null,
  pswd                    text not null,

  foreign key ("mid") references member ("mid") on delete cascade
);
comment on type mauth is 'The mauth table holds security sensitive member data.';

create type AddressName as enum (
  'home',
  'work',
  'other'
);
comment on type AddressName is 'The allowed address names (labels) recognized by the system.';

create table maddress (
  mid                     uuid not null,
  address_name            AddressName not null,

  modified                timestamp not null default now(),

  attn                    text null,
  street1                 text not null,
  street2                 text null,
  city                    text not null,
  state                   char(2) not null,
  postal_code             text null,
  country                 text not null default 'USA',
  
  primary key (mid, address_name),
  foreign key ("mid") references member ("mid") on delete cascade
);
comment on type maddress is 'The maddress table holds physical addresses of members.';

create type Beli as enum (
  '1',
  '2',
  '3',
  '4',
  '5',
  'P',
  '?'
);
comment on type Beli is 'Benefit Eligibility Level Indicator.';

create table mbenefits (
  mid                     uuid primary key,

  modified                timestamp not null default now(),

  foreign_adrs_flag       char null,

  beli                    Beli not null,
  mcb                     money not null,

  med_plan_code           char(2) null,
  med_opt_out             char(2) null,
  den_plan_code           char(2) null,
  den_opt_out             char(2) null,
  vis_plan_code           char(2) null,
  vis_opt_out             char(2) null,
  leg_plan_code           char(2) null,
  leg_opt_out             char(2) null,

  foreign key ("mid") references member ("mid") on delete cascade
);
comment on type mbenefits is 'The mbenefits table holds member benefits related data.';

