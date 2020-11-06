/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.records;


import com.tll.mcorpus.db.tables.GetActiveLogins;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.jooq.Field;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GetActiveLoginsRecord extends TableRecordImpl<GetActiveLoginsRecord> implements Record4<UUID, OffsetDateTime, OffsetDateTime, InetAddress> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.get_active_logins.jwt_id</code>.
     */
    public void setJwtId(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.get_active_logins.jwt_id</code>.
     */
    public UUID getJwtId() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.get_active_logins.login_expiration</code>.
     */
    public void setLoginExpiration(OffsetDateTime value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.get_active_logins.login_expiration</code>.
     */
    public OffsetDateTime getLoginExpiration() {
        return (OffsetDateTime) get(1);
    }

    /**
     * Setter for <code>public.get_active_logins.request_timestamp</code>.
     */
    public void setRequestTimestamp(OffsetDateTime value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.get_active_logins.request_timestamp</code>.
     */
    public OffsetDateTime getRequestTimestamp() {
        return (OffsetDateTime) get(2);
    }

    /**
     * Setter for <code>public.get_active_logins.request_origin</code>.
     */
    public void setRequestOrigin(InetAddress value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.get_active_logins.request_origin</code>.
     */
    public InetAddress getRequestOrigin() {
        return (InetAddress) get(3);
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<UUID, OffsetDateTime, OffsetDateTime, InetAddress> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<UUID, OffsetDateTime, OffsetDateTime, InetAddress> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<UUID> field1() {
        return GetActiveLogins.GET_ACTIVE_LOGINS.JWT_ID;
    }

    @Override
    public Field<OffsetDateTime> field2() {
        return GetActiveLogins.GET_ACTIVE_LOGINS.LOGIN_EXPIRATION;
    }

    @Override
    public Field<OffsetDateTime> field3() {
        return GetActiveLogins.GET_ACTIVE_LOGINS.REQUEST_TIMESTAMP;
    }

    @Override
    public Field<InetAddress> field4() {
        return GetActiveLogins.GET_ACTIVE_LOGINS.REQUEST_ORIGIN;
    }

    @Override
    public UUID component1() {
        return getJwtId();
    }

    @Override
    public OffsetDateTime component2() {
        return getLoginExpiration();
    }

    @Override
    public OffsetDateTime component3() {
        return getRequestTimestamp();
    }

    @Override
    public InetAddress component4() {
        return getRequestOrigin();
    }

    @Override
    public UUID value1() {
        return getJwtId();
    }

    @Override
    public OffsetDateTime value2() {
        return getLoginExpiration();
    }

    @Override
    public OffsetDateTime value3() {
        return getRequestTimestamp();
    }

    @Override
    public InetAddress value4() {
        return getRequestOrigin();
    }

    @Override
    public GetActiveLoginsRecord value1(UUID value) {
        setJwtId(value);
        return this;
    }

    @Override
    public GetActiveLoginsRecord value2(OffsetDateTime value) {
        setLoginExpiration(value);
        return this;
    }

    @Override
    public GetActiveLoginsRecord value3(OffsetDateTime value) {
        setRequestTimestamp(value);
        return this;
    }

    @Override
    public GetActiveLoginsRecord value4(InetAddress value) {
        setRequestOrigin(value);
        return this;
    }

    @Override
    public GetActiveLoginsRecord values(UUID value1, OffsetDateTime value2, OffsetDateTime value3, InetAddress value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached GetActiveLoginsRecord
     */
    public GetActiveLoginsRecord() {
        super(GetActiveLogins.GET_ACTIVE_LOGINS);
    }

    /**
     * Create a detached, initialised GetActiveLoginsRecord
     */
    public GetActiveLoginsRecord(UUID jwtId, OffsetDateTime loginExpiration, OffsetDateTime requestTimestamp, InetAddress requestOrigin) {
        super(GetActiveLogins.GET_ACTIVE_LOGINS);

        setJwtId(jwtId);
        setLoginExpiration(loginExpiration);
        setRequestTimestamp(requestTimestamp);
        setRequestOrigin(requestOrigin);
    }
}
