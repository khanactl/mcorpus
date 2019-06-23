/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.udt;


import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.enums.JwtIdStatus;
import com.tll.mcorpus.db.enums.McuserAuditType;
import com.tll.mcorpus.db.enums.McuserStatus;
import com.tll.mcorpus.db.udt.records.JwtMcuserStatusRecord;

import java.sql.Timestamp;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Schema;
import org.jooq.UDTField;
import org.jooq.impl.DSL;
import org.jooq.impl.SchemaImpl;
import org.jooq.impl.UDTImpl;


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
public class JwtMcuserStatus extends UDTImpl<JwtMcuserStatusRecord> {

    private static final long serialVersionUID = -264683276;

    /**
     * The reference instance of <code>public.jwt_mcuser_status</code>
     */
    public static final JwtMcuserStatus JWT_MCUSER_STATUS = new JwtMcuserStatus();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<JwtMcuserStatusRecord> getRecordType() {
        return JwtMcuserStatusRecord.class;
    }

    /**
     * The attribute <code>public.jwt_mcuser_status.mcuser_audit_record_type</code>.
     */
    public static final UDTField<JwtMcuserStatusRecord, McuserAuditType> MCUSER_AUDIT_RECORD_TYPE = createField("mcuser_audit_record_type", org.jooq.impl.SQLDataType.VARCHAR.asEnumDataType(com.tll.mcorpus.db.enums.McuserAuditType.class), JWT_MCUSER_STATUS, "");

    /**
     * The attribute <code>public.jwt_mcuser_status.jwt_id</code>.
     */
    public static final UDTField<JwtMcuserStatusRecord, UUID> JWT_ID = createField("jwt_id", org.jooq.impl.SQLDataType.UUID, JWT_MCUSER_STATUS, "");

    /**
     * The attribute <code>public.jwt_mcuser_status.jwt_id_status</code>.
     */
    public static final UDTField<JwtMcuserStatusRecord, JwtIdStatus> JWT_ID_STATUS = createField("jwt_id_status", org.jooq.impl.SQLDataType.VARCHAR.asEnumDataType(com.tll.mcorpus.db.enums.JwtIdStatus.class), JWT_MCUSER_STATUS, "");

    /**
     * The attribute <code>public.jwt_mcuser_status.login_expiration</code>.
     */
    public static final UDTField<JwtMcuserStatusRecord, Timestamp> LOGIN_EXPIRATION = createField("login_expiration", org.jooq.impl.SQLDataType.TIMESTAMP, JWT_MCUSER_STATUS, "");

    /**
     * The attribute <code>public.jwt_mcuser_status.uid</code>.
     */
    public static final UDTField<JwtMcuserStatusRecord, UUID> UID = createField("uid", org.jooq.impl.SQLDataType.UUID, JWT_MCUSER_STATUS, "");

    /**
     * The attribute <code>public.jwt_mcuser_status.mcuser_status</code>.
     */
    public static final UDTField<JwtMcuserStatusRecord, McuserStatus> MCUSER_STATUS = createField("mcuser_status", org.jooq.impl.SQLDataType.VARCHAR.asEnumDataType(com.tll.mcorpus.db.enums.McuserStatus.class), JWT_MCUSER_STATUS, "");

    /**
     * No further instances allowed
     */
    private JwtMcuserStatus() {
        super("jwt_mcuser_status", null, null, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC != null ? Public.PUBLIC : new SchemaImpl(DSL.name("public"));
    }
}
