/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.pojos;


import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.MemberStatus;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID mid;
    private final OffsetDateTime created;
    private final OffsetDateTime modified;
    private final String empId;
    private final Location location;
    private final String nameFirst;
    private final String nameMiddle;
    private final String nameLast;
    private final String displayName;
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
        UUID mid,
        OffsetDateTime created,
        OffsetDateTime modified,
        String empId,
        Location location,
        String nameFirst,
        String nameMiddle,
        String nameLast,
        String displayName,
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

    /**
     * Getter for <code>public.member.mid</code>.
     */
    public UUID getMid() {
        return this.mid;
    }

    /**
     * Getter for <code>public.member.created</code>.
     */
    public OffsetDateTime getCreated() {
        return this.created;
    }

    /**
     * Getter for <code>public.member.modified</code>.
     */
    public OffsetDateTime getModified() {
        return this.modified;
    }

    /**
     * Getter for <code>public.member.emp_id</code>.
     */
    public String getEmpId() {
        return this.empId;
    }

    /**
     * Getter for <code>public.member.location</code>.
     */
    public Location getLocation() {
        return this.location;
    }

    /**
     * Getter for <code>public.member.name_first</code>.
     */
    public String getNameFirst() {
        return this.nameFirst;
    }

    /**
     * Getter for <code>public.member.name_middle</code>.
     */
    public String getNameMiddle() {
        return this.nameMiddle;
    }

    /**
     * Getter for <code>public.member.name_last</code>.
     */
    public String getNameLast() {
        return this.nameLast;
    }

    /**
     * Getter for <code>public.member.display_name</code>.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Getter for <code>public.member.status</code>.
     */
    public MemberStatus getStatus() {
        return this.status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Member other = (Member) obj;
        if (this.mid == null) {
            if (other.mid != null)
                return false;
        }
        else if (!this.mid.equals(other.mid))
            return false;
        if (this.created == null) {
            if (other.created != null)
                return false;
        }
        else if (!this.created.equals(other.created))
            return false;
        if (this.modified == null) {
            if (other.modified != null)
                return false;
        }
        else if (!this.modified.equals(other.modified))
            return false;
        if (this.empId == null) {
            if (other.empId != null)
                return false;
        }
        else if (!this.empId.equals(other.empId))
            return false;
        if (this.location == null) {
            if (other.location != null)
                return false;
        }
        else if (!this.location.equals(other.location))
            return false;
        if (this.nameFirst == null) {
            if (other.nameFirst != null)
                return false;
        }
        else if (!this.nameFirst.equals(other.nameFirst))
            return false;
        if (this.nameMiddle == null) {
            if (other.nameMiddle != null)
                return false;
        }
        else if (!this.nameMiddle.equals(other.nameMiddle))
            return false;
        if (this.nameLast == null) {
            if (other.nameLast != null)
                return false;
        }
        else if (!this.nameLast.equals(other.nameLast))
            return false;
        if (this.displayName == null) {
            if (other.displayName != null)
                return false;
        }
        else if (!this.displayName.equals(other.displayName))
            return false;
        if (this.status == null) {
            if (other.status != null)
                return false;
        }
        else if (!this.status.equals(other.status))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.mid == null) ? 0 : this.mid.hashCode());
        result = prime * result + ((this.created == null) ? 0 : this.created.hashCode());
        result = prime * result + ((this.modified == null) ? 0 : this.modified.hashCode());
        result = prime * result + ((this.empId == null) ? 0 : this.empId.hashCode());
        result = prime * result + ((this.location == null) ? 0 : this.location.hashCode());
        result = prime * result + ((this.nameFirst == null) ? 0 : this.nameFirst.hashCode());
        result = prime * result + ((this.nameMiddle == null) ? 0 : this.nameMiddle.hashCode());
        result = prime * result + ((this.nameLast == null) ? 0 : this.nameLast.hashCode());
        result = prime * result + ((this.displayName == null) ? 0 : this.displayName.hashCode());
        result = prime * result + ((this.status == null) ? 0 : this.status.hashCode());
        return result;
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
