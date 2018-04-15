package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.glog;
import static com.tll.mcorpus.Util.isNull;
import static com.tll.mcorpus.web.WebSessionManager.webSessionDuration;
import static com.tll.mcorpus.web.WebSessionManager.wsi;

import java.util.UUID;

import com.tll.mcorpus.web.WebSessionManager.WebSession;
import com.tll.mcorpus.web.WebSessionManager.WebSessionInstance;
import com.tll.mcorpus.web.WebSessionManager.WebSessionStatus;

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
   * Get or create a web session to associate with or identify from the given
   * request context object.
   * 
   * @param ctx
   *          the web request context object
   * @return either the cached or newly created {@link WebSession} bound to the
   *         given request context.
   * @throws Exception
   *           When a sid (session id) cookie is present but malformed.
   */
  public static WebSession getOrCreateWebSession(final Context ctx) throws Exception {
    try {
      return ctx.getRequest().get(WebSession.class);
    } 
    catch (NotInRegistryException e) {
      glog().debug("No web session found. Creating one..");
      final UUID sid;
      final RequestSnapshot requestSnapshot = getOrCreateRequestSnapshot(ctx);
      if (requestSnapshot.hasSidCookie()) {
        // verify the session identifier
        try {
          sid = UUID.fromString(requestSnapshot.getSidCookie());
        } catch (Exception ex) {
          glog().error("Invalid session cookie: {}", requestSnapshot.getSidCookie());
          throw new Exception("Invalid session cookie.");
        }
      } 
      else {
        // no session cookie
        sid = UUID.randomUUID();
      }

      final WebSession webSession = WebSessionManager.getSession(sid, nsid -> {
        final WebSession ws = new WebSession(nsid, requestSnapshot);
        glog().info("Web session created: {}", ws);
        return ws;
      });

      return webSession;
    }
  }
  
  /**
   * This method determines the web session status of the given request
   * context.
   * <p>
   * The request is interrogated to determine if
   * there is a valid server side reference to a web session.
   * 
   * @param ctx
   *          the request context
   * @return Never-null, newly created {@link WebSessionInstance} that holds a
   *         never-null status and possibly null {@link WebSession} depending on
   *         the status.
   */
  public static WebSessionInstance webSessionStatus(final Context ctx) {
    WebSessionStatus status = null;
    WebSession webSession = null;
    try {
      webSession = ctx.getRequest().get(WebSession.class);
    } 
    catch (NotInRegistryException e) {
      final RequestSnapshot requestSnapshot = getOrCreateRequestSnapshot(ctx);
      if (requestSnapshot.hasSidCookie()) {
        try {
          webSession = WebSessionManager.getSession(UUID.fromString(requestSnapshot.getSidCookie()));
        } catch (Exception ex) {
          glog().error("Invalid session cookie: {}", requestSnapshot.getSidCookie());
          status = WebSessionStatus.BAD_SESSION_ID;
        }
      }
      else 
        status = WebSessionStatus.NO_SESSION_ID;
    }
    
    if(isNull(status)) 
      status = isNull(webSession) ? 
        WebSessionStatus.SESION_ID_EXPIRED : WebSessionStatus.VALID_SESSION_ID;
    
    // cache web session status in the current request instance for downstream handlers
    final WebSessionInstance wsi = wsi(status, webSession);
    ctx.getRequest().add(wsi);
    glog().info("Web session status '{}' cached in request.", status);
    return wsi;
  }
  
  /**
   * Add a web session tracking cookie to the response in the given request
   * context.
   * <p>
   * NOTE: The cookie max age is set to the pre-set web session duration.
   * 
   * @param ctx the request context object
   * @param sid the current session id
   * @throws Exception when trying to resolve the web session
   */
  public static void addSidCookieToResponse(final Context ctx, final UUID sid) throws Exception {
    final Cookie sidCookieRef = ctx.getResponse().cookie("sid", sid.toString());
    sidCookieRef.setSecure(true);
    sidCookieRef.setHttpOnly(true);
    sidCookieRef.setDomain(getServerDomainName(ctx));
    sidCookieRef.setPath("/");
    sidCookieRef.setMaxAge(webSessionDuration.getSeconds());
  }
  
  /**
   * Clear the session id (sid) cookie from the pending http response.
   * 
   * @param ctx the request context object
   */
  public static void clearSidCookie(final Context ctx) {
    final Cookie sidCookieRef = ctx.getResponse().expireCookie("sid");
    sidCookieRef.setPath("/");
    sidCookieRef.setDomain(getServerDomainName(ctx));
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
    final Cookie rstCookieRef = ctx.getResponse().cookie("rst", rst);
    rstCookieRef.setSecure(true);
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
    final Cookie jwtCookieRef = ctx.getResponse().cookie("jwt", jwt);
    jwtCookieRef.setDomain(getServerDomainName(ctx));
    jwtCookieRef.setMaxAge(maxAge);
    jwtCookieRef.setHttpOnly(true); // HTTP ONLY please!
    jwtCookieRef.setSecure(true); // secure
    jwtCookieRef.setPath("/");
  }
  
  /**
   * Add cookies for rst, sid and jwt that are expired to the response to force
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
    // sid cookie
    final Cookie sidCookieRef = ctx.getResponse().expireCookie("sid");
    sidCookieRef.setPath("/");
    sidCookieRef.setDomain(cookieServerName);
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
        req.oneCookie("sid"),
        req.oneCookie("rst"),
        req.getHeaders().get("rst")
    );
  }
}
