package com.tll.mcorpus.web.ratpack;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotBlank;
import static com.tll.core.Util.not;
import static com.tll.transform.TransformUtil.uuidFromToken;
import static com.tll.transform.TransformUtil.uuidToToken;
import static com.tll.web.ratpack.WebFileRenderer.TRefAndData.htmlNoCache;
import static ratpack.handling.Handlers.redirect;

import java.util.HashMap;
import java.util.Map;

import com.tll.mcorpus.repo.MCorpusRepoModule;
import com.tll.web.ratpack.CommonHttpHeaders;
import com.tll.web.ratpack.CorsHandler;
import com.tll.web.ratpack.CsrfGuardHandler;
import com.tll.web.ratpack.GraphQLHandler;
import com.tll.web.ratpack.JWTRequireAdminHandler;
import com.tll.web.ratpack.JWTStatusHandler;
import com.tll.web.ratpack.RequestSnapshotFactory;

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
        glog.info("metrics is {}", config.metricsOn ? "ON" : "OFF");
        glog.info("GraphiQL is {}", config.graphiql ? "ON" : "OFF");
        glog.info("CORS is {}",
          isNotBlank(config.httpClientOrigins) ?
            "ENABLED for " + config.httpClientOrigins :
            "DISABLED"
        );
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
        // log all incoming requests
        .all(RequestLogger.ncsa(glog))

        // always add common http response headers for good security
        .all(CommonHttpHeaders.class)

        // CORS support
        .all(CorsHandler.class)

        // CSRF protection
        .all(CsrfGuardHandler.class)

        // redirect to /index if coming in under /
        .path(redirect(301, "index"))

        // health check
        .get("health", ctx -> ctx.render("ok"))

        // graphql/
        .prefix("graphql", chainsub -> chainsub

          // the mcorpus GraphQL api (post only)
          .post(JWTStatusHandler.class)
          .post(GraphQLHandler.class)

          // the GraphiQL developer interface
          .prefix("index", chainsub2 -> chainsub2
            .all(ctx -> {
              if(not(ctx.getServerConfig().get(MCorpusServerConfig.class).graphiql)) {
                // graphiql is OFF
                ctx.clientError(404); // not found
              } else {
                ctx.next();
              }
            })
            .get(ctx -> {
              final Map<String, Object> dmap = new HashMap<>(2);
              dmap.put(
                "jwt",
                clean(ctx.get(RequestSnapshotFactory.class).getOrCreateRequestSnapshot(ctx).getAuthBearer())
              );
              dmap.put(
                ctx.getServerConfig().get(MCorpusServerConfig.class).rstTokenName,
                ctx.getRequest().get(CsrfGuardHandler.RST_TYPE).rst
              );
              ctx.render(htmlNoCache(
                ctx.file("public/graphiql/index.html"),
                dmap
              ));
            })
            .files(f -> f.dir("public/graphiql"))
          )
        )

        .prefix("admin", chainsub -> chainsub
          .all(JWTStatusHandler.class)
          .all(JWTRequireAdminHandler.class)
          .all(ctx -> {
            if(not(ctx.getServerConfig().get(MCorpusServerConfig.class).metricsOn)) {
              // metrics is OFF
              ctx.clientError(404); // not found
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
