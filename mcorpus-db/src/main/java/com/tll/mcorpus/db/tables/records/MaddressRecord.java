/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.records;


import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.tables.Maddress;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record10;
import org.jooq.Record2;
import org.jooq.Row10;
import org.jooq.impl.UpdatableRecordImpl;


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
public class MaddressRecord extends UpdatableRecordImpl<MaddressRecord> implements Record10<UUID, Addressname, OffsetDateTime, String, String, String, String, String, String, String> {

    private static final long serialVersionUID = -1812996607;

    /**
     * Setter for <code>public.maddress.mid</code>.
     */
    public void setMid(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.maddress.mid</code>.
     */
    public UUID getMid() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.maddress.address_name</code>.
     */
    public void setAddressName(Addressname value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.maddress.address_name</code>.
     */
    public Addressname getAddressName() {
        return (Addressname) get(1);
    }

    /**
     * Setter for <code>public.maddress.modified</code>.
     */
    public void setModified(OffsetDateTime value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.maddress.modified</code>.
     */
    public OffsetDateTime getModified() {
        return (OffsetDateTime) get(2);
    }

    /**
     * Setter for <code>public.maddress.attn</code>.
     */
    public void setAttn(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.maddress.attn</code>.
     */
    public String getAttn() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.maddress.street1</code>.
     */
    public void setStreet1(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.maddress.street1</code>.
     */
    public String getStreet1() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.maddress.street2</code>.
     */
    public void setStreet2(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.maddress.street2</code>.
     */
    public String getStreet2() {
        return (String) get(5);
    }

    /**
     * Setter for <code>public.maddress.city</code>.
     */
    public void setCity(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.maddress.city</code>.
     */
    public String getCity() {
        return (String) get(6);
    }

    /**
     * Setter for <code>public.maddress.state</code>.
     */
    public void setState(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.maddress.state</code>.
     */
    public String getState() {
        return (String) get(7);
    }

    /**
     * Setter for <code>public.maddress.postal_code</code>.
     */
    public void setPostalCode(String value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.maddress.postal_code</code>.
     */
    public String getPostalCode() {
        return (String) get(8);
    }

    /**
     * Setter for <code>public.maddress.country</code>.
     */
    public void setCountry(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.maddress.country</code>.
     */
    public String getCountry() {
        return (String) get(9);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<UUID, Addressname> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record10 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row10<UUID, Addressname, OffsetDateTime, String, String, String, String, String, String, String> fieldsRow() {
        return (Row10) super.fieldsRow();
    }

    @Override
    public Row10<UUID, Addressname, OffsetDateTime, String, String, String, String, String, String, String> valuesRow() {
        return (Row10) super.valuesRow();
    }

    @Override
    public Field<UUID> field1() {
        return Maddress.MADDRESS.MID;
    }

    @Override
    public Field<Addressname> field2() {
        return Maddress.MADDRESS.ADDRESS_NAME;
    }

    @Override
    public Field<OffsetDateTime> field3() {
        return Maddress.MADDRESS.MODIFIED;
    }

    @Override
    public Field<String> field4() {
        return Maddress.MADDRESS.ATTN;
    }

    @Override
    public Field<String> field5() {
        return Maddress.MADDRESS.STREET1;
    }

    @Override
    public Field<String> field6() {
        return Maddress.MADDRESS.STREET2;
    }

    @Override
    public Field<String> field7() {
        return Maddress.MADDRESS.CITY;
    }

    @Override
    public Field<String> field8() {
        return Maddress.MADDRESS.STATE;
    }

    @Override
    public Field<String> field9() {
        return Maddress.MADDRESS.POSTAL_CODE;
    }

    @Override
    public Field<String> field10() {
        return Maddress.MADDRESS.COUNTRY;
    }

    @Override
    public UUID component1() {
        return getMid();
    }

    @Override
    public Addressname component2() {
        return getAddressName();
    }

    @Override
    public OffsetDateTime component3() {
        return getModified();
    }

    @Override
    public String component4() {
        return getAttn();
    }

    @Override
    public String component5() {
        return getStreet1();
    }

    @Override
    public String component6() {
        return getStreet2();
    }

    @Override
    public String component7() {
        return getCity();
    }

    @Override
    public String component8() {
        return getState();
    }

    @Override
    public String component9() {
        return getPostalCode();
    }

    @Override
    public String component10() {
        return getCountry();
    }

    @Override
    public UUID value1() {
        return getMid();
    }

    @Override
    public Addressname value2() {
        return getAddressName();
    }

    @Override
    public OffsetDateTime value3() {
        return getModified();
    }

    @Override
    public String value4() {
        return getAttn();
    }

    @Override
    public String value5() {
        return getStreet1();
    }

    @Override
    public String value6() {
        return getStreet2();
    }

    @Override
    public String value7() {
        return getCity();
    }

    @Override
    public String value8() {
        return getState();
    }

    @Override
    public String value9() {
        return getPostalCode();
    }

    @Override
    public String value10() {
        return getCountry();
    }

    @Override
    public MaddressRecord value1(UUID value) {
        setMid(value);
        return this;
    }

    @Override
    public MaddressRecord value2(Addressname value) {
        setAddressName(value);
        return this;
    }

    @Override
    public MaddressRecord value3(OffsetDateTime value) {
        setModified(value);
        return this;
    }

    @Override
    public MaddressRecord value4(String value) {
        setAttn(value);
        return this;
    }

    @Override
    public MaddressRecord value5(String value) {
        setStreet1(value);
        return this;
    }

    @Override
    public MaddressRecord value6(String value) {
        setStreet2(value);
        return this;
    }

    @Override
    public MaddressRecord value7(String value) {
        setCity(value);
        return this;
    }

    @Override
    public MaddressRecord value8(String value) {
        setState(value);
        return this;
    }

    @Override
    public MaddressRecord value9(String value) {
        setPostalCode(value);
        return this;
    }

    @Override
    public MaddressRecord value10(String value) {
        setCountry(value);
        return this;
    }

    @Override
    public MaddressRecord values(UUID value1, Addressname value2, OffsetDateTime value3, String value4, String value5, String value6, String value7, String value8, String value9, String value10) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MaddressRecord
     */
    public MaddressRecord() {
        super(Maddress.MADDRESS);
    }

    /**
     * Create a detached, initialised MaddressRecord
     */
    public MaddressRecord(UUID mid, Addressname addressName, OffsetDateTime modified, String attn, String street1, String street2, String city, String state, String postalCode, String country) {
        super(Maddress.MADDRESS);

        set(0, mid);
        set(1, addressName);
        set(2, modified);
        set(3, attn);
        set(4, street1);
        set(5, street2);
        set(6, city);
        set(7, state);
        set(8, postalCode);
        set(9, country);
    }
}
