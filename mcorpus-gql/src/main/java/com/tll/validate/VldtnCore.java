package com.tll.validate;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNotNullOrEmpty;
import static com.tll.core.Util.isNullOrEmpty;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * Non-mutating validation primitives for use in validation implementations.
 * 
 * @author jpk
 */
public class VldtnCore {

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
