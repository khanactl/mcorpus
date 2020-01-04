package com.tll.mcorpus.web;

import static com.tll.core.Util.emptyIfNull;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.not;
import static com.tll.mcorpus.web.RequestUtil.getOrCreateRequestSnapshot;
import static com.tll.mcorpus.web.RequestUtil.setRstCookie;
import static com.tll.transform.TransformUtil.uuidFromToken;
import static com.tll.transform.TransformUtil.uuidToToken;

import java.util.Objects;
import java.util.UUID;
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
 * This is a variant on the double submit cookie method to thwart CSRF attacks.
 *
 * @author jpk
 */
public class CsrfGuardHandler implements Handler {
  public final class RST {
    public final String rst;
    public RST(final String rst) { this.rst = rst; }
  }

  public static final String RST_TOKEN_NAME = "rst";

  @SuppressWarnings("serial")
  public static final TypeToken<RST> RST_TYPE = new TypeToken<RST>() {};

  static final Pattern ptrnNoRst = Pattern.compile("^.*\\.(js|css|png|gif|jpg|jpeg|mpeg|ico)$", Pattern.CASE_INSENSITIVE);

  private final Logger log = LoggerFactory.getLogger(CsrfGuardHandler.class);

  private final long rstCookieTtlInSeconds;

  static boolean verifyRst(final Context ctx) {
    return ctx.getRequest().getMethod().isPost();
  }

  static boolean doNextRst(final Context ctx) {
    return not(ptrnNoRst.matcher(emptyIfNull(ctx.getRequest().getPath())).matches());
  }

  static String newRst() {
    return uuidToToken(UUID.randomUUID());
  }

  /**
   * Constructor.
   *
   * @param rstCookieTtlInSeconds
   */
  public CsrfGuardHandler(long rstCookieTtlInSeconds) {
    this.rstCookieTtlInSeconds = rstCookieTtlInSeconds;
  }

  @Override
  public void handle(final Context ctx) throws Exception {

    if(verifyRst(ctx)) {
      final RequestSnapshot requestSnapshot = getOrCreateRequestSnapshot(ctx);
      final UUID cookieRst;
      final UUID headerRst;
      try {
        cookieRst = requestSnapshot.hasRstCookie() ?
            uuidFromToken(requestSnapshot.getRstCookie()) : null;
        headerRst = requestSnapshot.hasRstHeader() ?
            uuidFromToken(requestSnapshot.getRstHeader()) : null;
      } catch(Exception e) {
        log.error("Bad rst token(s).");
        ctx.clientError(400); // bad request
        return;
      }

      // send a no content response if *both* the cookie and header rst are not present
      // this serves as a way for the clients to sync up and issue a valid subsequent request
      if(isNull(cookieRst) && isNull(headerRst)) {
        final String nextRst = newRst();
        log.warn("No request sync tokens present in request.  Re-setting with short-lived token: {}.", nextRst);
        setRstCookie(ctx, nextRst, 120, "/"); // you got 2 mins to re-request
        ctx.getResponse().getHeaders().add(RST_TOKEN_NAME, nextRst);
        ctx.clientError(205); // 205 - Reset Content
        return;
      }

      // one of expected 2 rst(s) not present
      if(isNull(cookieRst) || isNull(headerRst)) {
        log.error("Request sync token(s) missing in request: cookie: {}, header: {}.", requestSnapshot.getRstCookie(), requestSnapshot.getRstHeader());
        ctx.clientError(400); // bad request
        return;
      }

      // rst match verify (both are non-null)
      if(not(Objects.equals(headerRst, cookieRst))) {
        log.error("Request sync token mismatch in request: cookie: {}, header {}.", requestSnapshot.getRstCookie(), requestSnapshot.getRstHeader());
        ctx.clientError(400); // bad request
        return;
      }
      // rst now verified
      log.info("Request sync tokens {} verified for incoming http request.", requestSnapshot.getRstCookie());
    }

    if(doNextRst(ctx)) {
      final String nextRst = newRst();
      setRstCookie(ctx, nextRst, (int) rstCookieTtlInSeconds, "/");
      ctx.getResponse().getHeaders().add(RST_TOKEN_NAME, nextRst);
      ctx.getRequest().add(RST_TYPE, new RST(nextRst)); // make next rst available downstream
      log.info("Request sync token {} added to http response.", nextRst);
    }

    ctx.next();
  }

}
