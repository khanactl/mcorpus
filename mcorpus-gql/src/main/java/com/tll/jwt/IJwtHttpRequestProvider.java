package com.tll.jwt;

import java.net.InetAddress;
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
   * @return the resolved ip address of the sourcing http request.
   */
  InetAddress getRequestOrigin();

  /**
   * @return the JWT value held in the sourcing http request.
   */
  String getJwt();

  /**
   * @return the JWT refresh token held in the sourcing http request.
   */
  String getJwtRefreshToken();

  /**
   * Verify the request origin of the sourcing http request against the
   * extracted JWT audience claim gotten from the JWT held in the same http request.
   * <p>
   * This allows for the request origin definition to be owned and maintained in
   * {@link IJwtHttpRequestProvider} instances rather than in the JWT business/processing object.
   *
   * @param jwtAudience the JWT audience claim extracted from the JWT held in the
   *                    sourcing http request
   * @return true if the given JWT audience claim is verified against the
   *         client origin of the sourcing http request.  False otherwise.
   */
  boolean verifyRequestOrigin(final String jwtAudience);
}