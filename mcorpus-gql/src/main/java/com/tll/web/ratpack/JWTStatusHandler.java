package com.tll.web.ratpack;

import static com.tll.core.Util.isNotNull;
import static com.tll.web.ratpack.Cookie.expireCookie;
import static com.tll.web.ratpack.Cookie.setCookie;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.tll.jwt.IJwtHttpRequestProvider;
import com.tll.jwt.IJwtHttpResponseAction;
import com.tll.jwt.JWT;
import com.tll.jwt.JWTHttpRequestStatus;
import com.tll.web.RequestSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.exec.Blocking;
import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Determine the JWT status for incoming http requests and cache this status in
 * the request for downstrem handlers to access.
 * <p>
 * Specifically, this handler does three things in the following order:
 * <ol>
 * <li>Generate an {@link IJwtHttpRequestProvider} instance in the Ratpack
 * request object.
 * <li>Generate an {@link IJwtHttpResponseAction} instance in the Ratpack
 * request object.
 * <li>Cache a {@link JWTHttpRequestStatus} in the Ratpack request object.
 * <p>
 * <b>IMPT</b>: This task will potentially query the backend for the backend jwt status and
 * consequently, this task blocks.
 * </ol>
 * <p>
 * A {@link RequestSnapshot} instance is created as a pre-requisite in
 * determining the JWT status if one is not alredy cached in the request.
 *
 * @author jkirton
 */
public class JWTStatusHandler implements Handler {

  static class JwtHttpRequestProviderImpl implements IJwtHttpRequestProvider {

    private final String jwt;
    private final String refreshToken;
    private final Instant requestInstant;
    private final InetAddress requestOrigin;

    public JwtHttpRequestProviderImpl(String jwt, String refreshToken, Instant requestInstant, InetAddress requestOrigin) {
      this.jwt = jwt;
      this.refreshToken = refreshToken;
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
    public String getJwtRefreshToken() {
      return refreshToken;
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

  static class JwtHttpResponseActionImpl implements IJwtHttpResponseAction {

    private final boolean cookieSecure;
    private final String cookieDomain;
    private final String jwtRefreshTokenName;
    private final Context ctx;

    public JwtHttpResponseActionImpl(final boolean cookieSecure, final String cookieDomain, final String jwtRefreshTokenName, final Context ctx) {
      this.cookieSecure = cookieSecure;
      this.cookieDomain = cookieDomain;
      this.jwtRefreshTokenName = jwtRefreshTokenName;
      this.ctx = ctx;
    }

    @Override
    public void expireJwtClientside() {
      ctx.getResponse().getHeaders().set("Authorization", "");
      expireCookie(ctx, cookieDomain, jwtRefreshTokenName, "/", cookieSecure);
    }

    @Override
    public void setJwtClientside(String jwt, String refreshToken, Duration refreshTokenTimeToLive) {
      ctx.getResponse().getHeaders().set("Authorization", "Bearer " + jwt);
      setCookie(ctx, cookieDomain, jwtRefreshTokenName, refreshToken, "/", refreshTokenTimeToLive.getSeconds(), cookieSecure);
    }
  }

  /**
   * Resolve the incoming http request's originating IP address by interrogating
   * the http request headers.
   */
  static InetAddress resolveRequestOrigin(final RequestSnapshot rs) throws UnknownHostException {
    final String sro;
    // primary: x-forwarded-for http header
    if (isNotNull(rs.getXForwardedForClientIp()))
      sro = rs.getXForwardedForClientIp();
    // fallback: remote address host
    else if (isNotNull(rs.getRemoteAddressHost()))
      sro = rs.getRemoteAddressHost();
    else
      throw new UnknownHostException();
    return InetAddress.getByName(sro);
  }

  private final Logger log = LoggerFactory.getLogger(JWTStatusHandler.class);

  private final boolean cookieSecure;
  private final String jwtRefreshTokenName;

  /**
   * Constructor.
   *
   * @param cookieSecure the cookie secure flag (https or http)
   * @param jwtTokenName the name to use for generated JWTs
   */
  public JWTStatusHandler(boolean cookieSecure, String jwtRefreshTokenName) {
    this.cookieSecure = cookieSecure;
    this.jwtRefreshTokenName = jwtRefreshTokenName;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    final RequestSnapshot rs = ctx.get(RequestSnapshotFactory.class).getOrCreateRequestSnapshot(ctx);

    // create jwt request provider and cache in request for downstream access
    final IJwtHttpRequestProvider rp;
    try {
      // NOTE: the http request instant is truncated to seconds
      // so that conversion to/from <code>java.util.Date</code> objects are equal!
      rp = new JwtHttpRequestProviderImpl(
        rs.getAuthBearer(),
        rs.getJwtRefreshTokenCookie(),
        rs.getRequestInstant().truncatedTo(ChronoUnit.SECONDS),
        resolveRequestOrigin(rs)
      );
      ctx.getRequest().add(rp);
      log.debug("JWT request provider cached in request (Request origin: {}).", rp.getRequestOrigin());
    } catch (UnknownHostException e) {
      log.error("Un-resolvable http request origin: {}", rs);
      ctx.clientError(401); // unauthorized
      return;
    }

    // create jwt response action and cache in request for downstream access
    final IJwtHttpResponseAction ra = new JwtHttpResponseActionImpl(
      cookieSecure,
      rs.getHttpOriginDomain(),
      jwtRefreshTokenName,
      ctx
    );
    ctx.getRequest().add(ra);
    log.debug("JWT response action cached in request.");

    // determine the JWT http request status
    Blocking.get(() -> ctx.get(JWT.class).jwtHttpRequestStatus(rp)).then(jwtStatus -> {
      ctx.getRequest().add(jwtStatus);
      log.info("{} cached in request.", jwtStatus);
      ctx.next();
    });
  }

}
