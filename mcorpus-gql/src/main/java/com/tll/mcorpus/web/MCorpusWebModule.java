package com.tll.mcorpus.web;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tll.mcorpus.MCorpusServerConfig;
import com.tll.mcorpus.gql.MCorpusGraphQL;
import com.tll.jwt.CachingBackendJwtStatusProvider;
import com.tll.jwt.JWT;
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
    bind(JWTStatusHandler.class);
    bind(GraphQLIndexHandler.class);
    bind(CsrfGuardByCookieAndHeaderHandler.class);
    bind(ClientErrorHandler.class).to(WebErrorHandler.class);
    bind(ServerErrorHandler.class).to(WebErrorHandler.class);
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
  JWT jwt(ServerConfig serverConfig, MCorpusServerConfig config, MCorpusUserRepo mcuserRepo) {
    final MCorpusJwtBackendStatusProvider mcorpusJwtBSP = new MCorpusJwtBackendStatusProvider(mcuserRepo);
    return new JWT(
      config.jwtTtlInMillis, 
      JWT.deserialize(config.jwtSalt), 
      config.jwtStatusCacheTimeoutInMinutes <= 0 ? mcorpusJwtBSP : 
        new CachingBackendJwtStatusProvider(
          mcorpusJwtBSP, 
          config.jwtStatusCacheTimeoutInMinutes, 
          config.jwtStatusCacheMaxSize), 
      serverConfig.getPublicAddress().toString()
    );
  }
}
