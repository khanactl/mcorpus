package com.tll.web.ratpack;

import static com.tll.transform.TransformUtil.uuidToToken;

import com.tll.jwt.JWTHttpRequestStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.core.handling.Context;
import ratpack.core.handling.Handler;

/**
 * Require a valid JWT with an administrator role in order to proceed.
 * <p>
 * <b>IMPT: </b>
 * A {@link JWTHttpRequestStatus} is expected to already be cached in the request.
 *
 * @see <code>JWTStatusHandler</code> for caching the inbound http request JWT status
 * @author jkirton
 */
public class JWTRequireAdminHandler implements Handler {

  private final Logger log = LoggerFactory.getLogger(JWTRequireAdminHandler.class);

  @Override
  public void handle(Context ctx) throws Exception {
    final JWTHttpRequestStatus jwtRequestStatus = ctx.getRequest().get(JWTHttpRequestStatus.class);
    if(jwtRequestStatus.status().isValid() && jwtRequestStatus.isAdmin()) {
      log.info("JWT {} of user {} deemed as ADMIN.", uuidToToken(jwtRequestStatus.jwtId()), uuidToToken(jwtRequestStatus.userId()));
      ctx.next(); // you may proceed
    } else {
      ctx.clientError(403); // forbidden
    }
  }

}
