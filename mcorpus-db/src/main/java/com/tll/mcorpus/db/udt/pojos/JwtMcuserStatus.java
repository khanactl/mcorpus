/*
 * This file is generated by jOOQ.
*/
package com.tll.mcorpus.db.udt.pojos;


import com.tll.mcorpus.db.enums.JwtIdStatus;
import com.tll.mcorpus.db.enums.McuserStatus;

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
        "jOOQ version:3.10.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class JwtMcuserStatus implements Serializable {

    private static final long serialVersionUID = 1036364008;

    private final UUID         jwtId;
    private final JwtIdStatus  jwtIdStatus;
    private final Timestamp    loginExpiration;
    private final McuserStatus mcuserStatus;
    private final Boolean      admin;

    public JwtMcuserStatus(JwtMcuserStatus value) {
        this.jwtId = value.jwtId;
        this.jwtIdStatus = value.jwtIdStatus;
        this.loginExpiration = value.loginExpiration;
        this.mcuserStatus = value.mcuserStatus;
        this.admin = value.admin;
    }

    public JwtMcuserStatus(
        UUID         jwtId,
        JwtIdStatus  jwtIdStatus,
        Timestamp    loginExpiration,
        McuserStatus mcuserStatus,
        Boolean      admin
    ) {
        this.jwtId = jwtId;
        this.jwtIdStatus = jwtIdStatus;
        this.loginExpiration = loginExpiration;
        this.mcuserStatus = mcuserStatus;
        this.admin = admin;
    }

    public UUID getJwtId() {
        return this.jwtId;
    }

    public JwtIdStatus getJwtIdStatus() {
        return this.jwtIdStatus;
    }

    public Timestamp getLoginExpiration() {
        return this.loginExpiration;
    }

    public McuserStatus getMcuserStatus() {
        return this.mcuserStatus;
    }

    public Boolean getAdmin() {
        return this.admin;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("JwtMcuserStatus (");

        sb.append(jwtId);
        sb.append(", ").append(jwtIdStatus);
        sb.append(", ").append(loginExpiration);
        sb.append(", ").append(mcuserStatus);
        sb.append(", ").append(admin);

        sb.append(")");
        return sb.toString();
    }
}
