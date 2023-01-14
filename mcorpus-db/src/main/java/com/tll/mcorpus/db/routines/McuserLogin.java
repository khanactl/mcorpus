/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.routines;


import com.tll.jooqbind.PostgresInetAddressBinding;
import com.tll.mcorpus.db.Public;
import com.tll.mcorpus.db.tables.records.McuserRecord;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class McuserLogin extends AbstractRoutine<McuserRecord> {

		private static final long serialVersionUID = 1L;

		/**
		 * The parameter <code>public.mcuser_login.RETURN_VALUE</code>.
		 */
		public static final Parameter<McuserRecord> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", com.tll.mcorpus.db.tables.Mcuser.MCUSER.getDataType(), false, false);

		/**
		 * The parameter <code>public.mcuser_login.mcuser_username</code>.
		 */
		public static final Parameter<String> MCUSER_USERNAME = Internal.createParameter("mcuser_username", SQLDataType.CLOB, false, false);

		/**
		 * The parameter <code>public.mcuser_login.mcuser_password</code>.
		 */
		public static final Parameter<String> MCUSER_PASSWORD = Internal.createParameter("mcuser_password", SQLDataType.CLOB, false, false);

		/**
		 * The parameter <code>public.mcuser_login.in_request_timestamp</code>.
		 */
		public static final Parameter<OffsetDateTime> IN_REQUEST_TIMESTAMP = Internal.createParameter("in_request_timestamp", SQLDataType.TIMESTAMPWITHTIMEZONE(6), false, false);

		/**
		 * The parameter <code>public.mcuser_login.in_request_origin</code>.
		 */
		public static final Parameter<InetAddress> IN_REQUEST_ORIGIN = Internal.createParameter("in_request_origin", org.jooq.impl.DefaultDataType.getDefaultDataType("\"pg_catalog\".\"inet\""), false, false, new PostgresInetAddressBinding());

		/**
		 * The parameter <code>public.mcuser_login.in_login_expiration</code>.
		 */
		public static final Parameter<OffsetDateTime> IN_LOGIN_EXPIRATION = Internal.createParameter("in_login_expiration", SQLDataType.TIMESTAMPWITHTIMEZONE(6), false, false);

		/**
		 * The parameter <code>public.mcuser_login.in_jwt_id</code>.
		 */
		public static final Parameter<UUID> IN_JWT_ID = Internal.createParameter("in_jwt_id", SQLDataType.UUID, false, false);

		/**
		 * Create a new routine call instance
		 */
		public McuserLogin() {
				super("mcuser_login", Public.PUBLIC, com.tll.mcorpus.db.tables.Mcuser.MCUSER.getDataType());

				setReturnParameter(RETURN_VALUE);
				addInParameter(MCUSER_USERNAME);
				addInParameter(MCUSER_PASSWORD);
				addInParameter(IN_REQUEST_TIMESTAMP);
				addInParameter(IN_REQUEST_ORIGIN);
				addInParameter(IN_LOGIN_EXPIRATION);
				addInParameter(IN_JWT_ID);
		}

		/**
		 * Set the <code>mcuser_username</code> parameter IN value to the routine
		 */
		public void setMcuserUsername(String value) {
				setValue(MCUSER_USERNAME, value);
		}

		/**
		 * Set the <code>mcuser_username</code> parameter to the function to be used
		 * with a {@link org.jooq.Select} statement
		 */
		public void setMcuserUsername(Field<String> field) {
				setField(MCUSER_USERNAME, field);
		}

		/**
		 * Set the <code>mcuser_password</code> parameter IN value to the routine
		 */
		public void setMcuserPassword(String value) {
				setValue(MCUSER_PASSWORD, value);
		}

		/**
		 * Set the <code>mcuser_password</code> parameter to the function to be used
		 * with a {@link org.jooq.Select} statement
		 */
		public void setMcuserPassword(Field<String> field) {
				setField(MCUSER_PASSWORD, field);
		}

		/**
		 * Set the <code>in_request_timestamp</code> parameter IN value to the
		 * routine
		 */
		public void setInRequestTimestamp(OffsetDateTime value) {
				setValue(IN_REQUEST_TIMESTAMP, value);
		}

		/**
		 * Set the <code>in_request_timestamp</code> parameter to the function to be
		 * used with a {@link org.jooq.Select} statement
		 */
		public void setInRequestTimestamp(Field<OffsetDateTime> field) {
				setField(IN_REQUEST_TIMESTAMP, field);
		}

		/**
		 * Set the <code>in_request_origin</code> parameter IN value to the routine
		 */
		public void setInRequestOrigin(InetAddress value) {
				setValue(IN_REQUEST_ORIGIN, value);
		}

		/**
		 * Set the <code>in_request_origin</code> parameter to the function to be
		 * used with a {@link org.jooq.Select} statement
		 */
		public void setInRequestOrigin(Field<InetAddress> field) {
				setField(IN_REQUEST_ORIGIN, field);
		}

		/**
		 * Set the <code>in_login_expiration</code> parameter IN value to the
		 * routine
		 */
		public void setInLoginExpiration(OffsetDateTime value) {
				setValue(IN_LOGIN_EXPIRATION, value);
		}

		/**
		 * Set the <code>in_login_expiration</code> parameter to the function to be
		 * used with a {@link org.jooq.Select} statement
		 */
		public void setInLoginExpiration(Field<OffsetDateTime> field) {
				setField(IN_LOGIN_EXPIRATION, field);
		}

		/**
		 * Set the <code>in_jwt_id</code> parameter IN value to the routine
		 */
		public void setInJwtId(UUID value) {
				setValue(IN_JWT_ID, value);
		}

		/**
		 * Set the <code>in_jwt_id</code> parameter to the function to be used with
		 * a {@link org.jooq.Select} statement
		 */
		public void setInJwtId(Field<UUID> field) {
				setField(IN_JWT_ID, field);
		}
}
