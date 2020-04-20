package com.tll.web.ratpack;

import java.net.UnknownHostException;

import com.tll.jwt.IJwtHttpRequestProvider;
import com.tll.jwt.IJwtHttpResponseAction;
import com.tll.jwt.JWT;
import com.tll.jwt.JWTHttpRequestStatus;
import com.tll.web.JWTRequestProvider;
import com.tll.web.RequestSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.exec.Blocking;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Determine the JWT status for incoming http requests and cache a
 * {@link JWTHttpRequestStatus} for downstrem handlers to access.
 * <p>
 * <b>IMPT</b>: This handler will query the backend db to verify the held claims
 * of a JWT if one is present thus the reason for the asynchronous
 * {@link Promise} wrapper for the return value as db call blocks.
 * <p>
 * A {@link RequestSnapshot} instance is created as a pre-requisite in
 * determining the JWT status if one is not alredy cached in the request.
 *
 * @author jkirton
 */
public class JWTStatusHandler implements Handler {

  private final Logger log = LoggerFactory.getLogger(JWTStatusHandler.class);

  private final boolean cookieSecure;
  private final String jwtTokenName;

  /**
   * Constructor.
   *
   * @param cookieSecure the cookie secure flag (https or http)
   * @param jwtTokenName the name to use for generated JWTs
   */
  public JWTStatusHandler(boolean cookieSecure, String jwtTokenName) {
    this.cookieSecure = cookieSecure;
    this.jwtTokenName = jwtTokenName;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    final RequestSnapshot rs = ctx.get(RequestSnapshotFactory.class).getOrCreateRequestSnapshot(ctx);

    // create jwt request provider and cache in request for downstream access
    final IJwtHttpRequestProvider rp;
    try {
      rp = JWTRequestProvider.fromRequestSnapshot(rs);
      ctx.getRequest().add(rp);
      log.debug("JWT request provider cached in request (Request origin: {}).", rp.getRequestOrigin());
    } catch(UnknownHostException e) {
      log.error("Un-resolvable http request origin: {}", rs);
      ctx.clientError(401); // unauthorized
      return;
    }

    // create jwt response action and cache in request for downstream access
    final IJwtHttpResponseAction ra = JWTHttpResponseAction.fromRatpackContext(cookieSecure, jwtTokenName, ctx);
    ctx.getRequest().add(ra);
    log.debug("JWT response action cached in request.");

    Blocking.get(() -> ctx.get(JWT.class).jwtHttpRequestStatus(rp)).then(jwtStatus -> {
      ctx.getRequest().add(jwtStatus);
      log.info("{} cached in request.", jwtStatus);
      ctx.next();
    });
  }

}
