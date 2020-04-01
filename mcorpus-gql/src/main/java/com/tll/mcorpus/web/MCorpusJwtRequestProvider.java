package com.tll.mcorpus.web;

import static com.tll.core.Util.isNotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

import com.tll.jwt.IJwtHttpRequestProvider;
import com.tll.web.RequestSnapshot;

/**
 * Mcorpus specific implementation of {@link IJwtHttpRequestProvider}.
 */
public class MCorpusJwtRequestProvider implements IJwtHttpRequestProvider {

  public static MCorpusJwtRequestProvider fromRequestSnapshot(final RequestSnapshot rs) throws UnknownHostException {
    return new MCorpusJwtRequestProvider(
      rs.getJwtCookie(),
      rs.getRequestInstant(),
      resolveRequestOrigin(rs)
    );
  }

  private static InetAddress resolveRequestOrigin(final RequestSnapshot rs) throws UnknownHostException {
    final String sro;
    // prefer x-forwarded-for http header
    if(isNotNull(rs.getXForwardedFor()))
      sro = rs.getXForwardedFor();
    // 2nd-choice: remote address host
    else if(isNotNull(rs.getRemoteAddressHost()))
      sro = rs.getRemoteAddressHost();
    else
      throw new UnknownHostException();
    return InetAddress.getByName(sro);
  }

  private final String jwt;
  private final Instant requestInstant;
  private final InetAddress requestOrigin;

  private MCorpusJwtRequestProvider(String jwt, Instant requestInstant, InetAddress requestOrigin) {
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
    if(isNotNull(requestOrigin)) {
      return requestOrigin.getHostAddress().equals(jwtAudience);
    }
    // denied
    return false;
  }
}