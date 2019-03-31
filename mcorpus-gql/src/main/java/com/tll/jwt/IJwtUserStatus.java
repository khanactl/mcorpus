package com.tll.jwt;

import java.util.Date;
import java.util.UUID;

/**
 * Contract for conveying the status of a logged in JWT user.
 * 
 * @author jpk
 */
public interface IJwtUserStatus {

  /**
   * @return the id of the user bound to a target JWT.
   */
  UUID getJwtUserId();

  /**
   * @return the date of when the JWT was issued.
   */
  Date getSince();

  /**
   * @return when the JWT expires.
   */
  Date getExpires();

  /**
   * @return the number of active JWTs bound to the logged in JWT user.
   */
  int getNumActiveJWTs();
}