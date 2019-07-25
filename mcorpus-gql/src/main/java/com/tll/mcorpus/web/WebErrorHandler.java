package com.tll.mcorpus.web;

import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.error.ClientErrorHandler;
import ratpack.error.ServerErrorHandler;
import ratpack.handling.Context;

/**
 * The global error handler for the mcorpus web app.
 * 
 * @author jkirton
 */
public class WebErrorHandler implements ServerErrorHandler, ClientErrorHandler {

  private final Logger log = LoggerFactory.getLogger(WebErrorHandler.class);

  /**
   * The client error handler.
   */
  @Override
  public void error(Context ctx, int statusCode) throws Exception {
    ctx.getResponse().status(statusCode);
    switch(statusCode) {
    case 205: // reset content 
      ctx.getResponse().send("Reset Content (205)");
      break;
    case 400: // bad request
      ctx.getResponse().send("Bad Request (400)");
      break;
    case 401: // unauthorized
      ctx.getResponse().send("Unauthorized (401)");
      break;
    case 403: // forbidden
      ctx.getResponse().send("Forbidden (403)");
      break;
    case 404: // not found
      ctx.getResponse().send("Not Found (404)");
      break;
    default: // default client error response
      ctx.getResponse().send("Bad Client");
      break;
    }
    log.error("Client error {} response sent for request: {} - {}.", 
      statusCode, 
      ctx.getRequest().getPath(), 
      RequestUtil.getOrCreateRequestSnapshot(ctx)
    );
  }

  /**
   * The server/exception error handler.
   * <p>
   * Uncaught exceptions that bubble up in the handler chain when processing a
   * received request will be handled by this method.
   */
  @Override
  public void error(Context ctx, Throwable error) throws Exception {
    ctx.getResponse().send("Server error (500)");
    final String emsg = isNull(error) ? "UNKNOWN" : 
      (isNullOrEmpty(error.getMessage()) ? "UNKNOWN" : error.getMessage());
    log.error("Server error '{}' for request: {} - {}.", 
        emsg,
        ctx.getRequest().getPath(), 
        RequestUtil.getOrCreateRequestSnapshot(ctx)
    );
  }

}
