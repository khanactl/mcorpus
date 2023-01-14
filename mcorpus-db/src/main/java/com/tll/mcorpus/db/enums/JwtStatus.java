/*
 * This file is generated by jOOQ.
 */
package com.tll.mcorpus.db.enums;


import com.tll.mcorpus.db.Public;

import org.jooq.Catalog;
import org.jooq.EnumType;
import org.jooq.Schema;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public enum JwtStatus implements EnumType {

		PRESENT_BAD_STATE("PRESENT_BAD_STATE"),

		NOT_PRESENT("NOT_PRESENT"),

		BLACKLISTED("BLACKLISTED"),

		EXPIRED("EXPIRED"),

		MCUSER_NOTACTIVE("MCUSER_NOTACTIVE"),

		VALID("VALID");

		private final String literal;

		private JwtStatus(String literal) {
				this.literal = literal;
		}

		@Override
		public Catalog getCatalog() {
				return getSchema().getCatalog();
		}

		@Override
		public Schema getSchema() {
				return Public.PUBLIC;
		}

		@Override
		public String getName() {
				return "jwt_status";
		}

		@Override
		public String getLiteral() {
				return literal;
		}

		/**
		 * Lookup a value of this EnumType by its literal
		 */
		public static JwtStatus lookupLiteral(String literal) {
				return EnumType.lookupLiteral(JwtStatus.class, literal);
		}
}
