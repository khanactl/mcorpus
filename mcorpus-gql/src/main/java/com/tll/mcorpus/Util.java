package com.tll.mcorpus;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * General 'project-level' utility methods.
 *
 * Created by jpk on 11/22/17.
 */
public class Util {

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
   * Is the given string blank? (I.e. null or 0-length or contains only whitespace)
   *
   * @param s the string to check
   * @return true if blank, false otherwise.
   */
  public static boolean isBlank(final String s) {
    return isNullOrEmpty(s) || s.trim().length() == 0;
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
   * Convert an arbitrary object to a string, then trim it on both sides.
   *
   * @param o the object to clean and trim
   * @return never-null string
   */
  public static String asStringAndClean(final Object o) { return o == null ? "" : o.toString().trim(); }

  public static String upper(final String s) { return s == null ? "" : s.toUpperCase(); }

  public static String lower(final String s) { return s == null ? "" : s.toLowerCase(); }

  public static String upper(final Object o) { return asStringAndClean(o).toUpperCase(); }

  public static String lower(final Object o) { return asStringAndClean(o).toLowerCase(); }

  /**
   * Takes a list as input and adds each list element to a string
   * along with the given delimiter for every list element.
   *
   * <p>If the list is null or empty, an empty string ("") is returned.</p>
   *
   * @param list the list to express as a single string
   * @param delim the delimiter token to append to each element in the list
   * @return Never-null string
   */
  public static String flatten(final List<String> list, final String delim) {
    if(isNull(list) || list.isEmpty()) return "";
    final StringBuilder sb = new StringBuilder();
    for(final String s : list) sb.append(clean(s)).append(delim);
    return sb.toString();
  }

  /**
   * Check a string for null-ness and return an empty string if it is;
   * otherwise, return the given string.
   *
   * @param s the string to check for null
   * @return the given string when not-null, an empty string when null.
   */
  public static String emptyIfNull(final String s) { return s == null ? "" : s; }

  /**
   * A way to check for null for a given object ref returning a provided default value when it is.
   *
   * @param inst the instance of type &lt;T&gt; for which to check null-ness.
   * @param dflt the value to return when <code>inst</code> is null.
   * @param <T> the object type in play
   * @return <code>inst</code> when it is not null and <code>dflt</code> when <code>inst</code> is null.
   */
  public static <T> T dflt(T inst, T dflt) {
    return inst == null ? dflt : inst;
  }

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
   *
   * @param s the string s
   * @return the filtered string -OR- empty string when given string is null.
   */
  public static String digits(final String s) { return emptyIfNull(s).replaceAll("[\\D]", ""); }

  /**
   * Converts a {@link UUID} to a URL-safe base64-encoded string 24 characters long.
   *
   * @param uuid the uuid
   * @return unique token that is URL safe or null if null input
   */
  public static String uuidToToken(final UUID uuid) {
    if(uuid == null) return null;
    final ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return Base64.getUrlEncoder().encodeToString(bb.array());
  }

  /**
   * Converts either a uuid string (36 chars)
   * -OR- a base64-encoded uuid string (24 chars)
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
      switch (str.length()) {
        case 36:
          // assume raw uuid string
          return UUID.fromString(str);
        case 24:
          // assume base64 url encoded uuid string
          final byte[] bytes = Base64.getUrlDecoder().decode(str);
          final ByteBuffer bb = ByteBuffer.wrap(bytes);
          return new UUID(bb.getLong(), bb.getLong());
      }
    }
    catch(Throwable t) {
      // this function shall not leak info - log nothing
    }
    // default
    return null;
  }
}
