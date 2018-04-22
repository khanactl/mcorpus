package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.clean;
import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.lower;
import static com.tll.mcorpus.Util.not;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable snapshot of the key 'auditable' attributes of an incoming http
 * request attempting to access the mcorpus api.
 * 
 * @author jkirton
 */
public class RequestSnapshot {
  
  /**
   * Parse a given client origin token into its constituent parts.
   * 
   * @param clientOrigin the client origin to parse
   * @return Never-null String array of size 2 where:<br>
   *         <ul>
   *         <li>element 1: remote-address-host
   *         <li>element 2: X-Forwarded-For
   *         </ul>
   * @see #getClientOrigin() for the expected client origin format
   */
  public static String[] parseClientOriginToken(final String clientOrigin) {
    if(not(isNullOrEmpty(clientOrigin)) && clientOrigin.indexOf('|') >= 0) {
      final Matcher matcher = clientOriginExtractor.matcher(clientOrigin);
      if(matcher.matches()) {
        return new String[] { matcher.group(1), matcher.group(2) };
      }
    }
    return new String[] { "", "" };
  }
  
  private static final Pattern clientOriginExtractor = Pattern.compile("^(.*)\\|(.*)$");
  
  private static boolean isNullwiseOrEmpty(final String s) {
    return isNullOrEmpty(s) || "null".equals(lower(s));
  }

  private static String nullwiseClean(final String s) {
    return isNullwiseOrEmpty(s) ? "" : clean(s);
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
  private final String sidCookie;
  private final String rstCookie;
  
  private final String rstHeader;
  
  private final String clientOrigin;

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
   * @param jwtCookie the mcorpus JWT token cookie value
   * @param sidCookie the mcorpus session id token cookie value
   * @param rstCookie the mcorpus request sync token cookie value
   * @param rstHeader the mcorpus request sync token http header value
   */
  public RequestSnapshot(Instant requestInstant, String remoteAddressHost, String httpHost, String httpOrigin,
      String httpReferer, String httpForwarded, 
      String xForwardedFor, String xForwardedProto, String xForwardedPort,
      String jwtCookie, String sidCookie, String rstCookie, String rstHeader) {
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
    this.sidCookie = sidCookie;
    this.rstCookie = rstCookie;
    this.rstHeader = rstHeader;
    
    this.clientOrigin = String.format("%s|%s", nullwiseClean(remoteAddressHost), nullwiseClean(xForwardedFor));
  }
  
  /**
   * The client origin token.
   * <p>
   * <b>FORMAT:</b> <br>
   * <code>
   * "{remote-address-host}|{X-Forwarded-For}"
   * </code>
   * 
   * @return Never-null resolved client origin token containing both the remote
   *         address host of the received request and the X-Forwarded-For header
   *         value.
   */
  public String getClientOrigin() {
    return clientOrigin;
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
   * @return true if a web session id cookie value is present, false otherwise.
   */
  public boolean hasSidCookie() { return not(isNullwiseOrEmpty(sidCookie)); }

  /**
   * @return the mcorpus session id from cookie value.
   */
  public String getSidCookie() {
    return sidCookie;
  }

  /**
   * @return true if an mcorpus JWT cookie value is present, false otherwise
   */
  public boolean hasJwtCookie() { return not(isNullwiseOrEmpty(jwtCookie)); }
  
  /**
   * @return the mcorpus JWT cookie value.
   */
  public String getJwtCookie() {
    return jwtCookie;
  }

  /**
   * @return true if a request sync token is present, false otherwise.
   */
  public boolean hasRstCookie() { return not(isNullwiseOrEmpty(rstCookie)); }

  /**
   * @return the mcorpus request sync token cookie value.
   */
  public String getRstCookie() {
    return rstCookie;
  }

  /**
   * @return true if an mcorpus rst header value is present, false otherwise
   */
  public boolean hasRstHeader() { return not(isNullwiseOrEmpty(rstHeader)); }
  
  /**
   * @return the mcorpus rst header value.
   */
  public String getRstHeader() {
    return rstHeader;
  }

  @Override
  public String toString() {
    return String.format(
        "RequestSnapshot [%n"
        + "  requestInstant=%s,%n"
        + "  clientOrigin=%s, %n"
        + "  jwtCookie %s, %n"
        + "  sidCookie=%s, %n"
        + "  rstCookie=%s, %n"
        + "  rstHeader=%s %n"
        + "]",
        requestInstant, 
        clientOrigin,
        jwtCookie == null ? "--NOT present--" : "-present-", 
        sidCookie, 
        rstCookie, 
        rstHeader);
  }
}
