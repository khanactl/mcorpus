package com.tll.mcorpus.web;

import static com.tll.mcorpus.Main.glog;

import java.util.UUID;

import com.tll.mcorpus.MCorpusServerConfig;
import com.tll.web.RequestSnapshot;

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
   * Get the configured server domain name under which the app is running.
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
      glog().info("Request snapshot taken: {}", rs);
      return rs;
    }
  }

  /**
   * Add the next request sync token cookie named "rst"
   * to the response in the given request context.
   *
   * @param ctx the request context object
   * @param rst the rst value to use in the response cookie
   * @param path the path to use in the set cookie
   * @param maxAge the cookie max age in seconds
   */
  public static void setRstCookie(final Context ctx, final String rst, final String path, final long maxAge) {
    setCookie(ctx, "rst", rst, path, maxAge);
    glog().debug("RST cookie set (path: {}, maxAge: {}).", path, maxAge);
  }

  /**
   * Add a cookie named "jwt" to the response in the
   * given request context.
   *
   * @param ctx the request context object
   * @param jwt the JWT cookie value
   * @param path the path to use in the set cookie
   * @param maxAge the cookie max age in seconds
   */
  public static void setJwtCookie(final Context ctx, final String jwt, final String path, final long maxAge) {
    setCookie(ctx, "jwt", jwt, path, maxAge);
    glog().debug("JWT cookie set (path: {}, maxAge: {}).", path, maxAge);
  }

  public static void expireRstCookie(final Context ctx, final String path) {
    setCookie(ctx, "rst", "", path, 0);
    glog().debug("RST cookie expired at path {}.", path);
  }

  public static void expireJwtCookie(final Context ctx, final String path) {
    setCookie(ctx, "jwt", "", path, 0);
    glog().debug("JWT cookie expired at path {}.", path);
  }

  private static void setCookie(
    final Context ctx,
    final String cookieName,
    final String cookieValue,
    final String path,
    final long maxAge) {
      ctx.getResponse().getHeaders().add(
        "Set-Cookie",
        String.format("%s=%s; Domain=%s; Path=%s; Max-Age=%d; %sHttpOnly; SameSite=Strict;",
          cookieName,
          cookieValue,
          getServerDomainName(ctx),
          path,
          maxAge,
          ctx.get(MCorpusServerConfig.class).cookieSecure ? "Secure; " : ""
        )
      );
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
        req.getPath(),
        req.getMethod().getName(),
        req.getHeaders().get("Host"),
        req.getHeaders().get("Origin"),
        req.getHeaders().get("Referer"),
        req.getHeaders().get("Forwarded"),
        req.getHeaders().get("X-Forwarded-For"),
        req.getHeaders().get("X-Forwarded-Host"),
        req.getHeaders().get("X-Forwarded-Proto"),
        req.oneCookie("jwt"),
        req.oneCookie("rst"),
        req.getHeaders().get("rst"),
        req.maybeGet(RequestId.class).orElse(RequestId.of(UUID.randomUUID().toString())).toString()
    );
  }
}
