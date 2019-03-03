package com.tll.mcorpus.jwt;

import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.not;

import java.security.SecureRandom;
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
import com.tll.mcorpus.jwt.IJwtBackendStatusProvider.JwtBackendStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWT (Json Web Token) business rules and public methods for supporting them in
 * app web layer.
 * <p>
 * JWTs are used as a means to authenticate users over http before granting
 * access to server-based service(s).
 * <p>
 * Specifically, this class assesses the JWT status for incoming http requests and
 * also generates them.
 * 
 * @author jkirton
 */
public class JWT {

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
  
  public static JWTStatusInstance jsi(JWTStatus status, UUID jwtId, UUID userId, String roles, Date issued, Date expires) { 
    return new JWTStatusInstance(status, jwtId, userId, roles, issued, expires); 
  }
  
  private final Logger log = LoggerFactory.getLogger(JWT.class);
  
  private final long jwtCookieTtlInMillis; 
  private final long jwtCookieTtlInSeconds;
  private final IJwtBackendStatusProvider jbsp;
  private final SecretKey secretKey;
  private final String serverIssuer; 

  /**
   * Constructor.
   * 
   * @param jwtCookieTtlInMillis the jwt cookie time to live in milliseconds
   * @param jwtSharedSecret
   *          the cryptographically strong salt (or shared secret) to be used for
   *          signing and later verifying JWTs
   * @param jbsp the backend JWT status provider
   * @param serverIssuer the expected JWT issuer used to verify received JWTs
   */
  public JWT(long jwtCookieTtlInMillis, byte[] jwtSharedSecret, IJwtBackendStatusProvider jbsp, String serverIssuer) {
    super();
    this.jwtCookieTtlInMillis = jwtCookieTtlInMillis;
    this.jwtCookieTtlInSeconds = Math.floorDiv(jwtCookieTtlInMillis, 1000);
    this.jbsp = jbsp;
    this.secretKey = new SecretKeySpec(jwtSharedSecret, 0, jwtSharedSecret.length, "AES");
    this.serverIssuer = serverIssuer;
    log.info("JWT configured with cookie time-to-live: {} seconds, serverIssuer: {}.", jwtCookieTtlInSeconds, serverIssuer);
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
   * Generate an encrypted and signed JWT.
   * 
   * @param jwtId the unique and strongly random jwtid to use in generating the JWT
   * @param userId id of the associated user to use in generating the JWT
   * @param roles the optional roles of the <code>userId</code>
   * @param httpreq the provider for the needed incoming http request parameter values
   * @return newly created, never null encrypted and signed JWT.
   * @throws Exception upon any unexpected error generating the JWT
   */
  public String generate(final UUID jwtId, final UUID userId, String roles, final IJwtHttpRequestProvider httpreq) 
      throws Exception {
    final long requestTimestamp = httpreq.getRequestInstant().toEpochMilli();
    final long loginExpirationTimestamp = requestTimestamp + jwtCookieTtlInMillis;
    final String audience = httpreq.getClientOrigin();

    // create signed jwt object
    final SignedJWT signedJWT = new SignedJWT(
        new JWSHeader(JWSAlgorithm.HS256), 
        new JWTClaimsSet.Builder()
        .issuer(this.serverIssuer)
        .audience(audience)
        .jwtID(jwtId.toString())
        .subject(userId.toString())
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
   * Determine the status of a possibly absent JWT held in the given http request.
   * <p>
   * <b>IMPT</b>: This method issues a call to the backend system (db) to verify
   * the JWT subject (the associated user id) status and the JWT id status.
   * <p>
   * A backend call to the db happens ONLY AFTER these checks:
   * <ul>
   * <li>a JWT is present in the given http request.</li>
   * <li>the JWT signature is verified
   * <li>the JWT ISSUER and AUDIENCE claims are verified
   * <li>the JWT has not expired
   * </ul>
   * 
   * @param httpreq the http request data provider needed for JWT processing
   * @return Never-null {@link JWTStatusInstance}.
   */
  public JWTStatusInstance jwtStatus(final IJwtHttpRequestProvider httpreq) {
    // present?
    if(isNull(httpreq) || isNullOrEmpty(httpreq.getJwtCookie())) 
      return jsi(JWTStatus.NOT_PRESENT_IN_REQUEST);
    
    // decrypt JWT cookie
    final JWEObject jweObject;
    try {
      jweObject = JWEObject.parse(httpreq.getJwtCookie());
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
    // NOTE: we bake the user roles into the jwt token 
    //       with the assumption the jwt security and cryptography
    //       will keep this secret and unaltered.
    final UUID jwtId;
    final UUID userId;
    final Date issued;
    final Date expires;
    final String issuer;
    final String jwtAudience;
    final String roles; // bound to the user
    try {
      final JWTClaimsSet claims = sjwt.getJWTClaimsSet();
      
      jwtId = UUID.fromString(claims.getJWTID());
      userId = UUID.fromString(claims.getSubject());
      issued = claims.getIssueTime();
      expires = claims.getExpirationTime();
      issuer = claims.getIssuer();
      jwtAudience = claims.getAudience().isEmpty() ? "" : claims.getAudience().get(0);
      roles = (String) claims.getClaim("roles");

      if(
        isNull(jwtId)
        || isNull(userId) 
        || isNull(issued)
        || isNull(expires)
        || isNullOrEmpty(issuer)
        || isNullOrEmpty(jwtAudience)
        // NOTE: roles claim is optional
      ) {
        throw new Exception("One or more missing required claims.");
      }
      
      log.debug("JWT issuer: {}, audience: {}", issuer, jwtAudience);
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
    if(not(httpreq.verifyClientOrigin(jwtAudience))) {
      log.error("JWT client origin mis-match (JWT: {} (current request: {})", jwtAudience, httpreq.getClientOrigin());
      return jsi(JWTStatus.BAD_CLAIMS);
    }
    
    // expired? (check for exp. time in the past)
    if(new Date().after(expires)) {
      log.info("JWT expired for JWT ID: {}", jwtId);
      return jsi(JWTStatus.EXPIRED, jwtId, userId, null, issued, expires);
    }
    
    // Backend verification:
    // 1) the jwt id is *known* and *not blacklisted*
    // 2) the associated user has a valid status
    
    final JwtBackendStatus jwtbsi = jbsp.getBackendJwtStatus(jwtId);
    if(jwtbsi == null || jwtbsi.getStatus() == null) {
      log.error("JWT (jwtId: {}) fetch backend status error: {}", jwtId.toString(), jwtbsi.getErrorMsg());
      return jsi(JWTStatus.ERROR, jwtId, userId, null, issued, expires);
    }

    final JwtBackendStatus.Status jwtBackendStatus = jwtbsi.getStatus();
    final JWTStatusInstance jsi;

    switch(jwtBackendStatus) {
    case NOT_PRESENT:
      // jwt id not found in db - treat as blocked then
      log.warn("JWT not present on backend.  jwtId: {}", jwtId.toString());
      jsi = jsi(JWTStatus.NOT_PRESENT_BACKEND, jwtId, userId, null, issued, expires);
      break;
    case PRESENT_BAD_STATE:
      // jwt id found in db but the status could not be determined
      log.warn("JWT present but status is unknown.  jwtId: {}", jwtId.toString());
      jsi = jsi(JWTStatus.ERROR, jwtId, userId, null, issued, expires);
      break;
    case BLACKLISTED:
      log.warn("JWT blacklisted.  jwtId: {}", jwtId.toString());
      jsi = jsi(JWTStatus.BLOCKED, jwtId, userId, null, issued, expires);
      break;
    case BAD_USER:
      log.warn("JWT bad user.  jwtId: {}", jwtId.toString());
      jsi = jsi(JWTStatus.BLOCKED, jwtId, userId, null, issued, expires);
      break;
    case EXPIRED:
      // jwt (login) expired
      log.warn("JWT expired.  jwtId: {}", jwtId.toString());
      jsi = jsi(JWTStatus.EXPIRED, jwtId, userId, null, issued, expires);
      break;
    case VALID:
      // valid - provide roles
      jsi = jsi(JWTStatus.VALID, jwtId, userId, roles, issued, expires);
      break;
    case ERROR:
    default:
      // unhandled jwt status so convey status as backend error
      log.error("JWT backend status error: {}!  jwtId: {}", jwtBackendStatus);
      jsi = jsi(JWTStatus.ERROR, jwtId, userId, null, issued, expires);
      break;
    }

    return jsi;
  }
}
