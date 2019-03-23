package com.tll.mcorpus;

import static ratpack.handling.Handlers.redirect;

import com.tll.mcorpus.repo.MCorpusRepoModule;
import com.tll.mcorpus.web.CsrfGuardByCookieAndHeaderHandler;
import com.tll.mcorpus.web.GraphQLHandler;
import com.tll.mcorpus.web.GraphQLIndexHandler;
import com.tll.mcorpus.web.JWTStatusHandler;
import com.tll.mcorpus.web.MCorpusWebModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.guice.Guice;
import ratpack.handling.RequestLogger;
import ratpack.hikari.HikariModule;
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
   */
  private static final Logger appLog = LoggerFactory.getLogger("mcorpus-gql-server");
  
  /**
   * @return the global app logger.
   */
  public static Logger glog() { return appLog; }
  
  public static void main(final String... args) throws Exception {
    RatpackServer.start(serverSpec -> serverSpec
      .serverConfig(config -> config
        .baseDir(BaseDir.find())
        .args(args)
        .sysProps()
        .env()
        .env("MCORPUS_")
        .require("", MCorpusServerConfig.class)
        // .require("/metrics", DropwizardMetricsConfig.class)
      )
      .registry(Guice.registry(bindings -> bindings
        .module(HikariModule.class, hikariConfig -> {
          final MCorpusServerConfig config = bindings.getServerConfig().get(MCorpusServerConfig.class);
          hikariConfig.setDataSourceClassName(config.dbDataSourceClassName);
          hikariConfig.addDataSourceProperty("URL", config.dbUrl);
        })
        .module(MCorpusRepoModule.class)
        .module(MCorpusWebModule.class)
      ))
      .handlers(chain -> chain
        .all(RequestLogger.ncsa()) // log all incoming requests

        // redirect to /index if coming in under /
        .path(redirect(301, "index"))

        // graphql/
        .prefix("graphql", chainsub -> chainsub
         
          // the mcorpus GraphQL api (post only)
          .post(JWTStatusHandler.class)
          .post(CsrfGuardByCookieAndHeaderHandler.class)
          .post(GraphQLHandler.class)
         
          // the GraphiQL developer interface (get only)
          .get("index", GraphQLIndexHandler.class)
         
          .files(f -> f.dir("templates/graphql"))
        )

        // mcorpus graphql api html landing page
        .get("index", ctx -> ctx.render(ctx.file("templates/index.html")))

        .get("favicon.ico", ctx -> ctx.render(ctx.file("favicon.ico")))
      )
    );
  }
}
