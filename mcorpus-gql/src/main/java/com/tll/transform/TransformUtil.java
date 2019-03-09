package com.tll.transform;

import static com.tll.core.Util.emptyIfNull;
import static com.tll.core.Util.isNull;

import java.sql.Timestamp;
import java.util.Map;

/**
 * G entity tranform utility methods.
 * 
 * @author jpk
 */
public class TransformUtil {

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
   *         -OR- null if null or bad input.
   */
  public static Timestamp dateToTimestamp(final java.util.Date d) { return d == null ? null : new Timestamp(d.getTime()); }

  /**
   * Converts a java.util.Date to a java.sql.Date.
   * 
   * @param o the generalized date input argument assumed to be a java.util.Date instance.
   * @return newly created java.sql.Date instance<br> 
   *         -OR- null if null or bad input.
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
  
  private TransformUtil() {}
}