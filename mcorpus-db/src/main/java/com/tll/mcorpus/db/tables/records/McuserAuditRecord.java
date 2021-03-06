/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.records;


import com.tll.mcorpus.db.enums.JwtIdStatus;
import com.tll.mcorpus.db.enums.McuserAuditType;
import com.tll.mcorpus.db.tables.McuserAudit;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.jooq.Field;
import org.jooq.Record4;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class McuserAuditRecord extends UpdatableRecordImpl<McuserAuditRecord> implements Record8<UUID, OffsetDateTime, McuserAuditType, OffsetDateTime, String, OffsetDateTime, UUID, JwtIdStatus> {

    private static final long serialVersionUID = 1274062755;

    /**
     * Setter for <code>public.mcuser_audit.uid</code>.
     */
    public void setUid(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.mcuser_audit.uid</code>.
     */
    public UUID getUid() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.mcuser_audit.created</code>.
     */
    public void setCreated(OffsetDateTime value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.mcuser_audit.created</code>.
     */
    public OffsetDateTime getCreated() {
        return (OffsetDateTime) get(1);
    }

    /**
     * Setter for <code>public.mcuser_audit.type</code>.
     */
    public void setType(McuserAuditType value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.mcuser_audit.type</code>.
     */
    public McuserAuditType getType() {
        return (McuserAuditType) get(2);
    }

    /**
     * Setter for <code>public.mcuser_audit.request_timestamp</code>.
     */
    public void setRequestTimestamp(OffsetDateTime value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.mcuser_audit.request_timestamp</code>.
     */
    public OffsetDateTime getRequestTimestamp() {
        return (OffsetDateTime) get(3);
    }

    /**
     * Setter for <code>public.mcuser_audit.request_origin</code>.
     */
    public void setRequestOrigin(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.mcuser_audit.request_origin</code>.
     */
    public String getRequestOrigin() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.mcuser_audit.login_expiration</code>.
     */
    public void setLoginExpiration(OffsetDateTime value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.mcuser_audit.login_expiration</code>.
     */
    public OffsetDateTime getLoginExpiration() {
        return (OffsetDateTime) get(5);
    }

    /**
     * Setter for <code>public.mcuser_audit.jwt_id</code>.
     */
    public void setJwtId(UUID value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.mcuser_audit.jwt_id</code>.
     */
    public UUID getJwtId() {
        return (UUID) get(6);
    }

    /**
     * Setter for <code>public.mcuser_audit.jwt_id_status</code>.
     */
    public void setJwtIdStatus(JwtIdStatus value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.mcuser_audit.jwt_id_status</code>.
     */
    public JwtIdStatus getJwtIdStatus() {
        return (JwtIdStatus) get(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record4<UUID, OffsetDateTime, McuserAuditType, UUID> key() {
        return (Record4) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row8<UUID, OffsetDateTime, McuserAuditType, OffsetDateTime, String, OffsetDateTime, UUID, JwtIdStatus> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    @Override
    public Row8<UUID, OffsetDateTime, McuserAuditType, OffsetDateTime, String, OffsetDateTime, UUID, JwtIdStatus> valuesRow() {
        return (Row8) super.valuesRow();
    }

    @Override
    public Field<UUID> field1() {
        return McuserAudit.MCUSER_AUDIT.UID;
    }

    @Override
    public Field<OffsetDateTime> field2() {
        return McuserAudit.MCUSER_AUDIT.CREATED;
    }

    @Override
    public Field<McuserAuditType> field3() {
        return McuserAudit.MCUSER_AUDIT.TYPE;
    }

    @Override
    public Field<OffsetDateTime> field4() {
        return McuserAudit.MCUSER_AUDIT.REQUEST_TIMESTAMP;
    }

    @Override
    public Field<String> field5() {
        return McuserAudit.MCUSER_AUDIT.REQUEST_ORIGIN;
    }

    @Override
    public Field<OffsetDateTime> field6() {
        return McuserAudit.MCUSER_AUDIT.LOGIN_EXPIRATION;
    }

    @Override
    public Field<UUID> field7() {
        return McuserAudit.MCUSER_AUDIT.JWT_ID;
    }

    @Override
    public Field<JwtIdStatus> field8() {
        return McuserAudit.MCUSER_AUDIT.JWT_ID_STATUS;
    }

    @Override
    public UUID component1() {
        return getUid();
    }

    @Override
    public OffsetDateTime component2() {
        return getCreated();
    }

    @Override
    public McuserAuditType component3() {
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
    public OffsetDateTime component6() {
        return getLoginExpiration();
    }

    @Override
    public UUID component7() {
        return getJwtId();
    }

    @Override
    public JwtIdStatus component8() {
        return getJwtIdStatus();
    }

    @Override
    public UUID value1() {
        return getUid();
    }

    @Override
    public OffsetDateTime value2() {
        return getCreated();
    }

    @Override
    public McuserAuditType value3() {
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
    public OffsetDateTime value6() {
        return getLoginExpiration();
    }

    @Override
    public UUID value7() {
        return getJwtId();
    }

    @Override
    public JwtIdStatus value8() {
        return getJwtIdStatus();
    }

    @Override
    public McuserAuditRecord value1(UUID value) {
        setUid(value);
        return this;
    }

    @Override
    public McuserAuditRecord value2(OffsetDateTime value) {
        setCreated(value);
        return this;
    }

    @Override
    public McuserAuditRecord value3(McuserAuditType value) {
        setType(value);
        return this;
    }

    @Override
    public McuserAuditRecord value4(OffsetDateTime value) {
        setRequestTimestamp(value);
        return this;
    }

    @Override
    public McuserAuditRecord value5(String value) {
        setRequestOrigin(value);
        return this;
    }

    @Override
    public McuserAuditRecord value6(OffsetDateTime value) {
        setLoginExpiration(value);
        return this;
    }

    @Override
    public McuserAuditRecord value7(UUID value) {
        setJwtId(value);
        return this;
    }

    @Override
    public McuserAuditRecord value8(JwtIdStatus value) {
        setJwtIdStatus(value);
        return this;
    }

    @Override
    public McuserAuditRecord values(UUID value1, OffsetDateTime value2, McuserAuditType value3, OffsetDateTime value4, String value5, OffsetDateTime value6, UUID value7, JwtIdStatus value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached McuserAuditRecord
     */
    public McuserAuditRecord() {
        super(McuserAudit.MCUSER_AUDIT);
    }

    /**
     * Create a detached, initialised McuserAuditRecord
     */
    public McuserAuditRecord(UUID uid, OffsetDateTime created, McuserAuditType type, OffsetDateTime requestTimestamp, String requestOrigin, OffsetDateTime loginExpiration, UUID jwtId, JwtIdStatus jwtIdStatus) {
        super(McuserAudit.MCUSER_AUDIT);

        set(0, uid);
        set(1, created);
        set(2, type);
        set(3, requestTimestamp);
        set(4, requestOrigin);
        set(5, loginExpiration);
        set(6, jwtId);
        set(7, jwtIdStatus);
    }
}
