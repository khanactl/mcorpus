package com.tll.repo;

import org.jooq.Field;

import java.util.Map;

import static com.tll.core.Util.isBlank;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.not;

/**
 * General repository-level entity persistence utility methods.
 * 
 * @author jpk
 */
public class RepoUtil {

  /**
   * Given a {@link Field} ref, return the field name.
   *
   * @param f the field
   * @return the field name
   */
  public static String fname(final Field<?> f) { return f.getName(); }

  /**
   * Get the field value from a field name and value map.
   *
   * @param f the field whose value is to be fetched
   * @param fmap the field name and value map
   * @param <T> the field type
   * @return the field value gotten from the field map
   */
  public static <T> T fval(final Field<T> f, final Map<String, Object> fmap) {
    return fval(f.getName(), fmap);
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
  public static <T> T fval(final String fname, final Map<String, Object> fmap) {
    return (T) fmap.get(fname);
  }

  /**
   * Does the given field map contain a key for the given field ref?
   *
   * @param f the field ref
   * @param fmap the field map
   * @param <T> the field type
   * @return true when the <code>fmap</code> contains the field name key, false otherwise.
   */
  public static <T> boolean hasField(final Field<T> f, final Map<String, Object> fmap) {
    return fmap.containsKey(f.getName());
  }

  /**
   * Does the given field map NOT contain a key for the given field ref?
   *
   * @param f the field ref
   * @param fmap the field map
   * @param <T> the field type
   * @return true when the <code>fmap</code> does NOT contain the field name key,
   *         false when the field map does.
   */
  public static <T> boolean notHasField(final Field<T> f, final Map<String, Object> fmap) {
    return !fmap.containsKey(f.getName());
  }

  /**
   * Is the given field either not in the field map or null in the field map?
   *
   * @param f the field ref
   * @param fmap the field map
   * @param <T> the field type
   * @return true when the <code>fmap</code> either DOES NOT CONTAIN the field
   *          -OR- when it does contain the field but the field value is <code>null</code>,
   *         false otherwise.
   */
  public static <T> boolean fmissing(final Field<T> f, final Map<String, Object> fmap) {
    return notHasField(f, fmap) || fval(f, fmap) == null;
  }

  /**
   * Add a field value to the given field map.
   *
   * @param f the field ref
   * @param fval the field value
   * @param fmap the field map to which the field name and field value are added
   */
  public static <T> void fput(final Field<T> f, final T fval, final Map<String, Object> fmap) {
    fmap.put(f.getName(), fval);
  }

  /**
   * Add a field value to the given field map providing the given field value is NOT null.
   *
   * @param f the field ref
   * @param fval the field value
   * @param fmap the field map to which the field name and field value are added
   */
  public static <T> void fputWhenNotNull(final Field<T> f, final T fval, final Map<String, Object> fmap) {
    if(fval != null) fmap.put(f.getName(), fval);
  }

  /**
   * Add a field value to the given field map providing the given field value is NOT blank.
   *
   * @param f the String field ref
   * @param fval the field value string
   * @param fmap the field map to which the field name and field value are added
   */
  public static void fputWhenNotBlank(final Field<String> f, final String fval, final Map<String, Object> fmap) {
    if(not(isBlank(fval))) fmap.put(f.getName(), fval);
  }

  /**
   * Add a field value to the given field map providing the given field value is NOT null.
   *
   * @param f the field ref
   * @param fval the field value
   * @param fmap the field map to which the field name and field value are added
   */
  public static <T> void fputWhenNotEmpty(final Field<T[]> f, final T[] fval, final Map<String, Object> fmap) {
    if(not(isNullOrEmpty(fval))) fmap.put(f.getName(), fval);
  }

  /**
   * Static access only please.
   */
  private RepoUtil() {}
}
