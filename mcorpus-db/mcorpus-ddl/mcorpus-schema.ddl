-----------------------------------------------------
------------- mcorpus schema definition -------------
-----------------------------------------------------
-- Author               jkirton
-- Created:             10/15/17
-- Modified:            09/03/2018
-- Description:         Prototype member corpus db
-- PostgreSQL Version   10.4
-----------------------------------------------------

-- NOTE: a postgres com.tll.mcorpus.db must already exist for this script to work

-- bash> 'createdb mcorpus -E UTF8'
-- bash> 'psql mcorpus < mcorpus-schema.ddl'
-- bash> 'psql mcorpus < mcorpus-roles.ddl'
-- bash> 'psql mcorpus < mcorpus-data.ddl'

-- psql>
--   COPY mcuser TO '/Users/d2d/dev/mcorpus/mcorpus-db/mcorpus-ddl/mcorpus-mcuser.ddl';

--   COPY (select * from member order by mid limit 2000) TO '/Users/d2d/dev/mcorpus/mcorpus-db/mcorpus-ddl/mcorpus-member-2000.ddl';
--   COPY (select * from mauth order by mid limit 2000) to '/Users/d2d/dev/mcorpus/mcorpus-db/mcorpus-ddl/mcorpus-mauth-2000.ddl';
--   COPY (select * from maddress where mid in (select mid from member order by mid limit 2000)) TO '/Users/d2d/dev/mcorpus/mcorpus-db/mcorpus-ddl/mcorpus-maddress-2000.ddl';

--   UPDATE ... SET pswhash = crypt('new password', gen_salt('bf'));
--   SELECT (pswhash = crypt('entered password', pswhash)) AS pswmatch FROM ... ;

--   -- set all held member's pswd to 'test123':
--   UPDATE mauth set pswd = crypt('test123', gen_salt('bf'));

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

-- UUID support
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- CRYPTO and uuid support (https://www.postgresql.org/docs/9.6/static/pgcrypto.html)
CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public VERSION "1.3";
COMMENT ON EXTENSION pgcrypto IS 'pgcrypto v1.3';

SET search_path = public, pg_catalog;

---------------------------------------------------------------------------
-- mcorpus user and audit (those who access and mutate this corpus of data)
---------------------------------------------------------------------------

/*
pass_hash()

Use to generate a hash from a raw password to manually create mcuser passwords.

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

create type mcuser_role as enum (
  -- public - open to all read-only
  'PUBLIC',
  -- mcorpus - full read and write over all members
  'MCORPUS',
  -- member - read write only for the self-member
  'MEMBER',
  -- mpii - ability to see member pii related info
  'MPII'
);
comment on type mcuser_role is 'The roles defining the level of data access for an mcuser.';

create type mcuser_status as enum (
  'ACTIVE',
  'INACTIVE',
  'INVALIDATED'
);
comment on type mcuser_status is 'The allowed mcuser status values.';

create table mcuser (
  uid                     uuid primary key default gen_random_uuid(),

  created                 timestamp not null default now(),
  modified                timestamp null,

  name                    text not null,
  email                   text not null,
  username                text not null,
  pswd                    text not null,
  status                  mcuser_status not null default 'ACTIVE'::mcuser_status,
  --role                   mcuser_role not null default 'PUBLIC'::mcuser_role,

  unique(username),
  unique(email)
);
comment on type mcuser is 'The user table holding authentication credentials for access to the public mcorpus schema.';

create table mcuser_roles (
  uid                    uuid not null,
  role                   mcuser_role not null,

  primary key(uid, role)
);
comment on type mcuser_roles is 'Related 0-N mcuser roles bound to a single mcuser row.';

create type jwt_id_status as enum (
  'BLACKLISTED',
  'OK'
);
comment on type jwt_id_status is 'The allowed JWT ID status values.';

create type mcuser_audit_type as enum (
  'LOGIN',
  'LOGOUT',
  'GRAPHQL_QUERY'
);
comment on type mcuser_audit_type is 'The allowed mcuser audit types.';

create table mcuser_audit (
  uid                     uuid not null, -- i.e. the mcuser.uid
  created                 timestamp not null default now(),
  type                    mcuser_audit_type not null,
  request_timestamp       timestamp not null,
  request_origin          text not null,
  login_expiration        timestamp,
  jwt_id                  uuid,
  jwt_id_status           jwt_id_status,

  primary key (uid, created, type),
  unique(type, jwt_id)
);
comment on type mcuser_audit is 'Log of when mcusers login/out and access the api.';

/**
  get_num_active_logins()

  Calculate the current number of active non-expired JWT IDs 
  held in the mcuser_audit table for a given mcuser.
**/
CREATE OR REPLACE FUNCTION get_num_active_logins(mcuser_id uuid) RETURNS int
LANGUAGE plpgsql AS 
$_$
DECLARE num_valid_logins int;
BEGIN 
  select into num_valid_logins count(jwt_id) 
  from mcuser_audit 
  where 
        type = 'LOGIN' and jwt_id_status = 'OK' 
    and login_expiration >= now() 
    and uid = $1
    and jwt_id not in (
      select jwt_id 
      from mcuser_audit 
      where (type = 'LOGOUT' or jwt_id_status = 'BLACKLISTED') 
      and uid = $1); 
    return num_valid_logins;
END
$_$;

CREATE TYPE jwt_mcuser_status AS (
  mcuser_audit_record_type mcuser_audit_type,
  jwt_id uuid,
  jwt_id_status jwt_id_status,
  login_expiration timestamp,
  uid uuid,
  mcuser_status mcuser_status
);
comment on type jwt_mcuser_status is 'The pertinent db columns bound to a held JWT ID in the mcuser_audit table.';

CREATE TYPE jwt_status AS ENUM (
  'PRESENT_BAD_STATE',
  'NOT_PRESENT',
  'BLACKLISTED',
  'EXPIRED',
  'MCUSER_INACTIVE',
  'VALID'
);
comment on type jwt_status is 'Convey the state of a JWT ID held in the mcuser_audit table.';

CREATE TYPE jwt_status_mcuser_role as (
  jwt_status jwt_status,
  roles      text -- rollup of the related many mcuser_roles as a comma-delimited string
);
comment on type jwt_status_mcuser_role is 'Simple wrapper around a jwt_status instance and mcuser_role instance.';

/**
 * Fetch the most recently created mcuser audit record with the given jwt id 
 * of either login or logout type.
 * This is the authoritive record for determining the jwt id's status.
*/
CREATE OR REPLACE FUNCTION fetch_latest_jwt_mcuser_rec(jwt_id uuid) RETURNS jwt_mcuser_status 
  LANGUAGE plpgsql AS
$_$
  DECLARE qrec jwt_mcuser_status;
BEGIN
  SELECT INTO qrec a.type, a.jwt_id, a.jwt_id_status, a.login_expiration, m.uid, m.status
  FROM mcuser_audit a LEFT JOIN mcuser m ON a.uid = m.uid 
  WHERE a.jwt_id = $1 and (a.type = 'LOGIN'::mcuser_audit_type or a.type = 'LOGOUT'::mcuser_audit_type)
  ORDER BY a.created DESC LIMIT 1; 
  RETURN qrec;
END
$_$;

/**
  get_jwt_status()

  Provide the JWT status of the given jwt_id 
  as well as provide the role of the associated mcuser.

  Is the given jwt id valid by way of:
    1) The most recent mcuser_audit record having the given jwt_id 
       is present and marked as 'OK'.
    2) The associated mcuser's status is valid.
    3) The login expiration timestamp is in the future.
*/
CREATE OR REPLACE FUNCTION get_jwt_status(jwt_id uuid) RETURNS jwt_status_mcuser_role 
  LANGUAGE plpgsql AS 
$_$
  DECLARE qrec jwt_mcuser_status;
  DECLARE jstat jwt_status;
  DECLARE roles text;
  DECLARE rval jwt_status_mcuser_role;
BEGIN

  qrec := fetch_latest_jwt_mcuser_rec(jwt_id);

  IF qrec is null or qrec.jwt_id is null THEN 
    -- the input jwt id was not found
    RAISE NOTICE 'JWT id not found: %', jwt_id;
    jstat := 'NOT_PRESENT'::jwt_status;
  ELSEIF qrec.jwt_id_status = 'BLACKLISTED'::jwt_id_status THEN 
    -- jwt id is marked as blacklisted (jwt_status is set to blacklisted upon logout)
    jstat := 'BLACKLISTED'::jwt_status;
  ELSEIF qrec.mcuser_status != 'ACTIVE'::mcuser_status THEN
    -- mcuser is bad or inactive
    jstat := 'MCUSER_INACTIVE'::jwt_status;
  ELSEIF qrec.mcuser_audit_record_type != 'LOGIN'::mcuser_audit_type THEN
    -- not a login mcuser audit record! (shouldn't happen but we check to be sure)
    RAISE NOTICE 'Expected LOGIN type mcuser_audit record';
    jstat := 'PRESENT_BAD_STATE'::jwt_status;
  ELSEIF qrec.login_expiration is null or qrec.login_expiration <= now() THEN
    -- either no login expiration date present or the jwt id has expired
    jstat := 'EXPIRED'::jwt_status;
  ELSEIF qrec.jwt_id_status = 'OK' THEN
    jstat := 'VALID'::jwt_status;
    -- fetch the mcuser roles into a comma delimited string
    SELECT INTO roles string_agg(mr.role::text, ',') FROM mcuser_roles mr WHERE mr.uid = qrec.uid;
  ELSE
    -- error: unresolved jwt status
    RAISE NOTICE 'JWT id bad state: %', jwt_id;
    jstat := 'PRESENT_BAD_STATE'::jwt_status;
  END IF;

  SELECT INTO rval jstat, roles;
  return rval;
END 
$_$;

/*
mcuser_login

Call this function to authenticate mcuser users
by username and password along with 
http request context information.

When an mcuser authentication is successful,
a LOGIN-type mcuser_audit record is created
and the associated mcuser record is returned.

@return:
  the matching mcuser record upon successful login
  -OR-
  NULL when login fails for any reason.
*/
CREATE OR REPLACE FUNCTION mcuser_login(
  mcuser_username text, 
  mcuser_password text, 
  in_request_timestamp timestamp without time zone, 
  in_request_origin text, 
  in_login_expiration timestamp without time zone, 
  in_jwt_id uuid
) RETURNS public.mcuser
    LANGUAGE plpgsql
    AS $_$
  DECLARE existing_jwt_id uuid;
  DECLARE passed BOOLEAN;
  DECLARE row_mcuser mcuser%ROWTYPE;
  BEGIN
    -- verify the given in_jwt_id is unique against the existing jwt ids held
    -- in the mcuser_audit table
    select ma.jwt_id into existing_jwt_id from mcuser_audit ma where ma.jwt_id = in_jwt_id;
    IF existing_jwt_id IS NOT NULL THEN
      RAISE NOTICE 'Non-unique jwt id provided.';
      RETURN NULL;
    END IF;

    -- verify the existence of a single mcuser record 
    -- by the given username and password
    passed = false;
    SELECT (pswd = crypt(mcuser_password, pswd)) INTO passed
    FROM mcuser
    WHERE username = $1;

    IF passed THEN
      -- mcuser authenticated

      -- fetch mcuser record
      SELECT 
        uid, 
        created, 
        modified, 
        name, 
        email, 
        username, 
        null, 
        status 
      FROM mcuser 
      INTO row_mcuser 
      WHERE username = $1;

      -- add mcuser_audit LOGIN record upon successful login
      INSERT INTO mcuser_audit (
        uid, 
        type, 
        request_timestamp, 
        request_origin,
        login_expiration,
        jwt_id,
        jwt_id_status
      )
      VALUES (
        row_mcuser.uid, 
        'LOGIN', 
        in_request_timestamp, 
        in_request_origin,
        in_login_expiration,
        in_jwt_id,
        'OK'::jwt_id_status
      );
      -- return the mcuser
      RAISE NOTICE 'mcuser % logged in', row_mcuser.uid;
      RETURN row_mcuser;
    END IF;

    -- default
    RAISE NOTICE 'mcuser login failed';
    RETURN null;
  END
$_$;

/**
 * mcuser_logout
 * 
 * Logs an mcuser out.
 *
 * mcuser logout is only allowed when the bound jwt id and mcuser id 
 * are found to be currently logged in.
 * 
 * An mcuser_audit record is created of LOGOUT type.
 */
CREATE OR REPLACE FUNCTION mcuser_logout(
  mcuser_uid uuid, 
  jwt_id uuid, 
  request_timestamp timestamp without time zone, 
  request_origin text
) RETURNS boolean
    LANGUAGE plpgsql
AS $_$
  DECLARE jsi jwt_status_mcuser_role;
BEGIN
    -- logout is predicated on finding a single mcuser_audit record with the 
    -- given mcuserId *and* jwtId.
    IF EXISTS(SELECT uid FROM mcuser_audit m WHERE m.uid = $1 and m.jwt_id = $2 and m.type = 'LOGIN') THEN
      -- at this point, we know a LOGIN record was created with the given jwt id and mcuser id.

      -- only allow mcuser logout when the latest jwt id status is valid
      jsi := get_jwt_status($2);      
      
      IF jsi.jwt_status = 'VALID'::jwt_status THEN
        -- add a new LOGOUT type mcuser_audit record
        INSERT INTO mcuser_audit (
          uid, 
          type, 
          jwt_id,
          jwt_id_status,
          request_timestamp, 
          request_origin
        )
        VALUES (
          $1, 
          'LOGOUT'::mcuser_audit_type, 
          $2,
          'BLACKLISTED'::jwt_id_status,
          $3, 
          $4
        );
        RAISE NOTICE 'mcuser % logged out', mcuser_uid;
        return true;
      END IF;
    END IF;

    -- default
    RAISE NOTICE 'mcuser % logout failed', mcuser_uid;
    return false;
END
$_$;

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

create type mref AS (
  mid                     uuid,
  emp_id                  text,
  location                Location
);
comment on type mref is 'Uniquely identifies a single member in the corpus';

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
  
  unique(username),

  foreign key ("mid") references member ("mid") on delete cascade
);
comment on type mauth is 'The mauth table holds security sensitive member data.';

/**
 * set_modified()
 *
 * Function to set the member.modified column to current timestamp.
 */
CREATE OR REPLACE FUNCTION set_modified() RETURNS TRIGGER
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

create type member_audit_type as enum (
  'LOGIN',
  'LOGOUT'
);
comment on type member_audit_type is 'The member audit record/event type.';

create table member_audit (
  mid                     uuid not null, -- i.e. the member.mid
  created                 timestamp not null default now(),
  type                    member_audit_type not null,
  request_timestamp       timestamp not null,
  request_origin          text not null,

  primary key (created, type)
);
comment on type member_audit is 'Log of member events.';

/*
member_login

Authenticates a member by requiring one and only one mauth record exists 
with the given username and pswd.

@return:
  the matching member id upon successfull login
  -OR-
  NULL when member login fails for any reason.
*/
CREATE OR REPLACE FUNCTION member_login(
  member_username text, 
  member_password text, 
  in_request_timestamp timestamp without time zone, 
  in_request_origin text
) RETURNS public.mref
    LANGUAGE plpgsql
    AS $_$
  DECLARE passed BOOLEAN;
  DECLARE rec_mref mref;
  BEGIN
    passed = false;

    -- authenticate
    SELECT (pswd = crypt(member_password, pswd)) INTO passed
    FROM mauth
    WHERE username = $1;

    IF passed THEN
      -- member login success
      SELECT 
        m.mid, 
        m.emp_id, 
        m.location
      INTO rec_mref 
      FROM 
        member m join mauth ma on m.mid = ma.mid 
      WHERE ma.username = $1;
      -- add member_audit LOGIN record upon successful login
      INSERT INTO member_audit (
        mid, 
        type, 
        request_timestamp, 
        request_origin
      )
      VALUES (
        rec_mref.mid, 
        'LOGIN', 
        in_request_timestamp, 
        in_request_origin
      );
      -- return the gotten mref
      RAISE NOTICE 'member % logged in', rec_mref.mid;
      RETURN rec_mref;
    END IF;

    -- default
    RAISE NOTICE 'member login failed';
    RETURN null;
  END
$_$;

/*
member_logout
 
Logs a member out.
 
A member_audit record is created of LOGOUT type.
 
@return:
  the member id of the member that was logged out upon success
  -OR-
  NULL when member logout fails for any reason.
*/
CREATE OR REPLACE FUNCTION member_logout(
  mid uuid, 
  in_request_timestamp timestamp without time zone, 
  in_request_origin text
) RETURNS UUID
    LANGUAGE plpgsql
    AS $_$
  DECLARE member_exists BOOLEAN;
  BEGIN
    member_exists = false;
    SELECT EXISTS(SELECT m.mid FROM member m WHERE m.mid = $1) INTO member_exists;
    IF member_exists THEN
      INSERT INTO member_audit (
        mid, 
        type, 
        request_timestamp, 
        request_origin
      )
      VALUES (
        $1, 
        'LOGOUT', 
        in_request_timestamp, 
        in_request_origin
      );
      RAISE NOTICE 'member % logged out', $1;
      return $1;
    END IF;
    -- default
    RAISE NOTICE 'member logout failed';
    return null;
  END
$_$;

-- ******************************
-- *** member address related ***
-- ******************************

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


-- ********************************
-- *** member benefits related  ***
-- ********************************

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

