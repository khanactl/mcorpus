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
    return new JWTHttpRequestStatus(status, null, null, null, null, false, null);
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
    return new JWTHttpRequestStatus(status, jwtId, userId, issued, expires, admin, roles);
  }

  /**
   * The possible states of a JWT held in an incoming http request.
   * <p>
   * Backend JWT verification by jwt id is integrated in these possible states.
   *
   * @author jpk
   */
  public static enum JWTStatus {
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
     * You may proceed forward.
     */
    VALID;

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
    public boolean isExpired() { return this == EXPIRED; }

    /**
     * @return true if the JWT is invalid for any reason.
     */
    public boolean isInvalid() { return this != VALID; }

  } // JWTStatus enum

  private final JWTStatus status;
  private final UUID jwtId;
  private final UUID userId;
  private final Instant issued;
  private final Instant expires;
  private final boolean admin;
  private final String roles;

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
   */
  JWTHttpRequestStatus(JWTStatus status, UUID jwtId, UUID userId, Instant issued, Instant expires, boolean admin, String roles) {
    super();
    this.status = status;
    this.jwtId = jwtId;
    this.userId = userId;
    this.issued = issued;
    this.expires = expires;
    this.admin = admin;
    this.roles = roles;
  }

  /**
   * @return the status of the JWT in the associated http request
   */
  public JWTStatus status() { return status; }

  /**
   * @return true if the JWT status is either expired or not present,
   *         false otherwise.
   */
  public boolean isJWTStatusExpiredOrNotPresent() {
    return not(status.isPresent()) || status.isExpired();
  }

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
