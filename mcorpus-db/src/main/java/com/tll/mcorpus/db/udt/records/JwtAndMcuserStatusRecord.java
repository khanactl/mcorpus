/*
 * This file is generated by jOOQ.
*/
package com.tll.mcorpus.db.udt.records;


import com.tll.mcorpus.db.enums.JwtStatus;
import com.tll.mcorpus.db.enums.McuserStatus;
import com.tll.mcorpus.db.udt.JwtAndMcuserStatus;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
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
public class JwtAndMcuserStatusRecord extends UDTRecordImpl<JwtAndMcuserStatusRecord> implements Record2<JwtStatus, McuserStatus> {

    private static final long serialVersionUID = 1644319480;

    /**
     * Setter for <code>public.jwt_and_mcuser_status.jwt_id_status</code>.
     */
    public void setJwtIdStatus(JwtStatus value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.jwt_and_mcuser_status.jwt_id_status</code>.
     */
    public JwtStatus getJwtIdStatus() {
        return (JwtStatus) get(0);
    }

    /**
     * Setter for <code>public.jwt_and_mcuser_status.mcuser_status</code>.
     */
    public void setMcuserStatus(McuserStatus value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.jwt_and_mcuser_status.mcuser_status</code>.
     */
    public McuserStatus getMcuserStatus() {
        return (McuserStatus) get(1);
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<JwtStatus, McuserStatus> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<JwtStatus, McuserStatus> valuesRow() {
        return (Row2) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<JwtStatus> field1() {
        return JwtAndMcuserStatus.JWT_ID_STATUS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<McuserStatus> field2() {
        return JwtAndMcuserStatus.MCUSER_STATUS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtStatus component1() {
        return getJwtIdStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserStatus component2() {
        return getMcuserStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtStatus value1() {
        return getJwtIdStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserStatus value2() {
        return getMcuserStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtAndMcuserStatusRecord value1(JwtStatus value) {
        setJwtIdStatus(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtAndMcuserStatusRecord value2(McuserStatus value) {
        setMcuserStatus(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtAndMcuserStatusRecord values(JwtStatus value1, McuserStatus value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached JwtAndMcuserStatusRecord
     */
    public JwtAndMcuserStatusRecord() {
        super(JwtAndMcuserStatus.JWT_AND_MCUSER_STATUS);
    }

    /**
     * Create a detached, initialised JwtAndMcuserStatusRecord
     */
    public JwtAndMcuserStatusRecord(JwtStatus jwtIdStatus, McuserStatus mcuserStatus) {
        super(JwtAndMcuserStatus.JWT_AND_MCUSER_STATUS);

        set(0, jwtIdStatus);
        set(1, mcuserStatus);
    }
}
