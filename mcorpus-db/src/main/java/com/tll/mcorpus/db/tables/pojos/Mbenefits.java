/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.pojos;


import com.tll.mcorpus.db.enums.Beli;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Mbenefits implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID mid;
    private final OffsetDateTime modified;
    private final String foreignAdrsFlag;
    private final Beli beli;
    private final Object mcb;
    private final String medPlanCode;
    private final String medOptOut;
    private final String denPlanCode;
    private final String denOptOut;
    private final String visPlanCode;
    private final String visOptOut;
    private final String legPlanCode;
    private final String legOptOut;

    public Mbenefits(Mbenefits value) {
        this.mid = value.mid;
        this.modified = value.modified;
        this.foreignAdrsFlag = value.foreignAdrsFlag;
        this.beli = value.beli;
        this.mcb = value.mcb;
        this.medPlanCode = value.medPlanCode;
        this.medOptOut = value.medOptOut;
        this.denPlanCode = value.denPlanCode;
        this.denOptOut = value.denOptOut;
        this.visPlanCode = value.visPlanCode;
        this.visOptOut = value.visOptOut;
        this.legPlanCode = value.legPlanCode;
        this.legOptOut = value.legOptOut;
    }

    public Mbenefits(
        UUID mid,
        OffsetDateTime modified,
        String foreignAdrsFlag,
        Beli beli,
        Object mcb,
        String medPlanCode,
        String medOptOut,
        String denPlanCode,
        String denOptOut,
        String visPlanCode,
        String visOptOut,
        String legPlanCode,
        String legOptOut
    ) {
        this.mid = mid;
        this.modified = modified;
        this.foreignAdrsFlag = foreignAdrsFlag;
        this.beli = beli;
        this.mcb = mcb;
        this.medPlanCode = medPlanCode;
        this.medOptOut = medOptOut;
        this.denPlanCode = denPlanCode;
        this.denOptOut = denOptOut;
        this.visPlanCode = visPlanCode;
        this.visOptOut = visOptOut;
        this.legPlanCode = legPlanCode;
        this.legOptOut = legOptOut;
    }

    /**
     * Getter for <code>public.mbenefits.mid</code>.
     */
    public UUID getMid() {
        return this.mid;
    }

    /**
     * Getter for <code>public.mbenefits.modified</code>.
     */
    public OffsetDateTime getModified() {
        return this.modified;
    }

    /**
     * Getter for <code>public.mbenefits.foreign_adrs_flag</code>.
     */
    public String getForeignAdrsFlag() {
        return this.foreignAdrsFlag;
    }

    /**
     * Getter for <code>public.mbenefits.beli</code>.
     */
    public Beli getBeli() {
        return this.beli;
    }

    /**
     * @deprecated Unknown data type. If this is a qualified, user-defined type,
     * it may have been excluded from code generation. If this is a built-in
     * type, you can define an explicit {@link org.jooq.Binding} to specify how
     * this type should be handled. Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @Deprecated
    public Object getMcb() {
        return this.mcb;
    }

    /**
     * Getter for <code>public.mbenefits.med_plan_code</code>.
     */
    public String getMedPlanCode() {
        return this.medPlanCode;
    }

    /**
     * Getter for <code>public.mbenefits.med_opt_out</code>.
     */
    public String getMedOptOut() {
        return this.medOptOut;
    }

    /**
     * Getter for <code>public.mbenefits.den_plan_code</code>.
     */
    public String getDenPlanCode() {
        return this.denPlanCode;
    }

    /**
     * Getter for <code>public.mbenefits.den_opt_out</code>.
     */
    public String getDenOptOut() {
        return this.denOptOut;
    }

    /**
     * Getter for <code>public.mbenefits.vis_plan_code</code>.
     */
    public String getVisPlanCode() {
        return this.visPlanCode;
    }

    /**
     * Getter for <code>public.mbenefits.vis_opt_out</code>.
     */
    public String getVisOptOut() {
        return this.visOptOut;
    }

    /**
     * Getter for <code>public.mbenefits.leg_plan_code</code>.
     */
    public String getLegPlanCode() {
        return this.legPlanCode;
    }

    /**
     * Getter for <code>public.mbenefits.leg_opt_out</code>.
     */
    public String getLegOptOut() {
        return this.legOptOut;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Mbenefits other = (Mbenefits) obj;
        if (this.mid == null) {
            if (other.mid != null)
                return false;
        }
        else if (!this.mid.equals(other.mid))
            return false;
        if (this.modified == null) {
            if (other.modified != null)
                return false;
        }
        else if (!this.modified.equals(other.modified))
            return false;
        if (this.foreignAdrsFlag == null) {
            if (other.foreignAdrsFlag != null)
                return false;
        }
        else if (!this.foreignAdrsFlag.equals(other.foreignAdrsFlag))
            return false;
        if (this.beli == null) {
            if (other.beli != null)
                return false;
        }
        else if (!this.beli.equals(other.beli))
            return false;
        if (this.mcb == null) {
            if (other.mcb != null)
                return false;
        }
        else if (!this.mcb.equals(other.mcb))
            return false;
        if (this.medPlanCode == null) {
            if (other.medPlanCode != null)
                return false;
        }
        else if (!this.medPlanCode.equals(other.medPlanCode))
            return false;
        if (this.medOptOut == null) {
            if (other.medOptOut != null)
                return false;
        }
        else if (!this.medOptOut.equals(other.medOptOut))
            return false;
        if (this.denPlanCode == null) {
            if (other.denPlanCode != null)
                return false;
        }
        else if (!this.denPlanCode.equals(other.denPlanCode))
            return false;
        if (this.denOptOut == null) {
            if (other.denOptOut != null)
                return false;
        }
        else if (!this.denOptOut.equals(other.denOptOut))
            return false;
        if (this.visPlanCode == null) {
            if (other.visPlanCode != null)
                return false;
        }
        else if (!this.visPlanCode.equals(other.visPlanCode))
            return false;
        if (this.visOptOut == null) {
            if (other.visOptOut != null)
                return false;
        }
        else if (!this.visOptOut.equals(other.visOptOut))
            return false;
        if (this.legPlanCode == null) {
            if (other.legPlanCode != null)
                return false;
        }
        else if (!this.legPlanCode.equals(other.legPlanCode))
            return false;
        if (this.legOptOut == null) {
            if (other.legOptOut != null)
                return false;
        }
        else if (!this.legOptOut.equals(other.legOptOut))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.mid == null) ? 0 : this.mid.hashCode());
        result = prime * result + ((this.modified == null) ? 0 : this.modified.hashCode());
        result = prime * result + ((this.foreignAdrsFlag == null) ? 0 : this.foreignAdrsFlag.hashCode());
        result = prime * result + ((this.beli == null) ? 0 : this.beli.hashCode());
        result = prime * result + ((this.mcb == null) ? 0 : this.mcb.hashCode());
        result = prime * result + ((this.medPlanCode == null) ? 0 : this.medPlanCode.hashCode());
        result = prime * result + ((this.medOptOut == null) ? 0 : this.medOptOut.hashCode());
        result = prime * result + ((this.denPlanCode == null) ? 0 : this.denPlanCode.hashCode());
        result = prime * result + ((this.denOptOut == null) ? 0 : this.denOptOut.hashCode());
        result = prime * result + ((this.visPlanCode == null) ? 0 : this.visPlanCode.hashCode());
        result = prime * result + ((this.visOptOut == null) ? 0 : this.visOptOut.hashCode());
        result = prime * result + ((this.legPlanCode == null) ? 0 : this.legPlanCode.hashCode());
        result = prime * result + ((this.legOptOut == null) ? 0 : this.legOptOut.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Mbenefits (");

        sb.append(mid);
        sb.append(", ").append(modified);
        sb.append(", ").append(foreignAdrsFlag);
        sb.append(", ").append(beli);
        sb.append(", ").append(mcb);
        sb.append(", ").append(medPlanCode);
        sb.append(", ").append(medOptOut);
        sb.append(", ").append(denPlanCode);
        sb.append(", ").append(denOptOut);
        sb.append(", ").append(visPlanCode);
        sb.append(", ").append(visOptOut);
        sb.append(", ").append(legPlanCode);
        sb.append(", ").append(legOptOut);

        sb.append(")");
        return sb.toString();
    }
}
