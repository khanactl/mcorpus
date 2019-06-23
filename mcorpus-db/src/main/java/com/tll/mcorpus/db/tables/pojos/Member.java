/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.pojos;


import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.MemberStatus;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

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
public class Member implements Serializable {

    private static final long serialVersionUID = 1360578625;

    private final UUID         mid;
    private final Timestamp    created;
    private final Timestamp    modified;
    private final String       empId;
    private final Location     location;
    private final String       nameFirst;
    private final String       nameMiddle;
    private final String       nameLast;
    private final String       displayName;
    private final MemberStatus status;

    public Member(Member value) {
        this.mid = value.mid;
        this.created = value.created;
        this.modified = value.modified;
        this.empId = value.empId;
        this.location = value.location;
        this.nameFirst = value.nameFirst;
        this.nameMiddle = value.nameMiddle;
        this.nameLast = value.nameLast;
        this.displayName = value.displayName;
        this.status = value.status;
    }

    public Member(
        UUID         mid,
        Timestamp    created,
        Timestamp    modified,
        String       empId,
        Location     location,
        String       nameFirst,
        String       nameMiddle,
        String       nameLast,
        String       displayName,
        MemberStatus status
    ) {
        this.mid = mid;
        this.created = created;
        this.modified = modified;
        this.empId = empId;
        this.location = location;
        this.nameFirst = nameFirst;
        this.nameMiddle = nameMiddle;
        this.nameLast = nameLast;
        this.displayName = displayName;
        this.status = status;
    }

    public UUID getMid() {
        return this.mid;
    }

    public Timestamp getCreated() {
        return this.created;
    }

    public Timestamp getModified() {
        return this.modified;
    }

    public String getEmpId() {
        return this.empId;
    }

    public Location getLocation() {
        return this.location;
    }

    public String getNameFirst() {
        return this.nameFirst;
    }

    public String getNameMiddle() {
        return this.nameMiddle;
    }

    public String getNameLast() {
        return this.nameLast;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public MemberStatus getStatus() {
        return this.status;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Member (");

        sb.append(mid);
        sb.append(", ").append(created);
        sb.append(", ").append(modified);
        sb.append(", ").append(empId);
        sb.append(", ").append(location);
        sb.append(", ").append(nameFirst);
        sb.append(", ").append(nameMiddle);
        sb.append(", ").append(nameLast);
        sb.append(", ").append(displayName);
        sb.append(", ").append(status);

        sb.append(")");
        return sb.toString();
    }
}
