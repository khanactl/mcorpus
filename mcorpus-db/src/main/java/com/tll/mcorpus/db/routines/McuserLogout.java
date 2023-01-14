/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.routines;


import com.tll.jooqbind.PostgresInetAddressBinding;
import com.tll.mcorpus.db.Public;

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
public class McuserLogout extends AbstractRoutine<Boolean> {

		private static final long serialVersionUID = 1L;

		/**
		 * The parameter <code>public.mcuser_logout.RETURN_VALUE</code>.
		 */
		public static final Parameter<Boolean> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", SQLDataType.BOOLEAN, false, false);

		/**
		 * The parameter <code>public.mcuser_logout.mcuser_uid</code>.
		 */
		public static final Parameter<UUID> MCUSER_UID = Internal.createParameter("mcuser_uid", SQLDataType.UUID, false, false);

		/**
		 * The parameter <code>public.mcuser_logout.jwt_id</code>.
		 */
		public static final Parameter<UUID> JWT_ID = Internal.createParameter("jwt_id", SQLDataType.UUID, false, false);

		/**
		 * The parameter <code>public.mcuser_logout.request_timestamp</code>.
		 */
		public static final Parameter<OffsetDateTime> REQUEST_TIMESTAMP = Internal.createParameter("request_timestamp", SQLDataType.TIMESTAMPWITHTIMEZONE(6), false, false);

		/**
		 * The parameter <code>public.mcuser_logout.request_origin</code>.
		 */
		public static final Parameter<InetAddress> REQUEST_ORIGIN = Internal.createParameter("request_origin", org.jooq.impl.DefaultDataType.getDefaultDataType("\"pg_catalog\".\"inet\""), false, false, new PostgresInetAddressBinding());

		/**
		 * Create a new routine call instance
		 */
		public McuserLogout() {
				super("mcuser_logout", Public.PUBLIC, SQLDataType.BOOLEAN);

				setReturnParameter(RETURN_VALUE);
				addInParameter(MCUSER_UID);
				addInParameter(JWT_ID);
				addInParameter(REQUEST_TIMESTAMP);
				addInParameter(REQUEST_ORIGIN);
		}

		/**
		 * Set the <code>mcuser_uid</code> parameter IN value to the routine
		 */
		public void setMcuserUid(UUID value) {
				setValue(MCUSER_UID, value);
		}

		/**
		 * Set the <code>mcuser_uid</code> parameter to the function to be used with
		 * a {@link org.jooq.Select} statement
		 */
		public void setMcuserUid(Field<UUID> field) {
				setField(MCUSER_UID, field);
		}

		/**
		 * Set the <code>jwt_id</code> parameter IN value to the routine
		 */
		public void setJwtId(UUID value) {
				setValue(JWT_ID, value);
		}

		/**
		 * Set the <code>jwt_id</code> parameter to the function to be used with a
		 * {@link org.jooq.Select} statement
		 */
		public void setJwtId(Field<UUID> field) {
				setField(JWT_ID, field);
		}

		/**
		 * Set the <code>request_timestamp</code> parameter IN value to the routine
		 */
		public void setRequestTimestamp(OffsetDateTime value) {
				setValue(REQUEST_TIMESTAMP, value);
		}

		/**
		 * Set the <code>request_timestamp</code> parameter to the function to be
		 * used with a {@link org.jooq.Select} statement
		 */
		public void setRequestTimestamp(Field<OffsetDateTime> field) {
				setField(REQUEST_TIMESTAMP, field);
		}

		/**
		 * Set the <code>request_origin</code> parameter IN value to the routine
		 */
		public void setRequestOrigin(InetAddress value) {
				setValue(REQUEST_ORIGIN, value);
		}

		/**
		 * Set the <code>request_origin</code> parameter to the function to be used
		 * with a {@link org.jooq.Select} statement
		 */
		public void setRequestOrigin(Field<InetAddress> field) {
				setField(REQUEST_ORIGIN, field);
		}
}
