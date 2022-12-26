package com.tll.web.ratpack;

import static com.tll.core.Util.emptyIfNull;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.neclean;
import static com.tll.core.Util.not;
import static com.tll.web.ratpack.Cookie.setCookie;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.common.reflect.TypeToken;
import com.tll.web.RequestSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.core.handling.Context;
import ratpack.core.handling.Handler;

/**
 * Anti-CSRF statelessly by comparing the http header request-sync-token
 * value to the request-sync-token cookie value both contained in a received http request.
 * <p>
 * This is a variant of the double submit cookie method to thwart CSRF attacks.
 *
 * @author jpk
 */
public class CsrfGuardHandler implements Handler {

  public static final class RST {
    public final String rst;
    public RST(final String rst) { this.rst = rst; }
  }

  public static final TypeToken<RST> RST_TYPE = new TypeToken<RST>() {};

  private final Logger log = LoggerFactory.getLogger(CsrfGuardHandler.class);

  private final String rstTokenName;
  /**
   * The http request paths subject to CSRF guarding by way of either
   * checking (request) or issuing (response) a request-sync-token.
   * Case-sensitive.
   */
  private final Pattern rstPattern;
  private final long rstTtlInSeconds;
  private final boolean cookieSecureFlag;
  private final SecureRandom random;

  /**
   * Constructor.
   *
   * @param rstTokenName          the name to use for request-sync-tokens held in the
   *                              http request and response objects
   * @param rstRegexRequestPaths  the regex pattern to use against incoming http request paths
   *                              to assess when request-sync-token checking is performed
   * @param rstTtlInSeconds       the time to live for a request-sync-token in seconds
   * @param cookieSecureFlag      send request-sync-token cookies securely over https
   *                              or unsecurely over http
   */
  public CsrfGuardHandler(
    String rstTokenName,
    String rstRegexRequestPaths,
    long rstTtlInSeconds,
    boolean cookieSecureFlag
  ) {
    this.rstTokenName = rstTokenName;
    this.rstPattern = Pattern.compile(rstRegexRequestPaths);
    this.rstTtlInSeconds = rstTtlInSeconds;
    this.cookieSecureFlag = cookieSecureFlag;
    this.random = new SecureRandom();
  }

  @Override
  public void handle(final Context ctx) throws Exception {

    if(doRstCheck(ctx)) {
      final RequestSnapshot rs = ctx.get(RequestSnapshotFactory.class).getOrCreateRequestSnapshot(ctx);

      // require either http Origin or Referer header be present (assume https)
      if(isNull(rs.getHttpOrigin()) && isNull(rs.getHttpReferer())) {
        log.error("Origin and Referer http headers missing.");
        ctx.clientError(400); // bad request
        return;
      }

      final String cookieRst = neclean(rs.getRstCookie());
      final String headerRst = neclean(rs.getRstHeader());

      // send a no content response if *both* the cookie and header rst are not present
      // this serves as a way for the clients to sync up and issue a valid subsequent request
      if(isNull(cookieRst) && isNull(headerRst)) {
        final String nextRst = genRst();
        log.warn("No request sync tokens present in request.  Re-setting with short-lived token.");
        setCookie(ctx, rstTokenName, nextRst, "/", 120, cookieSecureFlag); // you got 2 mins to re-request
        ctx.getResponse().getHeaders().add(rstTokenName, nextRst);
        ctx.clientError(205); // 205 - Reset Content
        return;
      }

      // one of expected 2 rst(s) not present
      if(isNull(cookieRst) || isNull(headerRst)) {
        log.error("Request sync token(s) missing in request.");
        ctx.clientError(400); // bad request
        return;
      }

      // rst match verify (both are non-null)
      if(not(Objects.equals(headerRst, cookieRst))) {
        log.error("Request sync token mismatch in request.");
        ctx.clientError(400); // bad request
        return;
      }

      // rst now verified
      log.info("Request sync tokens verified for incoming http request.");
    }

    if(doNextRst(ctx)) {
      final String nextRst = genRst();
      setCookie(ctx, rstTokenName, nextRst, "/", rstTtlInSeconds, cookieSecureFlag);
      ctx.getResponse().getHeaders().add(rstTokenName, nextRst);
      ctx.getRequest().add(RST_TYPE, new RST(nextRst)); // make next rst available downstream
      log.info("Request sync token added to http response.");
    }

    ctx.next();
  }

  private boolean doRstCheck(final Context ctx) {
    return ctx.getRequest().getMethod().isPost();
  }

  private boolean doNextRst(final Context ctx) {
    return rstPattern.matcher(emptyIfNull(ctx.getRequest().getPath())).matches();
  }

  private String genRst() {
    final byte[] buffer = new byte[40];
    random.nextBytes(buffer);
    return Base64.getUrlEncoder().encodeToString(buffer);
  }
}
