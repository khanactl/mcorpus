package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Immutable snapshot of the key 'auditable' attributes of an incoming http
 * request attempting to access the mcorpus api.
 * 
 * @author jkirton
 */
public class RequestSnapshot {
  
  private static final Logger log = LoggerFactory.getLogger(RequestSnapshot.class);

  private final Instant requestInstant;
  
  private final String remoteAddr;
  
  private final String httpHost;
  private final String httpOrigin;
  private final String httpReferer;
  private final String httpForwarded;
  
  private final String jwtCookie;
  private final String sidCookie;
  private final String rstCookie;
  
  private final String rstHeader;

  /**
   * Constructor.
   *
   * @param requestInstant the instant the request hit the server
   * @param remoteAddr the tcp datagram remote address (ip and host)
   * @param httpHost the http Host header value
   * @param httpOrigin the http Origin header value
   * @param httpReferer the http Referer header value
   * @param httpForwarded the http Forwarded header value
   * @param jwtCookie the mcorpus JWT token cookie value
   * @param sidCookie the mcorpus session id token cookie value
   * @param rstCookie the mcorpus request sync token cookie value
   * @param rstHeader the mcorpus request sync token http header value
   */
  public RequestSnapshot(Instant requestInstant, String remoteAddr, String httpHost, String httpOrigin,
      String httpReferer, String httpForwarded, 
      String jwtCookie, String sidCookie, String rstCookie, String rstHeader) {
    super();
    this.requestInstant = requestInstant;
    this.remoteAddr = remoteAddr;
    this.httpHost = httpHost;
    this.httpOrigin = httpOrigin;
    this.httpReferer = httpReferer;
    this.httpForwarded = httpForwarded;
    this.jwtCookie = jwtCookie;
    this.sidCookie = sidCookie;
    this.rstCookie = rstCookie;
    this.rstHeader = rstHeader;
  }
  
  /**
   * Determines the client origin by interrogating the http headers.
   * <p>
   * The client origin is determined thus:
   * <ul>
   * <li>Origin http header value.
   * </ul>
   * 
   * @return Never-null resolved client origin as a {@link URL}.
   * @throws Exception upon any error resolving the Origin URL.
   */
  public URL getClientOrigin() throws Exception {
    if(isNullOrEmpty(httpOrigin) || "null".equalsIgnoreCase(httpOrigin)) {
      log.error("No http Origin present.");
      throw new Exception("No http Origin present.");
    } else {
      try {
        return new URL(httpOrigin);
      } catch (MalformedURLException e) {
        log.error("Invalid http Origin: {}", httpOrigin);
        throw new Exception("Invalid http Origin: " + httpOrigin);
      }
    }
  }

  /**
   * @return the instant the associated http request reached the server.
   */
  public Instant getRequestInstant() {
    return requestInstant;
  }
  
  /**
   * @return the http tcp datagram remote address (host and port).
   */
  public String getRemoteAddr() {
    return remoteAddr;
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
   * @return true if a web session id cookie value is present, false otherwise.
   */
  public boolean hasSidCookie() { return not(isNullOrEmpty(sidCookie)); }

  /**
   * @return the mcorpus session id from cookie value.
   */
  public String getSidCookie() {
    return sidCookie;
  }

  /**
   * @return true if an mcorpus JWT cookie value is present, false otherwise
   */
  public boolean hasJwtCookie() { return not(isNullOrEmpty(jwtCookie)); }
  
  /**
   * @return the mcorpus JWT cookie value.
   */
  public String getJwtCookie() {
    return jwtCookie;
  }

  /**
   * @return true if a request sync token is present, false otherwise.
   */
  public boolean hasRstCookie() { return not(isNullOrEmpty(rstCookie)); }

  /**
   * @return the mcorpus request sync token cookie value.
   */
  public String getRstCookie() {
    return rstCookie;
  }

  /**
   * @return true if an mcorpus rst header value is present, false otherwise
   */
  public boolean hasRstHeader() { return not(isNullOrEmpty(rstHeader)); }
  
  /**
   * @return the mcorpus rst header value.
   */
  public String getRstHeader() {
    return rstHeader;
  }

  @Override
  public String toString() {
    return String.format(
        "RequestSnapshot [requestTimestamp=%s, remoteAddr=%s, httpHost=%s, httpOrigin=%s, httpReferer=%s, httpForwarded=%s, jwtCookie=%s, sidCookie=%s, rstCookie=%s, rstHeader=%s]",
        requestInstant, remoteAddr, 
        httpHost, httpOrigin, httpReferer, httpForwarded, 
        jwtCookie, sidCookie, rstCookie, rstHeader);
  }
}
