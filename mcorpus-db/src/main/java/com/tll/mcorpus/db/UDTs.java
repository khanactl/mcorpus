/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db;


import com.tll.mcorpus.db.udt.JwtMcuserStatus;
import com.tll.mcorpus.db.udt.McuserAndRoles;
import com.tll.mcorpus.db.udt.Mref;

import javax.annotation.Generated;


/**
 * Convenience access to all UDTs in public
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.8"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UDTs {

    /**
     * The type <code>public.jwt_mcuser_status</code>
     */
    public static JwtMcuserStatus JWT_MCUSER_STATUS = com.tll.mcorpus.db.udt.JwtMcuserStatus.JWT_MCUSER_STATUS;

    /**
     * The type <code>public.mcuser_and_roles</code>
     */
    public static McuserAndRoles MCUSER_AND_ROLES = com.tll.mcorpus.db.udt.McuserAndRoles.MCUSER_AND_ROLES;

    /**
     * The type <code>public.mref</code>
     */
    public static Mref MREF = com.tll.mcorpus.db.udt.Mref.MREF;
}
