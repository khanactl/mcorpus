package com.tll.mcorpus.repo;

import static com.tll.mcorpus.TestUtil.ds;
// import static com.tll.mcorpus.TestUtil.dsl;
import static com.tll.mcorpus.TestUtil.testMcuserLoginInput;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.time.Instant;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.TestUtil;
import com.tll.mcorpus.UnitTest;
import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.db.tables.pojos.McuserAudit;
import com.tll.mcorpus.repo.model.FetchResult;

/**
 * Unit test for {@link com.tll.mcorpus.repo.MCorpusUserRepo}.
 */
@Category(UnitTest.class)
public class MCorpusUserRepoTest {
  private static final Logger log = LoggerFactory.getLogger(MCorpusUserRepoTest.class);

  /**
   * Test {MCorpusUserRepo#login} with valid user credentials.
   */
  @Test
  public void testMcuserLogin_AuthSuccess() {
    MCorpusUserRepo repo = null;
    try {
      DataSource ds = ds();
      repo = new MCorpusUserRepo(ds);

      McuserLogin loginInput = testMcuserLoginInput();
      FetchResult<Mcuser> loginResult = repo.login(loginInput);
      log.info("mcorpus login TEST result: {}", loginResult);
      assertNotNull(loginResult);
      assertNull(loginResult.getErrorMsg());
      assertFalse(loginResult.hasErrorMsg());
      Mcuser mcuser = loginResult.get();
      assertNotNull(mcuser);
      assertNotNull(mcuser.getUsername());
      assertNull(mcuser.getPswd());
      log.info("mcuser: {}", mcuser);
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) repo.close();
    }
  }

  /**
   * Test {MCorpusUserRepo#login} with INVALID user credentials.
   */
  @Test
  public void testMcuserLogin_AuthFail() {
    MCorpusUserRepo repo = null;
    try {
      DataSource ds = ds();
      repo = new MCorpusUserRepo(ds);
      McuserLogin loginInput = testMcuserLoginInput();
      loginInput.setMcuserPassword("bunko");
      FetchResult<Mcuser> loginResult = repo.login(loginInput);
      log.info("mcorpus login TEST result: {}", loginResult);
      assertNotNull(loginResult);
      assertNotNull(loginResult.getErrorMsg());
      assertTrue(loginResult.hasErrorMsg());
      Mcuser mcuser = loginResult.get();
      assertNull(mcuser);
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) repo.close();
    }
  }
  
  /**
   * Test {MCorpusUserRepo#logout}.
   */
  @Test
  public void testMcuserLogout() {
    MCorpusUserRepo repo = null;
    try {
      McuserAudit mcuserAudit = TestUtil.addTestMcuserAuditRecord();
      DataSource ds = ds();
      repo = new MCorpusUserRepo(ds);

      McuserLogout logoutInput = TestUtil.testMcuserLogoutInput();
      logoutInput.setJwtId(mcuserAudit.getJwtId());
      logoutInput.setMcuserUid(TestUtil.testMcuserUid);
      logoutInput.setRequestOrigin("request-origin");
      logoutInput.setRequestTimestamp(new Timestamp(Instant.now().toEpochMilli()));
      FetchResult<Boolean> logoutResult = repo.logout(logoutInput);
      log.info("mcorpus logout TEST result: {}", logoutResult);
      assertNotNull(logoutResult);
      assertNull(logoutResult.getErrorMsg());
      assertFalse(logoutResult.hasErrorMsg());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) repo.close();
    }
  }

  
}
