package com.tll.mcorpus.web;

import static com.tll.core.Util.isBlank;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.not;
import static com.tll.mcorpus.web.RequestUtil.addJwtCookieToResponse;
import static com.tll.mcorpus.web.RequestUtil.expireAllCookies;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tll.jwt.JWT;
import com.tll.jwt.JWTStatusInstance;
import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.gmodel.mcuser.Mcstatus;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.repo.FetchResult;
import com.tll.web.GraphQLWebContext;
import com.tll.web.RequestSnapshot;

import ratpack.handling.Context;

/**
 * Immutable encapsulation of a GraphQL query request for use in the app web
 * layer.
 * <p>
 * This is the GraphQL context object for http requests.
 * 
 * @author jkirton
 */
public class MCorpusGraphQLWebContext extends GraphQLWebContext {

  /**
   * The Ratpack http/web context object.
   */
  private final Context ctx;

  /**
   * Constructor.
   *
   * @param query           the GraphQL query string
   * @param vmap            optional query variables expressed as a name/value map
   * @param requestSnapshot snapshot of the sourcing http request
   * @param jwtStatus       the status of the JWT of the sourcing http request
   * @param ctx             the Ratpack request handling context
   */
  public MCorpusGraphQLWebContext(String query, Map<String, Object> vmap, RequestSnapshot requestSnapshot, JWTStatusInstance jwtStatus, Context ctx) {
    super(query, vmap, requestSnapshot, jwtStatus);
    this.ctx = ctx;
  }
  
  /**
   * Is this a valid GraphQL query ready to be handed off to further processing?
   * 
   * @return true/false
   */
  @Override
  public boolean isValid() { return super.isValid() && isNotNull(ctx); }
  
  /**
   * @return true when the GraphQL query is for mcuser login, false otherwise.
   */
  public boolean isMcuserLoginQuery() {
    return "mclogin".equals(getQueryMethodName());
  }

  /**
   * @return true when this GraphQL query is either an mcuser login mutation query
   *         or an introspection query, false otherwise.
   */
  public boolean isMcuserLoginOrIntrospectionQuery() { 
    return isMcuserLoginQuery() || isIntrospectionQuery();
  }

  /**
   * Get the mcuser login status.
   * <p>
   * Blocking - Db call is issued.
   * 
   * @return Newly created {@link Mcstatus} when an mcuser presents a valid and
   *         non-expired JWT -OR-<br>
   *         null when no JWT is present or is not valid.
   */
  public Mcstatus mcstatus() {
    final JWTStatusInstance jwtStatusInst = getJwtStatus();
    final UUID jwtId = jwtStatusInst.jwtId();
    if(jwtStatusInst.status().isValid()) {
      final UUID mcuserId = jwtStatusInst.userId();
      FetchResult<Integer> fetchResult = ctx.get(MCorpusUserRepo.class).getNumActiveLogins(mcuserId);
      if(fetchResult.isSuccess()) {
        // success
        final Date since = jwtStatusInst.issued();
        final Date expires = jwtStatusInst.expires();
        final int numActiveJWTs = fetchResult.get().intValue();
        return new Mcstatus(mcuserId, since, expires, numActiveJWTs);
      } else {
        final String emsg = fetchResult.getErrorMsg();
        log.error("Invalid mcuser login status fetch result for JWT of id: {}: {}", jwtId, emsg);
      }
    } else {
      log.warn("Invalid JWT ({}) presented for mcuser status.", jwtId);
    }

    // default
    return null;
  }

  /**
   * Log an mcuser in and issue a JWT back to client when the login was successful.
   * <p>
   * Blocking - Db call is issued.
   * 
   * @param username the posted mcuser username
   * @param pswd the posted mcuser passwrod
   * @return true when the mcuser was successfully logged in, false otherwise
   */
  public boolean mcuserLogin(final String username, final String pswd) {
    
    // verify the JWT status is either not present or expired
    if(not(jwtStatus.isJWTStatusExpiredOrNotPresent())) {
      return false;
    }

    // validate login input
    if(isBlank(username) || isBlank(pswd)) {
      return false;
    }
    
    final JWT jwtbiz = ctx.get(JWT.class);
    
    final UUID pendingJwtID = UUID.randomUUID();
    final long requestInstantMillis = requestSnapshot.getRequestInstant().toEpochMilli();
    final long loginExpiration = requestInstantMillis + jwtbiz.jwtCookieTtlInMillis();    

    final McuserLogin mcuserLogin = new McuserLogin();
    mcuserLogin.setMcuserUsername(username);
    mcuserLogin.setMcuserPassword(pswd);
    mcuserLogin.setInJwtId(pendingJwtID);
    mcuserLogin.setInLoginExpiration(new Timestamp(loginExpiration));
    mcuserLogin.setInRequestOrigin(requestSnapshot.getClientOrigin());
    mcuserLogin.setInRequestTimestamp(new Timestamp(requestInstantMillis));

    // call db login
    log.debug("Authenticating mcuser '{}'..", username);
    final FetchResult<Mcuser> loginResult = ctx.get(MCorpusUserRepo.class).login(mcuserLogin);
    if(not(loginResult.isSuccess())) {
      log.error("Mcuser login failed: {}", loginResult.getErrorMsg());
      return false;
    }
    log.info("Mcuser '{}' authenticated.", username);
    // at this point, we're authenticated
    
    log.debug("Generating JWT for mcuser '{}'..", username);
    final Mcuser mcuser = loginResult.get();
    try {
      // create the JWT - and set as a cookie to go back to user
      // the user is now expected to provide this JWT for subsequent mcorpus api requests
      final String jwt = jwtbiz.generate(
          pendingJwtID,
          mcuser.getUid(), 
          isNullOrEmpty(mcuser.getRoles()) ? "" : 
            Arrays.stream(mcuser.getRoles())
            .map(role -> { return role.getLiteral(); })
            .collect(Collectors.joining(",")),
          requestSnapshot
      );
      
      // jwt cookie
      addJwtCookieToResponse(ctx, jwt, jwtbiz.jwtCookieTtlInSeconds());
      
      log.info("Mcuser {} logged in.  JWT {} generated.", mcuser.getUid(), pendingJwtID);
      return true;
    }
    catch(Exception e) {
      log.error("Mcuser {} login error: {}", mcuser.getUid(), e.getMessage());
    }
    
    // default
    return false;
  }

  /**
   * Log an mcuser out.
   * <p>
   * Blocking - Db call is issued.
   * 
   * @return true when the mcuser bound to the presenting JWT in incoming request
   *         is successfully logged out, false otherwise.
   */
  public boolean mcuserLogout() {
    final McuserLogout mcuserLogout = new McuserLogout();
    mcuserLogout.setMcuserUid(jwtStatus.userId());
    mcuserLogout.setJwtId(jwtStatus.jwtId());
    mcuserLogout.setRequestTimestamp(new Timestamp(requestSnapshot.getRequestInstant().toEpochMilli()));
    mcuserLogout.setRequestOrigin(requestSnapshot.getClientOrigin());
    FetchResult<Boolean> fetchResult = ctx.get(MCorpusUserRepo.class).logout(mcuserLogout);
    if(fetchResult.isSuccess()) {
      // logout success - nix all mcorpus cookies clientside
      expireAllCookies(ctx);
      log.info("mcuser '{}' logged out.", jwtStatus.userId());
      return true;
    }

    // default - logout failed
    log.error("mcuser '{}' logout failed.", jwtStatus.userId());
    return false;
  }
}
