package com.tll.mcorpus.web;

import static com.tll.transform.TransformUtil.uuidToToken;

import com.tll.jwt.JWTHttpRequestStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Require a valid JWT with ADMIN role in order to proceed.
 * <p>
 * <b>IMPT: </b>
 * A {@link JWTHttpRequestStatus} is expected to already be cached in the request.
 *
 * @see JWTStatusHandler for caching the inbound http request JWT status
 * @author jkirton
 */
public class JWTRequireAdminHandler implements Handler {

  private final Logger log = LoggerFactory.getLogger(JWTRequireAdminHandler.class);

  @Override
  public void handle(Context ctx) throws Exception {
    final JWTHttpRequestStatus jwtRequestStatus = ctx.getRequest().get(JWTHttpRequestStatus.class);
    switch(jwtRequestStatus.status()) {
      case VALID:
        // valid mcuser logged in by jwt - now verify admin role
        if(GraphQLRole.hasAdminRole(jwtRequestStatus.roles())) {
          log.info("JWT {} of user {} deemed as ADMIN.", uuidToToken(jwtRequestStatus.jwtId()), uuidToToken(jwtRequestStatus.userId()));
          ctx.next(); // you may proceed
          return;
        }
        // fall through when not authorized
      default:
        ctx.clientError(403); // forbidden
    }
  }

}
