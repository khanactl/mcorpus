/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables;


import com.tll.mcorpus.db.Indexes;
import com.tll.mcorpus.db.Keys;
import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.tables.records.MauthRecord;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Mauth extends TableImpl<MauthRecord> {

    private static final long serialVersionUID = -2114637537;

    /**
     * The reference instance of <code>public.mauth</code>
     */
    public static final Mauth MAUTH = new Mauth();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<MauthRecord> getRecordType() {
        return MauthRecord.class;
    }

    /**
     * The column <code>public.mauth.mid</code>.
     */
    public final TableField<MauthRecord, UUID> MID = createField("mid", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.mauth.modified</code>.
     */
    public final TableField<MauthRecord, Timestamp> MODIFIED = createField("modified", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.field("now()", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * The column <code>public.mauth.dob</code>.
     */
    public final TableField<MauthRecord, Date> DOB = createField("dob", org.jooq.impl.SQLDataType.DATE.nullable(false), this, "");

    /**
     * The column <code>public.mauth.ssn</code>.
     */
    public final TableField<MauthRecord, String> SSN = createField("ssn", org.jooq.impl.SQLDataType.CHAR(9).nullable(false), this, "");

    /**
     * The column <code>public.mauth.email_personal</code>.
     */
    public final TableField<MauthRecord, String> EMAIL_PERSONAL = createField("email_personal", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.mauth.email_work</code>.
     */
    public final TableField<MauthRecord, String> EMAIL_WORK = createField("email_work", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.mauth.mobile_phone</code>.
     */
    public final TableField<MauthRecord, String> MOBILE_PHONE = createField("mobile_phone", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.mauth.home_phone</code>.
     */
    public final TableField<MauthRecord, String> HOME_PHONE = createField("home_phone", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.mauth.work_phone</code>.
     */
    public final TableField<MauthRecord, String> WORK_PHONE = createField("work_phone", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.mauth.fax</code>.
     */
    public final TableField<MauthRecord, String> FAX = createField("fax", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.mauth.username</code>.
     */
    public final TableField<MauthRecord, String> USERNAME = createField("username", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.mauth.pswd</code>.
     */
    public final TableField<MauthRecord, String> PSWD = createField("pswd", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * Create a <code>public.mauth</code> table reference
     */
    public Mauth() {
        this(DSL.name("mauth"), null);
    }

    /**
     * Create an aliased <code>public.mauth</code> table reference
     */
    public Mauth(String alias) {
        this(DSL.name(alias), MAUTH);
    }

    /**
     * Create an aliased <code>public.mauth</code> table reference
     */
    public Mauth(Name alias) {
        this(alias, MAUTH);
    }

    private Mauth(Name alias, Table<MauthRecord> aliased) {
        this(alias, aliased, null);
    }

    private Mauth(Name alias, Table<MauthRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Mauth(Table<O> child, ForeignKey<O, MauthRecord> key) {
        super(child, key, MAUTH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.MAUTH_PKEY, Indexes.MAUTH_USERNAME_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<MauthRecord> getPrimaryKey() {
        return Keys.MAUTH_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<MauthRecord>> getKeys() {
        return Arrays.<UniqueKey<MauthRecord>>asList(Keys.MAUTH_PKEY, Keys.MAUTH_USERNAME_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<MauthRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<MauthRecord, ?>>asList(Keys.MAUTH__MAUTH_MID_FKEY);
    }

    public Member member() {
        return new Member(this, Keys.MAUTH__MAUTH_MID_FKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mauth as(String alias) {
        return new Mauth(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mauth as(Name alias) {
        return new Mauth(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Mauth rename(String name) {
        return new Mauth(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Mauth rename(Name name) {
        return new Mauth(name, null);
    }
}
