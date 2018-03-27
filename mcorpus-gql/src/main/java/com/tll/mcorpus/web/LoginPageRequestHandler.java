package com.tll.mcorpus.web;

import static com.tll.mcorpus.web.RequestUtil.addRstCookieToResponse;
import static com.tll.mcorpus.web.RequestUtil.addSidCookieToResponse;
import static com.tll.mcorpus.web.RequestUtil.getOrCreateWebSession;
import static com.tll.mcorpus.web.WebFileRenderer.html;
import static com.tll.mcorpus.web.WebSessionManager.webSessionDuration;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.web.JWT.JWTStatusInstance;
import com.tll.mcorpus.web.WebSessionManager.WebSession;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Handles requests to view the login page.
 * <p>
 * We are constrained by:
 * <ul>
 * <li>Initiate a server-side session by way of a user session cookie if one
 * does not currently exist.
 * <p>
 * This is required to properly enforce request sync token exchange for the
 * mcorpus mcuser login routine.</li>
 * <li>If a valid and non-expired mcorpus JWT cookie is present then show logged
 * in summary page.</li>
 * </ul>
 * 
 * @author jkirton
 */
public class LoginPageRequestHandler implements Handler {

  private static final Logger log = LoggerFactory.getLogger(LoginPageRequestHandler.class);
  
  @Override
  public void handle(Context ctx) throws Exception {
    final JWTStatusInstance jwtStatusInst = ctx.getRequest().get(JWTStatusInstance.class);
    
    switch(jwtStatusInst.status()) {
    default:
    case BAD_TOKEN:
    case BAD_SIGNATURE:
    case BAD_CLAIMS:
      ctx.clientError(403); // forbidden
      return;
      
    case NOT_PRESENT:
    case EXPIRED:
      // login allowed
      break;
      
    case BLOCKED:
      ctx.clientError(401); // unauthorized
      return;
    
    case ERROR:
      ctx.clientError(500); // server error
      return;
      
    case VALID:
      // show logged in summary page
      final Map<String, Object> model = new HashMap<>(3);
      model.put("mcuserId", jwtStatusInst.mcuserId());
      model.put("loggedInSince", jwtStatusInst.issued());
      model.put("expires", jwtStatusInst.expires());
      ctx.render(html("loggedIn.html", model, true));
      return;
    }
    
    // At this point, we either have no JWT or an expired JWT
    // so we proceed to view the login page.
    
    // Now we add a request sync token (rst) to the response 
    // and we track it on the server side with a web session
    // that will cache the generated rst for subsequent
    // verification on login submit.
    // Note the user is subject to the server prescribed session timeout meaning 
    // if the user submits after the session times out on the server, 
    // then a bad session error response is returned

    final WebSession webSession;
    try {
      webSession = getOrCreateWebSession(ctx);
    } catch(Exception e) {
      // bad session id
      ctx.clientError(400);
      return;
    }
    
    final String nextRst = webSession.resetRst().toString();
    
    // IMPT: add secure and safe cookies for both:
    // 1) the current web session id (sid)
    // 2) freshly generated and strong rst
    addSidCookieToResponse(ctx, webSession.sid());
    addRstCookieToResponse(ctx, nextRst, (int) webSessionDuration.getSeconds());
    
    // render the login form
    final Map<String, Object> model = new HashMap<>(2);
    model.put("rst", nextRst);
    model.put("statusMsg", ""); // TODO need better templating
    ctx.render(html("loginForm.html", model, false));
    
    log.info("View login page request handled with next rst: '{}'.", nextRst);
  }

}
