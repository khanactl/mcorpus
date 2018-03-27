package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.isBlank;
import static com.tll.mcorpus.web.RequestUtil.getServerDomainName;
import static com.tll.mcorpus.web.WebFileRenderer.html;

import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.repo.MCorpusUserRepoAsync;
import com.tll.mcorpus.web.CsrfGuardByWebSessionAndPostHandler.NextRst;

import io.netty.handler.codec.http.cookie.Cookie;
import ratpack.form.Form;
import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Handles mcorpus mcuser login [submit] requests.
 * <p>
 * Expected mcuser login input:
 * <ul>
 * <li>rst</li>
 * <li>username</li>
 * <li>pswd</li>
 * </ul>
 * <p>
 * If successful, a JWT is generated and added as a secure, 
 * http-only cookie named 'jwt' to the http response.
 * 
 * @author jkirton
 */
public class LoginRequestHandler implements Handler {
  
  private static final Logger log = LoggerFactory.getLogger(LoginRequestHandler.class);
  
  private static void rerenderLoginPage(final Context ctx, final String nextRst, final String errorMsg) {
    // re-display login page with error message
    final Map<String, Object> model = new HashMap<>(2);
    model.put("rst", nextRst);
    model.put("statusMsg", errorMsg);
    ctx.render(html("loginForm.html", model, false));
  }

  @Override
  public void handle(Context ctx) {
    final RequestSnapshot requestSnapshot = ctx.getRequest().get(RequestSnapshot.class);
    final URL clientOrigin;
    try {
      clientOrigin = requestSnapshot.getClientOrigin();
    }
    catch(Exception e) {
      log.error("Bad client origin: {}", e.getMessage());
      ctx.clientError(403);
      return;
    }

    final Form formData = ctx.getRequest().get(Form.class);
    final String username = formData.get("username");
    final String pswd = formData.get("password");
    final UUID nextRst = ctx.getRequest().get(NextRst.class).nextRst;
    
    // validate login input
    if(isBlank(username) || isBlank(pswd)) {
      rerenderLoginPage(ctx, nextRst.toString(), "Invalid login credentials.");
      return;
    }
    
    // java uuids provide sufficient cryptographic strength and seem quite suited for this task
    // jwtID: stored on the backend to serve as a means to deny access at the db-level
    final JWT jwtbiz = ctx.get(JWT.class);
    final UUID pendingJwtID = UUID.randomUUID();
    final long requestInstantMillis = requestSnapshot.getRequestInstant().toEpochMilli();
    final long jwtExpiresMillis = requestInstantMillis + jwtbiz.jwtCookieTtlInMillis();
    
    final McuserLogin mcuserLogin = new McuserLogin();
    mcuserLogin.setMcuserUsername(username);
    mcuserLogin.setMcuserPassword(pswd);
    mcuserLogin.setInJwtId(pendingJwtID);
    mcuserLogin.setInLoginExpiration(new Timestamp(requestInstantMillis));
    mcuserLogin.setInRequestOrigin(clientOrigin.toString());
    mcuserLogin.setInRequestTimestamp(new Timestamp(jwtExpiresMillis));
    
    // call db login
    log.debug("Authenticating: mcuser..");
    ctx.get(MCorpusUserRepoAsync.class).loginAsync(mcuserLogin).then(loginResult -> {
      if(loginResult == null || loginResult.hasErrorMsg()) {
        // login failed
        rerenderLoginPage(ctx, nextRst.toString(), loginResult.getErrorMsg());
        return;
      }
      final Mcuser mcuser = loginResult.get();
      if(mcuser == null) throw new IllegalStateException("No mcuser ref returned upon login.");

      // at this point, we're authenticated
      
      // create the JWT - and set as a cookie to go back to user
      // the user is now expected to provide this JWT for subsequent mcorpus api requests
      final String jwt = jwtbiz.generate(
          requestSnapshot.getRequestInstant(), 
          mcuser.getUid(), 
          pendingJwtID,
          requestSnapshot.getClientOrigin());
      
      // jwt cookie
      final Cookie jwtCookieRef = ctx.getResponse().cookie("jwt", jwt);
      jwtCookieRef.setDomain(getServerDomainName(ctx));
      jwtCookieRef.setMaxAge(jwtbiz.jwtCookieTtlInSeconds()); // cookie max age is in seconds
      jwtCookieRef.setHttpOnly(true); // HTTP ONLY please!
      jwtCookieRef.setSecure(true); // secure
      jwtCookieRef.setPath("/");
      log.info("JWT mcorpus cookie generated: {}", jwtCookieRef.toString());

      log.info("Mcuser '{}' logged in and JWT generated.", mcuser.getUid());
      
      // finally, redirect to the home page
      ctx.redirect(301, "index");
    });
  }
}