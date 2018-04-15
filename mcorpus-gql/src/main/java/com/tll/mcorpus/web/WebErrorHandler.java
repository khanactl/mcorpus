package com.tll.mcorpus.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.error.ClientErrorHandler;
import ratpack.error.ServerErrorHandler;
import ratpack.handling.Context;

/**
 * The global error handler for the mcorpus web app.
 * 
 * @author jkirton
 *
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
      ctx.getResponse().send("Reset Content");
      break;
    case 400: // bad request
      ctx.render(ctx.file("templates/error400.html"));
      break;
    case 401: // unauthorized
      ctx.render(ctx.file("templates/error401.html"));
      break;
    case 403: // forbidden
      ctx.render(ctx.file("templates/error403.html"));
      break;
    case 404: // not found
      ctx.render(ctx.file("templates/error404.html"));
      break;
    default:
      // default client error response
      ctx.getResponse().send("Bad Client");
      break;
    }
    log.error("Client error {} response sent for request: {}.", statusCode, ctx.getRequest().getPath());
  }

  /**
   * The server/exception error handler.
   * <p>
   * Uncaught exceptions that bubble up in the handler chain when processing a
   * received request will be handled by this method.
   */
  @Override
  public void error(Context ctx, Throwable error) throws Exception {
    ctx.render(ctx.file("templates/error500.html"));
    log.error("Server error: {} for request: {}.", 
        error == null ? "-" : error.getMessage(),
        ctx.getRequest().getPath());
  }

}
