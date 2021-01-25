package com.tll.web.ratpack;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private final Logger log = LoggerFactory.getLogger(CorsHandler.class);

  private final boolean corsEnabled;
  private final String allowedOrigin;
  private final String allowedHeaders;
  private final String exposeHeaders;

  /**
   * Constructor.
   *
   * @param allowedOrigin the <code>Access-Control-Allow-Origin</code>
   *                      http response header value to use.
   *                      <p>
   *                      <b>IMPT: </b>If not set, NO CORS response headers are issued.
   * @param exposeHeaders optional comma-delimeted list of http header names
   *                      to use to set the <code>Access-Control-Expose-Headers</code>
   *                      response header value
   */
  public CorsHandler(String allowedOrigin, String exposeHeaders) {
    this.corsEnabled = isNotBlank(clean(allowedOrigin));
    this.allowedOrigin = this.corsEnabled ? clean(allowedOrigin) : null;
    this.allowedHeaders = this.corsEnabled ?
      "x-requested-with, origin, content-type, accept, " + clean(exposeHeaders) : null;
    this.exposeHeaders = this.corsEnabled ? clean(exposeHeaders) : null;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    if(corsEnabled) {
      ctx.getResponse().getHeaders()
        .add("Access-Control-Allow-Origin", allowedOrigin)
        .add("Access-Control-Allow-Credentials", "true")
        .add("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
        .add("Access-Control-Allow-Headers", allowedHeaders)
        .add("Access-Control-Expose-Headers", exposeHeaders)
        .add("Access-Control-Max-Age", "86400") // 24 hours in seconds
      ;
      log.debug("CORS headers added to response.");
    }
    ctx.next();
  }

}