package com.tll.web;

import static com.tll.mcorpus.MCorpusTestUtil.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import com.tll.UnitTest;
import com.tll.jwt.IJwtHttpRequestProvider;
import com.tll.jwt.JWT;
import com.tll.jwt.JWTHttpRequestStatus;
import com.tll.jwt.JWTHttpRequestStatus.JWTStatus;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for {@link JWT} class.
 *
 * @author d2d
 */
@Category(UnitTest.class)
public class JWTTest {

  private static final Logger log = LoggerFactory.getLogger(JWTTest.class);

  @Test
  public void testJwtSalt() throws Exception {
    final byte[] randarr = JWT.generateJwtSharedSecret();
    final String s = JWT.serialize(randarr);
    final byte[] derandarr = JWT.deserialize(s);
    assertTrue("JWT serialize/de-serialize mis-match.", Arrays.equals(randarr, derandarr));
    log.info("Random 32-byte hex token: {}", s);
  }

  @Test
  public void testJwtGenerateAndParse() throws Exception {

    final byte[] jwtSharedSecret = JWT.generateJwtSharedSecret();
    final String serverIssuer = "testhost.com";
    final Duration jwtTtl = Duration.ofDays(2);
    final JWT jwti = new JWT(mockJwtBackendHandler(), jwtTtl, jwtSharedSecret, serverIssuer);

    final Instant instantA = Instant.now();
    final Instant instantB = Instant.now().plus(Duration.ofSeconds(120));
    final UUID jwtId = UUID.randomUUID();
    final String roles = "AROLE";
    final UUID jwtUserId = UUID.randomUUID();

    final IJwtHttpRequestProvider jwtRpPre = new IJwtHttpRequestProvider() {

      final InetAddress ro = InetAddress.getByName("127.0.0.1");

      @Override
      public boolean verifyRequestOrigin(String jwtAudience) {
        return true;
      }

      @Override
      public Instant getRequestInstant() {
        return instantA;
      }

      @Override
      public String getJwt() {
        return null;
      }

      @Override
      public InetAddress getRequestOrigin() {
        return ro;
      }
    };

    // generate jwt
    String jwt = jwti.jwtGenerate(jwtId, jwtUserId, roles, jwtRpPre);
    assertNotNull(jwt);
    log.info("JWT generated: {}", jwt);

    final IJwtHttpRequestProvider jwtRpPost = new IJwtHttpRequestProvider(){

      final InetAddress ro = InetAddress.getByName("127.0.0.1");

      @Override
      public boolean verifyRequestOrigin(String jwtAudience) {
        return true;
      }

      @Override
      public Instant getRequestInstant() {
        return instantB;
      }

      @Override
      public String getJwt() {
        return jwt;
      }

      @Override
      public InetAddress getRequestOrigin() {
        return ro;
      }
    };

    // get jwt status
    JWTHttpRequestStatus jwtStatus = jwti.jwtHttpRequestStatus(jwtRpPost);
    assertNotNull(jwtStatus);
    assertEquals(JWTStatus.NOT_PRESENT_BACKEND, jwtStatus.status());
  }
}
