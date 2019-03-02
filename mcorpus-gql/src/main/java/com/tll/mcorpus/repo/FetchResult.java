package com.tll.mcorpus.repo;

import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.not;

/**
 * A simple wrapper object around a 'fetched' object of type &lt;T&gt;
 * which may be null and an error message string which also may be null.
 * <p>
 *    {@link #isSuccess()} is true when:
 *      {@link #get()} returns a non-null object -and-
 *      {@link #hasErrorMsg()} returns false
 * </p>
 *
 * @param <T> the fetched type
 */
public final class FetchResult<T> {

  private final T fetched;
  private final String errorMsg;

  /**
   * Constructor - No error msgs.
   *
   * @param fetched the fetched object
   */
  public FetchResult(final T fetched) {
    this(fetched, null);
  }

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
   * @return true when no error msg, no validation errors and a non null fetched object, false otherwise.
   */
  public boolean isSuccess() { return isNullOrEmpty(errorMsg) && isNotNull(fetched); }

  /**
   * @return true when an error message exists.
   */
  public boolean hasErrorMsg() { return not(isNullOrEmpty(errorMsg)); }

  /**
   * @return the error message.
   */
  public String getErrorMsg() { return errorMsg; }

  /**
   * Get the fetched object of type <T>.
   *
   * @return the fetched object.
   */
  public T get() { return fetched; }

  @Override
  public String toString() {
    return String.format("fetched: %s, errorMsg: %s", 
      isNotNull(fetched) ? "-present-" : "-ABSENT-",
      getErrorMsg()
    );
  }
}
