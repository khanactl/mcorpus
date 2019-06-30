package com.tll.mcorpus.web;

import com.tll.jwt.IJwtHttpResponseAction;

import ratpack.handling.Context;

/**
 * MCorpus specific jwt http response action.
 * 
 * @author jpk
 */
public class MCorpusJwtHttpResponseAction implements IJwtHttpResponseAction {

  public static MCorpusJwtHttpResponseAction fromRatpackContext(final Context ctx) {
    return new MCorpusJwtHttpResponseAction(ctx);
  }
  
  private final Context ctx;

  /**
   * Constructor - Ratpack style.
   * 
   * @param ctx the Ratpack http request context
   */
  private MCorpusJwtHttpResponseAction(final Context ctx) {
    this.ctx = ctx;
  }

  @Override
  public void expireJwtCookies() {
    com.tll.mcorpus.web.RequestUtil.expireAllCookies(ctx);
  }

  @Override
  public void setJwtCookie(String jwt, long jwtCookieTtlInSeconds) {
    com.tll.mcorpus.web.RequestUtil.addJwtCookieToResponse(ctx, jwt, jwtCookieTtlInSeconds);
  }

}