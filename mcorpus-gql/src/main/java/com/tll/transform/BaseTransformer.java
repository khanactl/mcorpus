package com.tll.transform;

import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * Base class for all {@link IGTransformer} implementations.
 * <p>
 * Nullness and emptiness of input arguments are generally handled 
 * in this base class to make implementations easier.
 * 
 * @author jpk
 */
public abstract class BaseTransformer<G, D> implements IGTransformer<G, D> {

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