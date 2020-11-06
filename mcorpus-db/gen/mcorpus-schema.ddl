-----------------------------------------------------
------------- mcorpus schema definition -------------
-----------------------------------------------------
-- Author               jkirton
-- Created:             10/15/17
-- Modified:            3/30/20
-- Description:         Prototype member corpus db
-- PostgreSQL Version   12.2
-----------------------------------------------------

-- NOTE: a postgres db must already exist for this script to work

-- bash> 'createdb mcorpus -E UTF8'
-- bash> 'psql mcorpus < mcorpus-schema.ddl'
-- bash> 'psql mcorpus < mcorpus-roles.ddl'
-- bash> 'psql mcorpus < mcorpus-data.ddl'

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
-- COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language'; (AWS complains so comment out)

-- UUID support
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- CRYPTO and uuid support (https://www.postgresql.org/docs/9.6/static/pgcrypto.html)
CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public VERSION "1.3";
-- COMMENT ON EXTENSION pgcrypto IS 'pgcrypto v1.3'; (AWS complains so comment out)

SET search_path = public, pg_catalog;

---------------------------------------
-- schema-agnostic utility functions --
---------------------------------------

/**
 * set_modified()
 *
 * Function to set the [TABLE].[modified] column to current timestamp.
 * NOTE: The TABLE name/ref is not explicitly called out here.
 */
CREATE OR REPLACE FUNCTION set_modified() RETURNS TRIGGER
LANGUAGE plpgsql AS
$$
BEGIN
  NEW.modified := CURRENT_TIMESTAMP;
  RETURN NEW;
END
$$;

/*
pass_hash()

Use to generate a persistence-ready hash from a raw password.
<p>
Only store password hashes with a salt and *never* raw passwords.

@return the generated hash of a text password
        with a randomly generated and self-contained salt.
*/
CREATE OR REPLACE FUNCTION pass_hash(pswd text) RETURNS text AS
$$
BEGIN
  -- NOTE: we use the blowfish algo for the salt
  -- SEE: https://www.postgresql.org/docs/9.6/static/pgcrypto.html#PGCRYPTO-CRYPT-ALGORITHMS
  return crypt(pswd, gen_salt('bf'));
END
$$
LANGUAGE plpgsql
RETURNS NULL ON NULL INPUT;

-- ********************************************************************
-- mcuser sub-schema (those who access and mutate this corpus of data)
-- ********************************************************************

create type mcuser_role as enum (
  -- mcorpus - full read and write over all members
  'MCORPUS',
  -- member - member login and logout capability
  'MEMBER',
  -- mpii - ability to see member PII field values
  'MPII',
  -- admin - only mcuser administrators may invoke the bound operation
  'ADMIN'
);
comment on type mcuser_role is 'The roles defining the level of data access for an mcuser.';

create type mcuser_status as enum (
  'ACTIVE',
  'INACTIVE'
);
comment on type mcuser_status is 'The allowed mcuser status values.';

create table mcuser (
  uid                     uuid primary key default gen_random_uuid(),

  created                 timestamptz not null default now(),
  modified                timestamptz null,

  name                    text not null,
  email                   text not null,
  username                text not null,
  pswd                    text not null,
  status                  mcuser_status not null default 'ACTIVE'::mcuser_status,

  roles                   mcuser_role[],

  unique(username)
);
comment on type mcuser is 'The user table holding authentication credentials for access to the public mcorpus schema.';

create type jwt_id_status as enum (
  'OK',
  'BLACKLISTED'
);
comment on type jwt_id_status is 'The allowed JWT ID status values.';

create type mcuser_audit_type as enum (
  'LOGIN',
  'LOGOUT'
);
comment on type mcuser_audit_type is 'The allowed mcuser audit types.';

create table mcuser_audit (
  uid                     uuid not null REFERENCES mcuser ON DELETE CASCADE,
  created                 timestamptz not null default now(),
  type                    mcuser_audit_type not null,
  request_timestamp       timestamptz not null,
  request_origin          inet not null,
  login_expiration        timestamptz,
  jwt_id                  uuid not null,
  jwt_id_status           jwt_id_status not null,

  primary key (uid, created, type, jwt_id)
);
create index mcuser_audit__jwt_id on mcuser_audit (jwt_id);
comment on type mcuser_audit is 'Log of when mcusers login/out and access the api.';

/**
  insert_mcuser()

  Insert an mcuser record.

  @return the inserted mcuser record.
 */
CREATE OR REPLACE FUNCTION insert_mcuser(
    in_name text,
    in_email text,
    in_username text,
    in_pswd text,
    in_status mcuser_status,
    in_roles mcuser_role[]
) RETURNS mcuser
LANGUAGE plpgsql AS
$_$
DECLARE
  arec mcuser;
BEGIN
  INSERT INTO mcuser (name, email, username, pswd, status, roles)
  VALUES (in_name, in_email, in_username, pass_hash(in_pswd), in_status, in_roles)
  RETURNING uid, created, modified, name, email, username, null, status, roles
  INTO arec;
  RETURN arec;
END
$_$;

/**
 * trigger_mcuser_updated
 */
CREATE TRIGGER trigger_mcuser_updated
  BEFORE UPDATE ON mcuser
  FOR EACH ROW
  EXECUTE PROCEDURE set_modified();

CREATE TYPE jwt_mcuser_status AS (
  mcuser_audit_record_type mcuser_audit_type,
  jwt_id uuid,
  jwt_id_status jwt_id_status,
  login_expiration timestamptz,
  uid uuid,
  mcuser_status mcuser_status
);
comment on type jwt_mcuser_status is 'The pertinent db columns bound to a held JWT ID in the mcuser_audit table.';

CREATE TYPE jwt_status AS ENUM (
  'PRESENT_BAD_STATE',
  'NOT_PRESENT',
  'BLACKLISTED',
  'EXPIRED',
  'MCUSER_NOTACTIVE',
  'VALID'
);
comment on type jwt_status is 'Convey the state of a JWT ID held in the mcuser_audit table.';

/**
  _fetch_latest_jwt_mcuser_rec()

  ** PRIVATE fn **

  Fetch the most recently created mcuser audit record with the given jwt id
  of either login or logout type.
  This is the authoritive record for determining the jwt id's status.
 */
CREATE OR REPLACE FUNCTION _fetch_latest_jwt_mcuser_rec(jwt_id uuid) RETURNS jwt_mcuser_status
LANGUAGE plpgsql AS
$_$
DECLARE
  qrec jwt_mcuser_status;
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
CREATE OR REPLACE FUNCTION get_jwt_status(jwt_id uuid) RETURNS jwt_status
LANGUAGE plpgsql AS
$_$
DECLARE
  qrec jwt_mcuser_status;
  jstat jwt_status;
BEGIN
  qrec := _fetch_latest_jwt_mcuser_rec(jwt_id);

  IF qrec is null or qrec.jwt_id is null THEN
    -- the input jwt id was not found
    RAISE NOTICE 'JWT id not found: %', jwt_id;
    jstat := 'NOT_PRESENT'::jwt_status;
  ELSEIF qrec.jwt_id_status = 'BLACKLISTED'::jwt_id_status THEN
    -- jwt id is marked as blacklisted (jwt_status is set to blacklisted upon logout)
    jstat := 'BLACKLISTED'::jwt_status;
  ELSEIF qrec.mcuser_status != 'ACTIVE'::mcuser_status THEN
    -- mcuser is not active
    jstat := 'MCUSER_NOTACTIVE'::jwt_status;
  ELSEIF qrec.mcuser_audit_record_type != 'LOGIN'::mcuser_audit_type THEN
    -- not a login mcuser audit record! (shouldn't happen but we check to be sure)
    RAISE NOTICE 'Expected LOGIN type mcuser_audit record';
    jstat := 'PRESENT_BAD_STATE'::jwt_status;
  ELSEIF qrec.login_expiration is null or qrec.login_expiration <= now() THEN
    -- either no login expiration date present or the jwt id has expired
    jstat := 'EXPIRED'::jwt_status;
  ELSEIF qrec.jwt_id_status = 'OK' THEN
    jstat := 'VALID'::jwt_status;
  ELSE
    -- error: unresolved jwt status
    RAISE NOTICE 'JWT id known but indeterminate state: %', jwt_id;
    jstat := 'PRESENT_BAD_STATE'::jwt_status;
  END IF;

  return jstat;
END
$_$;

/**
  get_active_logins()

  Return a table of active login records for a given mcuser.
 */
CREATE OR REPLACE FUNCTION get_active_logins(mcuser_id uuid) RETURNS TABLE (
  jwt_id uuid,
  login_expiration timestamptz,
  request_timestamp timestamptz,
  request_origin inet
)
LANGUAGE plpgsql AS
$_$
BEGIN
  return query
    select
      ma.jwt_id,
      ma.login_expiration,
      ma.request_timestamp,
      ma.request_origin
    from
      mcuser_audit ma
    where
      ma.uid = $1
      and ma.type = 'LOGIN'
      and ma.jwt_id_status = 'OK'
      and ma.login_expiration >= now()
      and ma.jwt_id not in (
        select ma2.jwt_id
        from mcuser_audit ma2
        where (ma2.type != 'LOGIN' or ma2.jwt_id_status != 'OK')
        and ma2.uid = $1
      );
END
$_$;

/**
  blacklist_jwt_ids_for()

  Insert new mcuser_audit records for each jwt_id held
  by the given mcuser id so that subsequent jwt id status queries
  will report them as blacklisted.

  @param in_uid the mcuser id for whom the jwt ids apply
  @param in_request_timestamp the instigating http request timestamp
  @param in_request_origin the instigating http request origin
 */
CREATE OR REPLACE FUNCTION blacklist_jwt_ids_for(
  in_uid uuid,
  in_request_timestamp timestamptz,
  in_request_origin inet
) RETURNS void
LANGUAGE plpgsql AS
$_$
BEGIN
  LOCK TABLE mcuser_audit IN ACCESS EXCLUSIVE MODE;

  insert into mcuser_audit
  (uid, type, request_timestamp, request_origin, jwt_id, jwt_id_status)
  select $1, 'LOGOUT'::mcuser_audit_type, $2, $3, adt.jwt_id, 'BLACKLISTED'::jwt_id_status
  from mcuser_audit adt
  where
    adt.uid = $1
    and adt.type = 'LOGIN'::mcuser_audit_type
    and adt.login_expiration >= now()
    and adt.jwt_id_status = 'OK'::jwt_id_status
    and adt.jwt_id not in (
      select jwt_id
      from mcuser_audit adtsub
      where (adtsub.type != 'LOGIN'::mcuser_audit_type or adtsub.jwt_id_status != 'OK'::jwt_id_status)
      and adtsub.uid = $1
    )
  ;
END
$_$;

CREATE OR REPLACE FUNCTION _blacklist_jwt(
  in_uid uuid,
  in_request_timestamp timestamptz,
  in_request_origin inet,
  in_jwt_id uuid
) RETURNS void
LANGUAGE plpgsql AS
$_$
BEGIN
  insert into mcuser_audit
  (uid, type, request_timestamp, request_origin, jwt_id, jwt_id_status)
  values (
    $1,
    'LOGOUT'::mcuser_audit_type,
    in_request_timestamp,
    in_request_origin,
    in_jwt_id,
    'BLACKLISTED'::jwt_id_status
  );
END
$_$;

/**
  _blacklist_jwt_ids_at_request_origin()

  *** PRIVATE fn ***

  Insert new mcuser_audit records for each jwt_id held
  by the given mcuser id at the given request origin
  so that subsequent jwt id status queries
  will report them as blacklisted.

  @param in_uid the mcuser id for whom the jwt ids apply
  @param in_request_origin the target request origin for which
         to blacklist any valid non-expired jwt ids
 */
CREATE OR REPLACE FUNCTION _blacklist_jwt_ids_at_request_origin(
  in_uid uuid,
  in_request_origin inet
) RETURNS void
LANGUAGE plpgsql AS
$_$
DECLARE
  vnow timestamptz;
BEGIN
  vnow := now();
  insert into mcuser_audit
  (uid, type, request_timestamp, request_origin, jwt_id, jwt_id_status)
  select $1, 'LOGOUT'::mcuser_audit_type, vnow, $2, adt.jwt_id, 'BLACKLISTED'::jwt_id_status
  from mcuser_audit adt
  where
    adt.uid = $1
    and adt.type = 'LOGIN'::mcuser_audit_type
    and adt.login_expiration >= vnow
    and adt.jwt_id_status = 'OK'::jwt_id_status
    and adt.request_origin = $2 -- constrain to the given request origin
    and adt.jwt_id not in (
      select jwt_id
      from mcuser_audit adtsub
      where (adtsub.type != 'LOGIN'::mcuser_audit_type or adtsub.jwt_id_status != 'OK'::jwt_id_status)
      and adtsub.uid = $1
      and adtsub.request_origin = $2 -- constrain to the given request origin
    )
  ;
END
$_$;

/**
  mcuser_pswd()

  @param in_uid the subject mcuser id
  @param in_pswd the pswd to set
 */
CREATE OR REPLACE FUNCTION mcuser_pswd(
  in_uid uuid,
  in_pswd text
) RETURNS void
LANGUAGE plpgsql AS
$_$
BEGIN
  update mcuser set pswd = pass_hash($2) where uid = $1;
END
$_$;

/*
  mcuser_refresh_login()

  Login w/out mcuser login credentials.
  Here we assume the web app has done due diligence to verify the user.
  UNSAFE!
*/
CREATE OR REPLACE FUNCTION mcuser_refresh_login(
  in_request_timestamp timestamptz,
  in_request_origin inet,
  in_login_expiration timestamptz,
  in_old_jwt_id uuid,
  in_new_jwt_id uuid
) RETURNS mcuser
LANGUAGE plpgsql AS
$_$
DECLARE
  passed BOOLEAN;
  uid uuid;
  qrec jwt_mcuser_status;
  mcuser_row mcuser%ROWTYPE;
BEGIN
  LOCK TABLE mcuser_audit IN ACCESS EXCLUSIVE MODE;

  qrec := _fetch_latest_jwt_mcuser_rec(in_old_jwt_id);

  IF qrec is null or qrec.jwt_id is null THEN
    -- the input jwt id was not found
    RAISE NOTICE 'JWT id not found: %', in_old_jwt_id;
    return null;
  ELSEIF qrec.jwt_id_status = 'BLACKLISTED'::jwt_id_status THEN
    -- jwt id is marked as blacklisted (jwt_status is set to blacklisted upon logout)
    RAISE NOTICE 'JWT id is blacklisted: %', in_old_jwt_id;
    return null;
  ELSEIF qrec.mcuser_status != 'ACTIVE'::mcuser_status THEN
    -- mcuser is not active
    RAISE NOTICE 'mcuser is not active: %', in_old_jwt_id;
    return null;
  ELSEIF qrec.mcuser_audit_record_type != 'LOGIN'::mcuser_audit_type THEN
    -- not a login mcuser audit record! (shouldn't happen but we check to be sure)
    RAISE NOTICE 'Expected LOGIN type mcuser_audit record';
    return null;
  ELSEIF qrec.jwt_id_status = 'OK' THEN
    --jstat := 'VALID'::jwt_status;
    -- fetch mcuser record and roles
    SELECT
      m.uid,
      m.created,
      m.modified,
      m.name,
      m.email,
      m.username,
      null,
      m.status,
      m.roles
    INTO mcuser_row
    FROM mcuser m
    WHERE m.uid = qrec.uid;

    -- invalidate the old jwt id
    perform _blacklist_jwt(
      qrec.uid,
      in_request_timestamp,
      in_request_origin,
      in_old_jwt_id
    );

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
      mcuser_row.uid,
      'LOGIN',
      in_request_timestamp,
      in_request_origin,
      in_login_expiration,
      in_new_jwt_id,
      'OK'::jwt_id_status
    );

    -- return the mcuser and roles
    RAISE NOTICE 'mcuser % refresh login successful', mcuser_row.uid;
    RETURN mcuser_row;
  ELSE
    -- error: unresolved jwt status
    RAISE NOTICE 'JWT id known but indeterminate state: %', jwt_id;
    return null;
  END IF;
END
$_$;

/*
  mcuser_login()

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
  in_request_timestamp timestamptz,
  in_request_origin inet,
  in_login_expiration timestamptz,
  in_jwt_id uuid
) RETURNS mcuser
LANGUAGE plpgsql AS
$_$
DECLARE
  existing_jwt_id uuid;
  passed BOOLEAN;
  uid uuid;
  rval mcuser%ROWTYPE;
BEGIN
  LOCK TABLE mcuser_audit IN ACCESS EXCLUSIVE MODE;

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

    -- fetch mcuser record and roles
    SELECT
      m.uid,
      m.created,
      m.modified,
      m.name,
      m.email,
      m.username,
      null,
      m.status,
      m.roles
    INTO rval
    FROM mcuser m
    WHERE m.username = $1;

    -- ** RULE: only allow *one* active mcuser login per request origin **
    -- blacklist existing valid and non-expired mcuser logins
    -- for the mcuser logging in at the given request origin
    perform _blacklist_jwt_ids_at_request_origin(rval.uid, in_request_origin);

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
      rval.uid,
      'LOGIN',
      in_request_timestamp,
      in_request_origin,
      in_login_expiration,
      in_jwt_id,
      'OK'::jwt_id_status
    );
    -- return the mcuser and roles
    RAISE NOTICE 'mcuser % logged in', rval.uid;
    RETURN rval;
  END IF;

  -- default
  RAISE NOTICE 'mcuser login failed';
  RETURN null;
END
$_$;

/**
  mcuser_logout()

  Logs an mcuser out.

  mcuser logout is only allowed when the bound jwt id and mcuser id
  are found to be currently logged in.

  An mcuser_audit record is created of LOGOUT type.
 */
CREATE OR REPLACE FUNCTION mcuser_logout(
  mcuser_uid uuid,
  jwt_id uuid,
  request_timestamp timestamptz,
  request_origin inet
) RETURNS boolean
LANGUAGE plpgsql AS
$_$
DECLARE
  jsi jwt_status;
BEGIN
  LOCK TABLE mcuser_audit IN ACCESS EXCLUSIVE MODE;

  -- logout is predicated on finding a single mcuser_audit record with the
  -- given mcuserId *and* jwtId.
  IF EXISTS(SELECT uid FROM mcuser_audit m WHERE m.uid = $1 and m.jwt_id = $2 and m.type = 'LOGIN') THEN
    -- at this point, we know a LOGIN record was created with the given jwt id and mcuser id.

    -- only allow mcuser logout when the latest jwt id status is valid
    jsi := get_jwt_status($2);

    IF jsi = 'VALID'::jwt_status THEN
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


-- **************************
-- *** mcorpus sub-schema ***
-- **************************

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

  created                 timestamptz not null default now(),
  modified                timestamptz null,

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
 * trigger_member_updated
 */
CREATE TRIGGER trigger_member_updated
  BEFORE UPDATE ON member
  FOR EACH ROW
  EXECUTE PROCEDURE set_modified();

create type mref AS (
  mid                     uuid,
  emp_id                  text,
  location                Location
);
comment on type mref is 'Uniquely identifies a single member in the corpus';

create table mauth (
  mid                     uuid primary key,

  modified                timestamptz not null default now(),

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
 * trigger_mauth_updated
 */
CREATE TRIGGER trigger_mauth_updated
  BEFORE UPDATE ON mauth
  FOR EACH ROW
  EXECUTE PROCEDURE set_modified();

/**
 * Add a member and mauth record (insert member).
 *
 * @return the added member fields as OUT params.
 */
CREATE OR REPLACE FUNCTION insert_member(
  in_emp_id text,
  in_location Location,
  in_name_first text,
  in_name_middle text,
  in_name_last text,
  in_display_name text,
  in_status member_status,
  in_dob date,
  in_ssn char(9),
  in_email_personal text,
  in_email_work text,
  in_mobile_phone text,
  in_home_phone text,
  in_work_phone text,
  in_fax text,
  in_username text,
  in_pswd text,

  OUT out_mid UUID,
  OUT out_created timestamptz,
  OUT out_modified timestamptz,
  OUT out_emp_id text,
  OUT out_location Location,
  OUT out_name_first text,
  OUT out_name_middle text,
  OUT out_name_last text,
  OUT out_display_name text,
  OUT out_status member_status,
  OUT out_dob date,
  OUT out_ssn char(9),
  OUT out_email_personal text,
  OUT out_email_work text,
  OUT out_mobile_phone text,
  OUT out_home_phone text,
  OUT out_work_phone text,
  OUT out_fax text,
  OUT out_username text
)
LANGUAGE plpgsql AS
$_$
BEGIN
  -- member
  INSERT INTO member (emp_id, location, name_first, name_middle, name_last, display_name, status)
  VALUES (in_emp_id, in_location, in_name_first, in_name_middle, in_name_last, in_display_name, in_status)
  RETURNING member.mid, member.created, member.modified, member.emp_id, member.location, member.name_first, member.name_middle, member.name_last, member.display_name, member.status
  INTO out_mid, out_created, out_modified, out_emp_id, out_location, out_name_first, out_name_middle, out_name_last, out_display_name, out_status
  ;
  -- mauth
  INSERT INTO mauth (mid, dob, ssn, email_personal, email_work, mobile_phone, home_phone, work_phone, fax, username, pswd)
  VALUES (out_mid, in_dob, in_ssn, in_email_personal, in_email_work, in_mobile_phone, in_home_phone, in_work_phone, in_fax, in_username, pass_hash(in_pswd))
  RETURNING mauth.dob, mauth.ssn, mauth.email_personal, mauth.email_work, mauth.mobile_phone, mauth.home_phone, mauth.work_phone, mauth.fax, mauth.username
  INTO out_dob, out_ssn, out_email_personal, out_email_work, out_mobile_phone, out_home_phone, out_work_phone, out_fax, out_username
  ;
END
$_$;

/**
 * member_pswd
 *
 * @param in_mid the subject member id
 * @param in_pswd the pswd to set
 */
CREATE OR REPLACE FUNCTION member_pswd(
  in_mid uuid,
  in_pswd text
) RETURNS void
LANGUAGE plpgsql AS
$_$
BEGIN
  update mauth set pswd = pass_hash($2) where mid = $1;
END
$_$;

create type member_audit_type as enum (
  'LOGIN',
  'LOGOUT'
);
comment on type member_audit_type is 'The member audit record/event type.';

create table member_audit (
  mid                     uuid not null, -- i.e. the member.mid
  created                 timestamptz not null default now(),
  type                    member_audit_type not null,
  request_timestamp       timestamptz not null,
  request_origin          inet not null,

  primary key (mid, created, type),
  foreign key ("mid") references member ("mid") on delete cascade
);
comment on type member_audit is 'Log of member login/logout events.';

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
  in_request_timestamp timestamptz,
  in_request_origin inet
) RETURNS public.mref
LANGUAGE plpgsql AS
$_$
DECLARE
  passed BOOLEAN;
  rec_mref mref;
BEGIN
  LOCK TABLE member_audit IN ACCESS EXCLUSIVE MODE;

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
  member_logout()

  Logs a member out.

  A member_audit record is created of LOGOUT type.

  @return:
    the member id of the member that was logged out upon success
    -OR-
    NULL when member logout fails for any reason.
*/
CREATE OR REPLACE FUNCTION member_logout(
  mid uuid,
  in_request_timestamp timestamptz,
  in_request_origin inet
) RETURNS UUID
  LANGUAGE plpgsql
AS $_$
DECLARE
  member_exists BOOLEAN;
BEGIN
  LOCK TABLE member_audit IN ACCESS EXCLUSIVE MODE;

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

  modified                timestamptz not null default now(),

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

/**
 * trigger_maddress_updated
 */
CREATE TRIGGER trigger_maddress_updated
  BEFORE UPDATE ON maddress
  FOR EACH ROW
  EXECUTE PROCEDURE set_modified();


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

  modified                timestamptz not null default now(),

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

/**
 * trigger_mbenefits_updated
 */
CREATE TRIGGER trigger_mbenefits_updated
  BEFORE UPDATE ON mbenefits
  FOR EACH ROW
  EXECUTE PROCEDURE set_modified();
