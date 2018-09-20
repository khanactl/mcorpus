/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db;


import com.tll.mcorpus.db.tables.Maddress;
import com.tll.mcorpus.db.tables.Mauth;
import com.tll.mcorpus.db.tables.Mbenefits;
import com.tll.mcorpus.db.tables.Mcuser;
import com.tll.mcorpus.db.tables.McuserAudit;
import com.tll.mcorpus.db.tables.McuserRoles;
import com.tll.mcorpus.db.tables.Member;
import com.tll.mcorpus.db.tables.MemberAudit;
import com.tll.mcorpus.db.tables.PgpArmorHeaders;
import com.tll.mcorpus.db.tables.records.PgpArmorHeadersRecord;

import javax.annotation.Generated;

import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Result;


/**
 * Convenience access to all tables in public
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>public.maddress</code>.
     */
    public static final Maddress MADDRESS = com.tll.mcorpus.db.tables.Maddress.MADDRESS;

    /**
     * The table <code>public.mauth</code>.
     */
    public static final Mauth MAUTH = com.tll.mcorpus.db.tables.Mauth.MAUTH;

    /**
     * The table <code>public.mbenefits</code>.
     */
    public static final Mbenefits MBENEFITS = com.tll.mcorpus.db.tables.Mbenefits.MBENEFITS;

    /**
     * The table <code>public.mcuser</code>.
     */
    public static final Mcuser MCUSER = com.tll.mcorpus.db.tables.Mcuser.MCUSER;

    /**
     * The table <code>public.mcuser_audit</code>.
     */
    public static final McuserAudit MCUSER_AUDIT = com.tll.mcorpus.db.tables.McuserAudit.MCUSER_AUDIT;

    /**
     * The table <code>public.mcuser_roles</code>.
     */
    public static final McuserRoles MCUSER_ROLES = com.tll.mcorpus.db.tables.McuserRoles.MCUSER_ROLES;

    /**
     * The table <code>public.member</code>.
     */
    public static final Member MEMBER = com.tll.mcorpus.db.tables.Member.MEMBER;

    /**
     * The table <code>public.member_audit</code>.
     */
    public static final MemberAudit MEMBER_AUDIT = com.tll.mcorpus.db.tables.MemberAudit.MEMBER_AUDIT;

    /**
     * The table <code>public.pgp_armor_headers</code>.
     */
    public static final PgpArmorHeaders PGP_ARMOR_HEADERS = com.tll.mcorpus.db.tables.PgpArmorHeaders.PGP_ARMOR_HEADERS;

    /**
     * Call <code>public.pgp_armor_headers</code>.
     */
    public static Result<PgpArmorHeadersRecord> PGP_ARMOR_HEADERS(Configuration configuration, String __1) {
        return configuration.dsl().selectFrom(com.tll.mcorpus.db.tables.PgpArmorHeaders.PGP_ARMOR_HEADERS.call(__1)).fetch();
    }

    /**
     * Get <code>public.pgp_armor_headers</code> as a table.
     */
    public static PgpArmorHeaders PGP_ARMOR_HEADERS(String __1) {
        return com.tll.mcorpus.db.tables.PgpArmorHeaders.PGP_ARMOR_HEADERS.call(__1);
    }

    /**
     * Get <code>public.pgp_armor_headers</code> as a table.
     */
    public static PgpArmorHeaders PGP_ARMOR_HEADERS(Field<String> __1) {
        return com.tll.mcorpus.db.tables.PgpArmorHeaders.PGP_ARMOR_HEADERS.call(__1);
    }
}
