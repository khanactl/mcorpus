package com.tll.mcorpus;

import static com.tll.mcorpus.web.GraphQLWebQuery.parse;
import static com.tll.mcorpus.web.WebFileRenderer.html;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static ratpack.handling.Handlers.redirect;
import static ratpack.jackson.Jackson.fromJson;
import static ratpack.jackson.Jackson.json;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.jwt.JwtClaims;
import org.pac4j.http.client.direct.CookieClient;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.jwt.config.encryption.EncryptionConfiguration;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.config.signature.SignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
import com.tll.mcorpus.gql.MCorpusGraphQL;
import com.tll.mcorpus.gql.MCorpusGraphQLModule;
import com.tll.mcorpus.repo.MCorpusRepoModule;
import com.tll.mcorpus.repo.MCorpusUserRepoAsync;
import com.tll.mcorpus.web.GraphQLWebQuery;
import com.tll.mcorpus.web.MCorpusAuthenticator;
import com.tll.mcorpus.web.RSTAuthorizer;
import com.tll.mcorpus.web.RSTGenerator;
import com.tll.mcorpus.web.RSTGeneratorAuthorizer;

import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.ssl.SslContextBuilder;
import ratpack.error.ClientErrorHandler;
import ratpack.error.ServerErrorHandler;
import ratpack.guice.Guice;
import ratpack.handling.RequestLogger;
import ratpack.hikari.HikariModule;
import ratpack.pac4j.RatpackPac4j;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import ratpack.session.SessionModule;

/**
 * MCorpus GraphQL Server entry point.
 * 
 * @author jkirton
 */
public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  @SuppressWarnings("serial")
  private static final TypeToken<Map<String, Object>> strObjMapTypeRef = new TypeToken<Map<String, Object>>() { };
  
  public static void main(final String... args) throws Exception {
    RatpackServer.start(serverSpec -> serverSpec
     .serverConfig(config -> config
       .baseDir(BaseDir.find())
       .props("app.properties")
       .args(args)
       .sysProps()
       .env()
       .ssl(SslContextBuilder.forServer(new File(cpr("ssl.crt")), new File(cpr("ssl.key"))).build())
       .require("", MCorpusServerConfig.class)
       // .require("/metrics", DropwizardMetricsConfig.class)
     )
     .registry(Guice.registry(bindings -> {
       final MCorpusServerConfig config = bindings.getServerConfig().get(MCorpusServerConfig.class);
       log.info(config.toString());

       bindings.module(HikariModule.class, hikariConfig -> {
         hikariConfig.setDataSourceClassName(config.dataSourceClassName);
         hikariConfig.setUsername(config.dbUsername);
         hikariConfig.setPassword(config.dbPassword);
         hikariConfig.addDataSourceProperty("serverName", config.dbServerName);
         hikariConfig.addDataSourceProperty("databaseName", config.dbName);
         hikariConfig.addDataSourceProperty("currentSchema", config.dbSchema);
         hikariConfig.addDataSourceProperty("portNumber", config.dbPortNumber);
       });

       bindings.module(SessionModule.class);
       bindings.module(MCorpusRepoModule.class);
       bindings.module(MCorpusGraphQLModule.class);

       bindings.bindInstance(ServerErrorHandler.class, (ctx, error) -> {
         log.error("Unexpected error", error);
         ctx.render(ctx.file("templates/error500.html"));
       });

       bindings.bindInstance(ClientErrorHandler.class, (ctx, statusCode) -> {
         ctx.getResponse().status(statusCode);
         if (statusCode == 404) {
           ctx.render(ctx.file("templates/error404.html"));
         } else if (statusCode == 401) {
           ctx.render(ctx.file("templates/error401.html"));
         } else if (statusCode == 403) {
           ctx.render(ctx.file("templates/error403.html"));
         } else {
           log.error("Unexpected: {}", statusCode);
         }
       });
     }))

     .handlers(chain -> {

       // re-fetch the mcorpus config
       // TODO we don't want to json de-serialize more than once - make this better!
       final MCorpusServerConfig config = chain.getServerConfig().get(MCorpusServerConfig.class);

       final SignatureConfiguration signatureConfiguration = new SecretSignatureConfiguration(config.jwtSalt);
       final EncryptionConfiguration encryptionConfiguration = new SecretEncryptionConfiguration(config.jwtSalt);
       final JwtGenerator<CommonProfile> jwtGenerator = new JwtGenerator<>(signatureConfiguration, encryptionConfiguration);

       final MCorpusAuthenticator mCorpusAuthenticator = new MCorpusAuthenticator(chain.getRegistry().get(MCorpusUserRepoAsync.class), jwtGenerator, config.jwtTtlInMillis, config.serverDomainName);

       // user login form
       final FormClient formClient = new FormClient("/loginForm", mCorpusAuthenticator);

       // JWT auth by cookie
       final JwtAuthenticator jwtAuthenticator = new JwtAuthenticator(asList(signatureConfiguration), asList(encryptionConfiguration));
       final CookieClient cookieClient = new CookieClient("JWT", jwtAuthenticator);

       // RST (request sync token) generator and authorizer 
       //  (i.e. anti-CSRF via per request synchronizer pattern)
       final RSTGeneratorAuthorizer rstGeneratorAuthorizer = new RSTGeneratorAuthorizer();
       // rstGeneratorAuthorizer.setHttpOnly(true);
       rstGeneratorAuthorizer.setDomain(config.serverDomainName);
       
       // verifies the incoming request's RST token is valid
       final RSTAuthorizer rstAuthorizer = new RSTAuthorizer(false);
       
       chain
         .all(RequestLogger.ncsa())

         // i.e. redirect to /index if coming in under /
         .path(redirect(301, "index"))

         .all(RatpackPac4j.authenticator("callback", cookieClient, formClient))

         // require RST token synchronization for all top-level http request paths
         .path("::^(index|graphql.*|login.*|logout)$", ctx -> RatpackPac4j.webContext(ctx).then(webContext -> {
           // we're just generating the per-request sync token here
           rstGeneratorAuthorizer.isAuthorized(webContext, null);
           ctx.next();
         }))
         
         // graphql/
         .prefix("graphql", chainsub -> chainsub
           // jwt auth and rst verification for graphql path
           .get("index", RatpackPac4j.requireAuth(CookieClient.class))
           .post(RatpackPac4j.requireAuth(CookieClient.class, rstAuthorizer))

           // graphql (the api via http post)
           .post(ctx -> ctx.parse(fromJson(strObjMapTypeRef)).then(qmap -> {
             // grab the http request info
             RatpackPac4j.webContext(ctx).then(webContext -> {
               final GraphQLWebQuery queryObject = parse(qmap, 
                   webContext.getRemoteAddr(),
                   webContext.getRequestHeader("Host"),
                   webContext.getRequestHeader("Origin"),
                   webContext.getRequestHeader("Referer"),
                   webContext.getRequestHeader("Forwarded"),
                   webContext.getSessionIdentifier());
               final GraphQLSchema schema = ctx.get(MCorpusGraphQL.class).getGraphQLSchema();
               final GraphQL graphQL = GraphQL.newGraphQL(schema).build();
               log.info("query: ***\n{}\n***", queryObject);
  
               // now process the gotten query
               try {
                 if (queryObject.isValid()) {
                   final ExecutionInput executionInput =
                           ExecutionInput.newExecutionInput()
                                   .query(queryObject.getQuery())
                                   .variables(queryObject.getVariables())
                                   // the graphql context: GraphQLWebQuery type
                                   .context(queryObject)
                                   .build();
                   graphQL.executeAsync(executionInput).thenAccept(executionResult -> {
                     if (executionResult.getErrors().isEmpty()) {
                       ctx.render(json(executionResult.toSpecification()));
                     } else {
                       ctx.render(json(executionResult.getErrors()));
                     }
                   });
                 }
               }
               catch (Throwable t) {
                 log.error("GraphQL query execution error: {}", t.getMessage());
                 ctx.render(json(t));
               }
             });
           }))

           // grapihql (grahql/index)
           .get("index", ctx -> RatpackPac4j.webContext(ctx).then(webContext -> ctx.render(
             html("graphql/index.html",
                 singletonMap(RSTGenerator.RST_TOKEN_NAME,
                     rstGeneratorAuthorizer.currentRst(webContext)), true))
           ))
           .files(f -> f.dir("templates/graphql"))
         )

         .prefix("login", chainsub -> chainsub
           .all(RatpackPac4j.requireAuth(FormClient.class))
           .get(ctx -> { // i.e. `/login` path only
             RatpackPac4j.userProfile(ctx).then(po -> {
               if(po.isPresent()) {
                 final CommonProfile p = po.get();
                 final Map<String, Object> model = new HashMap<>(3);
                 model.put("username", p.getUsername());
                 model.put("loggedInSince", p.getAttribute(JwtClaims.ISSUED_AT));
                 model.put("expires", p.getAttribute(JwtClaims.EXPIRATION_TIME));
                 ctx.render(html("loggedIn.html", model, true));
               }
             });
           })
         )
         .get("loginForm", ctx ->
           ctx.render(html("loginForm.html", singletonMap("callbackUrl", formClient.getCallbackUrl()), false))
         )
         .get("logout", ctx -> {
           RatpackPac4j.webContext(ctx).then(webContext -> {
             RatpackPac4j.userProfile(ctx).then(po -> {
               if (po.isPresent()) {
                 // we have an active user session as well as the JWT auth cookie
                 // so call db-level logout routine to capture user id and session id for auditing
                 ctx.get(MCorpusUserRepoAsync.class).logout(UUID.fromString(po.get().getId()), webContext.getSessionIdentifier());
               }
               // ensure no user profile in session and expire all cookies
               // user will subsequently be required to login to acquire another valid JWT auth token
               // finally redirect to index after logging out
               RatpackPac4j.logout(ctx).then(() -> {
                 ctx.getResponse().expireCookie("JSESSIONID");
                 final Cookie jwtCookieRef = ctx.getResponse().expireCookie("JWT");
                 // jwtCookieRef.setPath("/");
                 jwtCookieRef.setDomain(config.serverDomainName);
                 ctx.redirect("index");
               });
             });
           });
         })

         .get("index", ctx -> {
           log.debug("mcorpus index.");
           ctx.render(ctx.file("templates/index.html"));
         })

         .get("favicon.ico", ctx -> ctx.render(ctx.file("favicon.ico")));
     })
   );
  }

  /**
   * Class Path Resource. 
   * <p>
   * Get the identifying {@link URI} for a packaged (jar) resource by a given path
   * string by way of the current thread's class loader.
   * 
   * @param path
   *          the path of the packaged resource relative to the package root
   * @return never null {@link URI} identifying the resource
   * @throws Error
   *           a fatal runtime error exception is thrown upon failure to identify
   *           the resource by the given path
   */
  private static URI cpr(String path) {
    try {
      final URI uri = Thread.currentThread().getContextClassLoader().getResource(path).toURI();
      if(uri == null) throw new Error("Resource not found.");
      return uri;
    } catch (URISyntaxException e) {
      throw new Error("Resource load failure.");
    }
  }  
}
