package com.tll.mcorpus.web;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.lower;
import static com.tll.core.Util.not;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tll.jwt.IJwtHttpRequestProvider;
import com.tll.web.RequestSnapshot;

/**
 * Mcorpus specific implementation of {@link IJwtHttpRequestProvider}.
 */
public class MCorpusJwtRequestProvider implements IJwtHttpRequestProvider {

  public static MCorpusJwtRequestProvider fromRequestSnapshot(final RequestSnapshot rs) {
    return new MCorpusJwtRequestProvider(
      rs, 
      String.format("%s|%s", 
        nullwiseClean(rs.getRemoteAddressHost()), 
        nullwiseClean(rs.getxForwardedFor())
      )
    );
  }

  private static boolean isNullwiseOrEmpty(final String s) {
    return isNullOrEmpty(s) || "null".equals(lower(s));
  }

  private static String nullwiseClean(final String s) {
    return isNullwiseOrEmpty(s) ? "" : clean(s);
  }

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
  private static String[] parseClientOriginToken(final String clientOrigin) {
    if(not(isNullOrEmpty(clientOrigin)) && clientOrigin.indexOf('|') >= 0) {
      final Matcher matcher = clientOriginExtractor.matcher(clientOrigin);
      if(matcher.matches()) {
        return new String[] { matcher.group(1), matcher.group(2) };
      }
    }
    return new String[] { "", "" };
  }
  
  private static final Pattern clientOriginExtractor = Pattern.compile("^(.*)\\|(.*)$");
  
  private final RequestSnapshot rs;
  private final String clientOrigin;

  private MCorpusJwtRequestProvider(final RequestSnapshot rs, String clientOrigin) {
    this.rs = rs;
    this.clientOrigin = clientOrigin;
  }

  @Override
  public String getClientOrigin() {
    return clientOrigin;
  }

  @Override
  public String getJwt() {
    return rs.getJwtCookie();
  }

  @Override
  public String getRequestId() {
    return rs.getRequestId();
  }

  @Override
  public Instant getRequestInstant() {
    return rs.getRequestInstant();
  }

  @Override
  public boolean verifyClientOrigin(final String clientOrigin) {
    if(isNull(clientOrigin)) return false;
    
    final String[] thisParsed = parseClientOriginToken(getClientOrigin());
    final String[] toverifyParsed = parseClientOriginToken(clientOrigin);
    
    String thisRemoteAddrHost = thisParsed[0];
    String thisXForwardedFor = thisParsed[1];
    
    String toverifyRemoteAddrHost = toverifyParsed[0];
    String toverifyXForwardedFor = toverifyParsed[1];
    
    // if the original remote address host matches either the current remote address host 
    // -OR- the x-forwarded-for then we approve this message
    if(thisRemoteAddrHost.equals(toverifyRemoteAddrHost) || thisRemoteAddrHost.equals(toverifyXForwardedFor)) 
      return true;
    
    if(thisXForwardedFor.equals(toverifyRemoteAddrHost) || thisXForwardedFor.equals(toverifyXForwardedFor)) 
      return true;
    
    // denied
    return false;
  }

}