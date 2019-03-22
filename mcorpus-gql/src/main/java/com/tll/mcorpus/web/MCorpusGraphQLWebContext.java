package com.tll.mcorpus.web;

import static com.tll.core.Util.isBlank;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.not;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tll.jwt.IJwtBackendHandler;
import com.tll.jwt.IJwtHttpResponseProvider;
import com.tll.jwt.IJwtUser;
import com.tll.jwt.JWT;
import com.tll.jwt.JWTHttpRequestStatus;
import com.tll.mcorpus.gmodel.mcuser.Mcstatus;
import com.tll.repo.FetchResult;
import com.tll.web.GraphQLWebContext;
import com.tll.web.RequestSnapshot;

/**
 * MCorpus specific extension of {@link GraphQLWebContext} to facilitate 
 * JWT user login, logout and status methods.
 * 
 * @author jpk
 */
public class MCorpusGraphQLWebContext extends GraphQLWebContext {

  private final JWT jwtbiz;
  private final IJwtBackendHandler jwtBackend;
  private final IJwtHttpResponseProvider jwtResponse;

  /**
   * Constructor.
   *
   * @param query           the GraphQL query string
   * @param vmap            optional query variables expressed as a name/value map
   * @param requestSnapshot snapshot of the sourcing http request
   * @param jwtStatus       the status of the JWT of the sourcing http request
   * @param jwtbiz          the JWT manager
   * @param jwtBackend      the JWT backend handler
   * @param jwtResponse     the JWT http response access object
   */
  public MCorpusGraphQLWebContext(
    String query, 
    Map<String, Object> vmap, 
    RequestSnapshot requestSnapshot, 
    JWTHttpRequestStatus jwtStatus, 
    JWT jwtbiz, 
    IJwtBackendHandler jwtBackend, 
    IJwtHttpResponseProvider jwtResponse) {
    super(query, vmap, requestSnapshot, jwtStatus);
    this.jwtbiz = jwtbiz;
    this.jwtBackend = jwtBackend;
    this.jwtResponse = jwtResponse;
  }
  
  /**
   * Is this a valid GraphQL query ready to be handed off to further processing?
   * 
   * @return true/false
   */
  @Override
  public boolean isValid() { 
    return super.isValid() && 
    isNotNull(jwtbiz) && 
    isNotNull(jwtResponse)
    ;
  }
  
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
    final JWTHttpRequestStatus jwtRequestStatus = getJwtStatus();
    final UUID jwtId = jwtRequestStatus.jwtId();
    if(jwtRequestStatus.status().isValid()) {
      final UUID mcuserId = jwtRequestStatus.userId();
      FetchResult<Integer> fetchResult = jwtBackend.getNumActiveJwtLogins(mcuserId);
      if(fetchResult.isSuccess()) {
        // success
        final Date since = jwtRequestStatus.issued();
        final Date expires = jwtRequestStatus.expires();
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
    
    final UUID pendingJwtID = UUID.randomUUID();
    final String clientOriginToken = requestSnapshot.getClientOrigin();
    final long requestInstantMillis = requestSnapshot.getRequestInstant().toEpochMilli();
    final long loginExpiration = requestInstantMillis + jwtbiz.jwtCookieTtlInMillis();

    // call db login
    log.debug("Authenticating mcuser '{}'..", username);
    final FetchResult<IJwtUser> loginResult = jwtBackend.jwtBackendLogin(
      username, 
      pswd, 
      pendingJwtID, 
      clientOriginToken, 
      requestInstantMillis, 
      loginExpiration
    );
    if(not(loginResult.isSuccess())) {
      log.error("Mcuser login failed: {}", loginResult.getErrorMsg());
      return false;
    }
    log.info("Mcuser '{}' authenticated.", username);
    // at this point, we're authenticated
    
    log.debug("Generating JWT for mcuser '{}'..", username);
    final IJwtUser mcuser = loginResult.get();
    try {
      // create the JWT - and set as a cookie to go back to user
      // the user is now expected to provide this JWT for subsequent mcorpus api requests
      final String jwt = jwtbiz.jwtGenerate(
          pendingJwtID,
          mcuser.getJwtUserId(), 
          isNullOrEmpty(mcuser.getJwtUserRoles()) ? "" : 
            Arrays.stream(mcuser.getJwtUserRoles())
            .collect(Collectors.joining(",")),
          requestSnapshot
      );
      
      // jwt cookie
      jwtResponse.setJwtCookie(jwt, jwtbiz.jwtCookieTtlInSeconds());
      
      log.info("Mcuser {} logged in.  JWT {} generated.", mcuser.getJwtUserId(), pendingJwtID);
      return true;
    }
    catch(Exception e) {
      log.error("Mcuser {} login error: {}", mcuser.getJwtUserId(), e.getMessage());
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
    final FetchResult<Boolean> fetchResult = jwtBackend.jwtBackendLogout(
      jwtStatus.userId(), 
      jwtStatus.jwtId(), 
      requestSnapshot.getClientOrigin(), 
      requestSnapshot.getRequestInstant().toEpochMilli()
    );
    if(fetchResult.isSuccess()) {
      // logout success - nix all mcorpus cookies clientside
      jwtResponse.expireAllCookies();
      log.info("mcuser '{}' logged out.", jwtStatus.userId());
      return true;
    }

    // default - logout failed
    log.error("mcuser '{}' logout failed.", jwtStatus.userId());
    return false;
  }
}
