/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.routines;


import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.MemberStatus;

import java.sql.Date;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class InsertMember extends AbstractRoutine<java.lang.Void> {

    private static final long serialVersionUID = 1757340377;

    /**
     * The parameter <code>public.insert_member.in_emp_id</code>.
     */
    public static final Parameter<String> IN_EMP_ID = Internal.createParameter("in_emp_id", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.in_location</code>.
     */
    public static final Parameter<Location> IN_LOCATION = Internal.createParameter("in_location", org.jooq.impl.SQLDataType.VARCHAR.asEnumDataType(com.tll.mcorpus.db.enums.Location.class), false, false);

    /**
     * The parameter <code>public.insert_member.in_name_first</code>.
     */
    public static final Parameter<String> IN_NAME_FIRST = Internal.createParameter("in_name_first", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.in_name_middle</code>.
     */
    public static final Parameter<String> IN_NAME_MIDDLE = Internal.createParameter("in_name_middle", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.in_name_last</code>.
     */
    public static final Parameter<String> IN_NAME_LAST = Internal.createParameter("in_name_last", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.in_display_name</code>.
     */
    public static final Parameter<String> IN_DISPLAY_NAME = Internal.createParameter("in_display_name", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.in_status</code>.
     */
    public static final Parameter<MemberStatus> IN_STATUS = Internal.createParameter("in_status", org.jooq.impl.SQLDataType.VARCHAR.asEnumDataType(com.tll.mcorpus.db.enums.MemberStatus.class), false, false);

    /**
     * The parameter <code>public.insert_member.in_dob</code>.
     */
    public static final Parameter<Date> IN_DOB = Internal.createParameter("in_dob", org.jooq.impl.SQLDataType.DATE, false, false);

    /**
     * The parameter <code>public.insert_member.in_ssn</code>.
     */
    public static final Parameter<String> IN_SSN = Internal.createParameter("in_ssn", org.jooq.impl.SQLDataType.CHAR, false, false);

    /**
     * The parameter <code>public.insert_member.in_email_personal</code>.
     */
    public static final Parameter<String> IN_EMAIL_PERSONAL = Internal.createParameter("in_email_personal", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.in_email_work</code>.
     */
    public static final Parameter<String> IN_EMAIL_WORK = Internal.createParameter("in_email_work", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.in_mobile_phone</code>.
     */
    public static final Parameter<String> IN_MOBILE_PHONE = Internal.createParameter("in_mobile_phone", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.in_home_phone</code>.
     */
    public static final Parameter<String> IN_HOME_PHONE = Internal.createParameter("in_home_phone", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.in_work_phone</code>.
     */
    public static final Parameter<String> IN_WORK_PHONE = Internal.createParameter("in_work_phone", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.in_fax</code>.
     */
    public static final Parameter<String> IN_FAX = Internal.createParameter("in_fax", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.in_username</code>.
     */
    public static final Parameter<String> IN_USERNAME = Internal.createParameter("in_username", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.in_pswd</code>.
     */
    public static final Parameter<String> IN_PSWD = Internal.createParameter("in_pswd", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.out_mid</code>.
     */
    public static final Parameter<UUID> OUT_MID = Internal.createParameter("out_mid", org.jooq.impl.SQLDataType.UUID, false, false);

    /**
     * The parameter <code>public.insert_member.out_created</code>.
     */
    public static final Parameter<OffsetDateTime> OUT_CREATED = Internal.createParameter("out_created", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, false, false);

    /**
     * The parameter <code>public.insert_member.out_modified</code>.
     */
    public static final Parameter<OffsetDateTime> OUT_MODIFIED = Internal.createParameter("out_modified", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, false, false);

    /**
     * The parameter <code>public.insert_member.out_emp_id</code>.
     */
    public static final Parameter<String> OUT_EMP_ID = Internal.createParameter("out_emp_id", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.out_location</code>.
     */
    public static final Parameter<Location> OUT_LOCATION = Internal.createParameter("out_location", org.jooq.impl.SQLDataType.VARCHAR.asEnumDataType(com.tll.mcorpus.db.enums.Location.class), false, false);

    /**
     * The parameter <code>public.insert_member.out_name_first</code>.
     */
    public static final Parameter<String> OUT_NAME_FIRST = Internal.createParameter("out_name_first", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.out_name_middle</code>.
     */
    public static final Parameter<String> OUT_NAME_MIDDLE = Internal.createParameter("out_name_middle", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.out_name_last</code>.
     */
    public static final Parameter<String> OUT_NAME_LAST = Internal.createParameter("out_name_last", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.out_display_name</code>.
     */
    public static final Parameter<String> OUT_DISPLAY_NAME = Internal.createParameter("out_display_name", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.out_status</code>.
     */
    public static final Parameter<MemberStatus> OUT_STATUS = Internal.createParameter("out_status", org.jooq.impl.SQLDataType.VARCHAR.asEnumDataType(com.tll.mcorpus.db.enums.MemberStatus.class), false, false);

    /**
     * The parameter <code>public.insert_member.out_dob</code>.
     */
    public static final Parameter<Date> OUT_DOB = Internal.createParameter("out_dob", org.jooq.impl.SQLDataType.DATE, false, false);

    /**
     * The parameter <code>public.insert_member.out_ssn</code>.
     */
    public static final Parameter<String> OUT_SSN = Internal.createParameter("out_ssn", org.jooq.impl.SQLDataType.CHAR, false, false);

    /**
     * The parameter <code>public.insert_member.out_email_personal</code>.
     */
    public static final Parameter<String> OUT_EMAIL_PERSONAL = Internal.createParameter("out_email_personal", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.out_email_work</code>.
     */
    public static final Parameter<String> OUT_EMAIL_WORK = Internal.createParameter("out_email_work", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.out_mobile_phone</code>.
     */
    public static final Parameter<String> OUT_MOBILE_PHONE = Internal.createParameter("out_mobile_phone", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.out_home_phone</code>.
     */
    public static final Parameter<String> OUT_HOME_PHONE = Internal.createParameter("out_home_phone", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.out_work_phone</code>.
     */
    public static final Parameter<String> OUT_WORK_PHONE = Internal.createParameter("out_work_phone", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.out_fax</code>.
     */
    public static final Parameter<String> OUT_FAX = Internal.createParameter("out_fax", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>public.insert_member.out_username</code>.
     */
    public static final Parameter<String> OUT_USERNAME = Internal.createParameter("out_username", org.jooq.impl.SQLDataType.CLOB, false, false);

    /**
     * Create a new routine call instance
     */
    public InsertMember() {
        super("insert_member", Public.PUBLIC);

        addInParameter(IN_EMP_ID);
        addInParameter(IN_LOCATION);
        addInParameter(IN_NAME_FIRST);
        addInParameter(IN_NAME_MIDDLE);
        addInParameter(IN_NAME_LAST);
        addInParameter(IN_DISPLAY_NAME);
        addInParameter(IN_STATUS);
        addInParameter(IN_DOB);
        addInParameter(IN_SSN);
        addInParameter(IN_EMAIL_PERSONAL);
        addInParameter(IN_EMAIL_WORK);
        addInParameter(IN_MOBILE_PHONE);
        addInParameter(IN_HOME_PHONE);
        addInParameter(IN_WORK_PHONE);
        addInParameter(IN_FAX);
        addInParameter(IN_USERNAME);
        addInParameter(IN_PSWD);
        addOutParameter(OUT_MID);
        addOutParameter(OUT_CREATED);
        addOutParameter(OUT_MODIFIED);
        addOutParameter(OUT_EMP_ID);
        addOutParameter(OUT_LOCATION);
        addOutParameter(OUT_NAME_FIRST);
        addOutParameter(OUT_NAME_MIDDLE);
        addOutParameter(OUT_NAME_LAST);
        addOutParameter(OUT_DISPLAY_NAME);
        addOutParameter(OUT_STATUS);
        addOutParameter(OUT_DOB);
        addOutParameter(OUT_SSN);
        addOutParameter(OUT_EMAIL_PERSONAL);
        addOutParameter(OUT_EMAIL_WORK);
        addOutParameter(OUT_MOBILE_PHONE);
        addOutParameter(OUT_HOME_PHONE);
        addOutParameter(OUT_WORK_PHONE);
        addOutParameter(OUT_FAX);
        addOutParameter(OUT_USERNAME);
    }

    /**
     * Set the <code>in_emp_id</code> parameter IN value to the routine
     */
    public void setInEmpId(String value) {
        setValue(IN_EMP_ID, value);
    }

    /**
     * Set the <code>in_location</code> parameter IN value to the routine
     */
    public void setInLocation(Location value) {
        setValue(IN_LOCATION, value);
    }

    /**
     * Set the <code>in_name_first</code> parameter IN value to the routine
     */
    public void setInNameFirst(String value) {
        setValue(IN_NAME_FIRST, value);
    }

    /**
     * Set the <code>in_name_middle</code> parameter IN value to the routine
     */
    public void setInNameMiddle(String value) {
        setValue(IN_NAME_MIDDLE, value);
    }

    /**
     * Set the <code>in_name_last</code> parameter IN value to the routine
     */
    public void setInNameLast(String value) {
        setValue(IN_NAME_LAST, value);
    }

    /**
     * Set the <code>in_display_name</code> parameter IN value to the routine
     */
    public void setInDisplayName(String value) {
        setValue(IN_DISPLAY_NAME, value);
    }

    /**
     * Set the <code>in_status</code> parameter IN value to the routine
     */
    public void setInStatus(MemberStatus value) {
        setValue(IN_STATUS, value);
    }

    /**
     * Set the <code>in_dob</code> parameter IN value to the routine
     */
    public void setInDob(Date value) {
        setValue(IN_DOB, value);
    }

    /**
     * Set the <code>in_ssn</code> parameter IN value to the routine
     */
    public void setInSsn(String value) {
        setValue(IN_SSN, value);
    }

    /**
     * Set the <code>in_email_personal</code> parameter IN value to the routine
     */
    public void setInEmailPersonal(String value) {
        setValue(IN_EMAIL_PERSONAL, value);
    }

    /**
     * Set the <code>in_email_work</code> parameter IN value to the routine
     */
    public void setInEmailWork(String value) {
        setValue(IN_EMAIL_WORK, value);
    }

    /**
     * Set the <code>in_mobile_phone</code> parameter IN value to the routine
     */
    public void setInMobilePhone(String value) {
        setValue(IN_MOBILE_PHONE, value);
    }

    /**
     * Set the <code>in_home_phone</code> parameter IN value to the routine
     */
    public void setInHomePhone(String value) {
        setValue(IN_HOME_PHONE, value);
    }

    /**
     * Set the <code>in_work_phone</code> parameter IN value to the routine
     */
    public void setInWorkPhone(String value) {
        setValue(IN_WORK_PHONE, value);
    }

    /**
     * Set the <code>in_fax</code> parameter IN value to the routine
     */
    public void setInFax(String value) {
        setValue(IN_FAX, value);
    }

    /**
     * Set the <code>in_username</code> parameter IN value to the routine
     */
    public void setInUsername(String value) {
        setValue(IN_USERNAME, value);
    }

    /**
     * Set the <code>in_pswd</code> parameter IN value to the routine
     */
    public void setInPswd(String value) {
        setValue(IN_PSWD, value);
    }

    /**
     * Get the <code>out_mid</code> parameter OUT value from the routine
     */
    public UUID getOutMid() {
        return get(OUT_MID);
    }

    /**
     * Get the <code>out_created</code> parameter OUT value from the routine
     */
    public OffsetDateTime getOutCreated() {
        return get(OUT_CREATED);
    }

    /**
     * Get the <code>out_modified</code> parameter OUT value from the routine
     */
    public OffsetDateTime getOutModified() {
        return get(OUT_MODIFIED);
    }

    /**
     * Get the <code>out_emp_id</code> parameter OUT value from the routine
     */
    public String getOutEmpId() {
        return get(OUT_EMP_ID);
    }

    /**
     * Get the <code>out_location</code> parameter OUT value from the routine
     */
    public Location getOutLocation() {
        return get(OUT_LOCATION);
    }

    /**
     * Get the <code>out_name_first</code> parameter OUT value from the routine
     */
    public String getOutNameFirst() {
        return get(OUT_NAME_FIRST);
    }

    /**
     * Get the <code>out_name_middle</code> parameter OUT value from the routine
     */
    public String getOutNameMiddle() {
        return get(OUT_NAME_MIDDLE);
    }

    /**
     * Get the <code>out_name_last</code> parameter OUT value from the routine
     */
    public String getOutNameLast() {
        return get(OUT_NAME_LAST);
    }

    /**
     * Get the <code>out_display_name</code> parameter OUT value from the routine
     */
    public String getOutDisplayName() {
        return get(OUT_DISPLAY_NAME);
    }

    /**
     * Get the <code>out_status</code> parameter OUT value from the routine
     */
    public MemberStatus getOutStatus() {
        return get(OUT_STATUS);
    }

    /**
     * Get the <code>out_dob</code> parameter OUT value from the routine
     */
    public Date getOutDob() {
        return get(OUT_DOB);
    }

    /**
     * Get the <code>out_ssn</code> parameter OUT value from the routine
     */
    public String getOutSsn() {
        return get(OUT_SSN);
    }

    /**
     * Get the <code>out_email_personal</code> parameter OUT value from the routine
     */
    public String getOutEmailPersonal() {
        return get(OUT_EMAIL_PERSONAL);
    }

    /**
     * Get the <code>out_email_work</code> parameter OUT value from the routine
     */
    public String getOutEmailWork() {
        return get(OUT_EMAIL_WORK);
    }

    /**
     * Get the <code>out_mobile_phone</code> parameter OUT value from the routine
     */
    public String getOutMobilePhone() {
        return get(OUT_MOBILE_PHONE);
    }

    /**
     * Get the <code>out_home_phone</code> parameter OUT value from the routine
     */
    public String getOutHomePhone() {
        return get(OUT_HOME_PHONE);
    }

    /**
     * Get the <code>out_work_phone</code> parameter OUT value from the routine
     */
    public String getOutWorkPhone() {
        return get(OUT_WORK_PHONE);
    }

    /**
     * Get the <code>out_fax</code> parameter OUT value from the routine
     */
    public String getOutFax() {
        return get(OUT_FAX);
    }

    /**
     * Get the <code>out_username</code> parameter OUT value from the routine
     */
    public String getOutUsername() {
        return get(OUT_USERNAME);
    }
}
