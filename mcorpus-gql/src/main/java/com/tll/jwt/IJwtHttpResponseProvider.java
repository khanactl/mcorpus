package com.tll.jwt;

/**
 * Contract for setting http response state for JWT handling.
 * 
 * @author jpk
 */
public interface IJwtHttpResponseProvider {

  /**
   * Call on JWT user logout.
   */
  void expireAllCookies();

  /**
   * Set JWT cookie in outgoing http response.
   * 
   * @param jwt the JWT string
   * @param jwtCookieTtlInSeconds jwt cookie time to live in seconds
   */
  void setJwtCookie(String jwt, long jwtCookieTtlInSeconds);
}