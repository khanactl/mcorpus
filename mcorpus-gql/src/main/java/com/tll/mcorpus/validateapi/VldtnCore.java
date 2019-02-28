package com.tll.mcorpus.validateapi;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Non-mutating validation primitives for use in entity validation.
 * 
 * @author jpk
 */
public class VldtnCore {

    /**
   * Is the given object ref null?
   *
   * @param o the object ref
   * @return true when null
   */
  public static boolean isNull(final Object o) { return o == null; }

  /**
   * Is the given object ref NOT null?
   *
   * @param o the object ref
   * @return true when NOT null
   */
  public static boolean isNotNull(final Object o) { return o != null; }

  /**
   * Is the given string null or 0-length ("")?
   *
   * @param s the string to check
   * @return true if the given string is empty, false otherwise.
   */
  public static boolean isNullOrEmpty(final String s) { return s == null || s.isEmpty(); }
  
  /**
   * Is the given string non-null and not-empty (greater than zero-length)?
   *
   * @param s the string to check
   * @return true if the given string is no-null and not empty, false otherwise.
   */
  public static boolean isNotNullOrEmpty(final String s) { return s != null && !s.isEmpty(); }
  
  /**
   * Is the given array null or 0-length?
   *
   * @param a the array to check
   * @return true if the given array null 0-length, false otherwise.
   */
  public static boolean isNullOrEmpty(final Object[] a) { return a == null || a.length == 0; }
  
  /**
   * Is the given collection null or empty?
   * 
   * @param clc the collection to check
   * @return true if the given collection is null or empty, false otherwise
   */
  public static boolean isNullOrEmpty(final Collection<?> clc) { return clc == null || clc.isEmpty(); }

  /**
   * Is the given map null or empty?
   * 
   * @param clc the map to check
   * @return true if the given map is null or empty, false otherwise
   */
  public static boolean isNullOrEmpty(final Map<?, ?> map) { return map == null || map.isEmpty(); }

  /**
   * Is the given string blank? (I.e. null or 0-length or contains only whitespace)
   *
   * @param s the string to check
   * @return true if blank, false otherwise.
   */
  public static boolean isBlank(final String s) {
    return isNullOrEmpty(s) || s.trim().length() == 0;
  }

  /**
   * Is the given string not blank?
   *
   * @param s the string to check
   * @return true if not blank, false otherwise.
   */
  public static boolean isNotBlank(final String s) {
    return isNotNullOrEmpty(s) && s.trim().length() > 0;
  }

  /**
   * Alternative to the <code>!</code> java lang operator.
   *
   * @param b boolean arg
   * @return not <code>b</code>
   */
  public static boolean not(boolean b) { return !b; }

  /**
   * Verfiy the given string's length does not exceed the given max length.
   *
   * @param s the string to check
   * @param maxLen the max allowed length of the string
   * @return true if string <code>s</code> is less than or equal to the given max length<br>
   *          -OR- false when the given string is null or when its length is greater than
   *          than the given <code>maxLen</code>.
   */
  public static boolean lenchk(final String s, int maxLen) { return s != null && s.length() <= maxLen; }

  /**
   * Checks that a given string's length is within a certain range (bounds).
   * <p>If param <code>s</code> is empty, <code>false</code> is returned</p>
   * <p>If param <code>minLen</code> or <code>maxLen</code> is less than 1, <code>false</code> is returned</p>
   * <p>If param <code>minLen</code> is greater than or equal to <code>maxLen</code>, <code>false</code> is returned</p>
   *
   * @param s the string to check
   * @param minLen the minimum allowed length of the string
   * @param maxLen the maximum allowed length of the string
   * @return true if the bounds check passes, false when fails
   */
  public static boolean boundschk(final String s, int minLen, int maxLen) {
    if(isNullOrEmpty(s) || minLen < 1 || maxLen < 1 || minLen >= maxLen) return false;
    final int len = s.length();
    return len >= minLen && len <= maxLen;
  }

  /**
   * Convert a possibly absent (null) object to a string where null begets an empty string.
   *
   * @param o the object to express as a string
   * @return the string-wise object -OR- an empty string ("") when the given object is null.
   */
  public static String asString(final Object o) { return o == null ? "" : o.toString(); }

  /**
   * Get the length of a possibly null string.
   *
   * @param s the string
   * @return the string's length which is 0 for both null and empty length strings.
   */
  public static int strlen(final String s) { return s == null ? 0 : s.length(); }

  /**
   * Clean a string: if it is blank (null or whitespace-only), an empty string ("") is returned.
   * Otherwise, the string is trimmed on both sides and returned.
   *
   * @param s the string to clean
   * @return Never-null, trimmed string
   */
  public static String clean(final String s) { return s == null ? "" : s.trim(); }

  /**
   * Clean a string when NOT NULL.
   * If it is null, null is returned.
   * Otherwise, the string is trimmed on both sides and returned.
   *
   * @param s the string to nclean
   * @return Null when the input is null or the trimmed string
   */
  public static String nclean(final String s) { return s == null ? null : s.trim(); }

  /**
   * Convert an arbitrary object to a string, then trim it on both sides.
   *
   * @param o the object to clean and trim
   * @return never-null string
   */
  public static String asStringAndClean(final Object o) { return o == null ? "" : o.toString().trim(); }

  public static String upper(final String s) { return s == null ? "" : s.toUpperCase(Locale.getDefault()); }

  public static String lower(final String s) { return s == null ? "" : s.toLowerCase(Locale.getDefault()); }

  public static String upper(final Object o) { return upper(asStringAndClean(o)); }

  public static String lower(final Object o) { return lower(asStringAndClean(o)); }
  
  /**
   * namePattern: RegEx to apply to a 'name'.<br>
   *              Allowed chars: A-Z, a-z, comma(,), dash(-), period(.), space( ), apostrophe(')
   */
  public static final Pattern namePattern = Pattern.compile("^[a-zA-Z ,.'-]+$");

  /**
   * ssnPattern: Enforcement of one of two formats:
   *              1. 000-00-0000
   *              2. 000000000
   */
  public static final Pattern ssnPattern = Pattern.compile("^(\\d{3}-\\d{2}-\\d{4}|\\d{9})$");

  /**
   * emailPattern: RegEx for validating email addresses.  "Works 99% of the time."
   *
   * - min 2 chars long
   * - max 128 chars long
   *
   */
  public static final Pattern emailPattern = Pattern.compile("(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,128}$");

  /**
   * phonePattern: Allowed chars: digits(0-9), dash(-), dot(.), space( ), open-paren( '(' ), close-paren( ')' )
   */
  public static final Pattern phonePattern = Pattern.compile("^[\\d\\-. \\(\\)]+$");

  /**
   * usernamePattern: Allowed chars: alpha-numeric upper and lower.<br>
   *                  min length: 4 chars, max length: 26 chars
   */
  public static final Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9]{4,26}$");

  /**
   * pswdPattern: any non-whitespace character.<br>
   *              min length: 8 chars, max length: 50 chars
   */
  public static final Pattern pswdPattern = Pattern.compile("^\\S{8,50}$");

  /**
   * Verify a birth date satisfies:
   * - not null
   * - not greater than now
   *
   * @param dob any birth date
   * @return true when valid
   */
  public static boolean dobValid(final Date dob) {
    return isNotNull(dob) && dob.getTime() < System.currentTimeMillis();
  }

  /**
   * Verify an SSN adheres to format: <code>000-00-0000</code>.
   *
   * - required
   *
   * @param s the SSN
   * @return true when valid
   */
  public static boolean ssnValid(final String s) {
    return isNotNullOrEmpty(s) && ssnPattern.matcher(clean(s)).matches();
  }

  /**
   * Verify an email address is valid.
   *
   * @param s any email address
   * @return true when the given email address string is valid
   */
  public static boolean emailValid(final String s) {
    return isNotNullOrEmpty(s) && emailPattern.matcher(clean(s)).matches();
  }

  /**
   * Verify a phone number has only digits, spaces, dots or dashes.
   *
   * @param s any phone number
   * @return true when the given phone number string is valid
   */
  public static boolean phoneValid(final String s) {
    return isNotNullOrEmpty(s) && phonePattern.matcher(clean(s)).matches();
  }

  /**
   * Verify a member username satisfies:
   * - min 4 chars
   * - max 26 chars
   *
   * @param s the member username
   * @return true when valid
   */
  public static boolean usernameValid(final String s) {
    return isNotNullOrEmpty(s) && usernamePattern.matcher(clean(s)).matches();
  }

  /**
   * Verify a member password satisfies:
   *  - min 8 chars
   *  - max 50 chars
   *  - any non whitespace char
   *
   * @param s member password
   * @return true when valid
   */
  public static boolean pswdValid(final String s) {
    return isNotNullOrEmpty(s) && pswdPattern.matcher(clean(s)).matches();
  }

  private VldtnCore() {}
}
