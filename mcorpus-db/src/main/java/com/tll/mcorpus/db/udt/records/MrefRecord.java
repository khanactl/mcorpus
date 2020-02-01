/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.udt.records;


import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.udt.Mref;

import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UDTRecordImpl;


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
public class MrefRecord extends UDTRecordImpl<MrefRecord> implements Record3<UUID, String, Location> {

    private static final long serialVersionUID = -452382798;

    /**
     * Setter for <code>public.mref.mid</code>.
     */
    public void setMid(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.mref.mid</code>.
     */
    public UUID getMid() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.mref.emp_id</code>.
     */
    public void setEmpId(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.mref.emp_id</code>.
     */
    public String getEmpId() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.mref.location</code>.
     */
    public void setLocation(Location value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.mref.location</code>.
     */
    public Location getLocation() {
        return (Location) get(2);
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<UUID, String, Location> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<UUID, String, Location> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<UUID> field1() {
        return Mref.MID;
    }

    @Override
    public Field<String> field2() {
        return Mref.EMP_ID;
    }

    @Override
    public Field<Location> field3() {
        return Mref.LOCATION;
    }

    @Override
    public UUID component1() {
        return getMid();
    }

    @Override
    public String component2() {
        return getEmpId();
    }

    @Override
    public Location component3() {
        return getLocation();
    }

    @Override
    public UUID value1() {
        return getMid();
    }

    @Override
    public String value2() {
        return getEmpId();
    }

    @Override
    public Location value3() {
        return getLocation();
    }

    @Override
    public MrefRecord value1(UUID value) {
        setMid(value);
        return this;
    }

    @Override
    public MrefRecord value2(String value) {
        setEmpId(value);
        return this;
    }

    @Override
    public MrefRecord value3(Location value) {
        setLocation(value);
        return this;
    }

    @Override
    public MrefRecord values(UUID value1, String value2, Location value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MrefRecord
     */
    public MrefRecord() {
        super(Mref.MREF);
    }

    /**
     * Create a detached, initialised MrefRecord
     */
    public MrefRecord(UUID mid, String empId, Location location) {
        super(Mref.MREF);

        set(0, mid);
        set(1, empId);
        set(2, location);
    }
}
