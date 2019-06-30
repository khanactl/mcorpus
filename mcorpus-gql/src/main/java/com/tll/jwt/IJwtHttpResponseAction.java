package com.tll.jwt;

/**
 * Contract for setting http response state for JWT handling
 * in the web layer.
 * 
 * @author jpk
 */
public interface IJwtHttpResponseAction {

  /**
   * Call on JWT user logout.
   * <p>
   * This method is responsible for clearing out all JWT related state clientside 
   * such that the JWT user must [re] login in oder to obtain a freshly minted JWT.
   */
  void expireJwtCookies();

  /**
   * Set JWT cookie in outgoing http response.
   * <p>
   * This method is expected to be called upon a successful JWT user login by 
   * username and password and sets a freshly minted JWT clientside.
   * 
   * @param jwt the JWT string
   * @param jwtCookieTtlInSeconds jwt cookie time to live in seconds
   */
  void setJwtCookie(String jwt, long jwtCookieTtlInSeconds);
}