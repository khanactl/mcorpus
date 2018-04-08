package com.tll.mcorpus.web;

import static com.tll.mcorpus.TestUtil.ds_mcweb;
import static com.tll.mcorpus.TestUtil.testServerPublicAddress;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.TestUtil;
import com.tll.mcorpus.UnitTest;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.mcorpus.web.JWT.JWTStatus;
import com.tll.mcorpus.web.JWT.JWTStatusInstance;

/**
 * Unit test for {@link JWT}.
 * 
 * @author d2d
 */
@Category(UnitTest.class)
public class JWTTest {
  
  private static final Logger log = LoggerFactory.getLogger(JWTTest.class);
  
  private static JWT jwt() {
    MCorpusUserRepo repo = new MCorpusUserRepo(ds_mcweb());
    byte[] jwtSharedSecret = JWT.generateJwtSharedSecret();
    long jwtTtlInMillis = Duration.ofDays(2).toMillis();
    return new JWT(jwtTtlInMillis, jwtSharedSecret, repo, testServerPublicAddress);
  }
  
  @Test
  public void testJwtSalt() throws Exception {
    final byte[] randarr = JWT.generateJwtSharedSecret();
    final String s = JWT.serialize(randarr);
    final byte[] derandarr = JWT.deserialize(s);
    assertTrue("JWT serialize/de-serialize mis-match.", Arrays.equals(randarr, derandarr));
    System.out.println(String.format("Random 32-byte hex token: %s", s));
  }

  @Test
  public void testJwtGenerateAndParse() throws Exception {
    final JWT jwti = jwt();
    
    // generate
    Instant now = Instant.now();
    UUID jwtId = UUID.randomUUID();
    String issuer = testServerPublicAddress;
    String audience = "127.0.0.1";
    String jwt = jwti.generate(now, TestUtil.testMcuserUid, jwtId, issuer, audience);
    log.info("JWT generated: {}", jwt);
    
    // parse
    JWTStatusInstance jwtStatus = jwti.jwtRequestStatus(new RequestSnapshot(
        Instant.now(),
        "127.0.0.1",
        "localhost",
        "Origin",
        "Referer",
        "Forwarded",
        jwt,
        "sid",
        "rst",
        "rsth"));
    assertEquals(JWTStatus.NOT_PRESENT_BACKEND, jwtStatus.status());
  }
}
