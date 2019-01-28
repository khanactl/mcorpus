/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables;


import com.tll.mcorpus.db.Indexes;
import com.tll.mcorpus.db.Keys;
import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.enums.MemberAuditType;
import com.tll.mcorpus.db.tables.records.MemberAuditRecord;

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
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.8"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MemberAudit extends TableImpl<MemberAuditRecord> {

    private static final long serialVersionUID = -791644707;

    /**
     * The reference instance of <code>public.member_audit</code>
     */
    public static final MemberAudit MEMBER_AUDIT = new MemberAudit();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<MemberAuditRecord> getRecordType() {
        return MemberAuditRecord.class;
    }

    /**
     * The column <code>public.member_audit.mid</code>.
     */
    public final TableField<MemberAuditRecord, UUID> MID = createField("mid", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.member_audit.created</code>.
     */
    public final TableField<MemberAuditRecord, Timestamp> CREATED = createField("created", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.field("now()", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * The column <code>public.member_audit.type</code>.
     */
    public final TableField<MemberAuditRecord, MemberAuditType> TYPE = createField("type", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).asEnumDataType(com.tll.mcorpus.db.enums.MemberAuditType.class), this, "");

    /**
     * The column <code>public.member_audit.request_timestamp</code>.
     */
    public final TableField<MemberAuditRecord, Timestamp> REQUEST_TIMESTAMP = createField("request_timestamp", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "");

    /**
     * The column <code>public.member_audit.request_origin</code>.
     */
    public final TableField<MemberAuditRecord, String> REQUEST_ORIGIN = createField("request_origin", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * Create a <code>public.member_audit</code> table reference
     */
    public MemberAudit() {
        this(DSL.name("member_audit"), null);
    }

    /**
     * Create an aliased <code>public.member_audit</code> table reference
     */
    public MemberAudit(String alias) {
        this(DSL.name(alias), MEMBER_AUDIT);
    }

    /**
     * Create an aliased <code>public.member_audit</code> table reference
     */
    public MemberAudit(Name alias) {
        this(alias, MEMBER_AUDIT);
    }

    private MemberAudit(Name alias, Table<MemberAuditRecord> aliased) {
        this(alias, aliased, null);
    }

    private MemberAudit(Name alias, Table<MemberAuditRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> MemberAudit(Table<O> child, ForeignKey<O, MemberAuditRecord> key) {
        super(child, key, MEMBER_AUDIT);
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
        return Arrays.<Index>asList(Indexes.MEMBER_AUDIT_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<MemberAuditRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<MemberAuditRecord, ?>>asList(Keys.MEMBER_AUDIT__FKEY_MID_MEMBER);
    }

    public Member member() {
        return new Member(this, Keys.MEMBER_AUDIT__FKEY_MID_MEMBER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberAudit as(String alias) {
        return new MemberAudit(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberAudit as(Name alias) {
        return new MemberAudit(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public MemberAudit rename(String name) {
        return new MemberAudit(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public MemberAudit rename(Name name) {
        return new MemberAudit(name, null);
    }
}
