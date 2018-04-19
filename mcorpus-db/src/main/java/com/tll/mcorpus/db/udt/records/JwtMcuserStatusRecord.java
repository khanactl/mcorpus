/*
 * This file is generated by jOOQ.
*/
package com.tll.mcorpus.db.udt.records;


import com.tll.mcorpus.db.enums.JwtIdStatus;
import com.tll.mcorpus.db.enums.McuserStatus;
import com.tll.mcorpus.db.udt.JwtMcuserStatus;

import java.sql.Timestamp;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UDTRecordImpl;


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
public class JwtMcuserStatusRecord extends UDTRecordImpl<JwtMcuserStatusRecord> implements Record5<UUID, JwtIdStatus, Timestamp, McuserStatus, Boolean> {

    private static final long serialVersionUID = 1296632817;

    /**
     * Setter for <code>public.jwt_mcuser_status.jwt_id</code>.
     */
    public void setJwtId(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.jwt_mcuser_status.jwt_id</code>.
     */
    public UUID getJwtId() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.jwt_mcuser_status.jwt_id_status</code>.
     */
    public void setJwtIdStatus(JwtIdStatus value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.jwt_mcuser_status.jwt_id_status</code>.
     */
    public JwtIdStatus getJwtIdStatus() {
        return (JwtIdStatus) get(1);
    }

    /**
     * Setter for <code>public.jwt_mcuser_status.login_expiration</code>.
     */
    public void setLoginExpiration(Timestamp value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.jwt_mcuser_status.login_expiration</code>.
     */
    public Timestamp getLoginExpiration() {
        return (Timestamp) get(2);
    }

    /**
     * Setter for <code>public.jwt_mcuser_status.mcuser_status</code>.
     */
    public void setMcuserStatus(McuserStatus value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.jwt_mcuser_status.mcuser_status</code>.
     */
    public McuserStatus getMcuserStatus() {
        return (McuserStatus) get(3);
    }

    /**
     * Setter for <code>public.jwt_mcuser_status.admin</code>.
     */
    public void setAdmin(Boolean value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.jwt_mcuser_status.admin</code>.
     */
    public Boolean getAdmin() {
        return (Boolean) get(4);
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<UUID, JwtIdStatus, Timestamp, McuserStatus, Boolean> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<UUID, JwtIdStatus, Timestamp, McuserStatus, Boolean> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field1() {
        return JwtMcuserStatus.JWT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<JwtIdStatus> field2() {
        return JwtMcuserStatus.JWT_ID_STATUS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field3() {
        return JwtMcuserStatus.LOGIN_EXPIRATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<McuserStatus> field4() {
        return JwtMcuserStatus.MCUSER_STATUS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field5() {
        return JwtMcuserStatus.ADMIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID component1() {
        return getJwtId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtIdStatus component2() {
        return getJwtIdStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component3() {
        return getLoginExpiration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserStatus component4() {
        return getMcuserStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean component5() {
        return getAdmin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID value1() {
        return getJwtId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtIdStatus value2() {
        return getJwtIdStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value3() {
        return getLoginExpiration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserStatus value4() {
        return getMcuserStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value5() {
        return getAdmin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtMcuserStatusRecord value1(UUID value) {
        setJwtId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtMcuserStatusRecord value2(JwtIdStatus value) {
        setJwtIdStatus(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtMcuserStatusRecord value3(Timestamp value) {
        setLoginExpiration(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtMcuserStatusRecord value4(McuserStatus value) {
        setMcuserStatus(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtMcuserStatusRecord value5(Boolean value) {
        setAdmin(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtMcuserStatusRecord values(UUID value1, JwtIdStatus value2, Timestamp value3, McuserStatus value4, Boolean value5) {
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
     * Create a detached JwtMcuserStatusRecord
     */
    public JwtMcuserStatusRecord() {
        super(JwtMcuserStatus.JWT_MCUSER_STATUS);
    }

    /**
     * Create a detached, initialised JwtMcuserStatusRecord
     */
    public JwtMcuserStatusRecord(UUID jwtId, JwtIdStatus jwtIdStatus, Timestamp loginExpiration, McuserStatus mcuserStatus, Boolean admin) {
        super(JwtMcuserStatus.JWT_MCUSER_STATUS);

        set(0, jwtId);
        set(1, jwtIdStatus);
        set(2, loginExpiration);
        set(3, mcuserStatus);
        set(4, admin);
    }
}
