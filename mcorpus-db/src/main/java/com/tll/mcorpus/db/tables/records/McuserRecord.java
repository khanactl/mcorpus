/*
 * This file is generated by jOOQ.
*/
package com.tll.mcorpus.db.tables.records;


import com.tll.mcorpus.db.enums.MemberStatus;
import com.tll.mcorpus.db.tables.Mcuser;

import java.sql.Timestamp;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record9;
import org.jooq.Row9;
import org.jooq.impl.UpdatableRecordImpl;


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
public class McuserRecord extends UpdatableRecordImpl<McuserRecord> implements Record9<UUID, Timestamp, Timestamp, String, String, String, String, Boolean, MemberStatus> {

    private static final long serialVersionUID = 1797703004;

    /**
     * Setter for <code>public.mcuser.uid</code>.
     */
    public void setUid(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.mcuser.uid</code>.
     */
    public UUID getUid() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.mcuser.created</code>.
     */
    public void setCreated(Timestamp value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.mcuser.created</code>.
     */
    public Timestamp getCreated() {
        return (Timestamp) get(1);
    }

    /**
     * Setter for <code>public.mcuser.modified</code>.
     */
    public void setModified(Timestamp value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.mcuser.modified</code>.
     */
    public Timestamp getModified() {
        return (Timestamp) get(2);
    }

    /**
     * Setter for <code>public.mcuser.name</code>.
     */
    public void setName(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.mcuser.name</code>.
     */
    public String getName() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.mcuser.email</code>.
     */
    public void setEmail(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.mcuser.email</code>.
     */
    public String getEmail() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.mcuser.username</code>.
     */
    public void setUsername(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.mcuser.username</code>.
     */
    public String getUsername() {
        return (String) get(5);
    }

    /**
     * Setter for <code>public.mcuser.pswd</code>.
     */
    public void setPswd(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.mcuser.pswd</code>.
     */
    public String getPswd() {
        return (String) get(6);
    }

    /**
     * Setter for <code>public.mcuser.admin</code>.
     */
    public void setAdmin(Boolean value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.mcuser.admin</code>.
     */
    public Boolean getAdmin() {
        return (Boolean) get(7);
    }

    /**
     * Setter for <code>public.mcuser.status</code>.
     */
    public void setStatus(MemberStatus value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.mcuser.status</code>.
     */
    public MemberStatus getStatus() {
        return (MemberStatus) get(8);
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
    // Record9 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row9<UUID, Timestamp, Timestamp, String, String, String, String, Boolean, MemberStatus> fieldsRow() {
        return (Row9) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row9<UUID, Timestamp, Timestamp, String, String, String, String, Boolean, MemberStatus> valuesRow() {
        return (Row9) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field1() {
        return Mcuser.MCUSER.UID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field2() {
        return Mcuser.MCUSER.CREATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field3() {
        return Mcuser.MCUSER.MODIFIED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return Mcuser.MCUSER.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return Mcuser.MCUSER.EMAIL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return Mcuser.MCUSER.USERNAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return Mcuser.MCUSER.PSWD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field8() {
        return Mcuser.MCUSER.ADMIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<MemberStatus> field9() {
        return Mcuser.MCUSER.STATUS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID component1() {
        return getUid();
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
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component5() {
        return getEmail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component6() {
        return getUsername();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component7() {
        return getPswd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean component8() {
        return getAdmin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberStatus component9() {
        return getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID value1() {
        return getUid();
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
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getEmail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getUsername();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getPswd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value8() {
        return getAdmin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberStatus value9() {
        return getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserRecord value1(UUID value) {
        setUid(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserRecord value2(Timestamp value) {
        setCreated(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserRecord value3(Timestamp value) {
        setModified(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserRecord value4(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserRecord value5(String value) {
        setEmail(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserRecord value6(String value) {
        setUsername(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserRecord value7(String value) {
        setPswd(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserRecord value8(Boolean value) {
        setAdmin(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserRecord value9(MemberStatus value) {
        setStatus(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserRecord values(UUID value1, Timestamp value2, Timestamp value3, String value4, String value5, String value6, String value7, Boolean value8, MemberStatus value9) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached McuserRecord
     */
    public McuserRecord() {
        super(Mcuser.MCUSER);
    }

    /**
     * Create a detached, initialised McuserRecord
     */
    public McuserRecord(UUID uid, Timestamp created, Timestamp modified, String name, String email, String username, String pswd, Boolean admin, MemberStatus status) {
        super(Mcuser.MCUSER);

        set(0, uid);
        set(1, created);
        set(2, modified);
        set(3, name);
        set(4, email);
        set(5, username);
        set(6, pswd);
        set(7, admin);
        set(8, status);
    }
}
