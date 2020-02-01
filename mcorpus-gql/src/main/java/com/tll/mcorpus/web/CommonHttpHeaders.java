package com.tll.mcorpus.web;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Add HTTP response headers suitable for all
 * http responses in the application.
 */
public class CommonHttpHeaders implements Handler {

  @Override
  public void handle(Context ctx) throws Exception {
    ctx.getResponse().getHeaders()
      .add("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
      // .add("Content-Security-Policy", "default-src 'self'")
      .add("X-Frame-Options", "DENY")
      .add("X-XSS-Protection", "1; mode=block")
      .add("X-Content-Type-Options", "nosniff")
      ;
    ctx.next();
  }
}