/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables;


import com.tll.mcorpus.db.Indexes;
import com.tll.mcorpus.db.Keys;
import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.enums.JwtIdStatus;
import com.tll.mcorpus.db.enums.McuserAuditType;
import com.tll.mcorpus.db.tables.records.McuserAuditRecord;

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
        "jOOQ version:3.11.11"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class McuserAudit extends TableImpl<McuserAuditRecord> {

    private static final long serialVersionUID = -338446340;

    /**
     * The reference instance of <code>public.mcuser_audit</code>
     */
    public static final McuserAudit MCUSER_AUDIT = new McuserAudit();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<McuserAuditRecord> getRecordType() {
        return McuserAuditRecord.class;
    }

    /**
     * The column <code>public.mcuser_audit.uid</code>.
     */
    public final TableField<McuserAuditRecord, UUID> UID = createField("uid", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.mcuser_audit.created</code>.
     */
    public final TableField<McuserAuditRecord, Timestamp> CREATED = createField("created", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.field("now()", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * The column <code>public.mcuser_audit.type</code>.
     */
    public final TableField<McuserAuditRecord, McuserAuditType> TYPE = createField("type", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).asEnumDataType(com.tll.mcorpus.db.enums.McuserAuditType.class), this, "");

    /**
     * The column <code>public.mcuser_audit.request_timestamp</code>.
     */
    public final TableField<McuserAuditRecord, Timestamp> REQUEST_TIMESTAMP = createField("request_timestamp", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "");

    /**
     * The column <code>public.mcuser_audit.request_origin</code>.
     */
    public final TableField<McuserAuditRecord, String> REQUEST_ORIGIN = createField("request_origin", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.mcuser_audit.login_expiration</code>.
     */
    public final TableField<McuserAuditRecord, Timestamp> LOGIN_EXPIRATION = createField("login_expiration", org.jooq.impl.SQLDataType.TIMESTAMP, this, "");

    /**
     * The column <code>public.mcuser_audit.jwt_id</code>.
     */
    public final TableField<McuserAuditRecord, UUID> JWT_ID = createField("jwt_id", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.mcuser_audit.jwt_id_status</code>.
     */
    public final TableField<McuserAuditRecord, JwtIdStatus> JWT_ID_STATUS = createField("jwt_id_status", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).asEnumDataType(com.tll.mcorpus.db.enums.JwtIdStatus.class), this, "");

    /**
     * Create a <code>public.mcuser_audit</code> table reference
     */
    public McuserAudit() {
        this(DSL.name("mcuser_audit"), null);
    }

    /**
     * Create an aliased <code>public.mcuser_audit</code> table reference
     */
    public McuserAudit(String alias) {
        this(DSL.name(alias), MCUSER_AUDIT);
    }

    /**
     * Create an aliased <code>public.mcuser_audit</code> table reference
     */
    public McuserAudit(Name alias) {
        this(alias, MCUSER_AUDIT);
    }

    private McuserAudit(Name alias, Table<McuserAuditRecord> aliased) {
        this(alias, aliased, null);
    }

    private McuserAudit(Name alias, Table<McuserAuditRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> McuserAudit(Table<O> child, ForeignKey<O, McuserAuditRecord> key) {
        super(child, key, MCUSER_AUDIT);
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
        return Arrays.<Index>asList(Indexes.MCUSER_AUDIT__JWT_ID, Indexes.MCUSER_AUDIT_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<McuserAuditRecord> getPrimaryKey() {
        return Keys.MCUSER_AUDIT_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<McuserAuditRecord>> getKeys() {
        return Arrays.<UniqueKey<McuserAuditRecord>>asList(Keys.MCUSER_AUDIT_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<McuserAuditRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<McuserAuditRecord, ?>>asList(Keys.MCUSER_AUDIT__MCUSER_AUDIT_UID_FKEY);
    }

    public Mcuser mcuser() {
        return new Mcuser(this, Keys.MCUSER_AUDIT__MCUSER_AUDIT_UID_FKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserAudit as(String alias) {
        return new McuserAudit(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserAudit as(Name alias) {
        return new McuserAudit(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public McuserAudit rename(String name) {
        return new McuserAudit(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public McuserAudit rename(Name name) {
        return new McuserAudit(name, null);
    }
}
