package com.tll.mcorpus.web;

import static com.tll.mcorpus.web.RequestUtil.expireJwtCookie;
import static com.tll.mcorpus.web.RequestUtil.expireRstCookie;
import static com.tll.mcorpus.web.RequestUtil.setJwtCookie;

import java.time.Duration;

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
  public void expireJwtClientside() {
    expireJwtCookie(ctx, "/");
    expireRstCookie(ctx, "/");
  }

  @Override
  public void setJwtClientside(String jwt, Duration jwtTimeToLive) {
    setJwtCookie(ctx, jwt, "/", jwtTimeToLive.getSeconds());
  }

}