/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.routines;


import com.tll.mcorpus.db.Public;

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
        "jOOQ version:3.11.11"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GenRandomBytes extends AbstractRoutine<byte[]> {

    private static final long serialVersionUID = 1477372343;

    /**
     * The parameter <code>public.gen_random_bytes.RETURN_VALUE</code>.
     */
    public static final Parameter<byte[]> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", org.jooq.impl.SQLDataType.BLOB, false, false);

    /**
     * The parameter <code>public.gen_random_bytes._1</code>.
     */
    public static final Parameter<Integer> _1 = Internal.createParameter("_1", org.jooq.impl.SQLDataType.INTEGER, false, true);

    /**
     * Create a new routine call instance
     */
    public GenRandomBytes() {
        super("gen_random_bytes", Public.PUBLIC, org.jooq.impl.SQLDataType.BLOB);

        setReturnParameter(RETURN_VALUE);
        addInParameter(_1);
    }

    /**
     * Set the <code>_1</code> parameter IN value to the routine
     */
    public void set__1(Integer value) {
        setValue(_1, value);
    }

    /**
     * Set the <code>_1</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void set__1(Field<Integer> field) {
        setField(_1, field);
    }
}
