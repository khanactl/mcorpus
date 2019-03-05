package com.tll.core;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Core (language-level), project-agnostic static utility methods.
 * <p>
 * Created on 11/22/17.
 * 
 * @author jpk
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
   * Is the given array non-null and has at least one element?
   *
   * @param a the array to check
   * @return true if the given array null 0-length, false otherwise.
   */
  public static boolean isNotNullOrEmpty(final Object[] a) { return a != null && a.length > 0; }
  
  /**
   * Is the given collection null or empty?
   * 
   * @param clc the collection to check
   * @return true if the given collection is null or empty, false otherwise
   */
  public static boolean isNullOrEmpty(final Collection<?> clc) { return clc == null || clc.isEmpty(); }

  /**
   * Is the given collection non-null and not empty?
   * 
   * @param clc the collection to check
   * @return true if the given collection is non-null and not empty, false otherwise
   */
  public static boolean isNotNullOrEmpty(final Collection<?> clc) { return clc != null && !clc.isEmpty(); }

  /**
   * Is the given map null or empty?
   * 
   * @param clc the map to check
   * @return true if the given map is null or empty, false otherwise
   */
  public static boolean isNullOrEmpty(final Map<?, ?> map) { return map == null || map.isEmpty(); }

  /**
   * Is the given map non-null and not empty?
   * 
   * @param clc the map to check
   * @return true if the given map is non-null and not empty, false otherwise
   */
  public static boolean isNotNullOrEmpty(final Map<?, ?> map) { return map != null && !map.isEmpty(); }
  
  /**
   * Is the given string blank? (I.e. null or 0-length or contains only whitespace)
   *
   * @param s the string to check
   * @return true if blank, false otherwise.
   */
  public static boolean isBlank(final String s) { return isNullOrEmpty(s) || s.trim().length() == 0; }

  /**
   * Is the given string not blank?
   *
   * @param s the string to check
   * @return true if NOT blank, false otherwise.
   */
  public static boolean isNotBlank(final String s) { return !isNullOrEmpty(s) && s.trim().length() > 0; }

  /**
   * Alternative to the <code>!</code> java lang operator.
   *
   * @param b boolean arg
   * @return not <code>b</code>
   */
  public static boolean not(boolean b) { return !b; }

  /**
   * Get the length of a possibly null string.
   *
   * @param s the string
   * @return the string's length which is 0 for both null and empty length strings.
   */
  public static int strlen(final String s) { return s == null ? 0 : s.length(); }

  /**
   * Check a string for null-ness and return an empty string if it is;
   * otherwise, return the given string.
   *
   * @param s the string to check for null
   * @return the given string when not-null, an empty string when null.
   */
  public static String emptyIfNull(final String s) { return s == null ? "" : s; }

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
   * Clean a string when NOT EMPTY.
   * If it is null or empty, null is returned.
   * Otherwise, the string is trimmed on both sides and returned.
   *
   * @param s the string to nclean
   * @return Null when the input is null or empty otherwise the trimmed string
   */
  public static String neclean(final String s) { return isNullOrEmpty(s) ? null : s.trim(); }

  /**
   * Convert a possibly absent (null) object to a string where null begets an empty string.
   *
   * @param o the object to express as a string
   * @return the string-wise object -OR- an empty string ("") when the given object is null.
   */
  public static String asString(final Object o) { return o == null ? "" : o.toString(); }

  /**
   * Convert an arbitrary object to a string, then trim it on both sides.
   *
   * @param o the object to clean and trim
   * @return never-null string
   */
  public static String asStringAndClean(final Object o) { return o == null ? "" : o.toString().trim(); }

  public static String upper(final String s) { return s == null ? null : s.toUpperCase(Locale.getDefault()); }

  public static String lower(final String s) { return s == null ? null : s.toLowerCase(Locale.getDefault()); }

  /**
   * A way to check for null for a given object ref returning a provided default value when it is.
   *
   * @param inst the instance of type &lt;T&gt; for which to check null-ness.
   * @param dflt the value to return when <code>inst</code> is null.
   * @param <T> the object type in play
   * @return <code>inst</code> when it is not null and <code>dflt</code> when <code>inst</code> is null.
   */
  public static <T> T dflt(T inst, T dflt) { return inst == null ? dflt : inst; }

  /**
   * Physical copy for <code>java.util.Date</code> objects.
   * 
   * @return Newly created {@link Date} with the same time as <code>d</code> 
   *         or null if <code>d</code> is null.
   */
  public static Date copy(final Date d) { return d == null ? null : new Date(d.getTime()); }
  
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
   * Takes a list as input and adds each list element to a string
   * along with the given delimiter for every list element.
   *
   * <p>If the list is null or empty, <code>null</code> is returned.</p>
   *
   * @param list the list to express as a single string
   * @param delim the delimiter token to append to each element in the list
   * @return Possibly null string
   */
  public static String nflatten(final List<String> list, final String delim) {
    return isNullOrEmpty(list) ? null : flatten(list, delim);
  }

  /**
   * Static access only please.
   */
  private Util() {}
}
