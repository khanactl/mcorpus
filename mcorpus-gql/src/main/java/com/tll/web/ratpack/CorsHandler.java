package com.tll.web.ratpack;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotBlank;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * CORS support Ratpack style.
 * <p>
 * If the <code>allowedOrigin</code> property is set,
 * CORS http response headers are added to received requests
 * to allow http communication to the specified <code>allowedOrigin</code>.
 *
 * @author jpk
 */
public class CorsHandler implements Handler {

  private final String allowedOrigin;
  private final boolean corsEnabled;

  /**
   * Constructor.
   *
   * @param allowedOrigin the <code>Access-Control-Allow-Origin</code>
   *                      http header value to use.
   *                      <p>
   *                      If not set, NO CORS response headers are issued.
   */
  public CorsHandler(String allowedOrigin) {
    this.allowedOrigin = clean(allowedOrigin);
    this.corsEnabled = isNotBlank(this.allowedOrigin);
  }

  @Override
  public void handle(Context ctx) throws Exception {
    if(corsEnabled)
      ctx.getResponse().getHeaders()
        .add("Access-Control-Allow-Origin", allowedOrigin)
        .add("Access-Control-Allow-Credentials", "true")
        .add("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
        .add("Access-Control-Allow-Headers", "x-requested-with, origin, content-type, accept, rst")
        .add("Access-Control-Max-Age", "86400") // 24 hours in seconds
      ;
    ctx.next();
  }

}