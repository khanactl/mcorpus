package com.tll.jwt;

import static com.tll.core.Util.isNull;
import static com.tll.core.Util.not;
import static com.tll.transform.TransformUtil.uuidToToken;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable struct housing the status of a possibly held JWT
 * in a received http request.
 *
 * @author jpk
 */
public class JWTHttpRequestStatus {

  /**
   * Factory method to generate a {@link JWTHttpRequestStatus} from only a {@link JWTStatus}.
   *
   * @param status the JWT status
   * @return Newly created {@link JWTHttpRequestStatus}
   */
  public static JWTHttpRequestStatus create(JWTStatus status) {
    return new JWTHttpRequestStatus(status, null, null, null, null, false, null, RefreshTokenStatus.UNKNOWN);
  }

  /**
   * Factory method to generate a fully populated {@link JWTHttpRequestStatus}.
   *
   * @param status
   * @param jwtId
   * @param userId
   * @param issued
   * @param expires
   * @param admin
   * @param roles
   * @return Newly created {@link JWTHttpRequestStatus}
   */
  public static JWTHttpRequestStatus create(JWTStatus status, UUID jwtId, UUID userId, Instant issued, Instant expires, boolean admin, String roles) {
    return new JWTHttpRequestStatus(status, jwtId, userId, issued, expires, admin, roles, RefreshTokenStatus.UNKNOWN);
  }

  /**
   * Factory method to generate a fully populated {@link JWTHttpRequestStatus}.
   *
   * @param status
   * @param jwtId
   * @param userId
   * @param issued
   * @param expires
   * @param admin
   * @param roles
   * @param refreshTokenStatus
   * @return Newly created {@link JWTHttpRequestStatus}
   */
  public static JWTHttpRequestStatus create(JWTStatus status, UUID jwtId, UUID userId, Instant issued, Instant expires, boolean admin, String roles, RefreshTokenStatus refreshTokenStatus) {
    return new JWTHttpRequestStatus(status, jwtId, userId, issued, expires, admin, roles, refreshTokenStatus);
  }

  /**
   * The possible states of a JWT held in an incoming http request.
   * <p>
   * Backend JWT verification by jwt id is integrated in these possible states.
   */
  public enum JWTStatus {
    /**
     * No JWT present in the received request.
     */
    NOT_PRESENT_IN_REQUEST,
    /**
     * JWT is present but is not parseable.
     */
    BAD_TOKEN,
    /**
     * JWT signature check failed.
     */
    BAD_SIGNATURE,
    /**
     * The JWT claims are not parseable or the resolved claims are invalid.
     */
    BAD_CLAIMS,
    /**
     * The JWT signature and claims are valid but the JWT ID was not found in the
     * backend system.
     */
    NOT_PRESENT_BACKEND,
    /**
     * JWT has valid signature and claims but is expired.
     */
    EXPIRED,
    /**
     * The JWT signature and claims are valid but either the jwt id or
     * associated user are logically blocked by way of backend check.
     */
    BLOCKED,
    /**
     * The JWT signature and claims are valid but an error occurred checking
     * for JWT validity on the backend.
     */
    ERROR,
    /**
     * The JWT signature and claims are valid and valid by way of backend check.
     * <p>
     * The refresh token in present and non-expired.
     * <p>
     * You may proceed forward.
     */
    VALID
    ;

    /**
     * @return true of a JWT is present in the incoming (target) request.
     */
    public boolean isPresent() { return this != NOT_PRESENT_IN_REQUEST; }

    /**
     * @return true if the JWT is valid (non-expired and deemed legit).
     */
    public boolean isValid() { return this == VALID; }

    /**
     * @return true if the JWT is expired.
     */
    public boolean isJwtExpired() { return this == EXPIRED; }

  } // JWTStatus enum

  /**
   * The possible states of a refresh token cookie held in an incoming http request.
   */
  public enum RefreshTokenStatus {
    /**
     * Refresh token status was NOT checked.
     */
    UNKNOWN,
    /**
     * Refresh token cookie not present in incoming http request.
     */
    NOT_PRESENT_IN_REQUEST,
    /**
     * Refresh token value mis-match between jwt refresh token claim
     * and refresh token cookie value.
     */
    REFRESH_TOKEN_CLAIM_MISMATCH,
    /**
     * JWT refresh token is expired per the embedded claim.
     */
    REFRESH_TOKEN_CLAIM_EXPIRED,
    /**
     * Refresh token cookie is present, not expired
     * and its value matches the jwt refresh token claim.
     */
    VALID
    ;

    /**
     * @return true if the refresh token cookie is present in the incoming http request.
     */
    public boolean isPresent() { return this != NOT_PRESENT_IN_REQUEST; }

    /**
     * @return true if the refresh token is expired.
     */
    public boolean isExpired() { return this == REFRESH_TOKEN_CLAIM_EXPIRED; }

    /**
     * @return true if the refresh token is present, non-expired
     *         and its value matches the JWT refresh token claim value.
     */
    public boolean isValid() { return this == VALID; }

  } // RefreshTokenStatus

  private final JWTStatus status;
  private final UUID jwtId;
  private final UUID userId;
  private final Instant issued;
  private final Instant expires;
  private final boolean admin;
  private final String roles;
  private final RefreshTokenStatus refreshTokenStatus;

  /**
   * Constructor.
   *
   * @param status the status of the JWT in the received request
   * @param jwtId the bound jwt id claim
   * @param userId the associated user id
   * @param issued the issue date as a long of the associated JWT in the received request
   * @param expires the expiration date as a long of the JWT in the received request
   * @param admin true if the associated user has admin privileges
   * @param roles the associated user roles as a comma-delimeted string (if any)
   * @param refreshTokenStatus
   */
  JWTHttpRequestStatus(JWTStatus status, UUID jwtId, UUID userId, Instant issued, Instant expires, boolean admin, String roles, RefreshTokenStatus refreshTokenStatus) {
    super();
    this.status = status;
    this.jwtId = jwtId;
    this.userId = userId;
    this.issued = issued;
    this.expires = expires;
    this.admin = admin;
    this.roles = roles;
    this.refreshTokenStatus = refreshTokenStatus;
  }

  /**
   * @return the status of the JWT in the associated http request
   */
  public JWTStatus status() { return status; }

  /**
   * @return true if the JWT status is either expired or no JWT is present,
   *         false otherwise.
   */
  public boolean isJWTStatusExpiredOrNotPresent() {
    return not(status.isPresent()) || status.isJwtExpired();
  }

  /**
   * @return true when the refresh token is valid in the associated http request.
   */
  public boolean isRefreshTokenValid() { return refreshTokenStatus.isValid(); }

  /**
   * @return the bound jwt id claim.
   */
  public UUID jwtId() { return jwtId; }

  /**
   * @return the associated (bound) user id
   */
  public UUID userId() { return userId; }

  /**
   * @return the Date when the JWT was created
   */
  public Instant issued() { return issued; }

  /**
   * @return the Date when the JWT expires
   */
  public Instant expires() { return expires; }

  /**
   * @return Is the bound JWT user an administrator?
   */
  public boolean isAdmin() { return admin; }

  /**
   * @return the comma-delimited user roles (may be null).
   */
  public String roles() { return roles; }

  /**
   * @return the refresh token [cookie] status of the received http request.
   */
  public RefreshTokenStatus refreshTokenStatus() { return refreshTokenStatus; }

  @Override
  public String toString() {
    return String.format(
      "JWT HTTP Request Status: %s (jwtId: %s, userId: %s, issued: %s, expires: %s, admin: %b, roles: %s).",
      status().toString(),
      isNull(jwtId()) ? "-" : uuidToToken(jwtId()),
      isNull(userId()) ? "-" : uuidToToken(userId()),
      isNull(issued ) ? "-" : issued().toString(),
      isNull(expires) ? "-" : expires().toString(),
      admin,
      isNull(roles) ? "-" : roles
    );
  }
}
