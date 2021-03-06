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
   * @return the instant the associated http request reached the server.
   */
  Instant getRequestInstant();

  /**
   * @return the client origin of the sourcing http request.
   */
  String getClientOrigin();

  /**
   * @return the JWT value held in the sourcing http request.
   */
  String getJwt();

  /**
   * Verify the client origin of the sourcing http request against the
   * extracted JWT audience claim gotten from the JWT held in the same http request.
   * <p>
   * This allows for the client origin definition to be owned and maintained in
   * {@link IJwtHttpRequestProvider} instances rather than in the JWT business/processing object.
   *
   * @param jwtAudience the JWT audience claim extracted from the JWT held in the
   *                    sourcing http request
   * @return true if the given JWT audience claim is verified against the
   *         client origin of the sourcing http request.  False otherwise.
   */
  boolean verifyClientOrigin(final String jwtAudience);
}