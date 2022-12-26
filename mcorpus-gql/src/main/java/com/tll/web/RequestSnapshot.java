package com.tll.web;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.neclean;
import static com.tll.transform.TransformUtil.uuidFromToken;
import static com.tll.transform.TransformUtil.uuidToToken;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable snapshot of the key 'auditable' attributes
 * of an incoming http request.
 *
 * @author jkirton
 */
public class RequestSnapshot {

  static final Pattern PTRN_DOMAIN = Pattern.compile("^(http:\\/\\/|https:\\/\\/|)(.+)$");

  static String stripQS(final String s) {
    final int i = s == null ? -1 : s.indexOf("?");
    return (i > 0 && s != null) ? s.substring(0, i) : s;
  }

  static String stripSchemeAndPort(final String s) {
    if(isNull(s)) return null;
    Matcher m = PTRN_DOMAIN.matcher(s);
    String host = m.matches() ? m.group(2) : s;
    int i = host.lastIndexOf(":");
    return i > 0 ? host.substring(0, i) : host;
  }

  private final Instant requestInstant;

  private final String remoteAddressHost;

  private final String path;
  private final String method;

  private final String httpHost;
  private final String httpOrigin;
  private final String httpOriginDomain;
  private final String httpReferer;
  private final String httpForwarded;

  private final String xForwardedFor;
  private final String xForwardedForClientIp;
  private final String xForwardedHost;
  private final String xForwardedProto;

  // private final String jwtCookie;
  private final String authHeader;
  private final String jwtRefreshTokenCookie;

  private final String rstCookie;
  private final String rstHeader;

  private final String requestId;

  /**
   * Constructor.
   *
   * @param requestInstant the instant the request hit the server
   * @param remoteAddressHost the address of the client that initiated the request.
   *                          if we are behind a proxy server this value is expected to be the
   *                          ip address of said proxy server
   * @param path the request URI w/o query string and leading forward slash
   * @param method the http method (GET, POST,..)
   * @param httpHost the http Host header value
   * @param httpOrigin the http Origin header value
   * @param httpReferer the http Referer header value
   * @param httpForwarded the http Forwarded header value
   * @param xForwardedFor the X-Forwarded-For http header value
   * @param xForwardedHost the X-Forwarded-Host http header value
   * @param xForwardedProto the X-Forwarded-Proto http header value
   * @param authHeader the http Authrorization header value
   * @param jwtRefreshTokenCookie the jwt refresh token cookie value
   * @param rstCookie the http request sync token cookie value
   * @param rstHeader the http request sync token http header value
   * @param requestId the server generated unique request id for tracking purposes
   */
  public RequestSnapshot(
      Instant requestInstant,
      String remoteAddressHost,
      String path,
      String method,
      String httpHost,
      String httpOrigin,
      String httpReferer,
      String httpForwarded,
      String xForwardedFor,
      String xForwardedHost,
      String xForwardedProto,
      // String jwtCookie,
      String authHeader,
      String jwtRefreshTokenCookie,
      String rstCookie,
      String rstHeader,
      String requestId
  ) {
    super();
    this.requestInstant = requestInstant;
    this.remoteAddressHost = neclean(remoteAddressHost);
    this.path = neclean(path);
    this.method = neclean(method);

    this.httpHost = neclean(httpHost);
    this.httpOrigin = neclean(httpOrigin);
    this.httpOriginDomain = stripSchemeAndPort(this.httpOrigin);
    this.httpReferer = neclean(httpReferer);
    this.httpForwarded = neclean(httpForwarded);

    this.xForwardedFor = neclean(xForwardedFor);
    this.xForwardedForClientIp =
      isNotNull(xForwardedFor) && xForwardedFor.indexOf(",") > 0 ?
        neclean(xForwardedFor.split(",")[0]) :
        xForwardedFor;
    this.xForwardedHost = neclean(xForwardedHost);
    this.xForwardedProto = neclean(xForwardedProto);

    // this.jwtCookie = jwtCookie;
    this.authHeader = neclean(authHeader);
    this.jwtRefreshTokenCookie = neclean(jwtRefreshTokenCookie);

    this.rstCookie = neclean(rstCookie);
    this.rstHeader = neclean(rstHeader);

    this.requestId = neclean(requestId);
  }

  /**
   * @return the instant the associated http request reached the server.
   */
  public Instant getRequestInstant() {
    return requestInstant;
  }

  /**
   * @return the address of the client that initiated the request.
   */
  public String getRemoteAddressHost() {
    return remoteAddressHost;
  }

  /**
   * @return the http request Path.
   */
  public String getPath() {
    return path;
  }

  /**
   * @return the http method
   */
  public String getMethod() {
    return method;
  }

  /**
   * @return the http Host header value.
   */
  public String getHttpHost() {
    return httpHost;
  }

  /**
   * @return the http Origin header value.
   */
  public String getHttpOrigin() {
    return httpOrigin;
  }

  /**
   * @return the domain (hostname) part of the http Origin header value.
   */
  public String getHttpOriginDomain() {
    return httpOriginDomain;
  }

  /**
   * @return the http Referer header value.
   */
  public String getHttpReferer() {
    return httpReferer;
  }

  /**
   * @return the http Forwarded header value.
   */
  public String getHttpForwarded() {
    return httpForwarded;
  }

  /**
   * @return the X-Forwarded-For header value
   */
  public String getXForwardedFor() {
    return xForwardedFor;
  }

  /**
   * @return the left-most token of the comma-delimeted X-Forwarded-For header value -OR-
   *         the x-forwarded-for header value in full when it is not comma-delimited
   */
  public String getXForwardedForClientIp() {
    return xForwardedForClientIp;
  }

  /**
   * @return the X-Forwarded-Host header value
   */
  public String getXForwardedHost() {
    return xForwardedHost;
  }

  /**
   * @return the X-Forwarded-Proto header value
   */
  public String getXForwardedProto() {
    return xForwardedProto;
  }

  /*
  public boolean hasJwtCookie() { return isNotNull(jwtCookie); }

  public String getJwtCookie() {
    return jwtCookie;
  }
  */

  public boolean hasAuthHeader() { return isNotNull(authHeader); }

  public String getAuthHeader() { return authHeader; }

  public String getAuthBearer() {
    return (hasAuthHeader() && authHeader.startsWith("Bearer ")) ?
              authHeader.substring(7) : null;
  }

  public boolean hasJwtRefreshTokenCookie() { return isNotNull(jwtRefreshTokenCookie); }

  public String getJwtRefreshTokenCookie() { return jwtRefreshTokenCookie; }

  /**
   * @return true if a Request Sync Token is present, false otherwise.
   */
  public boolean hasRstCookie() { return isNotNull(rstCookie); }

  /**
   * @return the http Request Sync Token cookie value.
   */
  public String getRstCookie() {
    return rstCookie;
  }

  /**
   * @return true if an http rst header value is present, false otherwise
   */
  public boolean hasRstHeader() { return isNotNull(rstHeader); }

  /**
   * @return the http Request Sync Token header value.
   */
  public String getRstHeader() {
    return rstHeader;
  }

  /**
   * @return Opaque id associated with this request.
   *         <p>
   *         This is mainly intended to correlate server log output.
   */
  public String getRequestId() {
    return requestId;
  }

  /**
   * @return the base-64 encoded request id.
   */
  public String getShortRequestId() {
    return uuidToToken(uuidFromToken(requestId));
  }

  @Override
  public String toString() {
    return String.format(
      "Http-Request[%s] /%s - " +
        "remoteAddrHost: %s, " +
        "method: %s, " +
        "host: %s, " +
        "origin: %s, " +
        "origin domain: %s, " +
        "referer: %s, " +
        "forwarded: %s, " +
        "x-forwarded-for: %s, " +
        "x-forwarded-host: %s, " +
        "x-forwarded-proto: %s, " +
        "rstCookie: %s, "+
        "rstHeader: %s, " +
        "authHeader: %s, " +
        "jwtCookieRefreshTokenLen: %d",
      getShortRequestId(),
      getPath(),
      getRemoteAddressHost(),
      getMethod(),
      getHttpHost(),
      getHttpOrigin(),
      getHttpOriginDomain(),
      stripQS(getHttpReferer()),
      getHttpForwarded(),
      getXForwardedFor(),
      getXForwardedHost(),
      getXForwardedProto(),
      getRstCookie(),
      getRstHeader(),
      // clean(getJwtCookie()).length()
      getAuthHeader(),
      clean(getJwtRefreshTokenCookie()).length()
    );
  }
}
