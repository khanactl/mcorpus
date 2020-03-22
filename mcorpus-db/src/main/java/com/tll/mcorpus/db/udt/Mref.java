/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.udt;


import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.udt.records.MrefRecord;

import java.util.UUID;

import org.jooq.Schema;
import org.jooq.UDTField;
import org.jooq.impl.DSL;
import org.jooq.impl.SchemaImpl;
import org.jooq.impl.UDTImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Mref extends UDTImpl<MrefRecord> {

    private static final long serialVersionUID = -189559318;

    /**
     * The reference instance of <code>public.mref</code>
     */
    public static final Mref MREF = new Mref();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<MrefRecord> getRecordType() {
        return MrefRecord.class;
    }

    /**
     * The attribute <code>public.mref.mid</code>.
     */
    public static final UDTField<MrefRecord, UUID> MID = createField(DSL.name("mid"), org.jooq.impl.SQLDataType.UUID, MREF, "");

    /**
     * The attribute <code>public.mref.emp_id</code>.
     */
    public static final UDTField<MrefRecord, String> EMP_ID = createField(DSL.name("emp_id"), org.jooq.impl.SQLDataType.CLOB, MREF, "");

    /**
     * The attribute <code>public.mref.location</code>.
     */
    public static final UDTField<MrefRecord, Location> LOCATION = createField(DSL.name("location"), org.jooq.impl.SQLDataType.VARCHAR.asEnumDataType(com.tll.mcorpus.db.enums.Location.class), MREF, "");

    /**
     * No further instances allowed
     */
    private Mref() {
        super("mref", null, null, false);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC != null ? Public.PUBLIC : new SchemaImpl(DSL.name("public"));
    }
}
