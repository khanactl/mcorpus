package com.tll.mcorpus.web;

import static com.tll.core.Util.isNull;
import static com.tll.core.Util.not;
import static com.tll.mcorpus.web.RequestUtil.setRstCookie;
import static com.tll.mcorpus.web.RequestUtil.getOrCreateRequestSnapshot;

import java.util.Objects;
import java.util.UUID;

import com.google.common.reflect.TypeToken;
import com.tll.jwt.JWT;
import com.tll.web.RequestSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Anti-CSRF statelessly by comparing the http header 'rst' (request sync token)
 * value to the rst cookie value contained in the received request.
 * <p>
 * Note: No http header Origin / Referer checking is performed as this
 * proves problematic with an xhr server api as incoming requests source
 * from the application and not the end user. <br>
 * So instead, we bind clients by their remote address (no port) and this is
 * checked by way of JWT handling.
 * <p>
 * This is a variant on the double submit cookie method to thwart CSRF attacks.
 *
 * @author d2d
 */
public class CsrfGuardHandler implements Handler {
  public final class RST {
    public final String rst;
    public RST(final String rst) { this.rst = rst; }
  }

  public static final String RST_TOKEN_NAME = "rst";

  @SuppressWarnings("serial")
  public static final TypeToken<RST> rstTypeToken = new TypeToken<RST>() {};

  private final Logger log = LoggerFactory.getLogger(CsrfGuardHandler.class);

  @Override
  public void handle(Context ctx) throws Exception {
    final RequestSnapshot requestSnapshot = getOrCreateRequestSnapshot(ctx);

    final UUID cookieRst;
    final UUID headerRst;
    try {
      cookieRst = requestSnapshot.hasRstCookie() ?
          UUID.fromString(requestSnapshot.getRstCookie()) : null;
      headerRst = requestSnapshot.hasRstHeader() ?
          UUID.fromString(requestSnapshot.getRstHeader()) : null;
    } catch(Exception e) {
      log.error("Bad rst token(s).");
      ctx.clientError(400); // bad request
      return;
    }

    final String nextRst = UUID.randomUUID().toString();

    if(ctx.getRequest().getMethod().isPost()) {
      // send a no content response if *both* the cookie and header rst are not present
      // this serves as a way for the clients to sync up and issue a valid subsequent request
      if(isNull(cookieRst) && isNull(headerRst)) {
        log.warn("No request sync tokens present in request.  Re-setting with short-lived token: {}.", nextRst);
        setRstCookie(ctx, nextRst, 120, "/"); // you got 2 mins to re-request
        ctx.getResponse().getHeaders().add(RST_TOKEN_NAME, nextRst);
        ctx.clientError(205); // 205 - Reset Content
        return;
      }

      // one of expected 2 rst(s) not present
      if(isNull(cookieRst) || isNull(headerRst)) {
        log.error("Request sync token(s) missing in request: cookie: {}, header: {}).", cookieRst, headerRst);
        ctx.clientError(400); // bad request
        return;
      }

      // rst match verify (both are non-null)
      if(not(Objects.equals(headerRst, cookieRst))) {
        log.error("Request sync token mismatch in request: header: {}, cookie: {}.", headerRst, cookieRst);
        ctx.clientError(400); // bad request
        return;
      }
      // rst now verified
      log.info("Request sync tokens {} verified for incoming http request.", headerRst);
    }

    // TODO make independent of JWT!
    setRstCookie(ctx, nextRst, (int) ctx.get(JWT.class).jwtTimeToLive().getSeconds(), "/");
    ctx.getResponse().getHeaders().add(RST_TOKEN_NAME, nextRst);
    ctx.getRequest().add(rstTypeToken, new RST(nextRst)); // make next rst available downstream
    log.info("Long-lived request sync token {} added to http response.", nextRst);

    ctx.next(); // we may proceed forward
  }

}
