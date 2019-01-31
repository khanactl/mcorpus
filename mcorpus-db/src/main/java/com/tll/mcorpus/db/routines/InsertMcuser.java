/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.routines;


import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.enums.McuserRole;
import com.tll.mcorpus.db.enums.McuserStatus;
import com.tll.mcorpus.db.tables.records.McuserRecord;

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
        "jOOQ version:3.11.8"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class InsertMcuser extends AbstractRoutine<McuserRecord> {

    private static final long serialVersionUID = 341880195;

    /**
     * The parameter <code>public.insert_mcuser.RETURN_VALUE</code>.
     */
    public static final Parameter<McuserRecord> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", com.tll.mcorpus.db.tables.Mcuser.MCUSER.getDataType(), false, false);

    /**
     * The parameter <code>public.insert_mcuser.in_name</code>.
     */
    public static final Parameter<String> IN_NAME = Internal.createParameter("in_name", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_mcuser.in_email</code>.
     */
    public static final Parameter<String> IN_EMAIL = Internal.createParameter("in_email", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_mcuser.in_username</code>.
     */
    public static final Parameter<String> IN_USERNAME = Internal.createParameter("in_username", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_mcuser.in_pswd</code>.
     */
    public static final Parameter<String> IN_PSWD = Internal.createParameter("in_pswd", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_mcuser.in_status</code>.
     */
    public static final Parameter<McuserStatus> IN_STATUS = Internal.createParameter("in_status", org.jooq.impl.SQLDataType.VARCHAR.asEnumDataType(com.tll.mcorpus.db.enums.McuserStatus.class), false, false);

    /**
     * The parameter <code>public.insert_mcuser.in_roles</code>.
     */
    public static final Parameter<McuserRole[]> IN_ROLES = Internal.createParameter("in_roles", org.jooq.impl.SQLDataType.VARCHAR.asEnumDataType(com.tll.mcorpus.db.enums.McuserRole.class).getArrayDataType(), false, false);

    /**
     * Create a new routine call instance
     */
    public InsertMcuser() {
        super("insert_mcuser", Public.PUBLIC, com.tll.mcorpus.db.tables.Mcuser.MCUSER.getDataType());

        setReturnParameter(RETURN_VALUE);
        addInParameter(IN_NAME);
        addInParameter(IN_EMAIL);
        addInParameter(IN_USERNAME);
        addInParameter(IN_PSWD);
        addInParameter(IN_STATUS);
        addInParameter(IN_ROLES);
    }

    /**
     * Set the <code>in_name</code> parameter IN value to the routine
     */
    public void setInName(String value) {
        setValue(IN_NAME, value);
    }

    /**
     * Set the <code>in_name</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setInName(Field<String> field) {
        setField(IN_NAME, field);
    }

    /**
     * Set the <code>in_email</code> parameter IN value to the routine
     */
    public void setInEmail(String value) {
        setValue(IN_EMAIL, value);
    }

    /**
     * Set the <code>in_email</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setInEmail(Field<String> field) {
        setField(IN_EMAIL, field);
    }

    /**
     * Set the <code>in_username</code> parameter IN value to the routine
     */
    public void setInUsername(String value) {
        setValue(IN_USERNAME, value);
    }

    /**
     * Set the <code>in_username</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setInUsername(Field<String> field) {
        setField(IN_USERNAME, field);
    }

    /**
     * Set the <code>in_pswd</code> parameter IN value to the routine
     */
    public void setInPswd(String value) {
        setValue(IN_PSWD, value);
    }

    /**
     * Set the <code>in_pswd</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setInPswd(Field<String> field) {
        setField(IN_PSWD, field);
    }

    /**
     * Set the <code>in_status</code> parameter IN value to the routine
     */
    public void setInStatus(McuserStatus value) {
        setValue(IN_STATUS, value);
    }

    /**
     * Set the <code>in_status</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setInStatus(Field<McuserStatus> field) {
        setField(IN_STATUS, field);
    }

    /**
     * Set the <code>in_roles</code> parameter IN value to the routine
     */
    public void setInRoles(McuserRole... value) {
        setValue(IN_ROLES, value);
    }

    /**
     * Set the <code>in_roles</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setInRoles(Field<McuserRole[]> field) {
        setField(IN_ROLES, field);
    }
}
