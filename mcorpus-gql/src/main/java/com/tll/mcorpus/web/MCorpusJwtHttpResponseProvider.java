package com.tll.mcorpus.web;

import com.tll.jwt.IJwtHttpResponseProvider;

import ratpack.handling.Context;

/**
 * MCorpus specific jwt http response provider.
 * 
 * @author jpk
 */
public class MCorpusJwtHttpResponseProvider implements IJwtHttpResponseProvider {

  private final Context ctx;

  /**
   * Constructor - Ratpack style.
   * 
   * @param ctx the Ratpack http request context
   */
  public MCorpusJwtHttpResponseProvider(final Context ctx) {
    this.ctx = ctx;
  }

  @Override
  public void expireAllCookies() {
    com.tll.mcorpus.web.RequestUtil.expireAllCookies(ctx);
  }

  @Override
  public void setJwtCookie(String jwt, long jwtCookieTtlInSeconds) {
    com.tll.mcorpus.web.RequestUtil.addJwtCookieToResponse(ctx, jwt, jwtCookieTtlInSeconds);
  }

}