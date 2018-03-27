package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;

import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tll.mcorpus.repo.MCorpusUserRepoAsync;

import ratpack.exec.Promise;

/**
 * JWT (Json Web Token) business rules and public methods for supporting them in
 * app web layer.
 * <p>
 * JWTs are used as a means to authenticate users over http before granting
 * access to the mcorpus api.
 * <p>
 * Specifically, this class assess the JWT status for incoming http requests and
 * also generates them.  JWTs are generated after a successful user authentication
 * by username and password.
 * 
 * @author jkirton
 */
public class JWT {
  
  private static final Logger log = LoggerFactory.getLogger(JWT.class);
  
  /**
   * The supported states for an mcorpus JWT.
   */
  public static enum JWTStatus {
    /**
     * No JWT present. 
     */
    NOT_PRESENT,
    /**
     * JWT is present but is not parseable.
     */
    BAD_TOKEN,
    /**
     * JWT signature check failed.
     */
    BAD_SIGNATURE,
    /**
     * One or more embedded JWT claims are wrong or incorrect.
     */
    BAD_CLAIMS,
    /**
     * JWT has valid signature but has expired.
     */
    EXPIRED,
    /**
     * Either the jwt id or mcuser id are logically blocked.
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
    public boolean isPresent() { return this != NOT_PRESENT; }
    
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
  
  /**
   * Immutable struct to house the JWT status and the associated id of the bound
   * mcuser.
   */
  public static class JWTStatusInstance {
    
    private final JWTStatus status;
    private final UUID mcuserId;
    private final UUID jwtId;
    private final Date issued;
    private final Date expires;
    private final URL clientOrigin;
    
    /**
     * Constructor.
     *
     * @param status the status of the JWT in the received request
     * @param mcuserId the associated mcuser id
     * @param jwtId the bound jwt id claim
     * @param issued the issue date of the associated JWT in the received request
     * @param expires the expiration date of the JWT in the received request
     * @param clientOrigin the client's (mcuser) origin and protocol 
     */
    private JWTStatusInstance(JWTStatus status, UUID mcuserId, UUID jwtId, Date issued, Date expires, URL clientOrigin) {
      super();
      this.status = status;
      this.mcuserId = mcuserId;
      this.jwtId = jwtId;
      this.issued = issued;
      this.expires = expires;
      this.clientOrigin = clientOrigin;
    }
    
    /**
     * @return the status of the JWT in the received request
     */
    public JWTStatus status() { return status; }
    
    /**
     * @return the mcuser id 
     */
    public UUID mcuserId() { return mcuserId; }
    
    /**
     * @return the bound jwt id claim.
     */
    public UUID jwtId() { return jwtId; }
    
    /**
     * @return the Date when the JWT was created
     */
    public Date issued() { return issued == null ? null : new Date(issued.getTime()); }
    
    /**
     * @return the Date when the JWT expires
     */
    public Date expires() { return expires == null ? null : new Date(expires.getTime()); }
    
    /**
     * @return The originating client domain and protocol.
     */
    public URL clientOrigin() { return clientOrigin; }

    @Override
    public String toString() { return String.format("status: %s", status); }

  } // JWTStatusInstance class

  /**
   * The JWT Issuer claim used for mcorpus JWTs.
   */
  public static final String JWT_ISSUER = "mcorpus-gql-server";
  
  /**
   * The JWT Audience claim used for mcorpus JWTs.
   */
  public static final String JWT_AUDIENCE = "mcorpus";

  /**
   * The <code>JWT_CLIENT_ORIGIN</code> claim (key) name.
   */
  public static final String JWT_CLIENT_ORIGIN_KEY = "JWT_CLIENT_ORIGIN";
  
  private static JWTStatusInstance jsi(JWTStatus status) { 
    return new JWTStatusInstance(status, null, null, null, null, null); 
  }
  
  private static JWTStatusInstance jsi(JWTStatus status, UUID mcuserId, UUID jwtId, Date issued, Date expires, URL clientOrigin) { 
    return new JWTStatusInstance(status, mcuserId, jwtId, issued, expires, clientOrigin); 
  }
  
  private final long jwtCookieTtlInMillis; 
  private final long jwtCookieTtlInSeconds;
  private final MCorpusUserRepoAsync mcuserRepo;
  private final JWSSigner signer;
  private final JWSVerifier verifier;

  /**
   * Constructor.
   * 
   * @param jwtCookieTtlInMillis the jwt cookie time to live in milliseconds
   * @param jwtSharedSecret
   *          the cryptographically strong salt (or shared secret) to be used for
   *          signing and later verifying JWTs
   * @param mcuserRepo
   *          the mcorpus mcuser repo needed for backend JWT verification
   */
  public JWT(long jwtCookieTtlInMillis, byte[] jwtSharedSecret, MCorpusUserRepoAsync mcuserRepo) {
    super();
    this.jwtCookieTtlInMillis = jwtCookieTtlInMillis;
    this.jwtCookieTtlInSeconds = Math.floorDiv(jwtCookieTtlInMillis, 1000);
    this.mcuserRepo = mcuserRepo;
    try {
      this.signer = new MACSigner(jwtSharedSecret);
      this.verifier = new MACVerifier(jwtSharedSecret);
    } catch (Exception e) {
      throw new Error("Invalid jwt salt value.");
    }
  }

  /**
   * @return the configured JWT cookie time to live in milliseconds.
   */
  public long jwtCookieTtlInMillis() { return jwtCookieTtlInMillis; }

  /**
   * @return the configured JWT cookie time to live in seconds.
   */
  public long jwtCookieTtlInSeconds() { return jwtCookieTtlInSeconds; }
  
  /**
   * Generate an mcorpus JWT as a {@link SignedJWT}.
   * 
   * @param requestInstant when the associated http request hit the server
   * @param mcuserId id of the mcuser to use in generating the JWT
   * @param pendingJwtId the unique and strongly random jwtid to use in generating the JWT
   * @param clientOrigin the originating client host/domain/ip port and protocol
   * @return newly created, never null JWT as a string.
   * @throws Exception upon any error while generating the JWT
   */
  public String generate(final Instant requestInstant, final UUID mcuserId, final UUID pendingJwtId, URL clientOrigin) 
      throws Exception {
    final long requestTimestamp = requestInstant.toEpochMilli();
    final long loginExpirationTimestamp = requestTimestamp + jwtCookieTtlInMillis;
    final String clientOriginToken = clientOrigin.toString();
    final SignedJWT signedJWT = new SignedJWT(
        new JWSHeader(JWSAlgorithm.HS256), 
        new JWTClaimsSet.Builder()
        .issuer(JWT_ISSUER)
        .audience(JWT_AUDIENCE)
        .jwtID(pendingJwtId.toString()) // i.e. the nonce
        .subject(mcuserId.toString())
        .issueTime(new Date(requestTimestamp))
        .expirationTime(new Date(loginExpirationTimestamp))
        .claim(JWT_CLIENT_ORIGIN_KEY, clientOriginToken)
        .build());
    try {
      signedJWT.sign(signer);
    } catch (JOSEException e) {
      throw new Exception("JWT signing error: " + e.getMessage());
    }
    return signedJWT.serialize();
  }
  
  /**
   * Get a promise for the mcorpus JWT status of given request snapshot.
   * <p>
   * <b>IMPT</b>: We return a promise for the JWT status since a call to the
   * backend is required to check its held jwtid claim as well as the associated
   * mcuser's status and we honor processing this task off of the main compute
   * thread.
   * <p>
   * NOTE: a backend call to the db happens AFTER these checks:
   * <ul>
   * <li>a JWT is present in the given request snapshot <code>rs</code>.</li>
   * <li>the JWT signature is verified
   * <li>the JWT ISSUER and AUDIENCE claims are verified
   * <li>the JWT has not expired
   * <li>the JWT client origin claim matches the current request client origin
   * </ul>
   * 
   * @param rs
   *          the request snapshot for which to extract and verify a possibly held
   *          JWT
   * @return Never null promise for a never-null {@link JWTStatusInstance}.
   */
  public Promise<JWTStatusInstance> jwtRequestStatus(final RequestSnapshot rs) {
    // present?
    if(rs == null || rs.getJwtCookie() == null) return Promise.value(jsi(JWTStatus.NOT_PRESENT));
    
    // parse to object
    final SignedJWT sjwt;
    try {
      sjwt = SignedJWT.parse(rs.getJwtCookie());
    } catch (Exception e) {
      return Promise.value(jsi(JWTStatus.BAD_TOKEN));
    }
    
    // verify signature
    try {
      if(sjwt.verify(verifier) == false) throw new Exception();
    } catch (Exception e) {
      return Promise.value(jsi(JWTStatus.BAD_SIGNATURE));
    }
    
    // verify claims
    final JWTClaimsSet claims;
    try {
      claims = sjwt.getJWTClaimsSet();
    }
    catch (Exception e) {
      log.error("Error parsing JWT claims: {}", e.getMessage());
      return Promise.value(jsi(JWTStatus.BAD_CLAIMS));
    }
    
    // verify issuer and audience claims match 
    // to what is expected (i.e. the proclaimed and hard-coded issuer and audience) 
    if(not(JWT_ISSUER.equals(claims.getIssuer())) 
        || not(JWT_AUDIENCE.equals(isNullOrEmpty(
            claims.getAudience()) ? null : claims.getAudience().get(0))))
      return Promise.value(jsi(JWTStatus.BAD_CLAIMS));
    
    // JWT claims:
    // - mcuserId     (subject)
    // - jwtId        (JWTID)
    // - clientOrigin (JWT_CLIENT_ORIGIN)
    final UUID mcuserId;
    try {
      mcuserId = UUID.fromString(claims.getSubject());
    }
    catch(Exception e) {
      log.error("Error parsing subject (mcuserId) JWT claim.");
      return Promise.value(jsi(JWTStatus.BAD_CLAIMS));
    }
    final UUID jwtId;
    try {
      jwtId = UUID.fromString(claims.getJWTID());
    }
    catch(Exception e) {
      log.error("Error parsing JWT ID claim.");
      return Promise.value(jsi(JWTStatus.BAD_CLAIMS));
    }
    final Date issued = claims.getIssueTime();
    final Date expires = claims.getExpirationTime();
    final URL jwtClientOrigin;
    try {
      jwtClientOrigin = new URL(claims.getClaim(JWT_CLIENT_ORIGIN_KEY).toString());
    }
    catch(Exception e) {
      log.error("Error parsing client origin JWT claim.");
      return Promise.value(jsi(JWTStatus.BAD_CLAIMS));
    }
    
    // expired? (check for exp. time in the past)
    if(new Date().after(expires)) {
      return Promise.value(jsi(JWTStatus.EXPIRED, mcuserId, jwtId, issued, expires, null));
    }
    
    // verify the jwt client origin claim matches the current request client origin
    try {
      final URL requestClientOrigin = rs.getClientOrigin();
      if(not(Objects.equals(jwtClientOrigin, requestClientOrigin))) {
        log.error("JWT and request client origin mismatch: jwtClientOrigin: {}, requestClientOrigin: {}", jwtClientOrigin, requestClientOrigin);
        return Promise.value(jsi(JWTStatus.BAD_CLAIMS));
      }
    } catch(Exception e) {
      log.error("Bad request client origin.");
      return Promise.value(jsi(JWTStatus.BAD_CLAIMS));
    }

    // Backend verification:
    // 1) the jwt id is *known* and *not blacklisted*
    // 2) the associated mcuser has a valid status
    return mcuserRepo
            .isJwtValidAsync(jwtId)
            .map(fr -> {
              if(fr == null || not(fr.isSuccess())) return jsi(JWTStatus.ERROR, mcuserId, jwtId, issued, expires, null);
              return jsi( 
                  (fr.get() == Boolean.TRUE ? JWTStatus.VALID : JWTStatus.BLOCKED), 
                  mcuserId, 
                  jwtId, 
                  issued, 
                  expires, 
                  jwtClientOrigin
              );
            });
  }
}
