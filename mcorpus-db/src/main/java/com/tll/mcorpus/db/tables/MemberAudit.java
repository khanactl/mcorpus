/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.tables;


import com.tll.jooqbind.PostgresInetAddressBinding;
import com.tll.mcorpus.db.Keys;
import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.enums.MemberAuditType;
import com.tll.mcorpus.db.tables.records.MemberAuditRecord;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function5;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row5;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MemberAudit extends TableImpl<MemberAuditRecord> {

		private static final long serialVersionUID = 1L;

		/**
		 * The reference instance of <code>public.member_audit</code>
		 */
		public static final MemberAudit MEMBER_AUDIT = new MemberAudit();

		/**
		 * The class holding records for this type
		 */
		@Override
		public Class<MemberAuditRecord> getRecordType() {
				return MemberAuditRecord.class;
		}

		/**
		 * The column <code>public.member_audit.mid</code>.
		 */
		public final TableField<MemberAuditRecord, UUID> MID = createField(DSL.name("mid"), SQLDataType.UUID.nullable(false), this, "");

		/**
		 * The column <code>public.member_audit.created</code>.
		 */
		public final TableField<MemberAuditRecord, OffsetDateTime> CREATED = createField(DSL.name("created"), SQLDataType.TIMESTAMPWITHTIMEZONE(6).nullable(false).defaultValue(DSL.field("now()", SQLDataType.TIMESTAMPWITHTIMEZONE)), this, "");

		/**
		 * The column <code>public.member_audit.type</code>.
		 */
		public final TableField<MemberAuditRecord, MemberAuditType> TYPE = createField(DSL.name("type"), SQLDataType.VARCHAR.nullable(false).asEnumDataType(com.tll.mcorpus.db.enums.MemberAuditType.class), this, "");

		/**
		 * The column <code>public.member_audit.request_timestamp</code>.
		 */
		public final TableField<MemberAuditRecord, OffsetDateTime> REQUEST_TIMESTAMP = createField(DSL.name("request_timestamp"), SQLDataType.TIMESTAMPWITHTIMEZONE(6).nullable(false), this, "");

		/**
		 * The column <code>public.member_audit.request_origin</code>.
		 */
		public final TableField<MemberAuditRecord, InetAddress> REQUEST_ORIGIN = createField(DSL.name("request_origin"), org.jooq.impl.DefaultDataType.getDefaultDataType("\"pg_catalog\".\"inet\"").nullable(false), this, "", new PostgresInetAddressBinding());

		private MemberAudit(Name alias, Table<MemberAuditRecord> aliased) {
				this(alias, aliased, null);
		}

		private MemberAudit(Name alias, Table<MemberAuditRecord> aliased, Field<?>[] parameters) {
				super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
		}

		/**
		 * Create an aliased <code>public.member_audit</code> table reference
		 */
		public MemberAudit(String alias) {
				this(DSL.name(alias), MEMBER_AUDIT);
		}

		/**
		 * Create an aliased <code>public.member_audit</code> table reference
		 */
		public MemberAudit(Name alias) {
				this(alias, MEMBER_AUDIT);
		}

		/**
		 * Create a <code>public.member_audit</code> table reference
		 */
		public MemberAudit() {
				this(DSL.name("member_audit"), null);
		}

		public <O extends Record> MemberAudit(Table<O> child, ForeignKey<O, MemberAuditRecord> key) {
				super(child, key, MEMBER_AUDIT);
		}

		@Override
		public Schema getSchema() {
				return aliased() ? null : Public.PUBLIC;
		}

		@Override
		public UniqueKey<MemberAuditRecord> getPrimaryKey() {
				return Keys.MEMBER_AUDIT_PKEY;
		}

		@Override
		public List<ForeignKey<MemberAuditRecord, ?>> getReferences() {
				return Arrays.asList(Keys.MEMBER_AUDIT__MEMBER_AUDIT_MID_FKEY);
		}

		private transient Member _member;

		/**
		 * Get the implicit join path to the <code>public.member</code> table.
		 */
		public Member member() {
				if (_member == null)
						_member = new Member(this, Keys.MEMBER_AUDIT__MEMBER_AUDIT_MID_FKEY);

				return _member;
		}

		@Override
		public MemberAudit as(String alias) {
				return new MemberAudit(DSL.name(alias), this);
		}

		@Override
		public MemberAudit as(Name alias) {
				return new MemberAudit(alias, this);
		}

		@Override
		public MemberAudit as(Table<?> alias) {
				return new MemberAudit(alias.getQualifiedName(), this);
		}

		/**
		 * Rename this table
		 */
		@Override
		public MemberAudit rename(String name) {
				return new MemberAudit(DSL.name(name), null);
		}

		/**
		 * Rename this table
		 */
		@Override
		public MemberAudit rename(Name name) {
				return new MemberAudit(name, null);
		}

		/**
		 * Rename this table
		 */
		@Override
		public MemberAudit rename(Table<?> name) {
				return new MemberAudit(name.getQualifiedName(), null);
		}

		// -------------------------------------------------------------------------
		// Row5 type methods
		// -------------------------------------------------------------------------

		@Override
		public Row5<UUID, OffsetDateTime, MemberAuditType, OffsetDateTime, InetAddress> fieldsRow() {
				return (Row5) super.fieldsRow();
		}

		/**
		 * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
		 */
		public <U> SelectField<U> mapping(Function5<? super UUID, ? super OffsetDateTime, ? super MemberAuditType, ? super OffsetDateTime, ? super InetAddress, ? extends U> from) {
				return convertFrom(Records.mapping(from));
		}

		/**
		 * Convenience mapping calling {@link SelectField#convertFrom(Class,
		 * Function)}.
		 */
		public <U> SelectField<U> mapping(Class<U> toType, Function5<? super UUID, ? super OffsetDateTime, ? super MemberAuditType, ? super OffsetDateTime, ? super InetAddress, ? extends U> from) {
				return convertFrom(toType, Records.mapping(from));
		}
}
