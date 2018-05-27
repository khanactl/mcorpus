/*
 * This file is generated by jOOQ.
*/
package com.tll.mcorpus.db.tables;


import com.tll.mcorpus.db.Indexes;
import com.tll.mcorpus.db.Keys;
import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.enums.McuserStatus;
import com.tll.mcorpus.db.tables.records.McuserRecord;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Index;
import org.jooq.Name;
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
        "jOOQ version:3.10.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Mcuser extends TableImpl<McuserRecord> {

    private static final long serialVersionUID = -832061641;

    /**
     * The reference instance of <code>public.mcuser</code>
     */
    public static final Mcuser MCUSER = new Mcuser();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<McuserRecord> getRecordType() {
        return McuserRecord.class;
    }

    /**
     * The column <code>public.mcuser.uid</code>.
     */
    public final TableField<McuserRecord, UUID> UID = createField("uid", org.jooq.impl.SQLDataType.UUID.nullable(false).defaultValue(org.jooq.impl.DSL.field("gen_random_uuid()", org.jooq.impl.SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.mcuser.created</code>.
     */
    public final TableField<McuserRecord, Timestamp> CREATED = createField("created", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.field("now()", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * The column <code>public.mcuser.modified</code>.
     */
    public final TableField<McuserRecord, Timestamp> MODIFIED = createField("modified", org.jooq.impl.SQLDataType.TIMESTAMP, this, "");

    /**
     * The column <code>public.mcuser.name</code>.
     */
    public final TableField<McuserRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.mcuser.email</code>.
     */
    public final TableField<McuserRecord, String> EMAIL = createField("email", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.mcuser.username</code>.
     */
    public final TableField<McuserRecord, String> USERNAME = createField("username", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.mcuser.pswd</code>.
     */
    public final TableField<McuserRecord, String> PSWD = createField("pswd", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.mcuser.admin</code>.
     */
    public final TableField<McuserRecord, Boolean> ADMIN = createField("admin", org.jooq.impl.SQLDataType.BOOLEAN.nullable(false), this, "");

    /**
     * The column <code>public.mcuser.status</code>.
     */
    public final TableField<McuserRecord, McuserStatus> STATUS = createField("status", org.jooq.util.postgres.PostgresDataType.VARCHAR.asEnumDataType(com.tll.mcorpus.db.enums.McuserStatus.class), this, "");

    /**
     * Create a <code>public.mcuser</code> table reference
     */
    public Mcuser() {
        this(DSL.name("mcuser"), null);
    }

    /**
     * Create an aliased <code>public.mcuser</code> table reference
     */
    public Mcuser(String alias) {
        this(DSL.name(alias), MCUSER);
    }

    /**
     * Create an aliased <code>public.mcuser</code> table reference
     */
    public Mcuser(Name alias) {
        this(alias, MCUSER);
    }

    private Mcuser(Name alias, Table<McuserRecord> aliased) {
        this(alias, aliased, null);
    }

    private Mcuser(Name alias, Table<McuserRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
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
        return Arrays.<Index>asList(Indexes.MCUSER_EMAIL_KEY, Indexes.MCUSER_PKEY, Indexes.MCUSER_USERNAME_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<McuserRecord> getPrimaryKey() {
        return Keys.MCUSER_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<McuserRecord>> getKeys() {
        return Arrays.<UniqueKey<McuserRecord>>asList(Keys.MCUSER_PKEY, Keys.MCUSER_EMAIL_KEY, Keys.MCUSER_USERNAME_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mcuser as(String alias) {
        return new Mcuser(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mcuser as(Name alias) {
        return new Mcuser(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Mcuser rename(String name) {
        return new Mcuser(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Mcuser rename(Name name) {
        return new Mcuser(name, null);
    }
}
