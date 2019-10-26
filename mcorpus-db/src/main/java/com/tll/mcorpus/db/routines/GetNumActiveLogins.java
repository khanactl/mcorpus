/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.routines;


import com.tll.mcorpus.db.Public;

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
public class GetNumActiveLogins extends AbstractRoutine<Integer> {

    private static final long serialVersionUID = 1653416319;

    /**
     * The parameter <code>public.get_num_active_logins.RETURN_VALUE</code>.
     */
    public static final Parameter<Integer> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", org.jooq.impl.SQLDataType.INTEGER, false, false);

    /**
     * The parameter <code>public.get_num_active_logins.mcuser_id</code>.
     */
    public static final Parameter<UUID> MCUSER_ID = Internal.createParameter("mcuser_id", org.jooq.impl.SQLDataType.UUID, false, false);

    /**
     * Create a new routine call instance
     */
    public GetNumActiveLogins() {
        super("get_num_active_logins", Public.PUBLIC, org.jooq.impl.SQLDataType.INTEGER);

        setReturnParameter(RETURN_VALUE);
        addInParameter(MCUSER_ID);
    }

    /**
     * Set the <code>mcuser_id</code> parameter IN value to the routine
     */
    public void setMcuserId(UUID value) {
        setValue(MCUSER_ID, value);
    }

    /**
     * Set the <code>mcuser_id</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setMcuserId(Field<UUID> field) {
        setField(MCUSER_ID, field);
    }
}
