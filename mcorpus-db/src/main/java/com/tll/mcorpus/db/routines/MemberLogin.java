/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.routines;


import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.udt.records.MrefRecord;

import java.time.OffsetDateTime;

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
        "jOOQ version:3.12.3"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MemberLogin extends AbstractRoutine<MrefRecord> {

    private static final long serialVersionUID = 1086868729;

    /**
     * The parameter <code>public.member_login.RETURN_VALUE</code>.
     */
    public static final Parameter<MrefRecord> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", com.tll.mcorpus.db.udt.Mref.MREF.getDataType(), false, false);

    /**
     * The parameter <code>public.member_login.member_username</code>.
     */
    public static final Parameter<String> MEMBER_USERNAME = Internal.createParameter("member_username", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.member_login.member_password</code>.
     */
    public static final Parameter<String> MEMBER_PASSWORD = Internal.createParameter("member_password", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.member_login.in_request_timestamp</code>.
     */
    public static final Parameter<OffsetDateTime> IN_REQUEST_TIMESTAMP = Internal.createParameter("in_request_timestamp", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, false, false);

    /**
     * The parameter <code>public.member_login.in_request_origin</code>.
     */
    public static final Parameter<String> IN_REQUEST_ORIGIN = Internal.createParameter("in_request_origin", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * Create a new routine call instance
     */
    public MemberLogin() {
        super("member_login", Public.PUBLIC, com.tll.mcorpus.db.udt.Mref.MREF.getDataType());

        setReturnParameter(RETURN_VALUE);
        addInParameter(MEMBER_USERNAME);
        addInParameter(MEMBER_PASSWORD);
        addInParameter(IN_REQUEST_TIMESTAMP);
        addInParameter(IN_REQUEST_ORIGIN);
    }

    /**
     * Set the <code>member_username</code> parameter IN value to the routine
     */
    public void setMemberUsername(String value) {
        setValue(MEMBER_USERNAME, value);
    }

    /**
     * Set the <code>member_username</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setMemberUsername(Field<String> field) {
        setField(MEMBER_USERNAME, field);
    }

    /**
     * Set the <code>member_password</code> parameter IN value to the routine
     */
    public void setMemberPassword(String value) {
        setValue(MEMBER_PASSWORD, value);
    }

    /**
     * Set the <code>member_password</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setMemberPassword(Field<String> field) {
        setField(MEMBER_PASSWORD, field);
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
