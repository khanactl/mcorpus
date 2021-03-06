/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.routines;


import com.tll.mcorpus.db.Public;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MemberLogout extends AbstractRoutine<UUID> {

    private static final long serialVersionUID = -1837144987;

    /**
     * The parameter <code>public.member_logout.RETURN_VALUE</code>.
     */
    public static final Parameter<UUID> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", org.jooq.impl.SQLDataType.UUID, false, false);

    /**
     * The parameter <code>public.member_logout.mid</code>.
     */
    public static final Parameter<UUID> MID = Internal.createParameter("mid", org.jooq.impl.SQLDataType.UUID, false, false);

    /**
     * The parameter <code>public.member_logout.in_request_timestamp</code>.
     */
    public static final Parameter<OffsetDateTime> IN_REQUEST_TIMESTAMP = Internal.createParameter("in_request_timestamp", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, false, false);

    /**
     * The parameter <code>public.member_logout.in_request_origin</code>.
     */
    public static final Parameter<String> IN_REQUEST_ORIGIN = Internal.createParameter("in_request_origin", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * Create a new routine call instance
     */
    public MemberLogout() {
        super("member_logout", Public.PUBLIC, org.jooq.impl.SQLDataType.UUID);

        setReturnParameter(RETURN_VALUE);
        addInParameter(MID);
        addInParameter(IN_REQUEST_TIMESTAMP);
        addInParameter(IN_REQUEST_ORIGIN);
    }

    /**
     * Set the <code>mid</code> parameter IN value to the routine
     */
    public void setMid(UUID value) {
        setValue(MID, value);
    }

    /**
     * Set the <code>mid</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setMid(Field<UUID> field) {
        setField(MID, field);
    }

    /**
     * Set the <code>in_request_timestamp</code> parameter IN value to the routine
     */
    public void setInRequestTimestamp(OffsetDateTime value) {
        setValue(IN_REQUEST_TIMESTAMP, value);
    }

    /**
     * Set the <code>in_request_timestamp</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setInRequestTimestamp(Field<OffsetDateTime> field) {
        setField(IN_REQUEST_TIMESTAMP, field);
    }

    /**
     * Set the <code>in_request_origin</code> parameter IN value to the routine
     */
    public void setInRequestOrigin(String value) {
        setValue(IN_REQUEST_ORIGIN, value);
    }

    /**
     * Set the <code>in_request_origin</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setInRequestOrigin(Field<String> field) {
        setField(IN_REQUEST_ORIGIN, field);
    }
}
