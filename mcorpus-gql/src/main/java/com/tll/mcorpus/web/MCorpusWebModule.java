package com.tll.mcorpus.web;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tll.mcorpus.MCorpusServerConfig;
import com.tll.mcorpus.repo.MCorpusUserRepo;

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
    bind(GraphQLHandler.class);
    bind(GraphQLIndexHandler.class);
    bind(CsrfGuardByCookieAndHeaderHandler.class);
    bind(ClientErrorHandler.class).to(WebErrorHandler.class);
    bind(ServerErrorHandler.class).to(WebErrorHandler.class);
  }

  @Provides
  @Singleton
  JWT jwt(ServerConfig serverConfig, MCorpusServerConfig config, MCorpusUserRepo mcuserRepo) {
    return new JWT(
      config.jwtTtlInMillis, 
      JWT.deserialize(config.jwtSalt), 
      config.jwtStatusCacheTimeoutInMinutes <= 0 ? mcuserRepo : 
        new CachingJwtStatusProvider(mcuserRepo, config.jwtStatusCacheTimeoutInMinutes), 
      serverConfig.getPublicAddress().toString()
    );
  }
}
