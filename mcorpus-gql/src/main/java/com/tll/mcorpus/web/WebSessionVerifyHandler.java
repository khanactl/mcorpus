package com.tll.mcorpus.web;

import static com.tll.mcorpus.web.RequestUtil.webSessionStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.web.WebSessionManager.WebSession;
import com.tll.mcorpus.web.WebSessionManager.WebSessionInstance;
import com.tll.mcorpus.web.WebSessionManager.WebSessionStatus;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Verifies incoming requests have a valid mapping to a server held
 * {@link WebSession} object that hasn't expired.
 * <p>
 * A client error is thrown when the web session status is NOT
 * {@link WebSessionStatus#VALID_SESSION_ID}.
 * <p>
 * When valid, the {@link WebSessionInstance} is cached in the received request
 * for downstream handlers to optionally use.
 * 
 * @author d2d
 */
public class WebSessionVerifyHandler implements Handler {

  private static final Logger log = LoggerFactory.getLogger(WebSessionVerifyHandler.class);
  
  @Override
  public void handle(Context ctx) throws Exception {
    final WebSessionInstance webStatus = webSessionStatus(ctx);
    switch(webStatus.getStatus()) {
    default:
    case NO_SESSION_ID:
      log.info("No session id.");
      ctx.clientError(401); // unauthorized
      break;
    case BAD_SESSION_ID:
      log.info("Bad session id.");
      ctx.clientError(400); // bad request
      break;
    case SESION_ID_EXPIRED:
      log.info("Expired session id.");
      ctx.clientError(401); // unauthorized
      break;
    case VALID_SESSION_ID:
      log.info("Valid session id.");
      ctx.next(); // proceed forward
      break;
    }
  }

}
