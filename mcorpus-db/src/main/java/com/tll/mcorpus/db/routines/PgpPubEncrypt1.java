/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.routines;


import com.tll.mcorpus.db.Public;

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
        "jOOQ version:3.11.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PgpPubEncrypt1 extends AbstractRoutine<byte[]> {

    private static final long serialVersionUID = -330522437;

    /**
     * The parameter <code>public.pgp_pub_encrypt.RETURN_VALUE</code>.
     */
    public static final Parameter<byte[]> RETURN_VALUE = createParameter("RETURN_VALUE", org.jooq.impl.SQLDataType.BLOB, false, false);

    /**
     * The parameter <code>public.pgp_pub_encrypt._1</code>.
     */
    public static final Parameter<String> _1 = createParameter("_1", org.jooq.impl.SQLDataType.CLOB, false, true);

    /**
     * The parameter <code>public.pgp_pub_encrypt._2</code>.
     */
    public static final Parameter<byte[]> _2 = createParameter("_2", org.jooq.impl.SQLDataType.BLOB, false, true);

    /**
     * Create a new routine call instance
     */
    public PgpPubEncrypt1() {
        super("pgp_pub_encrypt", Public.PUBLIC, org.jooq.impl.SQLDataType.BLOB);

        setReturnParameter(RETURN_VALUE);
        addInParameter(_1);
        addInParameter(_2);
        setOverloaded(true);
    }

    /**
     * Set the <code>_1</code> parameter IN value to the routine
     */
    public void set__1(String value) {
        setValue(_1, value);
    }

    /**
     * Set the <code>_1</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void set__1(Field<String> field) {
        setField(_1, field);
    }

    /**
     * Set the <code>_2</code> parameter IN value to the routine
     */
    public void set__2(byte... value) {
        setValue(_2, value);
    }

    /**
     * Set the <code>_2</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void set__2(Field<byte[]> field) {
        setField(_2, field);
    }
}
