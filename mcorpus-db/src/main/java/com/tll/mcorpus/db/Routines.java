/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db;


import com.tll.mcorpus.db.enums.JwtStatus;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.McuserRole;
import com.tll.mcorpus.db.enums.McuserStatus;
import com.tll.mcorpus.db.enums.MemberStatus;
import com.tll.mcorpus.db.routines.BlacklistJwtIdsFor;
import com.tll.mcorpus.db.routines.GetJwtStatus;
import com.tll.mcorpus.db.routines.InsertMcuser;
import com.tll.mcorpus.db.routines.InsertMember;
import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.db.routines.McuserPswd;
import com.tll.mcorpus.db.routines.McuserRefreshLogin;
import com.tll.mcorpus.db.routines.MemberLogin;
import com.tll.mcorpus.db.routines.MemberLogout;
import com.tll.mcorpus.db.routines.MemberPswd;
import com.tll.mcorpus.db.routines.PassHash;
import com.tll.mcorpus.db.tables.GetActiveLogins;
import com.tll.mcorpus.db.tables.records.GetActiveLoginsRecord;
import com.tll.mcorpus.db.tables.records.McuserRecord;
import com.tll.mcorpus.db.udt.records.MrefRecord;

import java.net.InetAddress;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Result;


/**
 * Convenience access to all stored procedures and functions in public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Routines {

    /**
     * Call <code>public.blacklist_jwt_ids_for</code>
     */
    public static void blacklistJwtIdsFor(
          Configuration configuration
        , UUID inUid
        , OffsetDateTime inRequestTimestamp
        , InetAddress inRequestOrigin
    ) {
        BlacklistJwtIdsFor p = new BlacklistJwtIdsFor();
        p.setInUid(inUid);
        p.setInRequestTimestamp(inRequestTimestamp);
        p.setInRequestOrigin(inRequestOrigin);

        p.execute(configuration);
    }

    /**
     * Call <code>public.get_jwt_status</code>
     */
    public static JwtStatus getJwtStatus(
          Configuration configuration
        , UUID jwtId
    ) {
        GetJwtStatus f = new GetJwtStatus();
        f.setJwtId(jwtId);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>public.get_jwt_status</code> as a field.
     */
    public static Field<JwtStatus> getJwtStatus(
          UUID jwtId
    ) {
        GetJwtStatus f = new GetJwtStatus();
        f.setJwtId(jwtId);

        return f.asField();
    }

    /**
     * Get <code>public.get_jwt_status</code> as a field.
     */
    public static Field<JwtStatus> getJwtStatus(
          Field<UUID> jwtId
    ) {
        GetJwtStatus f = new GetJwtStatus();
        f.setJwtId(jwtId);

        return f.asField();
    }

    /**
     * Call <code>public.insert_mcuser</code>
     */
    public static McuserRecord insertMcuser(
          Configuration configuration
        , String inName
        , String inEmail
        , String inUsername
        , String inPswd
        , McuserStatus inStatus
        , McuserRole[] inRoles
    ) {
        InsertMcuser f = new InsertMcuser();
        f.setInName(inName);
        f.setInEmail(inEmail);
        f.setInUsername(inUsername);
        f.setInPswd(inPswd);
        f.setInStatus(inStatus);
        f.setInRoles(inRoles);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>public.insert_mcuser</code> as a field.
     */
    public static Field<McuserRecord> insertMcuser(
          String inName
        , String inEmail
        , String inUsername
        , String inPswd
        , McuserStatus inStatus
        , McuserRole[] inRoles
    ) {
        InsertMcuser f = new InsertMcuser();
        f.setInName(inName);
        f.setInEmail(inEmail);
        f.setInUsername(inUsername);
        f.setInPswd(inPswd);
        f.setInStatus(inStatus);
        f.setInRoles(inRoles);

        return f.asField();
    }

    /**
     * Get <code>public.insert_mcuser</code> as a field.
     */
    public static Field<McuserRecord> insertMcuser(
          Field<String> inName
        , Field<String> inEmail
        , Field<String> inUsername
        , Field<String> inPswd
        , Field<McuserStatus> inStatus
        , Field<McuserRole[]> inRoles
    ) {
        InsertMcuser f = new InsertMcuser();
        f.setInName(inName);
        f.setInEmail(inEmail);
        f.setInUsername(inUsername);
        f.setInPswd(inPswd);
        f.setInStatus(inStatus);
        f.setInRoles(inRoles);

        return f.asField();
    }

    /**
     * Call <code>public.insert_member</code>
     */
    public static InsertMember insertMember(
          Configuration configuration
        , String inEmpId
        , Location inLocation
        , String inNameFirst
        , String inNameMiddle
        , String inNameLast
        , String inDisplayName
        , MemberStatus inStatus
        , LocalDate inDob
        , String inSsn
        , String inEmailPersonal
        , String inEmailWork
        , String inMobilePhone
        , String inHomePhone
        , String inWorkPhone
        , String inFax
        , String inUsername
        , String inPswd
    ) {
        InsertMember p = new InsertMember();
        p.setInEmpId(inEmpId);
        p.setInLocation(inLocation);
        p.setInNameFirst(inNameFirst);
        p.setInNameMiddle(inNameMiddle);
        p.setInNameLast(inNameLast);
        p.setInDisplayName(inDisplayName);
        p.setInStatus(inStatus);
        p.setInDob(inDob);
        p.setInSsn(inSsn);
        p.setInEmailPersonal(inEmailPersonal);
        p.setInEmailWork(inEmailWork);
        p.setInMobilePhone(inMobilePhone);
        p.setInHomePhone(inHomePhone);
        p.setInWorkPhone(inWorkPhone);
        p.setInFax(inFax);
        p.setInUsername(inUsername);
        p.setInPswd(inPswd);

        p.execute(configuration);
        return p;
    }

    /**
     * Call <code>public.mcuser_login</code>
     */
    public static McuserRecord mcuserLogin(
          Configuration configuration
        , String mcuserUsername
        , String mcuserPassword
        , OffsetDateTime inRequestTimestamp
        , InetAddress inRequestOrigin
        , OffsetDateTime inLoginExpiration
        , UUID inJwtId
    ) {
        McuserLogin f = new McuserLogin();
        f.setMcuserUsername(mcuserUsername);
        f.setMcuserPassword(mcuserPassword);
        f.setInRequestTimestamp(inRequestTimestamp);
        f.setInRequestOrigin(inRequestOrigin);
        f.setInLoginExpiration(inLoginExpiration);
        f.setInJwtId(inJwtId);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>public.mcuser_login</code> as a field.
     */
    public static Field<McuserRecord> mcuserLogin(
          String mcuserUsername
        , String mcuserPassword
        , OffsetDateTime inRequestTimestamp
        , InetAddress inRequestOrigin
        , OffsetDateTime inLoginExpiration
        , UUID inJwtId
    ) {
        McuserLogin f = new McuserLogin();
        f.setMcuserUsername(mcuserUsername);
        f.setMcuserPassword(mcuserPassword);
        f.setInRequestTimestamp(inRequestTimestamp);
        f.setInRequestOrigin(inRequestOrigin);
        f.setInLoginExpiration(inLoginExpiration);
        f.setInJwtId(inJwtId);

        return f.asField();
    }

    /**
     * Get <code>public.mcuser_login</code> as a field.
     */
    public static Field<McuserRecord> mcuserLogin(
          Field<String> mcuserUsername
        , Field<String> mcuserPassword
        , Field<OffsetDateTime> inRequestTimestamp
        , Field<InetAddress> inRequestOrigin
        , Field<OffsetDateTime> inLoginExpiration
        , Field<UUID> inJwtId
    ) {
        McuserLogin f = new McuserLogin();
        f.setMcuserUsername(mcuserUsername);
        f.setMcuserPassword(mcuserPassword);
        f.setInRequestTimestamp(inRequestTimestamp);
        f.setInRequestOrigin(inRequestOrigin);
        f.setInLoginExpiration(inLoginExpiration);
        f.setInJwtId(inJwtId);

        return f.asField();
    }

    /**
     * Call <code>public.mcuser_logout</code>
     */
    public static Boolean mcuserLogout(
          Configuration configuration
        , UUID mcuserUid
        , UUID jwtId
        , OffsetDateTime requestTimestamp
        , InetAddress requestOrigin
    ) {
        McuserLogout f = new McuserLogout();
        f.setMcuserUid(mcuserUid);
        f.setJwtId(jwtId);
        f.setRequestTimestamp(requestTimestamp);
        f.setRequestOrigin(requestOrigin);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>public.mcuser_logout</code> as a field.
     */
    public static Field<Boolean> mcuserLogout(
          UUID mcuserUid
        , UUID jwtId
        , OffsetDateTime requestTimestamp
        , InetAddress requestOrigin
    ) {
        McuserLogout f = new McuserLogout();
        f.setMcuserUid(mcuserUid);
        f.setJwtId(jwtId);
        f.setRequestTimestamp(requestTimestamp);
        f.setRequestOrigin(requestOrigin);

        return f.asField();
    }

    /**
     * Get <code>public.mcuser_logout</code> as a field.
     */
    public static Field<Boolean> mcuserLogout(
          Field<UUID> mcuserUid
        , Field<UUID> jwtId
        , Field<OffsetDateTime> requestTimestamp
        , Field<InetAddress> requestOrigin
    ) {
        McuserLogout f = new McuserLogout();
        f.setMcuserUid(mcuserUid);
        f.setJwtId(jwtId);
        f.setRequestTimestamp(requestTimestamp);
        f.setRequestOrigin(requestOrigin);

        return f.asField();
    }

    /**
     * Call <code>public.mcuser_pswd</code>
     */
    public static void mcuserPswd(
          Configuration configuration
        , UUID inUid
        , String inPswd
    ) {
        McuserPswd p = new McuserPswd();
        p.setInUid(inUid);
        p.setInPswd(inPswd);

        p.execute(configuration);
    }

    /**
     * Call <code>public.mcuser_refresh_login</code>
     */
    public static McuserRecord mcuserRefreshLogin(
          Configuration configuration
        , OffsetDateTime inRequestTimestamp
        , InetAddress inRequestOrigin
        , OffsetDateTime inLoginExpiration
        , UUID inOldJwtId
        , UUID inNewJwtId
    ) {
        McuserRefreshLogin f = new McuserRefreshLogin();
        f.setInRequestTimestamp(inRequestTimestamp);
        f.setInRequestOrigin(inRequestOrigin);
        f.setInLoginExpiration(inLoginExpiration);
        f.setInOldJwtId(inOldJwtId);
        f.setInNewJwtId(inNewJwtId);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>public.mcuser_refresh_login</code> as a field.
     */
    public static Field<McuserRecord> mcuserRefreshLogin(
          OffsetDateTime inRequestTimestamp
        , InetAddress inRequestOrigin
        , OffsetDateTime inLoginExpiration
        , UUID inOldJwtId
        , UUID inNewJwtId
    ) {
        McuserRefreshLogin f = new McuserRefreshLogin();
        f.setInRequestTimestamp(inRequestTimestamp);
        f.setInRequestOrigin(inRequestOrigin);
        f.setInLoginExpiration(inLoginExpiration);
        f.setInOldJwtId(inOldJwtId);
        f.setInNewJwtId(inNewJwtId);

        return f.asField();
    }

    /**
     * Get <code>public.mcuser_refresh_login</code> as a field.
     */
    public static Field<McuserRecord> mcuserRefreshLogin(
          Field<OffsetDateTime> inRequestTimestamp
        , Field<InetAddress> inRequestOrigin
        , Field<OffsetDateTime> inLoginExpiration
        , Field<UUID> inOldJwtId
        , Field<UUID> inNewJwtId
    ) {
        McuserRefreshLogin f = new McuserRefreshLogin();
        f.setInRequestTimestamp(inRequestTimestamp);
        f.setInRequestOrigin(inRequestOrigin);
        f.setInLoginExpiration(inLoginExpiration);
        f.setInOldJwtId(inOldJwtId);
        f.setInNewJwtId(inNewJwtId);

        return f.asField();
    }

    /**
     * Call <code>public.member_login</code>
     */
    public static MrefRecord memberLogin(
          Configuration configuration
        , String memberUsername
        , String memberPassword
        , OffsetDateTime inRequestTimestamp
        , InetAddress inRequestOrigin
    ) {
        MemberLogin f = new MemberLogin();
        f.setMemberUsername(memberUsername);
        f.setMemberPassword(memberPassword);
        f.setInRequestTimestamp(inRequestTimestamp);
        f.setInRequestOrigin(inRequestOrigin);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>public.member_login</code> as a field.
     */
    public static Field<MrefRecord> memberLogin(
          String memberUsername
        , String memberPassword
        , OffsetDateTime inRequestTimestamp
        , InetAddress inRequestOrigin
    ) {
        MemberLogin f = new MemberLogin();
        f.setMemberUsername(memberUsername);
        f.setMemberPassword(memberPassword);
        f.setInRequestTimestamp(inRequestTimestamp);
        f.setInRequestOrigin(inRequestOrigin);

        return f.asField();
    }

    /**
     * Get <code>public.member_login</code> as a field.
     */
    public static Field<MrefRecord> memberLogin(
          Field<String> memberUsername
        , Field<String> memberPassword
        , Field<OffsetDateTime> inRequestTimestamp
        , Field<InetAddress> inRequestOrigin
    ) {
        MemberLogin f = new MemberLogin();
        f.setMemberUsername(memberUsername);
        f.setMemberPassword(memberPassword);
        f.setInRequestTimestamp(inRequestTimestamp);
        f.setInRequestOrigin(inRequestOrigin);

        return f.asField();
    }

    /**
     * Call <code>public.member_logout</code>
     */
    public static UUID memberLogout(
          Configuration configuration
        , UUID mid
        , OffsetDateTime inRequestTimestamp
        , InetAddress inRequestOrigin
    ) {
        MemberLogout f = new MemberLogout();
        f.setMid(mid);
        f.setInRequestTimestamp(inRequestTimestamp);
        f.setInRequestOrigin(inRequestOrigin);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>public.member_logout</code> as a field.
     */
    public static Field<UUID> memberLogout(
          UUID mid
        , OffsetDateTime inRequestTimestamp
        , InetAddress inRequestOrigin
    ) {
        MemberLogout f = new MemberLogout();
        f.setMid(mid);
        f.setInRequestTimestamp(inRequestTimestamp);
        f.setInRequestOrigin(inRequestOrigin);

        return f.asField();
    }

    /**
     * Get <code>public.member_logout</code> as a field.
     */
    public static Field<UUID> memberLogout(
          Field<UUID> mid
        , Field<OffsetDateTime> inRequestTimestamp
        , Field<InetAddress> inRequestOrigin
    ) {
        MemberLogout f = new MemberLogout();
        f.setMid(mid);
        f.setInRequestTimestamp(inRequestTimestamp);
        f.setInRequestOrigin(inRequestOrigin);

        return f.asField();
    }

    /**
     * Call <code>public.member_pswd</code>
     */
    public static void memberPswd(
          Configuration configuration
        , UUID inMid
        , String inPswd
    ) {
        MemberPswd p = new MemberPswd();
        p.setInMid(inMid);
        p.setInPswd(inPswd);

        p.execute(configuration);
    }

    /**
     * Call <code>public.pass_hash</code>
     */
    public static String passHash(
          Configuration configuration
        , String pswd
    ) {
        PassHash f = new PassHash();
        f.setPswd(pswd);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>public.pass_hash</code> as a field.
     */
    public static Field<String> passHash(
          String pswd
    ) {
        PassHash f = new PassHash();
        f.setPswd(pswd);

        return f.asField();
    }

    /**
     * Get <code>public.pass_hash</code> as a field.
     */
    public static Field<String> passHash(
          Field<String> pswd
    ) {
        PassHash f = new PassHash();
        f.setPswd(pswd);

        return f.asField();
    }

    /**
     * Call <code>public.get_active_logins</code>.
     */
    public static Result<GetActiveLoginsRecord> getActiveLogins(
          Configuration configuration
        , UUID mcuserId
    ) {
        return configuration.dsl().selectFrom(com.tll.mcorpus.db.tables.GetActiveLogins.GET_ACTIVE_LOGINS.call(
              mcuserId
        )).fetch();
    }

    /**
     * Get <code>public.get_active_logins</code> as a table.
     */
    public static GetActiveLogins getActiveLogins(
          UUID mcuserId
    ) {
        return com.tll.mcorpus.db.tables.GetActiveLogins.GET_ACTIVE_LOGINS.call(
              mcuserId
        );
    }

    /**
     * Get <code>public.get_active_logins</code> as a table.
     */
    public static GetActiveLogins getActiveLogins(
          Field<UUID> mcuserId
    ) {
        return com.tll.mcorpus.db.tables.GetActiveLogins.GET_ACTIVE_LOGINS.call(
              mcuserId
        );
    }
}
