/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.pojos;


import com.tll.mcorpus.db.enums.McuserRole;
import com.tll.mcorpus.db.enums.McuserStatus;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

import javax.annotation.Generated;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.3"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Mcuser implements Serializable {

    private static final long serialVersionUID = -454761922;

    private final UUID           uid;
    private final OffsetDateTime created;
    private final OffsetDateTime modified;
    private final String         name;
    private final String         email;
    private final String         username;
    private final String         pswd;
    private final McuserStatus   status;
    private final McuserRole[]   roles;

    public Mcuser(Mcuser value) {
        this.uid = value.uid;
        this.created = value.created;
        this.modified = value.modified;
        this.name = value.name;
        this.email = value.email;
        this.username = value.username;
        this.pswd = value.pswd;
        this.status = value.status;
        this.roles = value.roles;
    }

    public Mcuser(
        UUID           uid,
        OffsetDateTime created,
        OffsetDateTime modified,
        String         name,
        String         email,
        String         username,
        String         pswd,
        McuserStatus   status,
        McuserRole[]   roles
    ) {
        this.uid = uid;
        this.created = created;
        this.modified = modified;
        this.name = name;
        this.email = email;
        this.username = username;
        this.pswd = pswd;
        this.status = status;
        this.roles = roles;
    }

    public UUID getUid() {
        return this.uid;
    }

    public OffsetDateTime getCreated() {
        return this.created;
    }

    public OffsetDateTime getModified() {
        return this.modified;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPswd() {
        return this.pswd;
    }

    public McuserStatus getStatus() {
        return this.status;
    }

    public McuserRole[] getRoles() {
        return this.roles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Mcuser (");

        sb.append(uid);
        sb.append(", ").append(created);
        sb.append(", ").append(modified);
        sb.append(", ").append(name);
        sb.append(", ").append(email);
        sb.append(", ").append(username);
        sb.append(", ").append(pswd);
        sb.append(", ").append(status);
        sb.append(", ").append(Arrays.toString(roles));

        sb.append(")");
        return sb.toString();
    }
}
