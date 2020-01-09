package com.tll.mcorpus.web;

import java.time.Duration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tll.jwt.CachingJwtBackendHandler;
import com.tll.jwt.IJwtBackendHandler;
import com.tll.jwt.JWT;
import com.tll.mcorpus.MCorpusServerConfig;
import com.tll.mcorpus.repo.MCorpusRepo;
import com.tll.mcorpus.repo.MCorpusUserRepo;

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
    bind(JWTStatusHandler.class);
    bind(ClientErrorHandler.class).to(WebErrorHandler.class);
    bind(ServerErrorHandler.class).to(WebErrorHandler.class);
  }

  @Provides
  @Singleton
  CsrfGuardHandler csrfHandler(MCorpusServerConfig config) {
    return new CsrfGuardHandler(config.rstTtlInSeconds);
  }

  @Provides
  @Singleton
  GraphQLHandler gqlHandler(MCorpusUserRepo mcuserRepo, MCorpusRepo mcorpusRepo) {
    final MCorpusGraphQL mcorpusGraphQL = new MCorpusGraphQL(mcuserRepo, mcorpusRepo);
    final GraphQLSchema schema = mcorpusGraphQL.getGraphQLSchema();
    final GraphQL graphQL = GraphQL.newGraphQL(schema).build();
    final GraphQLHandler gqlHandler = new GraphQLHandler(graphQL);
    return gqlHandler;
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
      Duration.ofSeconds(config.jwtTtlInSeconds),
      JWT.deserialize(config.jwtSalt),
      serverConfig.getPublicAddress().toString()
    );
  }
}
