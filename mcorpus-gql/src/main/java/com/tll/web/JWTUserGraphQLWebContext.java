package com.tll.web;

import static com.tll.core.Util.isBlank;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.isNotNullOrEmpty;
import static com.tll.core.Util.not;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tll.jwt.IJwtBackendHandler;
import com.tll.jwt.IJwtHttpResponseAction;
import com.tll.jwt.IJwtUser;
import com.tll.jwt.IJwtUserStatus;
import com.tll.jwt.JWT;
import com.tll.jwt.JWTHttpRequestStatus;
import com.tll.repo.FetchResult;
import com.tll.web.GraphQLWebContext;
import com.tll.web.RequestSnapshot;

/**
 * JWT specific extension of {@link GraphQLWebContext} to facilitate 
 * JWT user login, logout and status methods.
 * 
 * @author jpk
 */
public class JWTUserGraphQLWebContext extends GraphQLWebContext {

  private static class JWTUserStatus implements IJwtUserStatus {

    private final UUID jwtUserId;
    private final Date since;
    private final Date expires;
    private final int numActiveJWTs;

    public JWTUserStatus(UUID jwtUserId, Date since, Date expires, int numActiveJWTs) {
      this.jwtUserId = jwtUserId;
      this.since = since;
      this.expires = expires;
      this.numActiveJWTs = numActiveJWTs;
    }

    @Override
    public UUID getJwtUserId() { return jwtUserId; }

    @Override
    public Date getSince() { return since; }

    @Override
    public Date getExpires() { return expires; }

    @Override
    public int getNumActiveJWTs() { return numActiveJWTs; }

  }

  private final JWTHttpRequestStatus jwtStatus;
  private final JWT jwtbiz;
  private final IJwtBackendHandler jwtBackend;
  private final IJwtHttpResponseAction jwtResponse;
  private final String jwtUserLoginQueryMethodName;

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
   * @param jwtUserLoginQueryMethodName the GraphQL schema method name purposed for JWT user logins
   */
  public JWTUserGraphQLWebContext(
    String query, 
    Map<String, Object> vmap, 
    RequestSnapshot requestSnapshot, 
    JWTHttpRequestStatus jwtStatus, 
    JWT jwtbiz, 
    IJwtBackendHandler jwtBackend, 
    IJwtHttpResponseAction jwtResponse, 
    String jwtUserLoginQueryMethodName
  ) {
    super(query, vmap, requestSnapshot);
    this.jwtStatus = jwtStatus;
    this.jwtbiz = jwtbiz;
    this.jwtBackend = jwtBackend;
    this.jwtResponse = jwtResponse;
    this.jwtUserLoginQueryMethodName = jwtUserLoginQueryMethodName;
  }
  
  @Override
  public boolean isValid() { 
    return super.isValid() && 
      isNotNull(jwtStatus) && 
      isNotNull(jwtbiz) && 
      isNotNull(jwtBackend) && 
      isNotNull(jwtResponse) && 
      isNotNullOrEmpty(jwtUserLoginQueryMethodName)
    ;
  }
  
  /**
   * @return the JWT status instance of the sourcing http request.
   */
  public JWTHttpRequestStatus getJwtStatus() { return jwtStatus; }

  /**
   * @return true when the GraphQL query is for JWT user login, false otherwise.
   */
  public boolean isJwtUserLoginQuery() {
    return jwtUserLoginQueryMethodName.equals(getQueryMethodName());
  }

  /**
   * @return true when this GraphQL query is either a JWT user login mutation query
   *         or an introspection query, false otherwise.
   */
  public boolean isJwtUserLoginOrIntrospectionQuery() { 
    return isJwtUserLoginQuery() || isIntrospectionQuery();
  }

  /**
   * Get the JWT user login status.
   * <p>
   * Blocking - Db call is issued.
   * 
   * @return Newly created {@link IJwtUserStatus} when the http client presents a valid and
   *         non-expired JWT -OR-<br>
   *         null when no JWT is present or is not valid.
   */
  public IJwtUserStatus jwtUserStatus() {
    final JWTHttpRequestStatus jwtRequestStatus = getJwtStatus();
    final UUID jwtId = jwtRequestStatus.jwtId();
    if(jwtRequestStatus.status().isValid()) {
      final UUID jwtUserId = jwtRequestStatus.userId();
      FetchResult<Integer> fr = jwtBackend.getNumActiveJwtLogins(jwtUserId);
      if(fr.isSuccess()) {
        // success
        final JWTUserStatus jus = new JWTUserStatus(
          jwtUserId, 
          jwtRequestStatus.issued(), 
          jwtRequestStatus.expires(), 
          fr.get().intValue()
        );
        return jus;
      } else {
        final String emsg = fr.getErrorMsg();
        log.error("Invalid JWT user login status fetch result for JWT of id: {}: {}", jwtId, emsg);
      }
    } else {
      log.warn("Invalid JWT ({}) presented for JWT user login status.", jwtId);
    }

    // default
    return null;
  }

  /**
   * Log a JWT user in and issue a JWT back to client when the login op is successful.
   * <p>
   * Blocking - Db call is issued.
   * 
   * @param username the posted JWT user username
   * @param pswd the posted JWT user password
   * @return true when the JWT user was successfully logged in, false otherwise
   */
  public boolean jwtUserLogin(final String username, final String pswd) {
    
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
    log.debug("Authenticating JWT user '{}'..", username);
    final FetchResult<IJwtUser> loginResult = jwtBackend.jwtBackendLogin(
      username, 
      pswd, 
      pendingJwtID, 
      clientOriginToken, 
      requestInstantMillis, 
      loginExpiration
    );
    if(not(loginResult.isSuccess())) {
      log.error("JWT user login failed: {}", loginResult.getErrorMsg());
      return false;
    }
    log.info("JWT user '{}' authenticated.", username);
    // at this point, we're authenticated
    
    log.debug("Generating JWT for user '{}'..", username);
    final IJwtUser jwtUser = loginResult.get();
    try {
      // create the JWT - and set as a cookie to go back to user
      // the user is now expected to provide this JWT for subsequent GraphQL api requests
      final String jwt = jwtbiz.jwtGenerate(
          pendingJwtID,
          jwtUser.getJwtUserId(), 
          isNullOrEmpty(jwtUser.getJwtUserRoles()) ? "" : 
            Arrays.stream(jwtUser.getJwtUserRoles())
            .collect(Collectors.joining(",")),
          requestSnapshot
      );
      
      // jwt cookie
      jwtResponse.setJwtCookie(jwt, jwtbiz.jwtCookieTtlInSeconds());
      
      log.info("JWT user '{}' logged in.  JWT {} generated.", jwtUser.getJwtUserId(), pendingJwtID);
      return true;
    }
    catch(Exception e) {
      log.error("JWT user '{}' login error: {}", jwtUser.getJwtUserId(), e.getMessage());
    }
    
    // default
    return false;
  }

  /**
   * Log a JWT user out.
   * <p>
   * Blocking - Db call is issued.
   * 
   * @return true when the user bound to the presenting JWT in incoming request
   *         is successfully logged out, false otherwise.
   */
  public boolean jwtUserLogout() {
    final FetchResult<Boolean> fetchResult = jwtBackend.jwtBackendLogout(
      jwtStatus.userId(), 
      jwtStatus.jwtId(), 
      requestSnapshot.getClientOrigin(), 
      requestSnapshot.getRequestInstant().toEpochMilli()
    );
    if(fetchResult.isSuccess()) {
      // logout success - nix all cookies clientside
      jwtResponse.expireAllCookies();
      log.info("JWT user '{}' logged out.", jwtStatus.userId());
      return true;
    }

    // default - logout failed
    log.error("JWT user '{}' logout failed.", jwtStatus.userId());
    return false;
  }

  @Override
  public String toString() {
    return String.format("qry: %s, %s", getQueryMethodName(), jwtStatus);
  }
}
