package com.tll.mcorpus.web;

import static com.tll.core.Util.emptyIfNull;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.neclean;
import static com.tll.core.Util.not;
import static com.tll.mcorpus.web.RequestUtil.getOrCreateRequestSnapshot;
import static com.tll.mcorpus.web.RequestUtil.setRstCookie;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.common.reflect.TypeToken;
import com.tll.web.RequestSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Anti-CSRF statelessly by comparing the http header 'rst' (request sync token)
 * value to the rst cookie value both contained in a received http request.
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

  public static final String RST_TOKEN_NAME = "rst";

  @SuppressWarnings("serial")
  public static final TypeToken<RST> RST_TYPE = new TypeToken<RST>() {};

  /**
   * Whitelist request paths subject to either checking (request)
   * or issuing (response) an RST.  Case-sensitive.
   * <p>
   * I.e. the pages subject to CSRF gurading.
   * <p>
   * For mcorpus app, we guard
   * <code>graphql[/]</code> and <code>graphql/index[/]</code>
   * request paths only.
   */
  static final Pattern PTRN_GEN_RST = Pattern.compile("^(graphql\\/index|graphql)\\/?$");

  private static final SecureRandom RND = new SecureRandom();

  static boolean doRstCheck(final Context ctx) {
    return ctx.getRequest().getMethod().isPost();
  }

  static boolean doNextRst(final Context ctx) {
    return PTRN_GEN_RST.matcher(emptyIfNull(ctx.getRequest().getPath())).matches();
  }

  /**
   * Generate an RST.
   *
   * @return Newly generated RST
   */
  static String genRst() {
    final byte[] buffer = new byte[40];
    RND.nextBytes(buffer);
    return Base64.getUrlEncoder().encodeToString(buffer);
  }

  private final Logger log = LoggerFactory.getLogger(CsrfGuardHandler.class);

  private final long rstTtlInSeconds;

  /**
   * Constructor.
   *
   * @param rstTtlInSeconds the time to live for an RST in seconds
   */
  public CsrfGuardHandler(long rstTtlInSeconds) {
    this.rstTtlInSeconds = rstTtlInSeconds;
  }

  @Override
  public void handle(final Context ctx) throws Exception {

    if(doRstCheck(ctx)) {
      final RequestSnapshot requestSnapshot = getOrCreateRequestSnapshot(ctx);

      // require either http Origin or Referer header be present (assume https)
      if(isNull(requestSnapshot.getHttpOrigin()) && isNull(requestSnapshot.getHttpReferer())) {
        log.error("Origin and Referer http headers missing.");
        ctx.clientError(400); // bad request
        return;
      }

      final String cookieRst = neclean(requestSnapshot.getRstCookie());
      final String headerRst = neclean(requestSnapshot.getRstHeader());

      // send a no content response if *both* the cookie and header rst are not present
      // this serves as a way for the clients to sync up and issue a valid subsequent request
      if(isNull(cookieRst) && isNull(headerRst)) {
        final String nextRst = genRst();
        log.warn("No request sync tokens present in request.  Re-setting with short-lived token.");
        setRstCookie(ctx, nextRst, "/", 120); // you got 2 mins to re-request
        ctx.getResponse().getHeaders().add(RST_TOKEN_NAME, nextRst);
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
      setRstCookie(ctx, nextRst, "/", rstTtlInSeconds);
      ctx.getResponse().getHeaders().add(RST_TOKEN_NAME, nextRst);
      ctx.getRequest().add(RST_TYPE, new RST(nextRst)); // make next rst available downstream
      log.info("Request sync token added to http response.");
    }

    ctx.next();
  }

}
