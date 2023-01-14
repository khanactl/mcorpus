/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db;


import com.tll.mcorpus.db.tables.GetActiveLogins;
import com.tll.mcorpus.db.tables.Maddress;
import com.tll.mcorpus.db.tables.Mauth;
import com.tll.mcorpus.db.tables.Mbenefits;
import com.tll.mcorpus.db.tables.Mcuser;
import com.tll.mcorpus.db.tables.McuserAudit;
import com.tll.mcorpus.db.tables.Member;
import com.tll.mcorpus.db.tables.MemberAudit;
import com.tll.mcorpus.db.tables.records.GetActiveLoginsRecord;
import com.tll.mcorpus.db.udt.JwtMcuserStatus;
import com.tll.mcorpus.db.udt.Mref;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.jooq.Catalog;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.UDT;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

		private static final long serialVersionUID = 1L;

		/**
		 * The reference instance of <code>public</code>
		 */
		public static final Public PUBLIC = new Public();

		/**
		 * The table <code>public.get_active_logins</code>.
		 */
		public final GetActiveLogins GET_ACTIVE_LOGINS = GetActiveLogins.GET_ACTIVE_LOGINS;

		/**
		 * Call <code>public.get_active_logins</code>.
		 */
		public static Result<GetActiveLoginsRecord> GET_ACTIVE_LOGINS(
					Configuration configuration
				, UUID mcuserId
		) {
				return configuration.dsl().selectFrom(com.tll.mcorpus.db.tables.GetActiveLogins.GET_ACTIVE_LOGINS.call(
							mcuserId
				)).fetch();
		}

		/**
		 * Get <code>public.get_active_logins</code> as a table.
		 */
		public static GetActiveLogins GET_ACTIVE_LOGINS(
					UUID mcuserId
		) {
				return com.tll.mcorpus.db.tables.GetActiveLogins.GET_ACTIVE_LOGINS.call(
						mcuserId
				);
		}

		/**
		 * Get <code>public.get_active_logins</code> as a table.
		 */
		public static GetActiveLogins GET_ACTIVE_LOGINS(
					Field<UUID> mcuserId
		) {
				return com.tll.mcorpus.db.tables.GetActiveLogins.GET_ACTIVE_LOGINS.call(
						mcuserId
				);
		}

		/**
		 * The table <code>public.maddress</code>.
		 */
		public final Maddress MADDRESS = Maddress.MADDRESS;

		/**
		 * The table <code>public.mauth</code>.
		 */
		public final Mauth MAUTH = Mauth.MAUTH;

		/**
		 * The table <code>public.mbenefits</code>.
		 */
		public final Mbenefits MBENEFITS = Mbenefits.MBENEFITS;

		/**
		 * The table <code>public.mcuser</code>.
		 */
		public final Mcuser MCUSER = Mcuser.MCUSER;

		/**
		 * The table <code>public.mcuser_audit</code>.
		 */
		public final McuserAudit MCUSER_AUDIT = McuserAudit.MCUSER_AUDIT;

		/**
		 * The table <code>public.member</code>.
		 */
		public final Member MEMBER = Member.MEMBER;

		/**
		 * The table <code>public.member_audit</code>.
		 */
		public final MemberAudit MEMBER_AUDIT = MemberAudit.MEMBER_AUDIT;

		/**
		 * No further instances allowed
		 */
		private Public() {
				super("public", null);
		}


		@Override
		public Catalog getCatalog() {
				return DefaultCatalog.DEFAULT_CATALOG;
		}

		@Override
		public final List<Table<?>> getTables() {
				return Arrays.asList(
						GetActiveLogins.GET_ACTIVE_LOGINS,
						Maddress.MADDRESS,
						Mauth.MAUTH,
						Mbenefits.MBENEFITS,
						Mcuser.MCUSER,
						McuserAudit.MCUSER_AUDIT,
						Member.MEMBER,
						MemberAudit.MEMBER_AUDIT
				);
		}

		@Override
		public final List<UDT<?>> getUDTs() {
				return Arrays.asList(
						JwtMcuserStatus.JWT_MCUSER_STATUS,
						Mref.MREF
				);
		}
}
