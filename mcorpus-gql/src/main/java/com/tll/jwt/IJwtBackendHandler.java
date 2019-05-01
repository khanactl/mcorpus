package com.tll.jwt;

import java.util.UUID;

import com.tll.repo.FetchResult;

/**
 * Contract for obtaining the status of a JWT from a backend repository or datastore.
 * 
 * @author jkirton
 */
public interface IJwtBackendHandler {

  public static enum JwtBackendStatus {
    /**
     * Present in backend but in a bad state.
     */
    PRESENT_BAD_STATE,
    /**
     * No JWT is present of provided jwt id.
     */
    NOT_PRESENT,
    /**
     * JWT is blacklisted in backend.
     */
    BLACKLISTED,
    /**
     * JWT is expired in backend.
     */
    EXPIRED,
    /**
     * The associated user is inactive or invlaid.
     */
    BAD_USER,
    /**
     * The JWT is valid.
     */
    VALID,
    /**
     * When an error happens while checking the backend jwt status.
     */
    ERROR
    ;
  }

  /**
   * Get the status of a known JWT from a remotely held datastore (the backend) 
   * given its jwt id.
   * 
   * @param jwtId id of the target JWT
   * @return Fetch result holding the backend JWT status
   */
  FetchResult<JwtBackendStatus> getBackendJwtStatus(UUID jwtId);

  /**
   * Do a JWT login in the backend system.
   * 
   * @param username
   * @param pswd
   * @param pendingJwtId
   * @param clientOriginToken
   * @param requestInstantMillis
   * @param jwtExpirationMillis
   * @return Fetch result holding the JWT user entity object.
   */
  FetchResult<IJwtUser> jwtBackendLogin(String username, String pswd, UUID pendingJwtId, String clientOriginToken, long requestInstantMillis, long jwtExpirationMillis);

  /**
   * Do a JWT logout in the backend system.
   * 
   * @param jwtUserid
   * @param jwtId
   * @param clientOriginToken
   * @param requestInstantMillis
   * @return Fetch result holding logout result (true/false).
   */
  FetchResult<Boolean> jwtBackendLogout(UUID jwtUserId, UUID jwtId, String clientOriginToken, long requestInstantMillis);

  /**
   * Get the number of active JWTs in plan for a given jwt user.
   * 
   * @param jwtUserId
   * @return Fetch result holding the the number of active JWT logins.
   */
  FetchResult<Integer> getNumActiveJwtLogins(UUID jwtUserId);
}