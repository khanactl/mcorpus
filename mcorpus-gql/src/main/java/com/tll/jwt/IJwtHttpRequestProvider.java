package com.tll.jwt;

import java.time.Instant;

/**
 * Contract for obtaining the status of a possibly absent JWT 
 * for an incoming http request.
 * 
 * @author jpk
 */
public interface IJwtHttpRequestProvider {

  /**
   * @return The ascribed id of the assockated http request.
   */
  String getRequestId();

  /**
   * @return the instant the associated http request reached the server.
   */
  Instant getRequestInstant();
  
  /**
   * @return the client origin token which is a constructed token 
   *         from http header values of an incoming http request.
   *         <p>
   *         Recommended client origin token:<br>
   *         <code>{remoteAddressHost}|{X-Forwarded-For}</code>.
   */
  String getClientOrigin();

  /**
   * @return the JWT value of the incoming http request.
   */
  String getJwt();

  /**
   * Verify the given client origin token against the held client origin in this instance.
   * <p>
   * This allows for the client origin definition to be defined and maintained outside 
   * the scope of JWT processing.
   * 
   * @param clientOrigin the client origin token to validate
   * @return true if the given client origin was successfully verified
   */
  boolean verifyClientOrigin(final String clientOrigin);
}