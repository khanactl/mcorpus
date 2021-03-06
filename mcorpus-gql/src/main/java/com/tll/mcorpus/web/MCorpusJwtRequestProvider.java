package com.tll.mcorpus.web;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNotNullOrEmpty;

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
        clean(rs.getRemoteAddressHost()),
        clean(rs.getXForwardedFor())
      )
    );
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
    if(isNotNullOrEmpty(clientOrigin) && clientOrigin.indexOf('|') >= 0) {
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
  public Instant getRequestInstant() {
    return rs.getRequestInstant();
  }

  @Override
  public boolean verifyClientOrigin(final String jwtAudience) {
    if(isNull(jwtAudience)) return false;

    final String[] httpReqClientOriginParsed = parseClientOriginToken(getClientOrigin());
    final String[] jwtAudienceParsed = parseClientOriginToken(jwtAudience);

    String httpReqRemoteAddrHost = httpReqClientOriginParsed[0];
    String httpReqXForwardedFor = httpReqClientOriginParsed[1];

    String jwtAudienceRemoteAddrHost = jwtAudienceParsed[0];
    String jwtAudienceXForwardedFor = jwtAudienceParsed[1];

    // if the original remote address host matches either the current remote address host
    // -OR- the x-forwarded-for then we approve this message
    if(httpReqRemoteAddrHost.equals(jwtAudienceRemoteAddrHost) || httpReqRemoteAddrHost.equals(jwtAudienceXForwardedFor))
      return true;

    if(httpReqXForwardedFor.equals(jwtAudienceRemoteAddrHost) || httpReqXForwardedFor.equals(jwtAudienceXForwardedFor))
      return true;

    // denied
    return false;
  }

}