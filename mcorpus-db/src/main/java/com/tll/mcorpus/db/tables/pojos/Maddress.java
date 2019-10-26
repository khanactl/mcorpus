/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.pojos;


import com.tll.mcorpus.db.enums.Addressname;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.annotation.Generated;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Maddress implements Serializable {

    private static final long serialVersionUID = -1923745498;

    private final UUID           mid;
    private final Addressname    addressName;
    private final OffsetDateTime modified;
    private final String         attn;
    private final String         street1;
    private final String         street2;
    private final String         city;
    private final String         state;
    private final String         postalCode;
    private final String         country;

    public Maddress(Maddress value) {
        this.mid = value.mid;
        this.addressName = value.addressName;
        this.modified = value.modified;
        this.attn = value.attn;
        this.street1 = value.street1;
        this.street2 = value.street2;
        this.city = value.city;
        this.state = value.state;
        this.postalCode = value.postalCode;
        this.country = value.country;
    }

    public Maddress(
        UUID           mid,
        Addressname    addressName,
        OffsetDateTime modified,
        String         attn,
        String         street1,
        String         street2,
        String         city,
        String         state,
        String         postalCode,
        String         country
    ) {
        this.mid = mid;
        this.addressName = addressName;
        this.modified = modified;
        this.attn = attn;
        this.street1 = street1;
        this.street2 = street2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    public UUID getMid() {
        return this.mid;
    }

    public Addressname getAddressName() {
        return this.addressName;
    }

    public OffsetDateTime getModified() {
        return this.modified;
    }

    public String getAttn() {
        return this.attn;
    }

    public String getStreet1() {
        return this.street1;
    }

    public String getStreet2() {
        return this.street2;
    }

    public String getCity() {
        return this.city;
    }

    public String getState() {
        return this.state;
    }

    public String getPostalCode() {
        return this.postalCode;
    }

    public String getCountry() {
        return this.country;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Maddress (");

        sb.append(mid);
        sb.append(", ").append(addressName);
        sb.append(", ").append(modified);
        sb.append(", ").append(attn);
        sb.append(", ").append(street1);
        sb.append(", ").append(street2);
        sb.append(", ").append(city);
        sb.append(", ").append(state);
        sb.append(", ").append(postalCode);
        sb.append(", ").append(country);

        sb.append(")");
        return sb.toString();
    }
}
