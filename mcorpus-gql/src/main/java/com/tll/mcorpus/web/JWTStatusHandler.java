package com.tll.mcorpus.web;

import static com.tll.mcorpus.web.RequestUtil.getOrCreateRequestSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.web.JWT.JWTStatusInstance;

import ratpack.exec.Blocking;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Determine the JWT status for incoming http requests and cache a
 * {@link JWTStatusInstance} for downstrem handlers to access.
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
    Blocking.get(() -> { 
      return ctx.get(JWT.class).jwtRequestStatus(getOrCreateRequestSnapshot(ctx)); 
    }).then(jwtStatus -> {
      ctx.getRequest().add(jwtStatus);
      log.info("JWT status cached in incoming request: {}", jwtStatus);
      ctx.next();
    });
  }

}
