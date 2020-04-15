package com.tll.mcorpus;

import static com.tll.core.Util.not;
import static com.tll.mcorpus.web.WebFileRenderer.TRefAndData.htmlNoCache;
import static com.tll.transform.TransformUtil.uuidFromToken;
import static com.tll.transform.TransformUtil.uuidToToken;
import static java.util.Collections.singletonMap;
import static ratpack.handling.Handlers.redirect;

import com.tll.mcorpus.repo.MCorpusRepoModule;
import com.tll.mcorpus.web.CommonHttpHeaders;
import com.tll.mcorpus.web.CorsHttpHeaders;
import com.tll.mcorpus.web.CsrfGuardHandler;
import com.tll.mcorpus.web.GraphQLHandler;
import com.tll.mcorpus.web.JWTRequireAdminHandler;
import com.tll.mcorpus.web.JWTStatusHandler;
import com.tll.mcorpus.web.MCorpusWebModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ratpack.dropwizard.metrics.DropwizardMetricsConfig;
import ratpack.dropwizard.metrics.DropwizardMetricsModule;
import ratpack.dropwizard.metrics.MetricsWebsocketBroadcastHandler;
import ratpack.guice.Guice;
import ratpack.handling.RequestId;
import ratpack.handling.RequestLogger;
import ratpack.hikari.HikariModule;
import ratpack.logging.MDCInterceptor;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;

/**
 * MCorpus GraphQL Server entry point.
 *
 * @author jkirton
 */
public class Main {

  /**
   * The app-wide global logger.
   * <p>
   * Use this sole static logger to issue application level logging
   * when logging at class/global level (i.e. inside static methods).
   *
   * @return the global app logger.
   */
  public static final Logger glog() { return glog; }

  private static final Logger glog = LoggerFactory.getLogger("mcorpus-gql");

  public static void main(final String... args) throws Exception {
    RatpackServer.start(serverSpec -> serverSpec
      .serverConfig(config -> config
        .baseDir(BaseDir.find())
        .props("app.properties")
        .args(args)
        .sysProps()
        .env("MCORPUS_")
        .require("", MCorpusServerConfig.class)
        .require("/metrics", DropwizardMetricsConfig.class)
      )
      .registry(Guice.registry(bindings -> {
        final MCorpusServerConfig config = bindings.getServerConfig().get(MCorpusServerConfig.class);
        if(config.metricsOn) {
          bindings.module(DropwizardMetricsModule.class);
        }
        glog().info("metrics is {}", config.metricsOn ? "ON" : "OFF");
        bindings.module(HikariModule.class, hikariConfig -> {
          hikariConfig.setDataSourceClassName(config.dbDataSourceClassName);
          hikariConfig.addDataSourceProperty("URL", config.dbUrl);
        });
        bindings.module(MCorpusRepoModule.class);
        bindings.module(MCorpusWebModule.class);
        // slf4j MDC Ratpack style
        bindings.add(MDCInterceptor.withInit(e ->
          e.maybeGet(RequestId.class).ifPresent(rid ->
            MDC.put("requestId", uuidToToken(uuidFromToken(rid.toString())))
          )
        ));
      }))
      .handlers(chain -> chain
        .all(RequestLogger.ncsa(glog)) // log all incoming requests

        .all(CommonHttpHeaders.class) // always add common http response headers for good security

        .all(CsrfGuardHandler.class) // CSRF protection

        // redirect to /index if coming in under /
        .path(redirect(301, "index"))

        // health check
        .get("health", ctx -> ctx.render("ok"))

        // graphql/
        .prefix("graphql", chainsub -> chainsub
          .all(CorsHttpHeaders.class) // CORS support

          // the mcorpus GraphQL api (post only)
          .post(JWTStatusHandler.class)
          .post(GraphQLHandler.class)

          // the GraphiQL developer interface
          .prefix("index", chainsub2 -> chainsub2
            .all(ctx -> {
              if(not(ctx.getServerConfig().get(MCorpusServerConfig.class).graphiql)) {
                // graphiql is OFF
                ctx.clientError(400); // bad request
              } else {
                ctx.next();
              }
            })
            .get(ctx -> ctx.render(htmlNoCache(
              ctx.file("public/graphiql/index.html"),
              singletonMap("rst", ctx.getRequest().get(CsrfGuardHandler.RST_TYPE).rst)
            )))
            .files(f -> f.dir("public/graphiql"))
          )
        )

        .prefix("admin", chainsub -> chainsub
          .all(JWTStatusHandler.class)
          .all(JWTRequireAdminHandler.class)
          .all(ctx -> {
            if(not(ctx.getServerConfig().get(MCorpusServerConfig.class).metricsOn)) {
              // metrics is OFF
              ctx.clientError(400); // bad request
            } else {
              ctx.next();
            }
          })
          .get("metrics-report", new MetricsWebsocketBroadcastHandler())
          .get("metrics", ctx -> ctx.render(htmlNoCache(ctx.file("public/admin/metrics/metrics.html"))))
          .files(f -> f.dir("public/admin"))
        )

        // mcorpus graphql api html landing page
        .get("index", ctx -> ctx.render(htmlNoCache(ctx.file("public/index.html"))))

        .get("favicon.ico", ctx -> ctx.render(ctx.file("public/favicon.ico")))
      )
    );
  }
}
