package com.tll.transform;

import static com.tll.core.Util.emptyIfNull;
import static com.tll.core.Util.isNull;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * G entity tranform utility methods.
 *
 * @author jpk
 */
public class TransformUtil {

	protected static final ZoneId localZoneId = ZoneId.systemDefault();

	protected static final ZoneOffset localToUtc = localZoneId.getRules().getOffset(Instant.MIN);

	/**
	 * RegEx filter that removes all NON-alphanumeric characters.
	 *
	 * @param s the string s
	 * @return the filtered string -OR- empty string when given string is null.
	 */
	public static String alphnum(final String s) { return emptyIfNull(s).replaceAll("[^a-zA-Z\\d]", ""); }

	/**
	 * RegEx filter that removes all NON-alpha characters.
	 *
	 * @param s the string s
	 * @return the filtered string -OR- empty string when given string is null.
	 */
	public static String alpha(final String s) { return emptyIfNull(s).replaceAll("[^a-zA-Z]", ""); }

	/**
	 * RegEx filter that removes all NON-digit characters.
	 * <p>
	 * Null input begets null output.
	 *
	 * @param s the string s
	 * @return the filtered string -OR- null when the given string is null.
	 */
	public static String digits(final String s) { return isNull(s) ? null : s.replaceAll("[\\D]", ""); }

	/**
	 * Converts a java.util.Date to a java.sql.Timestamp.
	 *
	 * @param d the java.util.Date object to convert
	 * @return newly created java.sql.Date instance<br>
	 *				 -OR- null if null or bad input.
	 */
	public static Timestamp dateToTimestamp(final java.util.Date d) { return d == null ? null : new Timestamp(d.getTime()); }

	/**
	 * Converts a java.util.Date to a java.sql.Date.
	 *
	 * @param o the generalized date input argument assumed to be a java.util.Date instance.
	 * @return newly created java.sql.Date instance<br>
	 *				 -OR- null if null or bad input.
	 */
	public static java.sql.Date asSqlDate(final java.util.Date d) {
		return d == null ? null : new java.sql.Date(d.getTime());
	}

	/**
	 * Get the field value from a field name and value map.
	 *
	 * @param fname the field name whose value is to be fetched
	 * @param fmap the field name and value map
	 * @param <T> the field type
	 * @return the field value gotten from the field map
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fval(final String fname, final Map<String, Object> fmap) { return (T) fmap.get(fname); }

	/**
	 * Convert an {@link OffsetDateTime} to a {@link Date}.
	 *
	 * @param odt the offset date time object to convert
	 * @return Newly created {@link Date} -OR- null if the input param is null
	 */
	public static Date odtToDate(final OffsetDateTime odt) {
		return odt == null ? null : Date.from(odt.toInstant());
	}

	/**
	 * Convert a {@link Date} to an {@link OffsetDateTime}.
	 *
	 * @param d the date to convert
	 * @return Newly created {@link OffsetDateTime} -OR- null if the input param is null
	 */
	public static OffsetDateTime odtFromDate(final Date d) {
		return d == null ? null : d.toInstant().atOffset(localToUtc);
	}

	/**
	 * Convert a {@link LocalDate} to a {@link Date}.
	 *
	 * @param ld the local date to convert
	 * @return {@link Date} -OR- null if input param is null
	 */
	public static Date localDateToDate(final LocalDate ld) {
		return ld == null ? null : Date.from(ld.atStartOfDay(localZoneId).toInstant());
	}

	/**
	 * Convert a {@link Date} to a {@link LocalDate}.
	 *
	 * @param d the {@link Date} to convert
	 * @return {@link LocalDate} -OR- null if input param is null
	 */
	public static LocalDate dateToLocalDate(final Date d) {
		return d == null ? null : d.toInstant().atZone(localZoneId).toLocalDate();
	}

	/**
	 * Converts a {@link UUID} to a URL-safe base64-encoded string of either
	 * 22 characters in length when it ends with "==" -OR- 24 chars in length
	 * when it does NOT end in "==".
	 *
	 * @param uuid the uuid
	 * @return unique token that is URL safe or null if null input
	 */
	public static String uuidToToken(final UUID uuid) {
		if(uuid == null) return null;
		final ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		String tok = Base64.getUrlEncoder().encodeToString(bb.array());
		return tok.endsWith("==") ? tok.substring(0, tok.length() - 2) : tok;
	}

	/**
	 * Converts either a 'raw' uuid string (36 chars)
	 * -OR- a base64-encoded uuid string (24 chars)
	 * -OR- a base64-encoded-and-trimmed (ending "==" stripped) uuid string (22 chars)
	 * to a UUID object.
	 *
	 * <p>No exceptions are thrown and null is always
	 * returned upon missing (null) or bad input.</p>
	 *
	 * <p>Nothing is logged on conversion failure
	 * rather only null is returned.</p>
	 *
	 * @param str the base64-encoded token uuid
	 * @return the matching {@link UUID} or null if null or invalid uuid token
	 */
	public static UUID uuidFromToken(final String str) {
		if(str == null) return null;
		try {
			final ByteBuffer bb;
			switch (str.length()) {
				case 36:
					// assume raw uuid string
					return UUID.fromString(str);
				case 24:
					// assume base64 url encoded uuid string WITH trailing "=="
					bb = ByteBuffer.wrap(Base64.getUrlDecoder().decode(str));
					return new UUID(bb.getLong(), bb.getLong());
				case 22:
					// assume base64 url encoded uuid string WITHOUT trailing "=="
					bb = ByteBuffer.wrap(Base64.getUrlDecoder().decode(str + "=="));
					return new UUID(bb.getLong(), bb.getLong());
			}
		}
		catch(Throwable t) {
			// this function shall not leak info - log nothing
		}
		// default
		return null;
	}

	private TransformUtil() {}
}