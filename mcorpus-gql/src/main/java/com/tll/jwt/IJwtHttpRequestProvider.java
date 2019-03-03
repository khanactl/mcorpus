package com.tll.jwt;

import java.time.Instant;

/**
 * Contract for obtaining needed JWT info held in an incoming http request.
 * 
 * @author jpk
 */
public interface IJwtHttpRequestProvider {

  /**
   * @return the instant the associated http request reached the server.
   */
  Instant getRequestInstant();
  
  /**
   * @return the client origin token which is a constructed token 
   *         from http hader values.
   *         <p>
   *         The reccommended client origin format:<br>
   *         <code>{remoteAddressHost}|{X-Forwarded-For}</code>.
   */
  String getClientOrigin();

  /**
   * @return the JWT cookie value of the incoming http request.
   */
  String getJwtCookie();

  /**
   * Verify the given client origin token against the held client origin in this instance.
   * <p>
   * This allows for the client origin definition to be defined and maintained outside 
   * the scope of JWT processing.
   * 
   * @param clientOrigin the client origin token to validate
   * @return true if the given client origin matches the one held.
   */
  boolean verifyClientOrigin(final String clientOrigin);
}