package com.tll.web.ratpack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.handling.Context;

/**
 * Cookie util methods Ratpack style.
 *
 * @author jpk
 */
public class Cookie {

  private static final Logger log = LoggerFactory.getLogger("cookie");

  public static void setCookie(
    final Context ctx,
    final String cookieName,
    final String cookieValue,
    final String path,
    final long maxAge,
    final boolean secure
  ) {
    setCookieImp(ctx, cookieName, cookieValue, path, maxAge, secure, "None");
    log.debug("{} cookie set (path: {}, maxAge: {}).", cookieName, path, maxAge);
  }

  public static void expireCookie(
    final Context ctx,
    final String cookieName,
    final String path,
    final boolean secure) {
    setCookieImp(ctx, cookieName, "", path, 0, secure, "None");
    log.debug("{} cookie expired.", cookieName);
  }

  private static void setCookieImp(
    final Context ctx,
    final String cookieName,
    final String cookieValue,
    final String path,
    final long maxAge,
    final boolean secure,
    final String sameSiteVal
  ) {
    ctx.getResponse().getHeaders().add(
      "Set-Cookie",
      String.format("%s=%s; Domain=%s; Path=%s; Max-Age=%d; %sHttpOnly; SameSite=%s;",
        cookieName,
        cookieValue,
        getServerDomainName(ctx),
        path,
        maxAge,
        secure ? "Secure; " : "",
        sameSiteVal
      )
    );
  }

  private static String getServerDomainName(final Context ctx) {
    return ctx.getServerConfig().getPublicAddress().getHost();
  }

  private Cookie() {}
}