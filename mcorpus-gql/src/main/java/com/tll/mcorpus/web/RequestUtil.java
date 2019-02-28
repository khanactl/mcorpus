package com.tll.mcorpus.web;

import static com.tll.mcorpus.Main.glog;

import com.tll.mcorpus.MCorpusServerConfig;

import io.netty.handler.codec.http.cookie.Cookie;
import ratpack.handling.Context;
import ratpack.http.Request;
import ratpack.registry.NotInRegistryException;

/**
 * Utility methods for processing incoming http requests.
 * 
 * @author jkirton
 */
public class RequestUtil {
  
  /**
   * Get the configured server domain name (aka public address) underwhich the
   * system is running.
   * 
   * @param ctx
   *          the request context
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
   * @param ctx
   *          the ratpack api {@link Context} instace encapsulating the inbound
   *          http request from which a request 'snapshot' is generated.
   * @return Never-null {@link RequestSnapshot} instance.
   */
  public static RequestSnapshot getOrCreateRequestSnapshot(final Context ctx) {
    try {
      return ctx.getRequest().get(RequestSnapshot.class);
    }
    catch(NotInRegistryException e) {
      final RequestSnapshot rs = takeRequestSnapshot(ctx.getRequest());
      ctx.getRequest().add(rs);
      glog().debug("Request snapshot taken and cached in request.\n{}\n", rs);
      return rs;
    }
  }
  
  /**
   * Add the next request sync token tracking cookie to the response in the given
   * request context.
   * 
   * @param ctx the request context object
   * @param rst the rst value to use in the response cookie
   * @param maxAge the cookie max age in seconds
   */
  public static void addRstCookieToResponse(final Context ctx, final String rst, final long maxAge) {
    final boolean secure = ctx.get(MCorpusServerConfig.class).cookieSecure;
    final Cookie rstCookieRef = ctx.getResponse().cookie("rst", rst);
    rstCookieRef.setSecure(secure);
    rstCookieRef.setHttpOnly(true);
    rstCookieRef.setDomain(getServerDomainName(ctx));
    rstCookieRef.setPath("/");
    rstCookieRef.setMaxAge(maxAge);
  }
  
  /**
   * Add a JWT cookie to the response in the given request context.
   * 
   * @param ctx the request context object
   * @param jwt the JWT cookie value
   * @param maxAge the cookie max age in seconds
   */
  public static void addJwtCookieToResponse(final Context ctx, final String jwt, final long maxAge) {
    final boolean secure = ctx.get(MCorpusServerConfig.class).cookieSecure;
    final Cookie jwtCookieRef = ctx.getResponse().cookie("jwt", jwt);
    jwtCookieRef.setDomain(getServerDomainName(ctx));
    jwtCookieRef.setMaxAge(maxAge);
    jwtCookieRef.setHttpOnly(true); // HTTP ONLY please!
    jwtCookieRef.setSecure(secure);
    jwtCookieRef.setPath("/");
  }
  
  /**
   * Add cookies for rst and jwt that are expired to the response to force
   * the client to expire them.
   * 
   * @param ctx the incoming request context
   */
  public static void expireAllCookies(final Context ctx) {
    final String cookieServerName = getServerDomainName(ctx);
    // rst cookie
    final Cookie rstCookieRef = ctx.getResponse().expireCookie("rst");
    rstCookieRef.setPath("/");
    rstCookieRef.setDomain(cookieServerName);
    // jwt cookie
    final Cookie jwtCookieRef = ctx.getResponse().expireCookie("jwt");
    jwtCookieRef.setPath("/");
    jwtCookieRef.setDomain(cookieServerName);
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
        req.getHeaders().get("rst")
    );
  }
}
