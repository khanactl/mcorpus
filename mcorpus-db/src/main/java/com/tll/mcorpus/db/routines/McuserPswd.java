/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.routines;


import com.tll.mcorpus.db.Public;

import java.util.UUID;

import javax.annotation.Generated;

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
public class McuserPswd extends AbstractRoutine<java.lang.Void> {

    private static final long serialVersionUID = 884203643;

    /**
     * The parameter <code>public.mcuser_pswd.in_uid</code>.
     */
    public static final Parameter<UUID> IN_UID = Internal.createParameter("in_uid", org.jooq.impl.SQLDataType.UUID, false, false);

    /**
     * The parameter <code>public.mcuser_pswd.in_pswd</code>.
     */
    public static final Parameter<String> IN_PSWD = Internal.createParameter("in_pswd", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * Create a new routine call instance
     */
    public McuserPswd() {
        super("mcuser_pswd", Public.PUBLIC);

        addInParameter(IN_UID);
        addInParameter(IN_PSWD);
    }

    /**
     * Set the <code>in_uid</code> parameter IN value to the routine
     */
    public void setInUid(UUID value) {
        setValue(IN_UID, value);
    }

    /**
     * Set the <code>in_pswd</code> parameter IN value to the routine
     */
    public void setInPswd(String value) {
        setValue(IN_PSWD, value);
    }
}
