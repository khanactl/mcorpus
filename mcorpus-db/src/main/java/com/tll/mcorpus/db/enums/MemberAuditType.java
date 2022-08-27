/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.enums;


import com.tll.mcorpus.db.Public;

import org.jooq.Catalog;
import org.jooq.EnumType;
import org.jooq.Schema;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public enum MemberAuditType implements EnumType {

    LOGIN("LOGIN"),

    LOGOUT("LOGOUT");

    private final String literal;

    private MemberAuditType(String literal) {
        this.literal = literal;
    }

    @Override
    public Catalog getCatalog() {
        return getSchema().getCatalog();
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public String getName() {
        return "member_audit_type";
    }

    @Override
    public String getLiteral() {
        return literal;
    }

    /**
     * Lookup a value of this EnumType by its literal
     */
    public static MemberAuditType lookupLiteral(String literal) {
        return EnumType.lookupLiteral(MemberAuditType.class, literal);
    }
}
