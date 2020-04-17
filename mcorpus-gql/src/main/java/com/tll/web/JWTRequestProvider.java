package com.tll.web;

import static com.tll.core.Util.isNotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.tll.jwt.IJwtHttpRequestProvider;

/**
 * JWT request provider Ratpack style.
 *
 * @author jpk
 */
public class JWTRequestProvider implements IJwtHttpRequestProvider {

  /**
   * Create a new {@link JWTRequestProvider} instance from a given {@link RequestSnapshot}.
   *
   * @param rs the request snapshot
   * @return Newly created {@link JWTRequestProvider} instance.
   *
   * @throws UnknownHostException when the http request client origin is un-resolvable.
   */
  public static JWTRequestProvider fromRequestSnapshot(final RequestSnapshot rs) throws UnknownHostException {
    return new JWTRequestProvider(
      rs.getJwtCookie(),
      rs.getRequestInstant().truncatedTo(ChronoUnit.SECONDS), // so that conversion to/from Date are equal!
      resolveRequestOrigin(rs)
    );
  }

  private static InetAddress resolveRequestOrigin(final RequestSnapshot rs) throws UnknownHostException {
    final String sro;
    // primary: x-forwarded-for http header
    if(isNotNull(rs.getXForwardedForClientIp()))
      sro = rs.getXForwardedForClientIp();
    // fallback: remote address host
    else if(isNotNull(rs.getRemoteAddressHost()))
      sro = rs.getRemoteAddressHost();
    else
      throw new UnknownHostException();
    return InetAddress.getByName(sro);
  }

  private final String jwt;
  private final Instant requestInstant;
  private final InetAddress requestOrigin;

  private JWTRequestProvider(String jwt, Instant requestInstant, InetAddress requestOrigin) {
    this.jwt = jwt;
    this.requestInstant = requestInstant;
    this.requestOrigin = requestOrigin;
  }

  @Override
  public InetAddress getRequestOrigin() {
    return requestOrigin;
  }

  @Override
  public String getJwt() {
    return jwt;
  }

  @Override
  public Instant getRequestInstant() {
    return requestInstant;
  }

  @Override
  public boolean verifyRequestOrigin(final String jwtAudience) {
    return requestOrigin.getHostAddress().equals(jwtAudience);
  }
}