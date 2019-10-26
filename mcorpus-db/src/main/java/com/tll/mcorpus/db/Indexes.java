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

import javax.annotation.Generated;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.Internal;


/**
 * A class modelling indexes of tables of the <code>public</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index MADDRESS_PKEY = Indexes0.MADDRESS_PKEY;
    public static final Index MAUTH_PKEY = Indexes0.MAUTH_PKEY;
    public static final Index MAUTH_USERNAME_KEY = Indexes0.MAUTH_USERNAME_KEY;
    public static final Index MBENEFITS_PKEY = Indexes0.MBENEFITS_PKEY;
    public static final Index MCUSER_PKEY = Indexes0.MCUSER_PKEY;
    public static final Index MCUSER_USERNAME_KEY = Indexes0.MCUSER_USERNAME_KEY;
    public static final Index MCUSER_AUDIT__JWT_ID = Indexes0.MCUSER_AUDIT__JWT_ID;
    public static final Index MCUSER_AUDIT_PKEY = Indexes0.MCUSER_AUDIT_PKEY;
    public static final Index MEMBER_EMP_ID_LOCATION_KEY = Indexes0.MEMBER_EMP_ID_LOCATION_KEY;
    public static final Index MEMBER_PKEY = Indexes0.MEMBER_PKEY;
    public static final Index MEMBER_AUDIT_PKEY = Indexes0.MEMBER_AUDIT_PKEY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 {
        public static Index MADDRESS_PKEY = Internal.createIndex("maddress_pkey", Maddress.MADDRESS, new OrderField[] { Maddress.MADDRESS.MID, Maddress.MADDRESS.ADDRESS_NAME }, true);
        public static Index MAUTH_PKEY = Internal.createIndex("mauth_pkey", Mauth.MAUTH, new OrderField[] { Mauth.MAUTH.MID }, true);
        public static Index MAUTH_USERNAME_KEY = Internal.createIndex("mauth_username_key", Mauth.MAUTH, new OrderField[] { Mauth.MAUTH.USERNAME }, true);
        public static Index MBENEFITS_PKEY = Internal.createIndex("mbenefits_pkey", Mbenefits.MBENEFITS, new OrderField[] { Mbenefits.MBENEFITS.MID }, true);
        public static Index MCUSER_PKEY = Internal.createIndex("mcuser_pkey", Mcuser.MCUSER, new OrderField[] { Mcuser.MCUSER.UID }, true);
        public static Index MCUSER_USERNAME_KEY = Internal.createIndex("mcuser_username_key", Mcuser.MCUSER, new OrderField[] { Mcuser.MCUSER.USERNAME }, true);
        public static Index MCUSER_AUDIT__JWT_ID = Internal.createIndex("mcuser_audit__jwt_id", McuserAudit.MCUSER_AUDIT, new OrderField[] { McuserAudit.MCUSER_AUDIT.JWT_ID }, false);
        public static Index MCUSER_AUDIT_PKEY = Internal.createIndex("mcuser_audit_pkey", McuserAudit.MCUSER_AUDIT, new OrderField[] { McuserAudit.MCUSER_AUDIT.UID, McuserAudit.MCUSER_AUDIT.CREATED, McuserAudit.MCUSER_AUDIT.TYPE, McuserAudit.MCUSER_AUDIT.JWT_ID }, true);
        public static Index MEMBER_EMP_ID_LOCATION_KEY = Internal.createIndex("member_emp_id_location_key", Member.MEMBER, new OrderField[] { Member.MEMBER.EMP_ID, Member.MEMBER.LOCATION }, true);
        public static Index MEMBER_PKEY = Internal.createIndex("member_pkey", Member.MEMBER, new OrderField[] { Member.MEMBER.MID }, true);
        public static Index MEMBER_AUDIT_PKEY = Internal.createIndex("member_audit_pkey", MemberAudit.MEMBER_AUDIT, new OrderField[] { MemberAudit.MEMBER_AUDIT.MID, MemberAudit.MEMBER_AUDIT.CREATED, MemberAudit.MEMBER_AUDIT.TYPE }, true);
    }
}
