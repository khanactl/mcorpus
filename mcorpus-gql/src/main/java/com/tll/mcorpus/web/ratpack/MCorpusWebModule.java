package com.tll.mcorpus.web.ratpack;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.strlen;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tll.jwt.CachingJwtBackendHandler;
import com.tll.jwt.IJwtBackendHandler;
import com.tll.jwt.JWT;
import com.tll.mcorpus.repo.MCorpusRepo;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.mcorpus.web.MCorpusGraphQL;
import com.tll.mcorpus.web.MCorpusJwtBackendHandler;
import com.tll.web.ratpack.CommonHttpHeaders;
import com.tll.web.ratpack.CorsHandler;
import com.tll.web.ratpack.CsrfGuardHandler;
import com.tll.web.ratpack.GraphQLHandler;
import com.tll.web.ratpack.JWTRequireAdminHandler;
import com.tll.web.ratpack.JWTStatusHandler;
import com.tll.web.ratpack.RequestSnapshotFactory;
import com.tll.web.ratpack.WebErrorHandler;
import com.tll.web.ratpack.WebFileRenderer;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import ratpack.error.ClientErrorHandler;
import ratpack.error.ServerErrorHandler;
import ratpack.server.ServerConfig;

/**
 * Object bindings for the web layer.
 *
 * @author jkirton
 */
public class MCorpusWebModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(WebFileRenderer.class);
    bind(CommonHttpHeaders.class);
    bind(JWTRequireAdminHandler.class);
    bind(ClientErrorHandler.class).to(WebErrorHandler.class);
    bind(ServerErrorHandler.class).to(WebErrorHandler.class);
  }

  @Provides
  @Singleton
  RequestSnapshotFactory requestSnapshotFactory(MCorpusServerConfig config) {
    return new RequestSnapshotFactory(config.rstTokenName, config.jwtRefreshTokenName);
  }

  @Provides
  @Singleton
  CorsHandler corsHandler(MCorpusServerConfig config) {
    List<String> allowedOrigins = Arrays.stream(clean(config.httpClientOrigins).split(","))
      .map(aelm -> clean(aelm))
      .filter(aelm -> strlen(aelm) > 0)
      .collect(Collectors.toList());
    return new CorsHandler(allowedOrigins, "rst, authorization");
  }

  @Provides
  @Singleton
  CsrfGuardHandler csrfHandler(MCorpusServerConfig config) {
    return new CsrfGuardHandler(
      config.rstTokenName,
      config.rstRegExRequestPaths,
      Duration.ofMinutes(config.rstTtlInMinutes).toSeconds(),
      config.cookieSecure
    );
  }

  @Provides
  @Singleton
  GraphQLHandler gqlHandler(MCorpusServerConfig config, MCorpusUserRepo mcuserRepo, MCorpusRepo mcorpusRepo) {
    final MCorpusGraphQL mcorpusGraphQL = new MCorpusGraphQL(mcuserRepo, mcorpusRepo);
    final GraphQLSchema schema = mcorpusGraphQL.getGraphQLSchema();
    final GraphQL graphQL = GraphQL.newGraphQL(schema).build();
    final GraphQLHandler gqlHandler = new GraphQLHandler(graphQL, config.jwtUserLoginGraphqlMethodName, config.jwtUserLoginRefreshGraphqlMethodName);
    return gqlHandler;
  }

  @Provides
  @Singleton
  JWTStatusHandler jwtStatusHandler(MCorpusServerConfig config) {
    return new JWTStatusHandler(config.cookieSecure, config.jwtRefreshTokenName);
  }

  @Provides
  @Singleton
  JWT jwt(MCorpusUserRepo mcuserRepo, ServerConfig serverConfig, MCorpusServerConfig config) {
    // the mcorpus server config determines whether we use a caching jwt handler or not
    final IJwtBackendHandler backendHandler = config.jwtStatusCacheTimeoutInMinutes <= 0 ?
      new MCorpusJwtBackendHandler(mcuserRepo) :
      new CachingJwtBackendHandler(
        new MCorpusJwtBackendHandler(mcuserRepo),
        config.jwtStatusCacheTimeoutInMinutes,
        config.jwtStatusCacheMaxSize
      );
    return new JWT(
      backendHandler,
      Duration.ofMinutes(config.jwtTtlInMinutes),
      Duration.ofMinutes(config.jwtRefreshTokenTtlInMinutes),
      JWT.deserialize(config.jwtSalt),
      serverConfig.getPublicAddress().toString()
    );
  }
}
