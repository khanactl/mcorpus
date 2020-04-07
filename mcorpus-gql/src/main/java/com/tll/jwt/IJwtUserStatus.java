package com.tll.jwt;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Contract for conveying the status of a logged in JWT user.
 *
 * @author jpk
 */
public interface IJwtUserStatus {

  /**
   * @return the actve JWT id.
   */
  UUID getJwtId();

  /**
   * @return the id of the user bound to a target JWT.
   */
  UUID getJwtUserId();

  /**
   * @return the date of when the JWT was issued.
   */
  Date getSince();

  /**
   * @return the JWT expires claim.
   */
  Date getExpires();

  /**
   * @return the JWT roles claim.
   */
  String getRoles();

  /**
   * @return the current active JWT details bound to the logged in JWT user.
   */
  List<IJwtInfo> getActiveJWTs();
}