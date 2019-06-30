package com.tll.web;

import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.lower;
import static com.tll.core.Util.not;

import java.time.Instant;

/**
 * Immutable snapshot of the key 'auditable' attributes of an incoming http
 * request.
 * 
 * @author jkirton
 */
public class RequestSnapshot /*implements IJwtHttpRequestProvider*/ {
  
  private static boolean isNullwiseOrEmpty(final String s) {
    return isNullOrEmpty(s) || "null".equals(lower(s));
  }

  private final Instant requestInstant;
  
  private final String remoteAddressHost;
  
  private final String httpHost;
  private final String httpOrigin;
  private final String httpReferer;
  private final String httpForwarded;
  
  private final String xForwardedFor;
  private final String xForwardedProto;
  private final String xForwardedPort;
  
  private final String jwtCookie;
  private final String rstCookie;
  
  private final String rstHeader;
  
  private final String requestId;

  /**
   * Constructor.
   *
   * @param requestInstant the instant the request hit the server
   * @param remoteAddressHost the tcp datagram remote ip address
   * @param httpHost the http Host header value
   * @param httpOrigin the http Origin header value
   * @param httpReferer the http Referer header value
   * @param httpForwarded the http Forwarded header value
   * @param xForwardedFor the X-Forwarded-For http header value
   * @param xForwardedProto the X-Forwarded-Proto http header value
   * @param xForwardedPort the X-Forwarded-Port http header value
   * @param jwtCookie the http JWT token cookie value
   * @param rstCookie the http request sync token cookie value
   * @param rstHeader the http request sync token http header value
   * @param requestId the http request id gotten from web layer impl
   */
  public RequestSnapshot(
      Instant requestInstant, 
      String remoteAddressHost, 
      String httpHost, 
      String httpOrigin,
      String httpReferer, 
      String httpForwarded, 
      String xForwardedFor, 
      String xForwardedProto, 
      String xForwardedPort,
      String jwtCookie, 
      String rstCookie, 
      String rstHeader,
      String requestId
  ) {
    super();
    this.requestInstant = requestInstant;
    this.remoteAddressHost = remoteAddressHost;
    
    this.httpHost = httpHost;
    this.httpOrigin = httpOrigin;
    this.httpReferer = httpReferer;
    this.httpForwarded = httpForwarded;
    
    this.xForwardedFor = xForwardedFor;
    this.xForwardedProto = xForwardedProto;
    this.xForwardedPort = xForwardedPort;
    
    this.jwtCookie = jwtCookie;
    this.rstCookie = rstCookie;
    this.rstHeader = rstHeader;
    
    this.requestId = requestId;
  }
  
  /**
   * @return the instant the associated http request reached the server.
   */
  public Instant getRequestInstant() {
    return requestInstant;
  }
  
  /**
   * @return the http tcp datagram remote IP address.
   */
  public String getRemoteAddressHost() {
    return remoteAddressHost;
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
   * @return the xForwardedFor
   */
  public String getxForwardedFor() {
    return xForwardedFor;
  }

  /**
   * @return the xForwardedProto
   */
  public String getxForwardedProto() {
    return xForwardedProto;
  }

  /**
   * @return the xForwardedPort
   */
  public String getxForwardedPort() {
    return xForwardedPort;
  }

  /**
   * @return true if a JWT cookie value is present, false otherwise
   */
  public boolean hasJwtCookie() { return not(isNullwiseOrEmpty(jwtCookie)); }
  
  /**
   * @return the JWT cookie value.
   */
  public String getJwtCookie() {
    return jwtCookie;
  }

  /**
   * @return true if a Request Sync Token is present, false otherwise.
   */
  public boolean hasRstCookie() { return not(isNullwiseOrEmpty(rstCookie)); }

  /**
   * @return the http Request Sync Token cookie value.
   */
  public String getRstCookie() {
    return rstCookie;
  }

  /**
   * @return true if an http rst header value is present, false otherwise
   */
  public boolean hasRstHeader() { return not(isNullwiseOrEmpty(rstHeader)); }
  
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
  
  @Override
  public String toString() {
    return String.format(
      "Http-Request Id: %s", 
      requestId 
    );
  }
}
