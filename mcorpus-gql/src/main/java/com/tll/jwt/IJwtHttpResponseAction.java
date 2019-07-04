package com.tll.jwt;

import java.time.Duration;

/**
 * Contract for setting <b>http response state</b> for JWT handling
 * in the web layer.
 * 
 * @author jpk
 */
public interface IJwtHttpResponseAction {

  /**
   * Set JWT http clientside state in outgoing http response.
   * <p>
   * Call on JWT user login.
   * <p>
   * This method is expected to be called upon a successful JWT user login by 
   * username and password and sets a freshly minted JWT clientside.
   * 
   * @param jwt the JWT string to set on the client browser
   * @param jwtTimeToLive the amount of time the JWT shall be considered valid on the client
   */
  void setJwtClientside(String jwt, Duration jwtTimeToLive);
  
  /**
   * Clears all JWT related http state clientside.
   * <p>
   * Call on JWT user logout.
   * <p>
   * This method is responsible for clearing out all JWT related state clientside 
   * such that the JWT user must [re] login in order to obtain a freshly minted JWT.
   */
  void expireJwtClientside();
}