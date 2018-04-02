package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
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
    private final boolean admin;
    
    /**
     * Constructor.
     *
     * @param status the status of the JWT in the received request
     * @param mcuserId the associated mcuser id
     * @param jwtId the bound jwt id claim
     * @param issued the issue date of the associated JWT in the received request
     * @param expires the expiration date of the JWT in the received request
     * @param admin admin priviliges?
     */
    private JWTStatusInstance(JWTStatus status, UUID mcuserId, UUID jwtId, Date issued, Date expires, boolean admin) {
      super();
      this.status = status;
      this.mcuserId = mcuserId;
      this.jwtId = jwtId;
      this.issued = issued;
      this.expires = expires;
      this.admin = admin;
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
     * @return true if admin access is permissable.
     */
    public boolean isAdmin() { return admin; }
    
    @Override
    public String toString() { return String.format("status: %s", status); }

  } // JWTStatusInstance class

  private static JWTStatusInstance jsi(JWTStatus status) { 
    return new JWTStatusInstance(status, null, null, null, null, false); 
  }
  
  private static JWTStatusInstance jsi(JWTStatus status, UUID mcuserId, UUID jwtId, Date issued, Date expires, boolean admin) { 
    return new JWTStatusInstance(status, mcuserId, jwtId, issued, expires, admin); 
  }
  
  private final long jwtCookieTtlInMillis; 
  private final long jwtCookieTtlInSeconds;
  private final MCorpusUserRepoAsync mcuserRepo;
  private final SecretKey secretKey;

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
    this.secretKey = new SecretKeySpec(jwtSharedSecret, 0, jwtSharedSecret.length, "AES");
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
   * @param pendingJwtId the unique and strongly random jwtid to use in generating
   *          the JWT
   * @param issuer the ascribed JWT issuer claim value which is expected to match
   *          <em>this server's public address</em>.
   * @param audience the ascribed JWT audience claim value which is expected to
   *          match incoming client request's remote address host address (the
   *          client's IP).
   * @return newly created, never null JWT as a string.
   * @throws Exception upon any error while generating the JWT
   */
  public String generate(final Instant requestInstant, final UUID mcuserId, final UUID pendingJwtId, final String issuer, final String audience) 
      throws Exception {
    final long requestTimestamp = requestInstant.toEpochMilli();
    final long loginExpirationTimestamp = requestTimestamp + jwtCookieTtlInMillis;
    
    // create signed jwt object
    final SignedJWT signedJWT = new SignedJWT(
        new JWSHeader(JWSAlgorithm.HS256), 
        new JWTClaimsSet.Builder()
        .issuer(issuer)
        .audience(audience)
        .jwtID(pendingJwtId.toString()) // i.e. the nonce
        .subject(mcuserId.toString())
        .issueTime(new Date(requestTimestamp))
        .expirationTime(new Date(loginExpirationTimestamp))
        .build());
    
    try {
      // sign
      signedJWT.sign(new MACSigner(secretKey.getEncoded()));
      // encrypt
      JWEObject jweObject = new JWEObject(
          new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256)
              .contentType("JWT") // required to signal nested JWT
              .build(),
          new Payload(signedJWT));
      jweObject.encrypt(new DirectEncrypter(secretKey.getEncoded()));
      return jweObject.serialize();
    } catch (Exception e) {
      throw new Exception("JWT signing/encryption error: " + e.getMessage());
    }
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
   * @param serverPublicAddress this server's public address which is used to verify the issuer claim
   * @param rs
   *          the request snapshot for which to extract and verify a possibly held
   *          JWT
   * @return Never null promise for a never-null {@link JWTStatusInstance}.
   */
  public Promise<JWTStatusInstance> jwtRequestStatus(final String serverPublicAddress, final RequestSnapshot rs) {
    // present?
    if(rs == null || rs.getJwtCookie() == null) 
      return Promise.value(jsi(JWTStatus.NOT_PRESENT));
    
    // decrypt JWT
    final JWEObject jweObject;
    try {
      jweObject = JWEObject.parse(rs.getJwtCookie());
      jweObject.decrypt(new DirectDecrypter(secretKey.getEncoded()));
    } catch (Exception e1) {
      return Promise.value(jsi(JWTStatus.BAD_TOKEN));
    }
    
    // parse to object
    final SignedJWT sjwt;
    try {
      sjwt = jweObject.getPayload().toSignedJWT();
      if(sjwt == null) throw new Exception();
    } catch (Exception e) {
      return Promise.value(jsi(JWTStatus.BAD_TOKEN));
    }
    
    // verify signature
    try {
      if(not(sjwt.verify(new MACVerifier(secretKey.getEncoded())))) 
        throw new Exception();
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
    
    final Date issued = claims.getIssueTime();
    final Date expires = claims.getExpirationTime();
    // expired? (check for exp. time in the past)
    if(new Date().after(expires)) {
      return Promise.value(jsi(JWTStatus.EXPIRED, null, null, issued, expires, false));
    }
    
    final String issuer = claims.getIssuer();
    final String audience = claims.getAudience().isEmpty() ? "" : claims.getAudience().get(0);
    
    // verify issuer (this server's public host name)
    if(isNullOrEmpty(issuer) || not(issuer.equals(serverPublicAddress))) {
      return Promise.value(jsi(JWTStatus.BAD_CLAIMS));
    }
    
    // verify audience (client IP address)
    if(isNullOrEmpty(audience) || not(audience.equals(rs.getRemoteAddressHost()))) {
      return Promise.value(jsi(JWTStatus.BAD_CLAIMS));
    }
    
    // JWT claims:
    // - mcuserId     (subject)
    // - jwtId        (JWTID)
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
    
    // Backend verification:
    // 1) the jwt id is *known* and *not blacklisted*
    // 2) the associated mcuser has a valid status
    return mcuserRepo
            .getJwtStatusAsync(jwtId)
            .map(fr -> {
              if(fr == null || not(fr.isSuccess())) {
                return jsi(JWTStatus.ERROR, mcuserId, jwtId, issued, expires, false);
              }
              switch(fr.get()) {
              default:
              case NOT_PRESENT:
              case BLACKLISTED:
              case MCUSER_INACTIVE:
                // logically blocked
                return jsi(JWTStatus.BLOCKED, mcuserId, jwtId, issued, expires, false);
              case VALID:
                // valid non-admin privs (standard mcuser)
                return jsi(JWTStatus.VALID, mcuserId, jwtId, issued, expires, false);
              case VALID_ADMIN:
                // valid with admin privs
                return jsi(JWTStatus.VALID, mcuserId, jwtId, issued, expires, true);
              }
            });
  }
}
