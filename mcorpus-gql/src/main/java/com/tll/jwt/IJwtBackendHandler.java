package com.tll.jwt;

import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
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
   * given its jwt id and bound user id.
   *
   * @param jwtId id of the target JWT
   * @param jwtUserId id of the bound jwt user
   * @return Fetch result holding the backend JWT status
   */
  FetchResult<JwtBackendStatus> getBackendJwtStatus(UUID jwtId, UUID jwtUserId);

  /**
   * Get the jwt user info for a given user.
   *
   * @param jwtUserId the user id
   * @return Fetch result holding the jwt user info.
   */
  FetchResult<IJwtUser> getJwtUserInfo(UUID jwtUserId);

  /**
   * Get a list of JWT info objects for each active JWTs in play for a known jwt user.
   *
   * @param jwtUserId the id of a known user - someone who logged successfully
   *                  and was issued a JWT at least once sometime in the recent past
   * @return Fetch result holding the the list of currently active JWTs for the
   *         given user id.
   */
  FetchResult<List<IJwtInfo>> getActiveJwtLogins(UUID jwtUserId);

  /**
   * Do a JWT login in the backend system.
   *
   * @param username the jwt user username
   * @param pswd the jwt user password
   * @param pendingJwtId the generated jwt id that will be valid upon successful backend login
   * @param requestOrigin the request origin ip address gotten from the sourcing http request
   * @param requestInstant the instant the sourcing http request hit the server
   * @param jwtExpiration the instant the pending JWT is set to expire
   * @return Fetch result holding the JWT user entity object.
   */
  FetchResult<IJwtUser> jwtBackendLogin(String username, String pswd, UUID pendingJwtId, InetAddress requestOrigin, Instant requestInstant, Instant jwtExpiration);

  /**
   * Do a JWT logout in the backend system.
   *
   * @param jwtUserid
   * @param jwtId
   * @param requestOrigin the request origin ip address gotten from the sourcing http request
   * @param requestInstant the instant the sourcing http request hit the server
   * @return Fetch result holding logout result (true/false).
   */
  FetchResult<Boolean> jwtBackendLogout(UUID jwtUserId, UUID jwtId, InetAddress requestOrigin, Instant requestInstant);

  /**
   * Invalidate all active JWTs for a known user in the backend system.
   *
   * @param jwtUserId id of the target JWT user
   * @param requestOrigin the request origin ip address gotten from the sourcing http request
   * @param requestInstant the instant the sourcing http request hit the server
   * @return Fetch result holding backend invalidation op result (true/false).
   */
  FetchResult<Boolean> jwtInvalidateAllForUser(UUID jwtUserId, InetAddress requestOrigin, Instant requestInstant);
}