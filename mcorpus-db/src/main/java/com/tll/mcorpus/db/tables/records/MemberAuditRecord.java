/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.records;


import com.tll.mcorpus.db.enums.MemberAuditType;
import com.tll.mcorpus.db.tables.MemberAudit;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.3"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MemberAuditRecord extends UpdatableRecordImpl<MemberAuditRecord> implements Record5<UUID, OffsetDateTime, MemberAuditType, OffsetDateTime, String> {

    private static final long serialVersionUID = -223105576;

    /**
     * Setter for <code>public.member_audit.mid</code>.
     */
    public void setMid(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.member_audit.mid</code>.
     */
    public UUID getMid() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.member_audit.created</code>.
     */
    public void setCreated(OffsetDateTime value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.member_audit.created</code>.
     */
    public OffsetDateTime getCreated() {
        return (OffsetDateTime) get(1);
    }

    /**
     * Setter for <code>public.member_audit.type</code>.
     */
    public void setType(MemberAuditType value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.member_audit.type</code>.
     */
    public MemberAuditType getType() {
        return (MemberAuditType) get(2);
    }

    /**
     * Setter for <code>public.member_audit.request_timestamp</code>.
     */
    public void setRequestTimestamp(OffsetDateTime value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.member_audit.request_timestamp</code>.
     */
    public OffsetDateTime getRequestTimestamp() {
        return (OffsetDateTime) get(3);
    }

    /**
     * Setter for <code>public.member_audit.request_origin</code>.
     */
    public void setRequestOrigin(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.member_audit.request_origin</code>.
     */
    public String getRequestOrigin() {
        return (String) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record3<UUID, OffsetDateTime, MemberAuditType> key() {
        return (Record3) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row5<UUID, OffsetDateTime, MemberAuditType, OffsetDateTime, String> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    @Override
    public Row5<UUID, OffsetDateTime, MemberAuditType, OffsetDateTime, String> valuesRow() {
        return (Row5) super.valuesRow();
    }

    @Override
    public Field<UUID> field1() {
        return MemberAudit.MEMBER_AUDIT.MID;
    }

    @Override
    public Field<OffsetDateTime> field2() {
        return MemberAudit.MEMBER_AUDIT.CREATED;
    }

    @Override
    public Field<MemberAuditType> field3() {
        return MemberAudit.MEMBER_AUDIT.TYPE;
    }

    @Override
    public Field<OffsetDateTime> field4() {
        return MemberAudit.MEMBER_AUDIT.REQUEST_TIMESTAMP;
    }

    @Override
    public Field<String> field5() {
        return MemberAudit.MEMBER_AUDIT.REQUEST_ORIGIN;
    }

    @Override
    public UUID component1() {
        return getMid();
    }

    @Override
    public OffsetDateTime component2() {
        return getCreated();
    }

    @Override
    public MemberAuditType component3() {
        return getType();
    }

    @Override
    public OffsetDateTime component4() {
        return getRequestTimestamp();
    }

    @Override
    public String component5() {
        return getRequestOrigin();
    }

    @Override
    public UUID value1() {
        return getMid();
    }

    @Override
    public OffsetDateTime value2() {
        return getCreated();
    }

    @Override
    public MemberAuditType value3() {
        return getType();
    }

    @Override
    public OffsetDateTime value4() {
        return getRequestTimestamp();
    }

    @Override
    public String value5() {
        return getRequestOrigin();
    }

    @Override
    public MemberAuditRecord value1(UUID value) {
        setMid(value);
        return this;
    }

    @Override
    public MemberAuditRecord value2(OffsetDateTime value) {
        setCreated(value);
        return this;
    }

    @Override
    public MemberAuditRecord value3(MemberAuditType value) {
        setType(value);
        return this;
    }

    @Override
    public MemberAuditRecord value4(OffsetDateTime value) {
        setRequestTimestamp(value);
        return this;
    }

    @Override
    public MemberAuditRecord value5(String value) {
        setRequestOrigin(value);
        return this;
    }

    @Override
    public MemberAuditRecord values(UUID value1, OffsetDateTime value2, MemberAuditType value3, OffsetDateTime value4, String value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MemberAuditRecord
     */
    public MemberAuditRecord() {
        super(MemberAudit.MEMBER_AUDIT);
    }

    /**
     * Create a detached, initialised MemberAuditRecord
     */
    public MemberAuditRecord(UUID mid, OffsetDateTime created, MemberAuditType type, OffsetDateTime requestTimestamp, String requestOrigin) {
        super(MemberAudit.MEMBER_AUDIT);

        set(0, mid);
        set(1, created);
        set(2, type);
        set(3, requestTimestamp);
        set(4, requestOrigin);
    }
}
