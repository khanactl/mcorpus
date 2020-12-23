package com.tll.web.ratpack;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotNullOrEmpty;

import java.util.List;
import java.util.Objects;

import com.tll.web.RequestSnapshot;

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
  private final List<String> allowedOrigins;
  private final String allowedHeaders;
  private final String exposeHeaders;

  /**
   * Constructor.
   *
   * @param allowedOrigins  List of allowed http client Origins.
   *                        <p>
   *                        <b>IMPT: </b>If not set, NO CORS response headers are issued.
   * @param exposeHeaders   optional comma-delimeted list of http header names
   *                        to use to set the <code>Access-Control-Expose-Headers</code>
   *                        response header value
   */
  public CorsHandler(List<String> allowedOrigins, String exposeHeaders) {
    this.corsEnabled = isNotNullOrEmpty(allowedOrigins);
    this.allowedOrigins = this.corsEnabled ? allowedOrigins : null;
    this.allowedHeaders = this.corsEnabled ?
      "x-requested-with, origin, content-type, accept, " + clean(exposeHeaders) : null;
    this.exposeHeaders = this.corsEnabled ? clean(exposeHeaders) : null;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    if(corsEnabled) {
      final RequestSnapshot rs = ctx.get(RequestSnapshotFactory.class).getOrCreateRequestSnapshot(ctx);
      final String origin = rs.getHttpOrigin();
      for(String aorigin : this.allowedOrigins) {
        if(Objects.equals(aorigin, origin)) {
          ctx.getResponse().getHeaders()
            .add("Access-Control-Allow-Origin", aorigin)
            .add("Access-Control-Allow-Credentials", "true")
            .add("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
            .add("Access-Control-Allow-Headers", allowedHeaders)
            .add("Access-Control-Expose-Headers", exposeHeaders)
            .add("Access-Control-Max-Age", "86400") // 24 hours in seconds
            ;
          log.debug("CORS headers added to response.");
          break;
        }
      }
    }
    ctx.next();
  }

}