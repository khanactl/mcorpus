package com.tll.jwt;

import java.util.UUID;

/**
 * Contract for obtaining the status of a JWT from a backend repository or datastore.
 * <p>
 * Since: 1/10/2019
 * 
 * @author jkirton
 */
@FunctionalInterface
public interface IJwtBackendStatusProvider {

  /**
   * The immutable backend JWT status value object
   * used to convey the results of a single request 
   * for the backend JWT status.
   */
  public static class JwtBackendStatus {

      public static enum Status {
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

      private final Status status;
      private final String errorMsg;

      /**
       * Constructor - Non-error case (jwt status obtained without error).
       */
      public JwtBackendStatus(Status status) {
        this.status = status;
        this.errorMsg = null;
      }
    
      /**
       * Constructor - Error case (no jwt status obtained).
       */
      public JwtBackendStatus(String errorMsg) {
        this.status = Status.ERROR;
        this.errorMsg = errorMsg;
      }

      /**
       * @return the obtained backend JWT status.
       */
      public Status getStatus() { return status; }
      
      /**
       * An error message is exptected to be provided when an error occurs 
       * fetching the backend JWT status.
       * 
       * @return the error message if present, null otherwise.
       */
      public String getErrorMsg() { return errorMsg; }
  }

  /**
   * Get the status of a known JWT from a remotely held datastore (the backend) 
   * given its jwt id.
   * 
   * @param jwtId id of the target JWT
   * @return Never-null {@link JwtBackendStatus} holding the JWT status 
   *         or an error message if the jwt status is not provided.
   */
  JwtBackendStatus getBackendJwtStatus(UUID jwtId);
}