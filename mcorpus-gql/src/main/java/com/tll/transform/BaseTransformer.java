package com.tll.transform;

import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;

import java.util.Map;

/**
 * Base class for all {@link IGTransformer} implementations.
 * <p>
 * Nullness and emptiness of input arguments are generally handled 
 * in this base class to make implementations easier.
 * 
 * @author jpk
 */
public abstract class BaseTransformer<G, D> implements IGTransformer<G, D> {

  public final G fromGraphQLMap(final Map<String, Object> gqlMap) {
    return isNullOrEmpty(gqlMap) ? null : fromNotEmptyGraphQLMap(gqlMap);
  }

  /**
   * @param gqlMap Never-null and never-empty GraphQL field map
   * @return Newly created frontend GraphQL entity instance
   */
  protected G fromNotEmptyGraphQLMap(final Map<String, Object> gqlMap) {
    throw new UnsupportedOperationException();
  }

  public final G fromGraphQLMapForAdd(final Map<String, Object> gqlMap) {
    return isNullOrEmpty(gqlMap) ? null : fromNotEmptyGraphQLMapForAdd(gqlMap);
  }

  /**
   * @param gqlMap Never-null and never-empty GraphQL field map
   * @return Newly created frontend GraphQL entity instance
   */
  protected G fromNotEmptyGraphQLMapForAdd(final Map<String, Object> gqlMap) {
    throw new UnsupportedOperationException();
  }

  public final G fromGraphQLMapForUpdate(final Map<String, Object> gqlMap) {
    return isNullOrEmpty(gqlMap) ? null : fromNotEmptyGraphQLMapForUpdate(gqlMap);
  }

  /**
   * @param gqlMap Never-null and never-empty GraphQL field map
   * @return Newly created frontend GraphQL entity instance
   */
  protected G fromNotEmptyGraphQLMapForUpdate(final Map<String, Object> gqlMap) {
    throw new UnsupportedOperationException();
  }

  public final D toBackend(final G e) {
    return isNull(e) ? null : toBackendFromNonNull(e);
  }

  /**
   * @param d Never-null frontend entity instance
   * @return Newly created backend domain entity instance
   */
  protected D toBackendFromNonNull(final G e) {
    throw new UnsupportedOperationException();
  }

  public final G fromBackend(final D d) {
    return isNull(d) ? null : fromNonNullBackend(d);
  }

  /**
   * @param d Never-null domain entity instance
   * @return Newly created frontend GraphQL entity instance
   */
  protected G fromNonNullBackend(final D d) {
    throw new UnsupportedOperationException();
  }

}