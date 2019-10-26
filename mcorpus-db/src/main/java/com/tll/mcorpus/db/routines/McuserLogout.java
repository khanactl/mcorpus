/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.routines;


import com.tll.mcorpus.db.Public;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;


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
public class McuserLogout extends AbstractRoutine<Boolean> {

    private static final long serialVersionUID = -434915107;

    /**
     * The parameter <code>public.mcuser_logout.RETURN_VALUE</code>.
     */
    public static final Parameter<Boolean> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", org.jooq.impl.SQLDataType.BOOLEAN, false, false);

    /**
     * The parameter <code>public.mcuser_logout.mcuser_uid</code>.
     */
    public static final Parameter<UUID> MCUSER_UID = Internal.createParameter("mcuser_uid", org.jooq.impl.SQLDataType.UUID, false, false);

    /**
     * The parameter <code>public.mcuser_logout.jwt_id</code>.
     */
    public static final Parameter<UUID> JWT_ID = Internal.createParameter("jwt_id", org.jooq.impl.SQLDataType.UUID, false, false);

    /**
     * The parameter <code>public.mcuser_logout.request_timestamp</code>.
     */
    public static final Parameter<OffsetDateTime> REQUEST_TIMESTAMP = Internal.createParameter("request_timestamp", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, false, false);

    /**
     * The parameter <code>public.mcuser_logout.request_origin</code>.
     */
    public static final Parameter<String> REQUEST_ORIGIN = Internal.createParameter("request_origin", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * Create a new routine call instance
     */
    public McuserLogout() {
        super("mcuser_logout", Public.PUBLIC, org.jooq.impl.SQLDataType.BOOLEAN);

        setReturnParameter(RETURN_VALUE);
        addInParameter(MCUSER_UID);
        addInParameter(JWT_ID);
        addInParameter(REQUEST_TIMESTAMP);
        addInParameter(REQUEST_ORIGIN);
    }

    /**
     * Set the <code>mcuser_uid</code> parameter IN value to the routine
     */
    public void setMcuserUid(UUID value) {
        setValue(MCUSER_UID, value);
    }

    /**
     * Set the <code>mcuser_uid</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setMcuserUid(Field<UUID> field) {
        setField(MCUSER_UID, field);
    }

    /**
     * Set the <code>jwt_id</code> parameter IN value to the routine
     */
    public void setJwtId(UUID value) {
        setValue(JWT_ID, value);
    }

    /**
     * Set the <code>jwt_id</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setJwtId(Field<UUID> field) {
        setField(JWT_ID, field);
    }

    /**
     * Set the <code>request_timestamp</code> parameter IN value to the routine
     */
    public void setRequestTimestamp(OffsetDateTime value) {
        setValue(REQUEST_TIMESTAMP, value);
    }

    /**
     * Set the <code>request_timestamp</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setRequestTimestamp(Field<OffsetDateTime> field) {
        setField(REQUEST_TIMESTAMP, field);
    }

    /**
     * Set the <code>request_origin</code> parameter IN value to the routine
     */
    public void setRequestOrigin(String value) {
        setValue(REQUEST_ORIGIN, value);
    }

    /**
     * Set the <code>request_origin</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setRequestOrigin(Field<String> field) {
        setField(REQUEST_ORIGIN, field);
    }
}
