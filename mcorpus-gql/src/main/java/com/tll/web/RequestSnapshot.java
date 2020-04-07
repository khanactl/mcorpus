package com.tll.web;

import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.neclean;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.lower;
import static com.tll.transform.TransformUtil.uuidFromToken;
import static com.tll.transform.TransformUtil.uuidToToken;

import java.time.Instant;

/**
 * Immutable snapshot of the key 'auditable' attributes of an incoming http
 * request.
 *
 * @author jkirton
 */
public class RequestSnapshot {

  static String stripQS(final String s) {
    final int i = s == null ? -1 : s.indexOf("?");
    return i > 0 ? s.substring(0, i) : s;
  }

  static boolean isNullwiseOrEmpty(final String s) {
    return isNullOrEmpty(s) || "null".equals(lower(s));
  }

  static String nullif(final String s) {
    return isNullwiseOrEmpty(s) ? null : s;
  }

  private final Instant requestInstant;

  private final String remoteAddressHost;

  private final String path;
  private final String method;

  private final String httpHost;
  private final String httpOrigin;
  private final String httpReferer;
  private final String httpForwarded;

  private final String xForwardedFor;
  private final String xForwardedHost;
  private final String xForwardedProto;

  private final String jwtCookie;

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
   * @param jwtCookie the http JWT cookie value
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
      String jwtCookie,
      String rstCookie,
      String rstHeader,
      String requestId
  ) {
    super();
    this.requestInstant = requestInstant;
    this.remoteAddressHost = nullif(remoteAddressHost);
    this.path = nullif(path);
    this.method = nullif(method);

    this.httpHost = nullif(httpHost);
    this.httpOrigin = nullif(httpOrigin);
    this.httpReferer = nullif(httpReferer);
    this.httpForwarded = nullif(httpForwarded);

    this.xForwardedFor = nullif(xForwardedFor);
    this.xForwardedHost = nullif(xForwardedHost);
    this.xForwardedProto = nullif(xForwardedProto);

    this.jwtCookie = nullif(jwtCookie);
    this.rstCookie = nullif(rstCookie);
    this.rstHeader = nullif(rstHeader);

    this.requestId = requestId;
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
   * @return the left-most ip of the x-forwarded-for header value
   */
  public String getXForwardedForClientIp() {
    return isNotNull(xForwardedFor) && xForwardedFor.indexOf(",") > 0 ?
      neclean(xForwardedFor.split(",")[0]) :
      xForwardedFor;
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

  /**
   * @return true if a JWT cookie value is present, false otherwise
   */
  public boolean hasJwtCookie() { return isNotNull(jwtCookie); }

  /**
   * @return the JWT cookie value.
   */
  public String getJwtCookie() {
    return jwtCookie;
  }

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
   *         This is mainly intended to correlate log statements.
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
        "referer: %s, " +
        "forwarded: %s, " +
        "x-forwarded-for: %s, " +
        "x-forwarded-host: %s, " +
        "x-forwarded-proto: %s, " +
        "hasRstCookie: %b, "+
        "hasRstHeader: %b, " +
        "hasJwtCookie: %b",
      getShortRequestId(),
      getPath(),
      getRemoteAddressHost(),
      getMethod(),
      getHttpHost(),
      stripQS(getHttpOrigin()),
      stripQS(getHttpReferer()),
      getHttpForwarded(),
      getXForwardedFor(),
      getXForwardedHost(),
      getXForwardedProto(),
      hasRstCookie(),
      hasRstHeader(),
      hasJwtCookie()
    );
  }
}
