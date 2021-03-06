/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.pojos;


import com.tll.mcorpus.db.enums.MemberAuditType;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MemberAudit implements Serializable {

    private static final long serialVersionUID = -2081979309;

    private final UUID            mid;
    private final OffsetDateTime  created;
    private final MemberAuditType type;
    private final OffsetDateTime  requestTimestamp;
    private final String          requestOrigin;

    public MemberAudit(MemberAudit value) {
        this.mid = value.mid;
        this.created = value.created;
        this.type = value.type;
        this.requestTimestamp = value.requestTimestamp;
        this.requestOrigin = value.requestOrigin;
    }

    public MemberAudit(
        UUID            mid,
        OffsetDateTime  created,
        MemberAuditType type,
        OffsetDateTime  requestTimestamp,
        String          requestOrigin
    ) {
        this.mid = mid;
        this.created = created;
        this.type = type;
        this.requestTimestamp = requestTimestamp;
        this.requestOrigin = requestOrigin;
    }

    public UUID getMid() {
        return this.mid;
    }

    public OffsetDateTime getCreated() {
        return this.created;
    }

    public MemberAuditType getType() {
        return this.type;
    }

    public OffsetDateTime getRequestTimestamp() {
        return this.requestTimestamp;
    }

    public String getRequestOrigin() {
        return this.requestOrigin;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MemberAudit (");

        sb.append(mid);
        sb.append(", ").append(created);
        sb.append(", ").append(type);
        sb.append(", ").append(requestTimestamp);
        sb.append(", ").append(requestOrigin);

        sb.append(")");
        return sb.toString();
    }
}
