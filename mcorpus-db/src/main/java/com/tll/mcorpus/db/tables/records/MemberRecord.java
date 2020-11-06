/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.records;


import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.MemberStatus;
import com.tll.mcorpus.db.tables.Member;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record10;
import org.jooq.Row10;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MemberRecord extends UpdatableRecordImpl<MemberRecord> implements Record10<UUID, OffsetDateTime, OffsetDateTime, String, Location, String, String, String, String, MemberStatus> {

    private static final long serialVersionUID = 1L;

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
    public void setCreated(OffsetDateTime value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.member.created</code>.
     */
    public OffsetDateTime getCreated() {
        return (OffsetDateTime) get(1);
    }

    /**
     * Setter for <code>public.member.modified</code>.
     */
    public void setModified(OffsetDateTime value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.member.modified</code>.
     */
    public OffsetDateTime getModified() {
        return (OffsetDateTime) get(2);
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

    @Override
    public Record1<UUID> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record10 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row10<UUID, OffsetDateTime, OffsetDateTime, String, Location, String, String, String, String, MemberStatus> fieldsRow() {
        return (Row10) super.fieldsRow();
    }

    @Override
    public Row10<UUID, OffsetDateTime, OffsetDateTime, String, Location, String, String, String, String, MemberStatus> valuesRow() {
        return (Row10) super.valuesRow();
    }

    @Override
    public Field<UUID> field1() {
        return Member.MEMBER.MID;
    }

    @Override
    public Field<OffsetDateTime> field2() {
        return Member.MEMBER.CREATED;
    }

    @Override
    public Field<OffsetDateTime> field3() {
        return Member.MEMBER.MODIFIED;
    }

    @Override
    public Field<String> field4() {
        return Member.MEMBER.EMP_ID;
    }

    @Override
    public Field<Location> field5() {
        return Member.MEMBER.LOCATION;
    }

    @Override
    public Field<String> field6() {
        return Member.MEMBER.NAME_FIRST;
    }

    @Override
    public Field<String> field7() {
        return Member.MEMBER.NAME_MIDDLE;
    }

    @Override
    public Field<String> field8() {
        return Member.MEMBER.NAME_LAST;
    }

    @Override
    public Field<String> field9() {
        return Member.MEMBER.DISPLAY_NAME;
    }

    @Override
    public Field<MemberStatus> field10() {
        return Member.MEMBER.STATUS;
    }

    @Override
    public UUID component1() {
        return getMid();
    }

    @Override
    public OffsetDateTime component2() {
        return getCreated();
    }

    @Override
    public OffsetDateTime component3() {
        return getModified();
    }

    @Override
    public String component4() {
        return getEmpId();
    }

    @Override
    public Location component5() {
        return getLocation();
    }

    @Override
    public String component6() {
        return getNameFirst();
    }

    @Override
    public String component7() {
        return getNameMiddle();
    }

    @Override
    public String component8() {
        return getNameLast();
    }

    @Override
    public String component9() {
        return getDisplayName();
    }

    @Override
    public MemberStatus component10() {
        return getStatus();
    }

    @Override
    public UUID value1() {
        return getMid();
    }

    @Override
    public OffsetDateTime value2() {
        return getCreated();
    }

    @Override
    public OffsetDateTime value3() {
        return getModified();
    }

    @Override
    public String value4() {
        return getEmpId();
    }

    @Override
    public Location value5() {
        return getLocation();
    }

    @Override
    public String value6() {
        return getNameFirst();
    }

    @Override
    public String value7() {
        return getNameMiddle();
    }

    @Override
    public String value8() {
        return getNameLast();
    }

    @Override
    public String value9() {
        return getDisplayName();
    }

    @Override
    public MemberStatus value10() {
        return getStatus();
    }

    @Override
    public MemberRecord value1(UUID value) {
        setMid(value);
        return this;
    }

    @Override
    public MemberRecord value2(OffsetDateTime value) {
        setCreated(value);
        return this;
    }

    @Override
    public MemberRecord value3(OffsetDateTime value) {
        setModified(value);
        return this;
    }

    @Override
    public MemberRecord value4(String value) {
        setEmpId(value);
        return this;
    }

    @Override
    public MemberRecord value5(Location value) {
        setLocation(value);
        return this;
    }

    @Override
    public MemberRecord value6(String value) {
        setNameFirst(value);
        return this;
    }

    @Override
    public MemberRecord value7(String value) {
        setNameMiddle(value);
        return this;
    }

    @Override
    public MemberRecord value8(String value) {
        setNameLast(value);
        return this;
    }

    @Override
    public MemberRecord value9(String value) {
        setDisplayName(value);
        return this;
    }

    @Override
    public MemberRecord value10(MemberStatus value) {
        setStatus(value);
        return this;
    }

    @Override
    public MemberRecord values(UUID value1, OffsetDateTime value2, OffsetDateTime value3, String value4, Location value5, String value6, String value7, String value8, String value9, MemberStatus value10) {
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
    public MemberRecord(UUID mid, OffsetDateTime created, OffsetDateTime modified, String empId, Location location, String nameFirst, String nameMiddle, String nameLast, String displayName, MemberStatus status) {
        super(Member.MEMBER);

        setMid(mid);
        setCreated(created);
        setModified(modified);
        setEmpId(empId);
        setLocation(location);
        setNameFirst(nameFirst);
        setNameMiddle(nameMiddle);
        setNameLast(nameLast);
        setDisplayName(displayName);
        setStatus(status);
    }
}
