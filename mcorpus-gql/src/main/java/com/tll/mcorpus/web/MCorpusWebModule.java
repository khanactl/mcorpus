package com.tll.mcorpus.web;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.nimbusds.jose.util.StandardCharset;
import com.tll.mcorpus.MCorpusServerConfig;
import com.tll.mcorpus.repo.MCorpusUserRepoAsync;

import ratpack.error.ClientErrorHandler;
import ratpack.error.ServerErrorHandler;

/**
 * Object bindings for the web layer.
 * 
 * @author jkirton
 */
public class MCorpusWebModule extends AbstractModule {
  
  @Override
  protected void configure() {
    bind(JWTStatusHandler.class);
    bind(JWTRequireValidHandler.class);
    bind(GraphQLHandler.class);
    bind(GraphQLIndexHandler.class);
    bind(ViewLoginPageRequestHandler.class);
    bind(LoginRequestHandler.class);
    bind(LogoutRequestHandler.class);
    bind(WebSessionVerifyHandler.class);
    bind(AntiCsrfHandler.class);
    bind(RstAwarePostRequestHandler.class);
    bind(ClientErrorHandler.class).to(WebErrorHandler.class);
    bind(ServerErrorHandler.class).to(WebErrorHandler.class);
  }

  @Provides
  @Singleton
  JWT jwt(MCorpusServerConfig config, MCorpusUserRepoAsync mcuserRepo) {
    return new JWT(config.jwtTtlInMillis, config.jwtSalt.getBytes(StandardCharset.UTF_8), mcuserRepo);
  }
}
