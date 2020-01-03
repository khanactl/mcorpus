package com.tll.mcorpus.web;

import static com.tll.mcorpus.Main.glog;

import java.util.UUID;

import com.tll.mcorpus.MCorpusServerConfig;
import com.tll.web.RequestSnapshot;

import io.netty.handler.codec.http.cookie.Cookie;
import ratpack.handling.Context;
import ratpack.handling.RequestId;
import ratpack.http.Request;
import ratpack.registry.NotInRegistryException;

/**
 * MCorpus-specific utility methods for processing incoming http requests.
 *
 * @author jkirton
 */
public class RequestUtil {

  /**
   * Get the configured server domain name (aka public address) underwhich the
   * system is running.
   *
   * @param ctx the request context
   * @return the configured server domain name
   */
  public static String getServerDomainName(final Context ctx) {
    return ctx.getServerConfig().getPublicAddress().getHost();
  }

  /**
   * Interrogate the given request object for the presence of a
   * {@link RequestSnapshot} instance. If one isn't present, take a request
   * snapshot and cache it in the request object. Finally, return the generated or
   * accessed instance.
   *
   * @param ctx the ratpack api {@link Context} instance encapsulating the inbound
   *            http request from which a request 'snapshot' is generated.
   * @return Never-null {@link RequestSnapshot} instance.
   */
  public static RequestSnapshot getOrCreateRequestSnapshot(final Context ctx) {
    try {
      return ctx.getRequest().get(RequestSnapshot.class);
    }
    catch(NotInRegistryException e) {
      final RequestSnapshot rs = takeRequestSnapshot(ctx.getRequest());
      ctx.getRequest().add(rs);
      glog().info("Request snapshot taken: {}", rs.toString());
      return rs;
    }
  }

  /**
   * Add the next request sync token cookie named "rst"
   * to the response in the given request context.
   *
   * @param ctx the request context object
   * @param rst the rst value to use in the response cookie
   * @param maxAge the cookie max age in seconds
   * @param path the path to use in the set cookie
   */
  public static void setRstCookie(final Context ctx, final String rst, final long maxAge, final String path) {
    final boolean secure = ctx.get(MCorpusServerConfig.class).cookieSecure;
    final Cookie rstCookieRef = ctx.getResponse().cookie("rst", rst);
    rstCookieRef.setSecure(secure);
    rstCookieRef.setHttpOnly(true);
    rstCookieRef.setDomain(getServerDomainName(ctx));
    rstCookieRef.setPath(path);
    rstCookieRef.setMaxAge(maxAge);
    glog().debug("RST cookie set (maxAge: {}, path: {}).", maxAge, path);
  }

  /**
   * Add a cookie named "jwt" to the response in the
   * given request context.
   *
   * @param ctx the request context object
   * @param jwt the JWT cookie value
   * @param maxAge the cookie max age in seconds
   * @param path the path to use in the set cookie
   */
  public static void setJwtCookie(final Context ctx, final String jwt, final long maxAge, final String path) {
    final String cookieServerName = getServerDomainName(ctx);
    final boolean secure = ctx.get(MCorpusServerConfig.class).cookieSecure;
    final Cookie jwtCookieRef = ctx.getResponse().cookie("jwt", jwt);
    jwtCookieRef.setDomain(cookieServerName);
    jwtCookieRef.setMaxAge(maxAge);
    jwtCookieRef.setHttpOnly(true); // HTTP ONLY please!
    jwtCookieRef.setSecure(secure);
    jwtCookieRef.setPath(path);
    glog().debug("JWT cookie set (maxAge: {}, path: {}).", maxAge, path);
  }

  public static void expireRstCookie(final Context ctx, final String path) {
    final String cookieServerName = getServerDomainName(ctx);
    final Cookie cookie = ctx.getResponse().expireCookie("rst");
    cookie.setPath(path);
    cookie.setDomain(cookieServerName);
    glog().debug("RST cookie expired at path {}.", path);
  }

  public static void expireJwtCookie(final Context ctx, final String path) {
    final String cookieServerName = getServerDomainName(ctx);
    final Cookie cookie = ctx.getResponse().expireCookie("jwt");
    cookie.setPath(path);
    cookie.setDomain(cookieServerName);
    glog().debug("JWT cookie expired at path {}.", path);
  }

  /**
   * Create a new {@link RequestSnapshot} instance from an incoming http request.
   *
   * @param req the incoming http request
   * @return newly created, never null {@link RequestSnapshot} instance.
   */
  private static RequestSnapshot takeRequestSnapshot(final Request req) {
    return new RequestSnapshot(
        req.getTimestamp(),
        req.getRemoteAddress().getHost(),
        req.getHeaders().get("Host"),
        req.getHeaders().get("Origin"),
        req.getHeaders().get("Referer"),
        req.getHeaders().get("Forwarded"),
        req.getHeaders().get("X-Forwarded-For"),
        req.getHeaders().get("X-Forwarded-Proto"),
        req.getHeaders().get("X-Forwarded-Port"),
        req.oneCookie("jwt"),
        req.oneCookie("rst"),
        req.getHeaders().get("rst"),
        req.maybeGet(RequestId.class).orElse(RequestId.of(UUID.randomUUID().toString())).toString()
    );
  }
}
