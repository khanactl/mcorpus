/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.pojos;


import java.io.Serializable;

import javax.annotation.Generated;


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
public class PgpArmorHeaders implements Serializable {

    private static final long serialVersionUID = 1537885984;

    private final String key;
    private final String value;

    public PgpArmorHeaders(PgpArmorHeaders value) {
        this.key = value.key;
        this.value = value.value;
    }

    public PgpArmorHeaders(
        String key,
        String value
    ) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PgpArmorHeaders (");

        sb.append(key);
        sb.append(", ").append(value);

        sb.append(")");
        return sb.toString();
    }
}
