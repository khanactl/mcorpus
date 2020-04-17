package com.tll.web.ratpack;

import static com.tll.web.ratpack.Cookie.expireCookie;
import static com.tll.web.ratpack.Cookie.setCookie;

import java.time.Duration;

import com.tll.jwt.IJwtHttpResponseAction;

import ratpack.handling.Context;

/**
 * JWT http response action implementation Ratpack style.
 *
 * @author jpk
 */
public class JWTHttpResponseAction implements IJwtHttpResponseAction {

  public static JWTHttpResponseAction fromRatpackContext(final boolean cookieSecure, final String jwtTokenName, final Context ctx) {
    return new JWTHttpResponseAction(cookieSecure, jwtTokenName, ctx);
  }

  private final boolean cookieSecure;
  private final String jwtTokenName;
  private final Context ctx;

  /**
   * Constructor.
   *
   * @param ctx the Ratpack http request context
   * @param cookieSecure the cookie secure flag (https or http)
   * @param jwtTokenName the name to use when generating client-bound JWTs
   */
  private JWTHttpResponseAction(final boolean cookieSecure, final String jwtTokenName, final Context ctx) {
    this.cookieSecure = cookieSecure;
    this.jwtTokenName = jwtTokenName;
    this.ctx = ctx;
  }

  @Override
  public void expireJwtClientside() {
    expireCookie(ctx, jwtTokenName, "/", cookieSecure);
  }

  @Override
  public void setJwtClientside(String jwt, Duration jwtTimeToLive) {
    setCookie(ctx, jwtTokenName, jwt, "/", jwtTimeToLive.getSeconds(), cookieSecure);
  }

}