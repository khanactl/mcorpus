/*
 * This file is generated by jOOQ.
*/
package com.tll.mcorpus.db.routines;


import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.udt.records.JwtMcuserStatusRecord;

import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;


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
public class FetchLatestJwtMcuserRec extends AbstractRoutine<JwtMcuserStatusRecord> {

    private static final long serialVersionUID = 940858170;

    /**
     * The parameter <code>public.fetch_latest_jwt_mcuser_rec.RETURN_VALUE</code>.
     */
    public static final Parameter<JwtMcuserStatusRecord> RETURN_VALUE = createParameter("RETURN_VALUE", com.tll.mcorpus.db.udt.JwtMcuserStatus.JWT_MCUSER_STATUS.getDataType(), false, false);

    /**
     * The parameter <code>public.fetch_latest_jwt_mcuser_rec.jwt_id</code>.
     */
    public static final Parameter<UUID> JWT_ID = createParameter("jwt_id", org.jooq.impl.SQLDataType.UUID, false, false);

    /**
     * Create a new routine call instance
     */
    public FetchLatestJwtMcuserRec() {
        super("fetch_latest_jwt_mcuser_rec", Public.PUBLIC, com.tll.mcorpus.db.udt.JwtMcuserStatus.JWT_MCUSER_STATUS.getDataType());

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
