/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables;


import com.tll.mcorpus.db.Indexes;
import com.tll.mcorpus.db.Keys;
import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.MemberStatus;
import com.tll.mcorpus.db.tables.records.MemberRecord;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row10;
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
        "jOOQ version:3.12.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Member extends TableImpl<MemberRecord> {

    private static final long serialVersionUID = 404228299;

    /**
     * The reference instance of <code>public.member</code>
     */
    public static final Member MEMBER = new Member();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<MemberRecord> getRecordType() {
        return MemberRecord.class;
    }

    /**
     * The column <code>public.member.mid</code>.
     */
    public final TableField<MemberRecord, UUID> MID = createField(DSL.name("mid"), org.jooq.impl.SQLDataType.UUID.nullable(false).defaultValue(org.jooq.impl.DSL.field("gen_random_uuid()", org.jooq.impl.SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.member.created</code>.
     */
    public final TableField<MemberRecord, OffsetDateTime> CREATED = createField(DSL.name("created"), org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE.nullable(false).defaultValue(org.jooq.impl.DSL.field("now()", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE)), this, "");

    /**
     * The column <code>public.member.modified</code>.
     */
    public final TableField<MemberRecord, OffsetDateTime> MODIFIED = createField(DSL.name("modified"), org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.member.emp_id</code>.
     */
    public final TableField<MemberRecord, String> EMP_ID = createField(DSL.name("emp_id"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.member.location</code>.
     */
    public final TableField<MemberRecord, Location> LOCATION = createField(DSL.name("location"), org.jooq.impl.SQLDataType.VARCHAR.nullable(false).asEnumDataType(com.tll.mcorpus.db.enums.Location.class), this, "");

    /**
     * The column <code>public.member.name_first</code>.
     */
    public final TableField<MemberRecord, String> NAME_FIRST = createField(DSL.name("name_first"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.member.name_middle</code>.
     */
    public final TableField<MemberRecord, String> NAME_MIDDLE = createField(DSL.name("name_middle"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.member.name_last</code>.
     */
    public final TableField<MemberRecord, String> NAME_LAST = createField(DSL.name("name_last"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.member.display_name</code>.
     */
    public final TableField<MemberRecord, String> DISPLAY_NAME = createField(DSL.name("display_name"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.member.status</code>.
     */
    public final TableField<MemberRecord, MemberStatus> STATUS = createField(DSL.name("status"), org.jooq.impl.SQLDataType.VARCHAR.nullable(false).defaultValue(org.jooq.impl.DSL.field("'ACTIVE'::member_status", org.jooq.impl.SQLDataType.VARCHAR)).asEnumDataType(com.tll.mcorpus.db.enums.MemberStatus.class), this, "");

    /**
     * Create a <code>public.member</code> table reference
     */
    public Member() {
        this(DSL.name("member"), null);
    }

    /**
     * Create an aliased <code>public.member</code> table reference
     */
    public Member(String alias) {
        this(DSL.name(alias), MEMBER);
    }

    /**
     * Create an aliased <code>public.member</code> table reference
     */
    public Member(Name alias) {
        this(alias, MEMBER);
    }

    private Member(Name alias, Table<MemberRecord> aliased) {
        this(alias, aliased, null);
    }

    private Member(Name alias, Table<MemberRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Member(Table<O> child, ForeignKey<O, MemberRecord> key) {
        super(child, key, MEMBER);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.MEMBER_EMP_ID_LOCATION_KEY, Indexes.MEMBER_PKEY);
    }

    @Override
    public UniqueKey<MemberRecord> getPrimaryKey() {
        return Keys.MEMBER_PKEY;
    }

    @Override
    public List<UniqueKey<MemberRecord>> getKeys() {
        return Arrays.<UniqueKey<MemberRecord>>asList(Keys.MEMBER_PKEY, Keys.MEMBER_EMP_ID_LOCATION_KEY);
    }

    @Override
    public Member as(String alias) {
        return new Member(DSL.name(alias), this);
    }

    @Override
    public Member as(Name alias) {
        return new Member(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Member rename(String name) {
        return new Member(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Member rename(Name name) {
        return new Member(name, null);
    }

    // -------------------------------------------------------------------------
    // Row10 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row10<UUID, OffsetDateTime, OffsetDateTime, String, Location, String, String, String, String, MemberStatus> fieldsRow() {
        return (Row10) super.fieldsRow();
    }
}
