package com.tll.jwt;

import static com.tll.core.Util.not;

import java.util.Date;
import java.util.UUID;

/**
 * Immutable struct to house the JWT status and the associated id of the 
 * bound user extracted from a JWT possibly held in a target http reqeest object.
 * 
 * @author jpk
 */
public class JWTHttpRequestStatus {
  
  /**
   * Factory method to generate a {@link JWTHttpRequestStatus} from only a {@link JWTStatus}.
   * 
   * @return Newly created {@link JWTHttpRequestStatus}
   */
  public static JWTHttpRequestStatus create(JWTStatus status) { 
    return new JWTHttpRequestStatus(status, null, null, null, -1, -1); 
  }
  
  /**
   * Factory method to generate a fully populated {@link JWTHttpRequestStatus}.
   * 
   * @return Newly created {@link JWTHttpRequestStatus}
   */
  public static JWTHttpRequestStatus create(JWTStatus status, UUID jwtId, UUID userId, String roles, long issued, long expires) { 
    return new JWTHttpRequestStatus(status, jwtId, userId, roles, issued, expires); 
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
     * JWT has valid signature but is expired.
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
  private final String roles;
  private final long issued;
  private final long expires;

  /**
   * Constructor.
   *
   * @param status the status of the JWT in the received request
   * @param jwtId the bound jwt id claim
   * @param userId the associated user id
   * @param roles the associated user roles as a comma-delimeted string (if any)
   * @param issued the issue date as a long of the associated JWT in the received request
   * @param expires the expiration date as a long of the JWT in the received request
   */
  JWTHttpRequestStatus(JWTStatus status, UUID jwtId, UUID userId, String roles, long issued, long expires) {
    super();
    this.status = status;
    this.userId = userId;
    this.jwtId = jwtId;
    this.issued = issued;
    this.expires = expires;
    this.roles = roles;
  }
  
  /**
   * @return the status of the JWT in the received request
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
   * @return the comma-delimited user roles (may be null).
   */
  public String roles() { return roles; }

  /**
   * @return the Date when the JWT was created
   */
  public Date issued() { return issued < 0 ? null : new Date(issued); }
  
  /**
   * @return the Date when the JWT expires
   */
  public Date expires() { return expires < 0 ? null : new Date(expires); }
  
  @Override
  public String toString() { return String.format("status: %s", status); }

}
