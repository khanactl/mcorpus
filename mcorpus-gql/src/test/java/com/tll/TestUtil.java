package com.tll;

import static com.tll.transform.TransformUtil.dateToLocalDate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class TestUtil {

	private static final DateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * Class path resource to string.
	 *
	 * @param path the string-wise path to the test resource to load into a string
	 * @return the loaded classpath resource as a UTF-8 string
	 */
	public static String cpr(String path) {
		try {
			Path p = Paths.get(Thread.currentThread().getContextClassLoader().getResource(path).toURI());
			byte[] bytes = Objects.requireNonNull(Files.readAllBytes(p));
			return new String(bytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Return a string of repeated characters: <code><fillChar/code> of length <code>n</code>.
	 *
	 * @param n the number of characters to return
	 * @return String of <code>'c'</code> characters repeated n times.
	 */
	public static String strN(int n) {
		if(n > 0) {
		 final char[] carr = new char[n];
		 Arrays.fill(carr, 'c');
		 return new String(carr);
		}
		return "";
	}

	/**
	 * Convert a string date token of format: "yyyy-MM-dd" to a {@link Date}.
	 *
	 * @param s the date token
	 * @return {@link Date} instance
	 * @throws RuntimeException when the date parsing fails
	 */
	public static Date toDate(final String s) {
		try {
			return dfmt.parse(s);
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Convert a string date token of format: "yyyy-MM-dd" to a {@link Timestamp}.
	 *
	 * @param s the date token
	 * @return {@link Timestamp} instance
	 * @throws RuntimeException when the date parsing fails
	 */
	public static Timestamp toTimestamp(final String s) {
		final Date d = toDate(s);
		return new Timestamp(d.getTime());
	}

	/**
	 * Convert a string date token of format: "yyyy-MM-dd" to a {@link LocalDate}.
	 *
	 * @param s the date token
	 * @return {@link LocalDate} instance
	 * @throws RuntimeException when the date parsing fails
	 */
	public static LocalDate toLocalDate(final String s) {
		return dateToLocalDate(toDate(s));
	}

	private TestUtil() {}
}
