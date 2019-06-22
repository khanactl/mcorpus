package com.tll.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import com.tll.UnitTest;
import com.tll.jwt.JWT;
import com.tll.jwt.JWTHttpRequestStatus;
import com.tll.jwt.JWTHttpRequestStatus.JWTStatus;
import com.tll.web.RequestSnapshot;

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
    
    byte[] jwtSharedSecret = JWT.generateJwtSharedSecret();
    long jwtTtlInMillis = Duration.ofDays(2).toMillis();
    final JWT jwti = new JWT(jwtTtlInMillis, jwtSharedSecret, "https://site.com");
    
    Instant now = Instant.now();
    UUID jwtId = UUID.randomUUID();
    String roles = "AROLE";
    UUID jwtUserId = UUID.randomUUID();
    
    final RequestSnapshot rsPre = new RequestSnapshot(
      now,
      "127.0.0.1",
      "localhost",
      "https://site.com/index",
      "https://site.com/index/target",
      "",
      "127.0.0.1",
      "https",
      "5150",
      null,
      "rst",
      "rsth", 
      UUID.randomUUID().toString()
    );
    
      // generate jwt
    String jwt = jwti.jwtGenerate(jwtId, jwtUserId, roles, rsPre);
    assertNotNull(jwt);
    log.info("JWT generated: {}", jwt);
    
    final RequestSnapshot rsPost = new RequestSnapshot(
      now,
      "127.0.0.1",
      "localhost",
      "https://site.com/index",
      "https://site.com/index/target",
      "",
      "127.0.0.1",
      "https",
      "5150",
      jwt,
      "rst",
      "rsth", 
      UUID.randomUUID().toString()
    );
    
    // get jwt status
    JWTHttpRequestStatus jwtStatus = jwti.jwtHttpRequestStatus(rsPost, null);
    assertNotNull(jwtStatus);
    assertEquals(JWTStatus.NOT_PRESENT_BACKEND, jwtStatus.status());
  }
}
