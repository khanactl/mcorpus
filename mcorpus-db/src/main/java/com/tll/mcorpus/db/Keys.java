/*
 * This file is generated by jOOQ.
*/
package com.tll.mcorpus.db;


import com.tll.mcorpus.db.tables.Maddress;
import com.tll.mcorpus.db.tables.Mauth;
import com.tll.mcorpus.db.tables.Mbenefits;
import com.tll.mcorpus.db.tables.Mcuser;
import com.tll.mcorpus.db.tables.McuserAudit;
import com.tll.mcorpus.db.tables.Member;
import com.tll.mcorpus.db.tables.MemberAudit;
import com.tll.mcorpus.db.tables.records.MaddressRecord;
import com.tll.mcorpus.db.tables.records.MauthRecord;
import com.tll.mcorpus.db.tables.records.MbenefitsRecord;
import com.tll.mcorpus.db.tables.records.McuserAuditRecord;
import com.tll.mcorpus.db.tables.records.McuserRecord;
import com.tll.mcorpus.db.tables.records.MemberAuditRecord;
import com.tll.mcorpus.db.tables.records.MemberRecord;

import javax.annotation.Generated;

import org.jooq.ForeignKey;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>public</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<MaddressRecord> MADDRESS_PKEY = UniqueKeys0.MADDRESS_PKEY;
    public static final UniqueKey<MauthRecord> MAUTH_PKEY = UniqueKeys0.MAUTH_PKEY;
    public static final UniqueKey<MauthRecord> UNIQUE_MEMBER_USERNAME = UniqueKeys0.UNIQUE_MEMBER_USERNAME;
    public static final UniqueKey<MbenefitsRecord> MBENEFITS_PKEY = UniqueKeys0.MBENEFITS_PKEY;
    public static final UniqueKey<McuserRecord> MCUSER_PKEY = UniqueKeys0.MCUSER_PKEY;
    public static final UniqueKey<McuserRecord> MCUSER_EMAIL_KEY = UniqueKeys0.MCUSER_EMAIL_KEY;
    public static final UniqueKey<McuserRecord> MCUSER_USERNAME_KEY = UniqueKeys0.MCUSER_USERNAME_KEY;
    public static final UniqueKey<McuserAuditRecord> MCUSER_AUDIT_PKEY = UniqueKeys0.MCUSER_AUDIT_PKEY;
    public static final UniqueKey<MemberRecord> MEMBER_PKEY = UniqueKeys0.MEMBER_PKEY;
    public static final UniqueKey<MemberRecord> MEMBER_EMP_ID_LOCATION_KEY = UniqueKeys0.MEMBER_EMP_ID_LOCATION_KEY;
    public static final UniqueKey<MemberAuditRecord> MEMBER_AUDIT_PKEY = UniqueKeys0.MEMBER_AUDIT_PKEY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<MaddressRecord, MemberRecord> MADDRESS__MADDRESS_MID_FKEY = ForeignKeys0.MADDRESS__MADDRESS_MID_FKEY;
    public static final ForeignKey<MauthRecord, MemberRecord> MAUTH__MAUTH_MID_FKEY = ForeignKeys0.MAUTH__MAUTH_MID_FKEY;
    public static final ForeignKey<MbenefitsRecord, MemberRecord> MBENEFITS__MBENEFITS_MID_FKEY = ForeignKeys0.MBENEFITS__MBENEFITS_MID_FKEY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class UniqueKeys0 {
        public static final UniqueKey<MaddressRecord> MADDRESS_PKEY = Internal.createUniqueKey(Maddress.MADDRESS, "maddress_pkey", Maddress.MADDRESS.MID, Maddress.MADDRESS.ADDRESS_NAME);
        public static final UniqueKey<MauthRecord> MAUTH_PKEY = Internal.createUniqueKey(Mauth.MAUTH, "mauth_pkey", Mauth.MAUTH.MID);
        public static final UniqueKey<MauthRecord> UNIQUE_MEMBER_USERNAME = Internal.createUniqueKey(Mauth.MAUTH, "unique_member_username", Mauth.MAUTH.USERNAME);
        public static final UniqueKey<MbenefitsRecord> MBENEFITS_PKEY = Internal.createUniqueKey(Mbenefits.MBENEFITS, "mbenefits_pkey", Mbenefits.MBENEFITS.MID);
        public static final UniqueKey<McuserRecord> MCUSER_PKEY = Internal.createUniqueKey(Mcuser.MCUSER, "mcuser_pkey", Mcuser.MCUSER.UID);
        public static final UniqueKey<McuserRecord> MCUSER_EMAIL_KEY = Internal.createUniqueKey(Mcuser.MCUSER, "mcuser_email_key", Mcuser.MCUSER.EMAIL);
        public static final UniqueKey<McuserRecord> MCUSER_USERNAME_KEY = Internal.createUniqueKey(Mcuser.MCUSER, "mcuser_username_key", Mcuser.MCUSER.USERNAME);
        public static final UniqueKey<McuserAuditRecord> MCUSER_AUDIT_PKEY = Internal.createUniqueKey(McuserAudit.MCUSER_AUDIT, "mcuser_audit_pkey", McuserAudit.MCUSER_AUDIT.UID, McuserAudit.MCUSER_AUDIT.CREATED, McuserAudit.MCUSER_AUDIT.TYPE);
        public static final UniqueKey<MemberRecord> MEMBER_PKEY = Internal.createUniqueKey(Member.MEMBER, "member_pkey", Member.MEMBER.MID);
        public static final UniqueKey<MemberRecord> MEMBER_EMP_ID_LOCATION_KEY = Internal.createUniqueKey(Member.MEMBER, "member_emp_id_location_key", Member.MEMBER.EMP_ID, Member.MEMBER.LOCATION);
        public static final UniqueKey<MemberAuditRecord> MEMBER_AUDIT_PKEY = Internal.createUniqueKey(MemberAudit.MEMBER_AUDIT, "member_audit_pkey", MemberAudit.MEMBER_AUDIT.CREATED, MemberAudit.MEMBER_AUDIT.TYPE);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<MaddressRecord, MemberRecord> MADDRESS__MADDRESS_MID_FKEY = Internal.createForeignKey(com.tll.mcorpus.db.Keys.MEMBER_PKEY, Maddress.MADDRESS, "maddress__maddress_mid_fkey", Maddress.MADDRESS.MID);
        public static final ForeignKey<MauthRecord, MemberRecord> MAUTH__MAUTH_MID_FKEY = Internal.createForeignKey(com.tll.mcorpus.db.Keys.MEMBER_PKEY, Mauth.MAUTH, "mauth__mauth_mid_fkey", Mauth.MAUTH.MID);
        public static final ForeignKey<MbenefitsRecord, MemberRecord> MBENEFITS__MBENEFITS_MID_FKEY = Internal.createForeignKey(com.tll.mcorpus.db.Keys.MEMBER_PKEY, Mbenefits.MBENEFITS, "mbenefits__mbenefits_mid_fkey", Mbenefits.MBENEFITS.MID);
    }
}
