/*
 * This file is generated by jOOQ.
*/
package com.tll.mcorpus.db.tables.records;


import com.tll.mcorpus.db.tables.Mauth;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record12;
import org.jooq.Row12;
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
public class MauthRecord extends UpdatableRecordImpl<MauthRecord> implements Record12<UUID, Timestamp, Date, String, String, String, String, String, String, String, String, String> {

    private static final long serialVersionUID = 1701436418;

    /**
     * Setter for <code>public.mauth.mid</code>.
     */
    public void setMid(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.mauth.mid</code>.
     */
    public UUID getMid() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.mauth.modified</code>.
     */
    public void setModified(Timestamp value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.mauth.modified</code>.
     */
    public Timestamp getModified() {
        return (Timestamp) get(1);
    }

    /**
     * Setter for <code>public.mauth.dob</code>.
     */
    public void setDob(Date value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.mauth.dob</code>.
     */
    public Date getDob() {
        return (Date) get(2);
    }

    /**
     * Setter for <code>public.mauth.ssn</code>.
     */
    public void setSsn(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.mauth.ssn</code>.
     */
    public String getSsn() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.mauth.email_personal</code>.
     */
    public void setEmailPersonal(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.mauth.email_personal</code>.
     */
    public String getEmailPersonal() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.mauth.email_work</code>.
     */
    public void setEmailWork(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.mauth.email_work</code>.
     */
    public String getEmailWork() {
        return (String) get(5);
    }

    /**
     * Setter for <code>public.mauth.mobile_phone</code>.
     */
    public void setMobilePhone(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.mauth.mobile_phone</code>.
     */
    public String getMobilePhone() {
        return (String) get(6);
    }

    /**
     * Setter for <code>public.mauth.home_phone</code>.
     */
    public void setHomePhone(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.mauth.home_phone</code>.
     */
    public String getHomePhone() {
        return (String) get(7);
    }

    /**
     * Setter for <code>public.mauth.work_phone</code>.
     */
    public void setWorkPhone(String value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.mauth.work_phone</code>.
     */
    public String getWorkPhone() {
        return (String) get(8);
    }

    /**
     * Setter for <code>public.mauth.fax</code>.
     */
    public void setFax(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.mauth.fax</code>.
     */
    public String getFax() {
        return (String) get(9);
    }

    /**
     * Setter for <code>public.mauth.username</code>.
     */
    public void setUsername(String value) {
        set(10, value);
    }

    /**
     * Getter for <code>public.mauth.username</code>.
     */
    public String getUsername() {
        return (String) get(10);
    }

    /**
     * Setter for <code>public.mauth.pswd</code>.
     */
    public void setPswd(String value) {
        set(11, value);
    }

    /**
     * Getter for <code>public.mauth.pswd</code>.
     */
    public String getPswd() {
        return (String) get(11);
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
    // Record12 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row12<UUID, Timestamp, Date, String, String, String, String, String, String, String, String, String> fieldsRow() {
        return (Row12) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row12<UUID, Timestamp, Date, String, String, String, String, String, String, String, String, String> valuesRow() {
        return (Row12) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field1() {
        return Mauth.MAUTH.MID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field2() {
        return Mauth.MAUTH.MODIFIED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Date> field3() {
        return Mauth.MAUTH.DOB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return Mauth.MAUTH.SSN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return Mauth.MAUTH.EMAIL_PERSONAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return Mauth.MAUTH.EMAIL_WORK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return Mauth.MAUTH.MOBILE_PHONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field8() {
        return Mauth.MAUTH.HOME_PHONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field9() {
        return Mauth.MAUTH.WORK_PHONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field10() {
        return Mauth.MAUTH.FAX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field11() {
        return Mauth.MAUTH.USERNAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field12() {
        return Mauth.MAUTH.PSWD;
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
        return getModified();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date component3() {
        return getDob();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getSsn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component5() {
        return getEmailPersonal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component6() {
        return getEmailWork();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component7() {
        return getMobilePhone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component8() {
        return getHomePhone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component9() {
        return getWorkPhone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component10() {
        return getFax();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component11() {
        return getUsername();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component12() {
        return getPswd();
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
        return getModified();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date value3() {
        return getDob();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getSsn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getEmailPersonal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getEmailWork();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getMobilePhone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value8() {
        return getHomePhone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value9() {
        return getWorkPhone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value10() {
        return getFax();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value11() {
        return getUsername();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value12() {
        return getPswd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MauthRecord value1(UUID value) {
        setMid(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MauthRecord value2(Timestamp value) {
        setModified(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MauthRecord value3(Date value) {
        setDob(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MauthRecord value4(String value) {
        setSsn(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MauthRecord value5(String value) {
        setEmailPersonal(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MauthRecord value6(String value) {
        setEmailWork(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MauthRecord value7(String value) {
        setMobilePhone(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MauthRecord value8(String value) {
        setHomePhone(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MauthRecord value9(String value) {
        setWorkPhone(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MauthRecord value10(String value) {
        setFax(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MauthRecord value11(String value) {
        setUsername(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MauthRecord value12(String value) {
        setPswd(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MauthRecord values(UUID value1, Timestamp value2, Date value3, String value4, String value5, String value6, String value7, String value8, String value9, String value10, String value11, String value12) {
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
        value11(value11);
        value12(value12);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MauthRecord
     */
    public MauthRecord() {
        super(Mauth.MAUTH);
    }

    /**
     * Create a detached, initialised MauthRecord
     */
    public MauthRecord(UUID mid, Timestamp modified, Date dob, String ssn, String emailPersonal, String emailWork, String mobilePhone, String homePhone, String workPhone, String fax, String username, String pswd) {
        super(Mauth.MAUTH);

        set(0, mid);
        set(1, modified);
        set(2, dob);
        set(3, ssn);
        set(4, emailPersonal);
        set(5, emailWork);
        set(6, mobilePhone);
        set(7, homePhone);
        set(8, workPhone);
        set(9, fax);
        set(10, username);
        set(11, pswd);
    }
}
