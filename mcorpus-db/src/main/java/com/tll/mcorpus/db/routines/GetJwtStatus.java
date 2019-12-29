/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.routines;


import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.enums.JwtStatus;

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
        "jOOQ version:3.12.3"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GetJwtStatus extends AbstractRoutine<JwtStatus> {

    private static final long serialVersionUID = 787320071;

    /**
     * The parameter <code>public.get_jwt_status.RETURN_VALUE</code>.
     */
    public static final Parameter<JwtStatus> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", org.jooq.impl.SQLDataType.VARCHAR.asEnumDataType(com.tll.mcorpus.db.enums.JwtStatus.class), false, false);

    /**
     * The parameter <code>public.get_jwt_status.jwt_id</code>.
     */
    public static final Parameter<UUID> JWT_ID = Internal.createParameter("jwt_id", org.jooq.impl.SQLDataType.UUID, false, false);

    /**
     * Create a new routine call instance
     */
    public GetJwtStatus() {
        super("get_jwt_status", Public.PUBLIC, org.jooq.impl.SQLDataType.VARCHAR.asEnumDataType(com.tll.mcorpus.db.enums.JwtStatus.class));

        setReturnParameter(RETURN_VALUE);
        addInParameter(JWT_ID);
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
}
