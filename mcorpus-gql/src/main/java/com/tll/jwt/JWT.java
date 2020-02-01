package com.tll.jwt;

import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.not;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
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
import com.tll.jwt.IJwtBackendHandler.JwtBackendStatus;
import com.tll.jwt.JWTHttpRequestStatus.JWTStatus;
import com.tll.repo.FetchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWT (Json Web Token) implementation.
 * <p>
 * This class assesses the JWT status for incoming http requests and
 * generates them for outbound http responses.
 * <p>
 * JWTs are used as a means to authenticate users over http before granting
 * access to server-based service(s).
  *
 * @author jkirton
 */
public class JWT {

  /**
   * Generate a new JWT shared secret for use server-side to both encrypt
   * newly created JWTs and decrypt incoming JWTs.
   *
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

  private final Logger log = LoggerFactory.getLogger(JWT.class);

  private final IJwtBackendHandler backendHandler;
  private final Duration jwtTimeToLive;
  private final SecretKey jkey;
  private final String serverIssuer;

  /**
   * Constructor.
   *
   * @param backendHandler the jwt backend handler.  Required.
   * @param jwtTimeToLive the amount of time a JWT shall live clientside
   * @param jwtSharedSecret the cryptographically strong secret to be used for
   *                        signing and verifying JWTs.  Required.
   * @param serverIssuer the expected JWT issuer used to verify received JWTs.  Required.
   */
  public JWT(IJwtBackendHandler backendHandler, Duration jwtTimeToLive, byte[] jwtSharedSecret, String serverIssuer) {
    super();
    this.backendHandler = Objects.requireNonNull(backendHandler);
    this.jwtTimeToLive = jwtTimeToLive;
    this.jkey = new SecretKeySpec(Objects.requireNonNull(jwtSharedSecret), 0, jwtSharedSecret.length, "AES");
    this.serverIssuer = Objects.requireNonNull(serverIssuer);
    log.info("JWT configured with Time-to-live: {} hours, Issuer: {}.", jwtTimeToLive.toHours(), serverIssuer);
  }

  /**
   * @return the jwt backend handler ref.
   */
  public IJwtBackendHandler getBackendHandler() { return backendHandler; }

  /**
   * @return the configured amount of time a JWT is considered valid.
   */
  public Duration jwtTimeToLive() { return jwtTimeToLive; }

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
  public String jwtGenerate(final UUID jwtId, final UUID userId, String roles, final IJwtHttpRequestProvider httpreq)
      throws Exception {
        final Instant requestInstant = httpreq.getRequestInstant();
        final Instant loginExpiration = requestInstant.plus(jwtTimeToLive);
        final String audience = httpreq.getClientOrigin();

    // create signed jwt object
    final SignedJWT signedJWT = new SignedJWT(
        new JWSHeader(JWSAlgorithm.HS256),
        new JWTClaimsSet.Builder()
        .issuer(this.serverIssuer)
        .audience(audience)
        .jwtID(jwtId.toString())
        .subject(userId.toString())
        .issueTime(Date.from(requestInstant))
        .expirationTime(Date.from(loginExpiration))
        .claim("roles", roles)
        .build());

    try {
      // sign
      signedJWT.sign(new MACSigner(jkey.getEncoded()));
      // encrypt
      JWEObject jweObject = new JWEObject(
          new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
              .contentType("JWT") // required to signal nested JWT
              .build(),
          new Payload(signedJWT));
      jweObject.encrypt(new DirectEncrypter(jkey.getEncoded()));
      return jweObject.serialize();
    } catch (Exception e) {
      throw new Exception(String.format("JWT signing/encryption error: %s.",  e.getMessage()));
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
   * <li>the JWT has not expired based on the extracted expires claim
   * </ul>
   *
   * @param httpreq the http request data provider needed for JWT processing
   * @return Never-null {@link JWTHttpRequestStatus}.
   */
  public JWTHttpRequestStatus jwtHttpRequestStatus(final IJwtHttpRequestProvider httpreq) {

    // present?
    if(isNull(httpreq) || isNullOrEmpty(httpreq.getJwt()))
      return JWTHttpRequestStatus.create(JWTStatus.NOT_PRESENT_IN_REQUEST);

    // decrypt JWT
    final JWEObject jweObject;
    try {
      jweObject = JWEObject.parse(httpreq.getJwt());
      jweObject.decrypt(new DirectDecrypter(jkey.getEncoded()));
    } catch (Exception e) {
      log.error("JWT decrypt error: {}.", e.getMessage());
      return JWTHttpRequestStatus.create(JWTStatus.BAD_TOKEN);
    }

    // parse to object
    final SignedJWT sjwt;
    try {
      sjwt = jweObject.getPayload().toSignedJWT();
      if(sjwt == null) throw new Exception();
    } catch (Exception e) {
      log.error("JWT un-signing error: {}.", e.getMessage());
      return JWTHttpRequestStatus.create(JWTStatus.BAD_TOKEN);
    }

    // verify signature
    try {
      if(not(sjwt.verify(new MACVerifier(jkey.getEncoded()))))
        throw new Exception();
    } catch (Exception e) {
      log.error("JWT verify signature error: {}.", e.getMessage());
      return JWTHttpRequestStatus.create(JWTStatus.BAD_SIGNATURE);
    }

    // extract and verify the held JWT claims
    // NOTE: we bake the user roles into the jwt token
    //       with the assumption the jwt security and cryptography
    //       will keep this secret and unaltered.
    final UUID jwtId;
    final UUID userId;
    final Instant issued;
    final Instant expires;
    final String issuer;
    final String jwtAudience;
    final String roles; // bound to the user
    try {
      final JWTClaimsSet claims = sjwt.getJWTClaimsSet();

      jwtId = UUID.fromString(claims.getJWTID());
      userId = UUID.fromString(claims.getSubject());
      issued = isNull(claims.getIssueTime()) ? null : claims.getIssueTime().toInstant();
      expires = isNull(claims.getExpirationTime()) ? null : claims.getExpirationTime().toInstant();
      issuer = claims.getIssuer();
      jwtAudience = isNullOrEmpty(claims.getAudience()) ? "" : claims.getAudience().get(0);
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
        throw new Exception(String.format("JWT one or more missing required claims."));
      }

      log.debug("JWT id: {}, issuer: {}, audience: {}.", jwtId, issuer, jwtAudience);
    }
    catch (Exception e) {
      log.error("JWT bad claims: {}.'", e.getMessage());
      return JWTHttpRequestStatus.create(JWTStatus.BAD_CLAIMS);
    }

    // verify issuer (this server's public host name)
    if(not(issuer.equals(serverIssuer))) {
      log.error("JWT {} bad issuer: {} (expected: {}).", jwtId, issuer, serverIssuer);
      return JWTHttpRequestStatus.create(JWTStatus.BAD_CLAIMS);
    }

    // verify audience (client origin)
    if(not(httpreq.verifyClientOrigin(jwtAudience))) {
      log.error(
        "JWT {} client origin mis-match (JwtAudience: {} (current request: {}).",
        jwtId, jwtAudience, httpreq.getClientOrigin()
      );
      return JWTHttpRequestStatus.create(JWTStatus.BAD_CLAIMS);
    }
    log.info("JWT audience {} verified against http request client origin {}.", jwtAudience, httpreq.getClientOrigin());

    // expired? (check for exp. time in the past)
    if(Instant.now().isAfter(expires)) {
      log.info("JWT {} expired.", jwtId);
      return JWTHttpRequestStatus.create(JWTStatus.EXPIRED, jwtId, userId, null, issued, expires);
    }

    // [Default] Backend verification behavior:
    // 1) the jwt id is *known* and *not blacklisted*
    // 2) the associated user has a valid status
    final FetchResult<JwtBackendStatus> fr = backendHandler.getBackendJwtStatus(jwtId);
    if(isNull(fr) || isNull(fr.get())) {
      log.error("JWT {} fetch backend status error: {}.",
        jwtId,
        isNull(fr) ? "UNKNOWN" : fr.getErrorMsg()
      );
      return JWTHttpRequestStatus.create(JWTStatus.ERROR, jwtId, userId, null, issued, expires);
    }
    final JwtBackendStatus jwtBackendStatus = fr.get();
    final JWTStatus jwtRequestStatus;

    // map backend jwt status -> jwt http request status
    switch(jwtBackendStatus) {
    case NOT_PRESENT:
      // jwt id not found in db - treat as blocked then
      log.warn("JWT {} not present in backend.", jwtId);
      jwtRequestStatus = JWTStatus.NOT_PRESENT_BACKEND;
      break;
    case PRESENT_BAD_STATE:
      // jwt id found in db but the status could not be determined
      log.warn("JWT {} present in backend but in bad state.", jwtId);
      jwtRequestStatus = JWTStatus.ERROR;
      break;
    case BLACKLISTED:
      log.warn("JWT {} blacklisted.", jwtId);
      jwtRequestStatus = JWTStatus.BLOCKED;
      break;
    case BAD_USER:
      log.warn("JWT {} bad user.", jwtId);
      jwtRequestStatus = JWTStatus.BLOCKED;
      break;
    case EXPIRED:
      // jwt (login) expired
      log.warn("JWT {} expired.", jwtId);
      jwtRequestStatus = JWTStatus.EXPIRED;
      break;
    case VALID:
      // valid - provide roles
      jwtRequestStatus = JWTStatus.VALID;
      break;
    case ERROR:
    default:
      // unhandled jwt status so convey status as backend error
      log.error("JWT {} backend status error: {}.", jwtId, jwtBackendStatus);
      jwtRequestStatus = JWTStatus.ERROR;
      break;
    }

    return JWTHttpRequestStatus.create(jwtRequestStatus, jwtId, userId, roles, issued, expires);
  }
}
