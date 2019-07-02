package com.tll.mcorpus.web;

import static com.tll.core.Util.isNull;
import static com.tll.core.Util.not;
import static com.tll.mcorpus.web.RequestUtil.addRstCookieToResponse;
import static com.tll.mcorpus.web.RequestUtil.getOrCreateRequestSnapshot;

import java.util.Objects;
import java.util.UUID;

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
    
    // send a no content response if *both* the cookie and header rst are not present
    // this serves as a way for the clients to sync up and issue a valid subsequent request
    if(isNull(cookieRst) && isNull(headerRst)) {
      final String nextRst = UUID.randomUUID().toString();
      log.warn("No request sync tokens present in request {}.  Re-setting with short-lived token: {}..", requestSnapshot.getRequestId(), nextRst);
      addRstCookieToResponse(ctx, nextRst, 120); // you got 2 mins to re-submit
      ctx.getResponse().getHeaders().add("rst", nextRst);
      ctx.clientError(205); // 205 - Reset Content
      return;
    }
    
    // one of expected 2 rst(s) not present
    if(isNull(cookieRst) || isNull(headerRst)) {
      log.error("Request sync token(s) missing in request {}: cookie: {}, header: {}).", requestSnapshot.getRequestId(), cookieRst, headerRst);
      ctx.clientError(400); // bad request
      return;
    }
    
    // rst match verify (both are non-null)
    if(not(Objects.equals(headerRst, cookieRst))) {
      log.error("Request sync token mismatch in request {}: header: {}, cookie: {}.", requestSnapshot.getRequestId(), headerRst, cookieRst);
      ctx.clientError(400); // bad request
      return;
    }
    // rst now verified
    log.info("Request sync tokens verified for incoming http request {}: {}.", requestSnapshot.getRequestId(), headerRst);
    
    // reset the current rst and provide the new rst in the response
    final String nextRst = UUID.randomUUID().toString();
    // here we set the rst time to live to equal the time to live of the JWT cookie
    addRstCookieToResponse(ctx, nextRst, (int) ctx.get(JWT.class).jwtTimeToLive().getSeconds());
    ctx.getResponse().getHeaders().add("rst", nextRst);
    log.info("Long-lived request sync token added to response {} from request {}.", nextRst, requestSnapshot.getRequestId());

    ctx.next(); // we may proceed forward
  }

}
