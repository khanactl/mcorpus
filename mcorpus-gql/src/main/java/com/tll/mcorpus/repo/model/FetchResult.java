package com.tll.mcorpus.repo.model;

import com.google.common.base.MoreObjects;

/**
 * A simple wrapper object around a 'fetched' object of type &lt;T&gt;
 * which may be null and an error message string which also may be null.
 *
 * When the fetched object is null, an error message *should* be present.<br>
 * When the fetched object is NOT null, *NO* error message is expected.
 * <p>
 *   In other words:
 *    {@link #isSuccess()} is true when:
 *      {@link #get()} returns a non-null object -and-
 *      {@link #hasErrorMsg()} returns false.
 * </p>
 *
 * @param <T> the fetched entity type
 */
public final class FetchResult<T> {

  private final T fetched;
  private final String errorMsg;

  /**
   * Constructor.
   *
   * @param fetched the fetched object
   * @param errorMsg the error message
   */
  public FetchResult(final T fetched, final String errorMsg) {
    this.errorMsg = errorMsg;
    this.fetched = fetched;
  }

  /**
   * @return true when no error msg and a non null fetched object, false otherwise.
   */
  public boolean isSuccess() { return errorMsg == null && fetched != null; }

  /**
   * @return true when an error message exists.
   */
  public boolean hasErrorMsg() { return errorMsg != null; }

  /**
   * @return the error message.
   */
  public String getErrorMsg() { return errorMsg; }

  /**
   * Get the fetched object of type <T>.
   *
   * @return the fetched object.
   */
  public T get() {
    return fetched;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("errorMsg", errorMsg)
      .add("fetched", fetched)
      .toString();
  }
}
