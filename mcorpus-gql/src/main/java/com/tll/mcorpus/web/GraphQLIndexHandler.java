package com.tll.mcorpus.web;

import static com.tll.mcorpus.web.RequestUtil.addRstCookieToResponse;
import static com.tll.mcorpus.web.WebFileRenderer.html;
import static java.util.Collections.singletonMap;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Handles /graphql/index requests.
 * <p>
 * The GraphiQL interface.
 * <p>
 * We use a fresh rst for each requst of this page. The subsequent requests
 * into /graphql will require the rst to be passed back.
 * 
 * @author jkirton
 */
public class GraphQLIndexHandler implements Handler {

  private static final Logger log = LoggerFactory.getLogger(GraphQLIndexHandler.class);
  
  @Override
  public void handle(Context ctx) throws Exception {
    final String rst = UUID.randomUUID().toString();
    addRstCookieToResponse(ctx, rst);
    ctx.render(html("graphql/index.html", singletonMap("rst", rst), true));
    log.info("GraphiQL page rendered with rst: {}", rst);
  }

}
