package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.isNull;
import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

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
import com.tll.mcorpus.db.enums.JwtStatus;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.mcorpus.repo.model.FetchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWT (Json Web Token) business rules and public methods for supporting them in
 * app web layer.
 * <p>
 * JWTs are used as a means to authenticate users over http before granting
 * access to the mcorpus api.
 * <p>
 * Specifically, this class assesses the JWT status for incoming http requests and
 * also generates them.  JWTs are generated after successful user authentication
 * by username and password.
 * 
 * @author jkirton
 */
public class JWT {

  /**
   * The supported states for an mcorpus JWT.
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
     * The JWT ID was not found in the backend database.
     */
    NOT_PRESENT_BACKEND,
    /**
     * JWT has valid signature but has expired.
     */
    EXPIRED,
    /**
     * Either the jwt id or mcuser id are logically blocked by way of backend check.
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
    private final String roles;

    /**
     * Constructor.
     *
     * @param status the status of the JWT in the received request
     * @param mcuserId the associated mcuser id
     * @param jwtId the bound jwt id claim
     * @param issued the issue date of the associated JWT in the received request
     * @param expires the expiration date of the JWT in the received request
     * @param roles the mcuser roles as a comma-delimeted string (if any)
     */
    private JWTStatusInstance(JWTStatus status, UUID mcuserId, UUID jwtId, Date issued, Date expires, String roles) {
      super();
      this.status = status;
      this.mcuserId = mcuserId;
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
     * @return the comma-delimited mcuser roles (may be null).
     */
    public String roles() { return roles; }

    @Override
    public String toString() { return String.format("status: %s", status); }

  } // JWTStatusInstance class

  /**
   * @return Cryptographically strong random 32-byte (256 bits) array to serve as
   *         a JWT salt (shared secret).
   */
  public static byte[] generateJwtSharedSecret() {
    final SecureRandom random = new SecureRandom();
    final byte[] sharedSecret = new byte[32]; // i.e. 256 bits
    random.nextBytes(sharedSecret);
    return sharedSecret;
  }

  /**
   * Generate a hex-wise string from an arbitrary byte array.
   * 
   * @param bytes the bytes array
   * @return newly created string that represents the given byte array as a
   *         hex-based string
   */
  public static String serialize(final byte[] bytes) {
    return DatatypeConverter.printHexBinary(bytes);
  }
  
  /**
   * De-serialize a random hex-based string.
   * 
   * @param hexToken
   * @return the de-serialized hex token as a byte array
   */
  public static byte[] deserialize(final String hexToken) {
    return DatatypeConverter.parseHexBinary(hexToken);
  }
  
  public static JWTStatusInstance jsi(JWTStatus status) { 
    return new JWTStatusInstance(status, null, null, null, null, null); 
  }
  
  public static JWTStatusInstance jsi(JWTStatus status, UUID mcuserId, UUID jwtId, Date issued, Date expires, String roles) { 
    return new JWTStatusInstance(status, mcuserId, jwtId, issued, expires, roles); 
  }
  
  private final Logger log = LoggerFactory.getLogger(JWT.class);
  
  private final long jwtCookieTtlInMillis; 
  private final long jwtCookieTtlInSeconds;
  private final MCorpusUserRepo mcuserRepo;
  private final SecretKey secretKey;
  private final String serverIssuer; 

  /**
   * Constructor.
   * 
   * @param jwtCookieTtlInMillis the jwt cookie time to live in milliseconds
   * @param jwtSharedSecret
   *          the cryptographically strong salt (or shared secret) to be used for
   *          signing and later verifying JWTs
   * @param mcuserRepo
   *          the mcorpus mcuser repo needed for backend JWT verification
   * @param serverIssuer the expected JWT issuer used to verify received JWTs
   */
  public JWT(long jwtCookieTtlInMillis, byte[] jwtSharedSecret, MCorpusUserRepo mcuserRepo, String serverIssuer) {
    super();
    this.jwtCookieTtlInMillis = jwtCookieTtlInMillis;
    this.jwtCookieTtlInSeconds = Math.floorDiv(jwtCookieTtlInMillis, 1000);
    this.mcuserRepo = mcuserRepo;
    this.secretKey = new SecretKeySpec(jwtSharedSecret, 0, jwtSharedSecret.length, "AES");
    this.serverIssuer = serverIssuer;
    log.info("JWT configured.  JWT cookie time-to-live: {} seconds.", jwtCookieTtlInSeconds);
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
   *          match incoming client request's remote host address (the
   *          client's IP).
   * @param roles the endowed roles of the <code>mcuserId</code>
   * @return newly created, never null JWT as a string.
   * @throws Exception upon any error while generating the JWT
   */
  public String generate(final Instant requestInstant, final UUID mcuserId, final UUID pendingJwtId, final String issuer, final String audience, String roles) 
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
        .claim("roles", roles)
        .build());
    
    try {
      // sign
      signedJWT.sign(new MACSigner(secretKey.getEncoded()));
      // encrypt
      JWEObject jweObject = new JWEObject(
          new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
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
   * Verify a current client origin against an originating client origin.
   * 
   * @param original the originating client origin
   * @param current the received client origin
   * @return true if the given client origin matches the one held.
   */
  private static boolean verifyClientOrigin(final String original, final String current) {
    if(isNullOrEmpty(original) || isNullOrEmpty(current)) return false;
    
    final String[] originalParsed = RequestSnapshot.parseClientOriginToken(original);
    final String[] currentParsed = RequestSnapshot.parseClientOriginToken(current);
    
    String originalRemoteAddrHost = originalParsed[0];
    String originalXForwardedFor = originalParsed[1];
    
    String currentRemoteAddrHost = currentParsed[0];
    String currentXForwardedFor = currentParsed[1];
    
    // if the original remote address host matches either the current remote address host 
    // -OR- the x-forwarded-for then we approve this message
    if(originalRemoteAddrHost.equals(currentRemoteAddrHost) || originalRemoteAddrHost.equals(currentXForwardedFor)) 
      return true;
    
    if(originalXForwardedFor.equals(currentRemoteAddrHost) || originalXForwardedFor.equals(currentXForwardedFor)) 
      return true;
    
    // denied
    return false;
  }
  
  /**
   * Get the JWT status for the given received server request snapshot.
   * <p>
   * <b>IMPT</b>: This method issues a call to the backend system (db) to verify
   * the JWT subject (the mcuser id) status and the JWTID status.
   * <p>
   * A backend call to the db happens ONLY AFTER these checks:
   * <ul>
   * <li>a JWT is present in the given request snapshot <code>rs</code>.</li>
   * <li>the JWT signature is verified
   * <li>the JWT ISSUER and AUDIENCE claims are verified
   * <li>the JWT has not expired
   * </ul>
   * 
   * @param serverPublicAddress this server's public address which is used to
   *          verify the issuer claim
   * @param rs the request snapshot for which to extract and verify a possibly
   *          held JWT
   * @return Never null promise for a never-null {@link JWTStatusInstance}.
   */
  public JWTStatusInstance jwtRequestStatus(final RequestSnapshot rs) {
    // present?
    if(rs == null || rs.getJwtCookie() == null) 
      return jsi(JWTStatus.NOT_PRESENT_IN_REQUEST);
    
    // decrypt JWT
    final JWEObject jweObject;
    try {
      jweObject = JWEObject.parse(rs.getJwtCookie());
      jweObject.decrypt(new DirectDecrypter(secretKey.getEncoded()));
    } catch (Exception e) {
      log.error("JWT decrypt error: {}", e.getMessage());
      return jsi(JWTStatus.BAD_TOKEN);
    }
    
    // parse to object
    final SignedJWT sjwt;
    try {
      sjwt = jweObject.getPayload().toSignedJWT();
      if(sjwt == null) throw new Exception();
    } catch (Exception e) {
      log.error("JWT un-signing error: {}", e.getMessage());
      return jsi(JWTStatus.BAD_TOKEN);
    }
    
    // verify signature
    try {
      if(not(sjwt.verify(new MACVerifier(secretKey.getEncoded())))) 
        throw new Exception();
    } catch (Exception e) {
      log.error("JWT verify signature error: {}", e.getMessage());
      return jsi(JWTStatus.BAD_SIGNATURE);
    }
    
    // extract and verify the held JWT claims
    // NOTE: we bake the mcuser roles into the jwt token 
    //       with the assumption the jwt security and cryptography
    //       will keep this secret and unaltered.
    final UUID jwtId;
    final UUID mcuserId;
    final Date issued;
    final Date expires;
    final String issuer;
    final String audience;
    final String roles; // bound to the mcuser
    try {
      final JWTClaimsSet claims = sjwt.getJWTClaimsSet();
      
      jwtId = UUID.fromString(claims.getJWTID());
      mcuserId = UUID.fromString(claims.getSubject());
      issued = claims.getIssueTime();
      expires = claims.getExpirationTime();
      issuer = claims.getIssuer();
      audience = claims.getAudience().isEmpty() ? "" : claims.getAudience().get(0);
      roles = (String) claims.getClaim("roles");

      if(
        isNull(jwtId)
        || isNull(mcuserId) 
        || isNull(issued)
        || isNull(expires)
        || isNullOrEmpty(issuer)
        || isNullOrEmpty(audience)
        // NOTE: roles claim is optional
      ) {
        throw new Exception("One or more missing required claims.");
      }
      
      log.debug("JWT issuer: {}, audience: {}", issuer, audience);
    }
    catch (Exception e) {
      log.error("JWT bad claims: {}", e.getMessage());
      return jsi(JWTStatus.BAD_CLAIMS);
    }
    
    // verify issuer (this server's public host name)
    if(not(issuer.equals(serverIssuer))) {
      log.error("JWT bad issuer: {} (expected: {})", issuer, serverIssuer);
      return jsi(JWTStatus.BAD_CLAIMS);
    }
    
    // verify audience (client origin)
    if(not(verifyClientOrigin(audience, rs.getClientOrigin()))) {
      log.error("JWT bad audience: {} (expected: {})", audience, rs.getClientOrigin());
      return jsi(JWTStatus.BAD_CLAIMS);
    }
    
    // expired? (check for exp. time in the past)
    if(new Date().after(expires)) {
      log.info("JWT expired for JWT ID: {}", jwtId);
      return jsi(JWTStatus.EXPIRED, mcuserId, jwtId, issued, expires, null);
    }
    
    // Backend verification:
    // 1) the jwt id is *known* and *not blacklisted*
    // 2) the associated mcuser has a valid status
    
    final FetchResult<JwtStatus> fr = mcuserRepo.getJwtStatus(jwtId);
    if(fr == null || not(fr.isSuccess())) {
      log.error("JWT (jwtId: {}) fetch backend status error: {}", jwtId.toString(), fr.getErrorMsg());
      return jsi(JWTStatus.ERROR, mcuserId, jwtId, issued, expires, null);
    }

    final JwtStatus jwtStatus = fr.get();
    final JWTStatusInstance jsi;

    switch(jwtStatus) {
    case NOT_PRESENT:
      // jwt id not found in db - treat as blocked then
      log.warn("JWT not present on backend.  jwtId: {}", jwtId.toString());
      jsi = jsi(JWTStatus.NOT_PRESENT_BACKEND, mcuserId, jwtId, issued, expires, null);
      break;
    case PRESENT_BAD_STATE:
      // jwt id found in db but the status could not be determined
      log.warn("JWT present but status is unknown.  jwtId: {}", jwtId.toString());
      jsi = jsi(JWTStatus.ERROR, mcuserId, jwtId, issued, expires, null);
      break;
    case BLACKLISTED:
    case MCUSER_INACTIVE:
      // logically blocked
      log.warn("JWT logically blocked.  jwtId: {}", jwtId.toString());
      jsi = jsi(JWTStatus.BLOCKED, mcuserId, jwtId, issued, expires, null);
      break;
    case EXPIRED:
      // jwt (login) expired
      log.warn("JWT expired.  jwtId: {}", jwtId.toString());
      jsi = jsi(JWTStatus.EXPIRED, mcuserId, jwtId, issued, expires, null);
      break;
    case VALID:
      // valid - provide roles
      jsi = jsi(JWTStatus.VALID, mcuserId, jwtId, issued, expires, roles);
      break;
    default:
      // unhandled jwt status so convey status as backend error
      log.warn("JWT unhandled status: {}!  jwtId: {}", fr.get());
      jsi = jsi(JWTStatus.ERROR, mcuserId, jwtId, issued, expires, null);
      break;
    }

    return jsi;
  }
}
