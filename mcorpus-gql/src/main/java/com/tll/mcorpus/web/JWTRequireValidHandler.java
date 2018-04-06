package com.tll.mcorpus.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.web.JWT.JWTStatusInstance;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Handler that filters out requests with a non-valid jwt status.
 * <p>
 * IMPT: The jwt status instance is assumed to be cached in the request.
 * 
 * @author d2d
 */
public class JWTRequireValidHandler implements Handler {

  private final Logger log = LoggerFactory.getLogger(JWTRequireValidHandler.class);

  @Override
  public void handle(Context ctx) throws Exception {
    switch (ctx.getRequest().get(JWTStatusInstance.class).status()) {
    default:
      ctx.clientError(403); // forbidden
      return;

    case NOT_PRESENT_IN_REQUEST:
    case EXPIRED:
    case BLOCKED:
      ctx.clientError(401); // unauthorized
      return;

    case ERROR:
      ctx.clientError(500); // server error
      return;

    case VALID:
      // we good
      log.info("JWT is valid.");
      ctx.next();
      break;
    }
  }
}
