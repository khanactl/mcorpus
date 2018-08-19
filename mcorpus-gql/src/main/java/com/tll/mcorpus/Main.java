package com.tll.mcorpus;

import static com.tll.mcorpus.Util.glog;
import static ratpack.handling.Handlers.redirect;

import com.tll.mcorpus.gql.MCorpusGraphQLModule;
import com.tll.mcorpus.repo.MCorpusRepoModule;
import com.tll.mcorpus.web.CsrfGuardByCookieAndHeaderHandler;
import com.tll.mcorpus.web.CsrfGuardByWebSessionAndPostHandler;
import com.tll.mcorpus.web.GraphQLHandler;
import com.tll.mcorpus.web.GraphQLIndexHandler;
import com.tll.mcorpus.web.JWTRequireValidHandler;
import com.tll.mcorpus.web.JWTStatusHandler;
import com.tll.mcorpus.web.LoginPageRequestHandler;
import com.tll.mcorpus.web.LoginRequestHandler;
import com.tll.mcorpus.web.LogoutRequestHandler;
import com.tll.mcorpus.web.MCorpusWebModule;
import com.tll.mcorpus.web.WebSessionVerifyHandler;

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
       .module(MCorpusGraphQLModule.class)
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
         .post(JWTRequireValidHandler.class)
         .post(CsrfGuardByCookieAndHeaderHandler.class)
         .post(GraphQLHandler.class)
         
         // the GraphiQL developer interface (get only)
         .get("index", JWTStatusHandler.class)
         .get("index", JWTRequireValidHandler.class)
         .get("index", GraphQLIndexHandler.class)
         
         .files(f -> f.dir("templates/graphql"))
       )

       // login page
       .prefix("loginPage", chainsub -> chainsub
         .get(JWTStatusHandler.class)
         .get(LoginPageRequestHandler.class)
       )

       // login (post only)
       .prefix("login", chainsub -> chainsub 
         .post(JWTStatusHandler.class)
         .post(WebSessionVerifyHandler.class)
         .post(CsrfGuardByWebSessionAndPostHandler.class)
         .post(LoginRequestHandler.class)
       )

       // logout (post only)
       .prefix("logout", chainsub -> chainsub
         .post(JWTStatusHandler.class)
         .post(JWTRequireValidHandler.class)
         .post(LogoutRequestHandler.class)
       )

       // mcorpus graphql api landing page
       .get("index", ctx -> {
         glog().debug("mcorpus index.");
         ctx.render(ctx.file("templates/index.html"));
       })

       .get("favicon.ico", ctx -> ctx.render(ctx.file("favicon.ico")))
     )
    );
  }
}
