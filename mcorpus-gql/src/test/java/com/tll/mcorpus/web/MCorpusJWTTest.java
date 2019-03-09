package com.tll.mcorpus.web;

import static com.tll.mcorpus.McorpusTestUtil.ds_mcweb;
import static com.tll.mcorpus.McorpusTestUtil.testServerPublicAddress;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import com.tll.UnitTest;
import com.tll.jwt.JWT;
import com.tll.jwt.JWTStatusInstance;
import com.tll.jwt.JWTStatusInstance.JWTStatus;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.web.RequestSnapshot;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for mcorpus specific {@link JWT} processing.
 * 
 * @author d2d
 */
@Category(UnitTest.class)
public class MCorpusJWTTest {
  
  private static final Logger log = LoggerFactory.getLogger(MCorpusJWTTest.class);
  
  private static JWT jwt() {
    MCorpusUserRepo repo = new MCorpusUserRepo(ds_mcweb());
    MCorpusJwtBackendStatusProvider provider = new MCorpusJwtBackendStatusProvider(repo);
    byte[] jwtSharedSecret = JWT.generateJwtSharedSecret();
    long jwtTtlInMillis = Duration.ofDays(2).toMillis();
    return new JWT(jwtTtlInMillis, jwtSharedSecret, provider, testServerPublicAddress);
  }
  
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
    final JWT jwti = jwt();
    
    Instant now = Instant.now();
    UUID jwtId = UUID.randomUUID();
    String roles = "MCORPUS";
    UUID mcuserId = UUID.randomUUID();
    
    final RequestSnapshot rsPre = new RequestSnapshot(
      now,
      "127.0.0.1",
      "localhost",
      "https://mcorpus.d2d:5150/loginPage",
      "https://mcorpus.d2d:5150/index",
      "",
      "127.0.0.1",
      "https",
      "5150",
      null,
      "rst",
      "rsth");
    
      // generate jwt
    String jwt = jwti.jwtGenerate(jwtId, mcuserId, roles, rsPre);
    assertNotNull(jwt);
    log.info("JWT generated: {}", jwt);
    
    final RequestSnapshot rsPost = new RequestSnapshot(
      now,
      "127.0.0.1",
      "localhost",
      "https://mcorpus.d2d:5150/loginPage",
      "https://mcorpus.d2d:5150/index",
      "",
      "127.0.0.1",
      "https",
      "5150",
      jwt,
      "rst",
      "rsth");
    
    // get jwt status
    JWTStatusInstance jwtStatus = jwti.jwtStatus(rsPost);
    assertNotNull(jwtStatus);
    assertEquals(JWTStatus.NOT_PRESENT_BACKEND, jwtStatus.status());
  }
}
