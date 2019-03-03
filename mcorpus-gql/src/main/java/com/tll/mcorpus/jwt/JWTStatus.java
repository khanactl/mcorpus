package com.tll.mcorpus.jwt;

/**
 * The supported states for a JWT.
 * 
 * @author jpk
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
   * The JWT ID was not found in the backend system.
   */
  NOT_PRESENT_BACKEND,
  /**
   * JWT has valid signature but has expired.
   */
  EXPIRED,
  /**
   * Either the jwt id or associated user are logically blocked by way of backend check.
   */
  BLOCKED,
  /**
   * An error occurred checking for JWT validity on the backend.
   */
  ERROR,
  /**
   * JWT is valid.  You may proceed forward.
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
