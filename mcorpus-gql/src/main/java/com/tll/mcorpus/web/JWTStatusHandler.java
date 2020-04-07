package com.tll.mcorpus.web;

import static com.tll.mcorpus.web.RequestUtil.getOrCreateRequestSnapshot;

import java.net.UnknownHostException;

import com.tll.jwt.JWT;
import com.tll.jwt.JWTHttpRequestStatus;
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

  @Override
  public void handle(Context ctx) throws Exception {
    final RequestSnapshot rs = getOrCreateRequestSnapshot(ctx);

    // create jwt request provider and cache in request for downstream access
    final MCorpusJwtRequestProvider rp;
    try {
      rp = MCorpusJwtRequestProvider.fromRequestSnapshot(rs);
      ctx.getRequest().add(rp);
      log.debug("JWT request provider cached in request (Request origin: {}).", rp.getRequestOrigin());
    } catch(UnknownHostException e) {
      log.error("Un-resolvable http request origin: {}", rs);
      ctx.clientError(401); // unauthorized
      return;
    }

    Blocking.get(() -> ctx.get(JWT.class).jwtHttpRequestStatus(rp)).then(jwtStatus -> {
      ctx.getRequest().add(jwtStatus);
      log.info("{} cached in request.", jwtStatus);
      ctx.next();
    });
  }

}
