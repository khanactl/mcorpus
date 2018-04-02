package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.isNull;
import static com.tll.mcorpus.Util.not;
import static com.tll.mcorpus.web.RequestUtil.addRstCookieToResponse;
import static com.tll.mcorpus.web.RequestUtil.getOrCreateRequestSnapshot;

import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Anti-CSRF statelessly. Requires no server-side web sessions.
 * <p>
 * Verifies an incoming request by verifying the following:
 * <ul>
 * <li>A valid JWT cookie is present.
 * <li>The incoming request client origin matches that held in the signed JWT
 * {@link JWT#JWT_CLIENT_ORIGIN_KEY} claim.
 * <li>The incoming request's rst http header value matches the rst cookie
 * value.
 * </ul>
 * <p>
 * This is a variant on the double submit cookie method to thwart CSRF attacks.
 * 
 * @author d2d
 */
public class CsrfGuardByCookieAndHeaderHandler implements Handler {
  
  private static final Logger log = LoggerFactory.getLogger(CsrfGuardByCookieAndHeaderHandler.class);

  @Override
  public void handle(Context ctx) throws Exception {
    final RequestSnapshot requestSnapshot = getOrCreateRequestSnapshot(ctx);
    
    // verify client Origin matches the server configured public address
    final String serverPublicAddress = ctx.getServerConfig().getPublicAddress().toString();
    final String clientOrigin;
    try {
      clientOrigin = requestSnapshot.getClientOrigin().toString();
    } catch(Exception e) {
      log.error("Bad client Origin.");
      ctx.clientError(400); // bad request
      return;
    }
    if(isNull(clientOrigin) || not(Objects.equals(clientOrigin, serverPublicAddress))) {
      log.error("Client Origin mis-match.  incomingOrigin: {}, serverPublicAdderss: {}.", clientOrigin, serverPublicAddress);
      ctx.clientError(400); // bad request
      return;
    }
    
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
    
    // send a no content response if either the cookie or header rst are not present
    // this serves as a way for http clients to sync the per request rst exchange
    if(isNull(cookieRst) || isNull(headerRst)) {
      log.error("rst absent: cookieRst: {}, headerRst: {}.", cookieRst, headerRst);
      ctx.clientError(205); // 205 - Reset Content
      return;
    }
    
    // rst match verify (both are non-null)
    if(not(Objects.equals(headerRst, cookieRst))) {
      log.error("rst mismatch: headerRst: {}, cookieRst: {}.", headerRst, cookieRst);
      ctx.clientError(400); // bad request
      return;
    }
    // rst now verified
    
    log.info("Client Origin ({}) and provided rst ({}) verified.", clientOrigin, headerRst);
    
    // reset the current rst and provide the new rst in the response
    final String nextRst = UUID.randomUUID().toString();
    addRstCookieToResponse(ctx, nextRst, (int) ctx.get(JWT.class).jwtCookieTtlInSeconds());
    ctx.getResponse().getHeaders().add("rst", nextRst);
    log.info("rst (next) added to response: {}.", nextRst);

    ctx.next(); // we may proceed forward
  }

}
