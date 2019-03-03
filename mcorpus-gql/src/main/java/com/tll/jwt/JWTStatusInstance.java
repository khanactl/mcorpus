package com.tll.jwt;

import static com.tll.core.Util.not;

import java.util.Date;
import java.util.UUID;

/**
 * Immutable struct to house the JWT status and the associated id 
 * of the bound user for use in the web layer.
 * 
 * @author jpk
 */
public class JWTStatusInstance {
  
  private final JWTStatus status;
  private final UUID jwtId;
  private final UUID userId;
  private final String roles;
  private final Date issued;
  private final Date expires;

  /**
   * Constructor.
   *
   * @param status the status of the JWT in the received request
   * @param jwtId the bound jwt id claim
   * @param userId the associated user id
   * @param roles the associated user roles as a comma-delimeted string (if any)
   * @param issued the issue date of the associated JWT in the received request
   * @param expires the expiration date of the JWT in the received request
   */
  JWTStatusInstance(JWTStatus status, UUID jwtId, UUID userId, String roles, Date issued, Date expires) {
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
  public Date issued() { return issued == null ? null : new Date(issued.getTime()); }
  
  /**
   * @return the Date when the JWT expires
   */
  public Date expires() { return expires == null ? null : new Date(expires.getTime()); }
  
  @Override
  public String toString() { return String.format("status: %s", status); }

} // JWTStatusInstance class
