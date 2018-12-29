/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.records;


import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.MemberStatus;
import com.tll.mcorpus.db.tables.Member;

import java.sql.Timestamp;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record10;
import org.jooq.Row10;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.8"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MemberRecord extends UpdatableRecordImpl<MemberRecord> implements Record10<UUID, Timestamp, Timestamp, String, Location, String, String, String, String, MemberStatus> {

    private static final long serialVersionUID = 1032728603;

    /**
     * Setter for <code>public.member.mid</code>.
     */
    public void setMid(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.member.mid</code>.
     */
    public UUID getMid() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.member.created</code>.
     */
    public void setCreated(Timestamp value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.member.created</code>.
     */
    public Timestamp getCreated() {
        return (Timestamp) get(1);
    }

    /**
     * Setter for <code>public.member.modified</code>.
     */
    public void setModified(Timestamp value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.member.modified</code>.
     */
    public Timestamp getModified() {
        return (Timestamp) get(2);
    }

    /**
     * Setter for <code>public.member.emp_id</code>.
     */
    public void setEmpId(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.member.emp_id</code>.
     */
    public String getEmpId() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.member.location</code>.
     */
    public void setLocation(Location value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.member.location</code>.
     */
    public Location getLocation() {
        return (Location) get(4);
    }

    /**
     * Setter for <code>public.member.name_first</code>.
     */
    public void setNameFirst(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.member.name_first</code>.
     */
    public String getNameFirst() {
        return (String) get(5);
    }

    /**
     * Setter for <code>public.member.name_middle</code>.
     */
    public void setNameMiddle(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.member.name_middle</code>.
     */
    public String getNameMiddle() {
        return (String) get(6);
    }

    /**
     * Setter for <code>public.member.name_last</code>.
     */
    public void setNameLast(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.member.name_last</code>.
     */
    public String getNameLast() {
        return (String) get(7);
    }

    /**
     * Setter for <code>public.member.display_name</code>.
     */
    public void setDisplayName(String value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.member.display_name</code>.
     */
    public String getDisplayName() {
        return (String) get(8);
    }

    /**
     * Setter for <code>public.member.status</code>.
     */
    public void setStatus(MemberStatus value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.member.status</code>.
     */
    public MemberStatus getStatus() {
        return (MemberStatus) get(9);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<UUID> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record10 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row10<UUID, Timestamp, Timestamp, String, Location, String, String, String, String, MemberStatus> fieldsRow() {
        return (Row10) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row10<UUID, Timestamp, Timestamp, String, Location, String, String, String, String, MemberStatus> valuesRow() {
        return (Row10) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field1() {
        return Member.MEMBER.MID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field2() {
        return Member.MEMBER.CREATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field3() {
        return Member.MEMBER.MODIFIED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return Member.MEMBER.EMP_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Location> field5() {
        return Member.MEMBER.LOCATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return Member.MEMBER.NAME_FIRST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return Member.MEMBER.NAME_MIDDLE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field8() {
        return Member.MEMBER.NAME_LAST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field9() {
        return Member.MEMBER.DISPLAY_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<MemberStatus> field10() {
        return Member.MEMBER.STATUS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID component1() {
        return getMid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component2() {
        return getCreated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component3() {
        return getModified();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getEmpId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location component5() {
        return getLocation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component6() {
        return getNameFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component7() {
        return getNameMiddle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component8() {
        return getNameLast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component9() {
        return getDisplayName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberStatus component10() {
        return getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID value1() {
        return getMid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value2() {
        return getCreated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value3() {
        return getModified();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getEmpId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location value5() {
        return getLocation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getNameFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getNameMiddle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value8() {
        return getNameLast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value9() {
        return getDisplayName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberStatus value10() {
        return getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberRecord value1(UUID value) {
        setMid(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberRecord value2(Timestamp value) {
        setCreated(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberRecord value3(Timestamp value) {
        setModified(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberRecord value4(String value) {
        setEmpId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberRecord value5(Location value) {
        setLocation(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberRecord value6(String value) {
        setNameFirst(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberRecord value7(String value) {
        setNameMiddle(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberRecord value8(String value) {
        setNameLast(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberRecord value9(String value) {
        setDisplayName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberRecord value10(MemberStatus value) {
        setStatus(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberRecord values(UUID value1, Timestamp value2, Timestamp value3, String value4, Location value5, String value6, String value7, String value8, String value9, MemberStatus value10) {
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
     * Create a detached MemberRecord
     */
    public MemberRecord() {
        super(Member.MEMBER);
    }

    /**
     * Create a detached, initialised MemberRecord
     */
    public MemberRecord(UUID mid, Timestamp created, Timestamp modified, String empId, Location location, String nameFirst, String nameMiddle, String nameLast, String displayName, MemberStatus status) {
        super(Member.MEMBER);

        set(0, mid);
        set(1, created);
        set(2, modified);
        set(3, empId);
        set(4, location);
        set(5, nameFirst);
        set(6, nameMiddle);
        set(7, nameLast);
        set(8, displayName);
        set(9, status);
    }
}
