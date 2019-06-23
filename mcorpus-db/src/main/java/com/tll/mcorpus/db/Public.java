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
import com.tll.mcorpus.db.tables.PgpArmorHeaders;
import com.tll.mcorpus.db.tables.records.PgpArmorHeadersRecord;
import com.tll.mcorpus.db.udt.JwtMcuserStatus;
import com.tll.mcorpus.db.udt.Mref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.UDT;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.11"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = -1909332054;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.maddress</code>.
     */
    public final Maddress MADDRESS = com.tll.mcorpus.db.tables.Maddress.MADDRESS;

    /**
     * The table <code>public.mauth</code>.
     */
    public final Mauth MAUTH = com.tll.mcorpus.db.tables.Mauth.MAUTH;

    /**
     * The table <code>public.mbenefits</code>.
     */
    public final Mbenefits MBENEFITS = com.tll.mcorpus.db.tables.Mbenefits.MBENEFITS;

    /**
     * The table <code>public.mcuser</code>.
     */
    public final Mcuser MCUSER = com.tll.mcorpus.db.tables.Mcuser.MCUSER;

    /**
     * The table <code>public.mcuser_audit</code>.
     */
    public final McuserAudit MCUSER_AUDIT = com.tll.mcorpus.db.tables.McuserAudit.MCUSER_AUDIT;

    /**
     * The table <code>public.member</code>.
     */
    public final Member MEMBER = com.tll.mcorpus.db.tables.Member.MEMBER;

    /**
     * The table <code>public.member_audit</code>.
     */
    public final MemberAudit MEMBER_AUDIT = com.tll.mcorpus.db.tables.MemberAudit.MEMBER_AUDIT;

    /**
     * The table <code>public.pgp_armor_headers</code>.
     */
    public final PgpArmorHeaders PGP_ARMOR_HEADERS = com.tll.mcorpus.db.tables.PgpArmorHeaders.PGP_ARMOR_HEADERS;

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

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            Maddress.MADDRESS,
            Mauth.MAUTH,
            Mbenefits.MBENEFITS,
            Mcuser.MCUSER,
            McuserAudit.MCUSER_AUDIT,
            Member.MEMBER,
            MemberAudit.MEMBER_AUDIT,
            PgpArmorHeaders.PGP_ARMOR_HEADERS);
    }

    @Override
    public final List<UDT<?>> getUDTs() {
        List result = new ArrayList();
        result.addAll(getUDTs0());
        return result;
    }

    private final List<UDT<?>> getUDTs0() {
        return Arrays.<UDT<?>>asList(
            JwtMcuserStatus.JWT_MCUSER_STATUS,
            Mref.MREF);
    }
}
