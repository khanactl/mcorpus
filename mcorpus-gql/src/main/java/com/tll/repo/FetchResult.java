package com.tll.repo;

import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNotNullOrEmpty;
import static com.tll.core.Util.isNullOrEmpty;

/**
 * A simple wrapper object around a 'fetched' object of type <code>T</code>
 * which may be null and an error message which also may be null.
 * <p>
 * {@link #isSuccess()} is determined by:
 * <code>isNullOrEmpty(errorMsg) && (isNotNull(fetched) || successOnNull)</code>.
 *
 * @param <T> the fetched type
 */
public final class FetchResult<T> {

  /**
   * Generate a {@link FetchResult} for a given <code>T</code> instance and error message.
   * <p>
   * {@link #isSuccess()} returns <code>true</code> only when the <code>inst</code> is
   * NON-null and <code>errorMsg</code> IS null.
   *
   * @param <T>
   * @param inst
   * @param errorMsg
   * @return Newly created {@link FetchResult}
   */
  public static <T> FetchResult<T> fetchrslt(T inst, String errorMsg) {
    return new FetchResult<>(inst, errorMsg, false);
  }

  /**
   * Generate a {@link FetchResult} for a given error message.
   * <p>
   * {@link #isSuccess()} will always return false.
   *
   * @param errorMsg the optional error message
   * @return Newly created {@link FetchResult}
   */
  public static <T> FetchResult<T> fetchrslt(String errorMsg) {
    return new FetchResult<>(null, errorMsg, false);
  }

  /**
   * Generate a {@link FetchResult} for a given fetched object.
   * <p>
   * {@link #isSuccess()} will only return <code>true</code> when
   * <code>inst</code> is NON null.
   *
   * @param inst the fetched object ref
   * @return Newly created {@link FetchResult}
   */
  public static <T> FetchResult<T> fetchrslt(T inst) {
    return new FetchResult<>(inst, null, false);
  }

  /**
   * Generate a {@link FetchResult} for a given fetched object
   * which is allowed to be null.
   * <p>
   * {@link #isSuccess()} will return <code>true</code>
   * when either the <code>inst</code> param is NON null -OR-
   * <code>inst</code> param is null and the <code>successOnNull</code>
   * param is <code>false</code>.
   *
   * @param inst the fetched object ref which may be null
   * @param successOnNull consider the fetch result successfull even
   *                      when the fetched object is null?
   * @return Newly created {@link FetchResult}
   */
  public static <T> FetchResult<T> fetchrslt(T inst, boolean successOnNull) {
    return new FetchResult<>(inst, null, successOnNull);
  }

  private final T fetched;
  private final String errorMsg;
  private final boolean successOnNull;

  private FetchResult(final T fetched, final String errorMsg, final boolean successOnNull) {
    this.errorMsg = errorMsg;
    this.fetched = fetched;
    this.successOnNull = successOnNull;
  }

  /**
   * @return true when no error msg is present (it is null or empty)
   *         and either the fetched object is not null
   *         or the fetched object is null and the {@link #successOnNull}
   *         flag is set to true.
   */
  public boolean isSuccess() {
    return isNullOrEmpty(errorMsg) && (isNotNull(fetched) || successOnNull);
  }

  /**
   * @return true when an error message exists.
   */
  public boolean hasErrorMsg() { return isNotNullOrEmpty(errorMsg); }

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

  /**
   * @return the <code>successOnNull</code> flag where if set to true,
   *         {@link #isSuccess()} will return true when no error message is present.
   */
  public boolean isSuccessOnNull() { return successOnNull; }

  @Override
  public String toString() {
    return String.format("fetched: %s, errorMsg: %s",
      isNotNull(fetched) ? "-present-" : "-ABSENT-",
      getErrorMsg()
    );
  }
}
