package com.tll.mcorpus.web;

import static com.tll.mcorpus.web.RequestUtil.expireAllCookies;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.mcorpus.web.JWT.JWTStatusInstance;

import ratpack.exec.Blocking;
import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Handles mcuser logout requests.
 * <p>
 * Logging out is only valid when a valid JWT is provided in the request.
 * <p>
 * Upon successful logout, the JWT cookie is expired on the response as well as
 * any session id cookie.
 * 
 * @author jkirton
 */
public class LogoutRequestHandler implements Handler {

  private final Logger log = LoggerFactory.getLogger(LogoutRequestHandler.class);

  @Override
  public void handle(Context ctx) throws Exception {
    // IMPT: we assume to have a valid JWT present in request by way of upstream handlers
    
    // call db-level logout routine to invalidate the jwt id 
    // and capture the logout event as an mcuser audit record
    final JWTStatusInstance jwtStatus = ctx.getRequest().get(JWTStatusInstance.class);
    final RequestSnapshot requestSnapshot = ctx.getRequest().get(RequestSnapshot.class);
    final McuserLogout mcuserLogout = new McuserLogout();
    mcuserLogout.setMcuserUid(jwtStatus.mcuserId());
    mcuserLogout.setJwtId(jwtStatus.jwtId());
    mcuserLogout.setRequestTimestamp(new Timestamp(requestSnapshot.getRequestInstant().toEpochMilli()));
    mcuserLogout.setRequestOrigin(requestSnapshot.getClientOrigin());
    Blocking.get(() -> {
      return ctx.get(MCorpusUserRepo.class).logout(mcuserLogout);
    }).then(fetchResult -> {
      if(fetchResult.isSuccess()) {
        // logout success - nix all mcorpus cookies clientside
        expireAllCookies(ctx);
        log.info("mcuser '{}' logged out.", jwtStatus.mcuserId().toString());
        ctx.redirect("index"); // go back to main page now
      }
      else {
        // logout failed
        log.error("mcuser '{}' logout failed.", jwtStatus.mcuserId().toString());
        ctx.clientError(403); // forbidden
      }
    });
  }
}
