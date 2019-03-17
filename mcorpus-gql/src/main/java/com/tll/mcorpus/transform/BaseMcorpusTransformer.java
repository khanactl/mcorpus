package com.tll.mcorpus.transform;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

import com.tll.transform.BaseTransformer;

public abstract class BaseMcorpusTransformer<G, D> extends BaseTransformer<G, D> {

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