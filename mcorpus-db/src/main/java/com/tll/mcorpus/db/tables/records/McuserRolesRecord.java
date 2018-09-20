/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables.records;


import com.tll.mcorpus.db.enums.McuserRole;
import com.tll.mcorpus.db.tables.McuserRoles;

import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class McuserRolesRecord extends UpdatableRecordImpl<McuserRolesRecord> implements Record2<UUID, McuserRole> {

    private static final long serialVersionUID = 422026654;

    /**
     * Setter for <code>public.mcuser_roles.uid</code>.
     */
    public void setUid(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.mcuser_roles.uid</code>.
     */
    public UUID getUid() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.mcuser_roles.role</code>.
     */
    public void setRole(McuserRole value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.mcuser_roles.role</code>.
     */
    public McuserRole getRole() {
        return (McuserRole) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record2<UUID, McuserRole> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<UUID, McuserRole> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<UUID, McuserRole> valuesRow() {
        return (Row2) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field1() {
        return McuserRoles.MCUSER_ROLES.UID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<McuserRole> field2() {
        return McuserRoles.MCUSER_ROLES.ROLE;
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
    public McuserRole component2() {
        return getRole();
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
    public McuserRole value2() {
        return getRole();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserRolesRecord value1(UUID value) {
        setUid(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserRolesRecord value2(McuserRole value) {
        setRole(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public McuserRolesRecord values(UUID value1, McuserRole value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached McuserRolesRecord
     */
    public McuserRolesRecord() {
        super(McuserRoles.MCUSER_ROLES);
    }

    /**
     * Create a detached, initialised McuserRolesRecord
     */
    public McuserRolesRecord(UUID uid, McuserRole role) {
        super(McuserRoles.MCUSER_ROLES);

        set(0, uid);
        set(1, role);
    }
}