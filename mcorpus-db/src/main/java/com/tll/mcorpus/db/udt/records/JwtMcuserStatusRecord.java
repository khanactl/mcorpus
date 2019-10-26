/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.udt.records;


import com.tll.mcorpus.db.enums.JwtIdStatus;
import com.tll.mcorpus.db.enums.McuserAuditType;
import com.tll.mcorpus.db.enums.McuserStatus;
import com.tll.mcorpus.db.udt.JwtMcuserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UDTRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class JwtMcuserStatusRecord extends UDTRecordImpl<JwtMcuserStatusRecord> implements Record6<McuserAuditType, UUID, JwtIdStatus, OffsetDateTime, UUID, McuserStatus> {

    private static final long serialVersionUID = 1578773844;

    /**
     * Setter for <code>public.jwt_mcuser_status.mcuser_audit_record_type</code>.
     */
    public void setMcuserAuditRecordType(McuserAuditType value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.jwt_mcuser_status.mcuser_audit_record_type</code>.
     */
    public McuserAuditType getMcuserAuditRecordType() {
        return (McuserAuditType) get(0);
    }

    /**
     * Setter for <code>public.jwt_mcuser_status.jwt_id</code>.
     */
    public void setJwtId(UUID value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.jwt_mcuser_status.jwt_id</code>.
     */
    public UUID getJwtId() {
        return (UUID) get(1);
    }

    /**
     * Setter for <code>public.jwt_mcuser_status.jwt_id_status</code>.
     */
    public void setJwtIdStatus(JwtIdStatus value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.jwt_mcuser_status.jwt_id_status</code>.
     */
    public JwtIdStatus getJwtIdStatus() {
        return (JwtIdStatus) get(2);
    }

    /**
     * Setter for <code>public.jwt_mcuser_status.login_expiration</code>.
     */
    public void setLoginExpiration(OffsetDateTime value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.jwt_mcuser_status.login_expiration</code>.
     */
    public OffsetDateTime getLoginExpiration() {
        return (OffsetDateTime) get(3);
    }

    /**
     * Setter for <code>public.jwt_mcuser_status.uid</code>.
     */
    public void setUid(UUID value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.jwt_mcuser_status.uid</code>.
     */
    public UUID getUid() {
        return (UUID) get(4);
    }

    /**
     * Setter for <code>public.jwt_mcuser_status.mcuser_status</code>.
     */
    public void setMcuserStatus(McuserStatus value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.jwt_mcuser_status.mcuser_status</code>.
     */
    public McuserStatus getMcuserStatus() {
        return (McuserStatus) get(5);
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row6<McuserAuditType, UUID, JwtIdStatus, OffsetDateTime, UUID, McuserStatus> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    @Override
    public Row6<McuserAuditType, UUID, JwtIdStatus, OffsetDateTime, UUID, McuserStatus> valuesRow() {
        return (Row6) super.valuesRow();
    }

    @Override
    public Field<McuserAuditType> field1() {
        return JwtMcuserStatus.MCUSER_AUDIT_RECORD_TYPE;
    }

    @Override
    public Field<UUID> field2() {
        return JwtMcuserStatus.JWT_ID;
    }

    @Override
    public Field<JwtIdStatus> field3() {
        return JwtMcuserStatus.JWT_ID_STATUS;
    }

    @Override
    public Field<OffsetDateTime> field4() {
        return JwtMcuserStatus.LOGIN_EXPIRATION;
    }

    @Override
    public Field<UUID> field5() {
        return JwtMcuserStatus.UID;
    }

    @Override
    public Field<McuserStatus> field6() {
        return JwtMcuserStatus.MCUSER_STATUS;
    }

    @Override
    public McuserAuditType component1() {
        return getMcuserAuditRecordType();
    }

    @Override
    public UUID component2() {
        return getJwtId();
    }

    @Override
    public JwtIdStatus component3() {
        return getJwtIdStatus();
    }

    @Override
    public OffsetDateTime component4() {
        return getLoginExpiration();
    }

    @Override
    public UUID component5() {
        return getUid();
    }

    @Override
    public McuserStatus component6() {
        return getMcuserStatus();
    }

    @Override
    public McuserAuditType value1() {
        return getMcuserAuditRecordType();
    }

    @Override
    public UUID value2() {
        return getJwtId();
    }

    @Override
    public JwtIdStatus value3() {
        return getJwtIdStatus();
    }

    @Override
    public OffsetDateTime value4() {
        return getLoginExpiration();
    }

    @Override
    public UUID value5() {
        return getUid();
    }

    @Override
    public McuserStatus value6() {
        return getMcuserStatus();
    }

    @Override
    public JwtMcuserStatusRecord value1(McuserAuditType value) {
        setMcuserAuditRecordType(value);
        return this;
    }

    @Override
    public JwtMcuserStatusRecord value2(UUID value) {
        setJwtId(value);
        return this;
    }

    @Override
    public JwtMcuserStatusRecord value3(JwtIdStatus value) {
        setJwtIdStatus(value);
        return this;
    }

    @Override
    public JwtMcuserStatusRecord value4(OffsetDateTime value) {
        setLoginExpiration(value);
        return this;
    }

    @Override
    public JwtMcuserStatusRecord value5(UUID value) {
        setUid(value);
        return this;
    }

    @Override
    public JwtMcuserStatusRecord value6(McuserStatus value) {
        setMcuserStatus(value);
        return this;
    }

    @Override
    public JwtMcuserStatusRecord values(McuserAuditType value1, UUID value2, JwtIdStatus value3, OffsetDateTime value4, UUID value5, McuserStatus value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached JwtMcuserStatusRecord
     */
    public JwtMcuserStatusRecord() {
        super(JwtMcuserStatus.JWT_MCUSER_STATUS);
    }

    /**
     * Create a detached, initialised JwtMcuserStatusRecord
     */
    public JwtMcuserStatusRecord(McuserAuditType mcuserAuditRecordType, UUID jwtId, JwtIdStatus jwtIdStatus, OffsetDateTime loginExpiration, UUID uid, McuserStatus mcuserStatus) {
        super(JwtMcuserStatus.JWT_MCUSER_STATUS);

        set(0, mcuserAuditRecordType);
        set(1, jwtId);
        set(2, jwtIdStatus);
        set(3, loginExpiration);
        set(4, uid);
        set(5, mcuserStatus);
    }
}
