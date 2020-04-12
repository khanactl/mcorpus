package com.tll.mcorpus.web;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * CORS support.
 *
 * @author jpk
 */
public class CorsHttpHeaders implements Handler {

  @Override
  public void handle(Context ctx) throws Exception {
    ctx.getResponse().getHeaders()
      .add("Access-Control-Allow-Origin", "*")
      .add("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
      .add("Access-Control-Allow-Headers", "x-requested-with, origin, content-type, accept")
      .add("Access-Control-Max-Age", "86400") // 24 hours in seconds
    ;
    ctx.next();
  }

}