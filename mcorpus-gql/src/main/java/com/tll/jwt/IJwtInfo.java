package com.tll.jwt;

import java.util.Date;
import java.util.UUID;

/**
 * Contract for conveying summary info for a single JWT
 * known about in the backend.
 *
 * @author jpk
 */
public interface IJwtInfo {
  /**
   * @return The unique id of the JWT.
   */
  UUID getJwtId();

  /**
   * @return timestamp of when the JWT was created.
   */
  Date created();

  /**
   * @return timestamp of when the JWT will expire.
   */
  Date expires();

  /**
   * @return The resolved ip address of the http request that generated the JWT.
   */
  String clientOrigin();
}