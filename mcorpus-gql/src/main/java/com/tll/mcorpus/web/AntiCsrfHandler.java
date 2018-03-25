package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.isNull;
import static com.tll.mcorpus.Util.not;
import static com.tll.mcorpus.web.RequestUtil.addRstCookieToResponse;
import static com.tll.mcorpus.web.RequestUtil.getOrCreateRequestSnapshot;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.web.JWT.JWTStatusInstance;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Verifies an incoming reqest by verifying the following:
 * <ul>
 * <li>The incoming request's resloved origin URL matches that held in the JWT
 * claim as gotten from the 'jwt' request cookie.
 * <li>The incoming request's rst http header value matches the rst cookie
 * value.
 * </ul>
 * <p>
 * This is a variant on the double submit cookie method to thwart CSRF attacks.
 * 
 * @author d2d
 */
public class AntiCsrfHandler implements Handler {
  
  private static final Logger log = LoggerFactory.getLogger(AntiCsrfHandler.class);

  @Override
  public void handle(Context ctx) throws Exception {
    final RequestSnapshot requestSnapshot = getOrCreateRequestSnapshot(ctx);
    final JWTStatusInstance jwtStatus = ctx.get(JWTStatusInstance.class);
    
    // verify http client origin header matches with that held in the inbound JWT claim
    final URL clientOrigin;
    try {
      clientOrigin = requestSnapshot.getClientOrigin();
    } catch(Exception e) {
      log.error("Bad client Origin.");
      ctx.clientError(400); // bad request
      return;
    }
    final URL jwtClaimOrigin = jwtStatus.clientOrigin();
    if(isNull(clientOrigin) || isNull(jwtClaimOrigin) || not(Objects.equals(clientOrigin, jwtClaimOrigin))) {
      log.error("Client Origin mis-match.  incomingOrigin: {}, jwtClaimOrigin: {}.", clientOrigin, jwtClaimOrigin);
      ctx.clientError(400); // bad request
      return;
    }
    
    final UUID cookieRst = requestSnapshot.hasRstCookie() ? 
            UUID.fromString(requestSnapshot.getRstCookie()) : null;
    
    final UUID headerRst = requestSnapshot.hasRstHeader() ? 
        UUID.fromString(requestSnapshot.getRstHeader()) : null;
    
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
    addRstCookieToResponse(ctx, nextRst);
    ctx.getResponse().getHeaders().add("rst", nextRst);
    log.info("rst (next) added to response: {}.", nextRst);

    ctx.next(); // we may proceed forward
  }

}
