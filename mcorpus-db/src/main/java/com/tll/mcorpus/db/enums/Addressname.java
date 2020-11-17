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
public enum Addressname implements EnumType {

    home("home"),

    work("work"),

    other("other");

    private final String literal;

    private Addressname(String literal) {
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
        return "addressname";
    }

    @Override
    public String getLiteral() {
        return literal;
    }
}
