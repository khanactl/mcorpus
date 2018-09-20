package com.tll.mcorpus.repo;

import static com.tll.mcorpus.TestUtil.ds_mcweb;
import static com.tll.mcorpus.TestUtil.testDslMcwebTest;
import static com.tll.mcorpus.TestUtil.isTestDslMcwebTestLoaded;
import static com.tll.mcorpus.TestUtil.testMcuserLoginInput;
import static com.tll.mcorpus.TestUtil.testRequestOrigin;
import static com.tll.mcorpus.db.Tables.MCUSER_AUDIT;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.AfterClass;
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
import com.tll.mcorpus.db.udt.pojos.McuserAndRoles;
import com.tll.mcorpus.repo.model.FetchResult;

/**
 * Unit test for {@link com.tll.mcorpus.repo.MCorpusUserRepo}.
 */
@Category(UnitTest.class)
public class MCorpusUserRepoTest {
  private static final Logger log = LoggerFactory.getLogger(MCorpusUserRepoTest.class);
  
  @AfterClass
  public static void clearBackend() {
    try {
      // using mcwebtest db creds - 
      //   remove any test generated mcuser audit records (if any).
     log.info("Num mcuser audit records deleted after test: {}.", 
         testDslMcwebTest().delete(MCUSER_AUDIT).where(MCUSER_AUDIT.REQUEST_ORIGIN.eq(testRequestOrigin)).execute());
    }
    catch(Exception e) {
      log.error(e.getMessage());
    }
    finally {
      if(isTestDslMcwebTestLoaded()) testDslMcwebTest().close();
    }
  }
  
  /**
   * Test {MCorpusUserRepo#login} with valid user credentials.
   */
  @Test
  public void testMcuserLogin_AuthSuccess() {
    MCorpusUserRepo repo = null;
    try {
      DataSource ds = ds_mcweb();
      repo = new MCorpusUserRepo(ds);

      McuserLogin loginInput = testMcuserLoginInput();
      FetchResult<McuserAndRoles> loginResult = repo.login(loginInput);
      log.info("mcorpus LOGIN WITH AUTH SUCCESS TEST result: {}", loginResult);
      
      McuserAndRoles mcuser = loginResult.get();
      log.info("mcuser and roles: {}", mcuser);
      
      assertNotNull(loginResult);
      assertNull(loginResult.getErrorMsg());
      assertFalse(loginResult.hasErrorMsg());
      
      assertNotNull(mcuser);
      assertNotNull(mcuser.getMcuser().getUsername());
      assertNull(mcuser.getMcuser().getPswd());
      
      assertNotNull(mcuser.getRoles());
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
   * Test {MCorpusUserRepo#login} with a bad password.
   * <p>
   * The mcuser login return value is expected to return null indicating login
   * failure on the backend.
   */
  @Test
  public void testMcuserLogin_BadPassword() {
    MCorpusUserRepo repo = null;
    try {
      DataSource ds = ds_mcweb();
      repo = new MCorpusUserRepo(ds);
      McuserLogin loginInput = testMcuserLoginInput();
      loginInput.setMcuserPassword("bunko");
      FetchResult<McuserAndRoles> loginResult = repo.login(loginInput);
      log.info("mcorpus LOGIN WITH BAD PASSWORD TEST result: {}", loginResult);
      
      assertNotNull(loginResult);
      assertNotNull(loginResult.getErrorMsg());
      assertTrue(loginResult.hasErrorMsg());
      assertNull(loginResult.get());
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
   * Test {MCorpusUserRepo#login} with a username not currently in the backend
   * system.
   * <p>
   * The mcuser login return value is expected to return null indicating login
   * failure on the backend.
   */
  @Test
  public void testMcuserLogin_UnknownUsername() {
    MCorpusUserRepo repo = null;
    try {
      DataSource ds = ds_mcweb();
      repo = new MCorpusUserRepo(ds);
      McuserLogin loginInput = testMcuserLoginInput();
      loginInput.setMcuserUsername("unknown");
      FetchResult<McuserAndRoles> loginResult = repo.login(loginInput);
      log.info("mcorpus LOGIN WITH UNKNOWN USERNAME TEST result: {}", loginResult);
      
      assertNotNull(loginResult);
      assertNotNull(loginResult.getErrorMsg());
      assertTrue(loginResult.hasErrorMsg());
      assertNull(loginResult.get());
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
   * Test {MCorpusUserRepo#logout} when an mcuser is currently logged in.
   * <p>
   * This is the happy path test for mcuser logout functionality.
   */
  @Test
  public void testMcuserLogout_ValidLogin() {
    MCorpusUserRepo repo = null;
    try {
      // establish the condition that a target test mcuser is validly logged in on the backend
      McuserAudit mcuserAudit = TestUtil.addTestMcuserAuditRecord();
      
      DataSource ds = ds_mcweb();
      repo = new MCorpusUserRepo(ds);

      McuserLogout logoutInput = TestUtil.testMcuserLogoutInput();
      logoutInput.setJwtId(mcuserAudit.getJwtId());
      logoutInput.setMcuserUid(TestUtil.testMcuserUid);
      logoutInput.setRequestOrigin(testRequestOrigin);
      logoutInput.setRequestTimestamp(new Timestamp(Instant.now().toEpochMilli()));
      FetchResult<Boolean> logoutResult = repo.logout(logoutInput);
      log.info("mcorpus LOGOUT WITH VALID LOGIN TEST result: {}", logoutResult);
      
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

  /**
   * Test {MCorpusUserRepo#logout} when an mcuser is NOT currently logged in.
   * <p>
   * This test verifies that the mcuser logout functionality should fail when a
   * valid mcuser is <em>not</em> currently logged in.
   */
  @Test
  public void testMcuserLogout_InvalidLogin() {
    MCorpusUserRepo repo = null;
    try {
      DataSource ds = ds_mcweb();
      repo = new MCorpusUserRepo(ds);

      // create a logout input instance where the jwt id is not in the backend system
      // this is expected to fail the logout on the backend
      McuserLogout logoutInput = TestUtil.testMcuserLogoutInput();
      logoutInput.setJwtId(UUID.randomUUID());
      logoutInput.setMcuserUid(TestUtil.testMcuserUid);
      logoutInput.setRequestOrigin(testRequestOrigin);
      logoutInput.setRequestTimestamp(new Timestamp(Instant.now().toEpochMilli()));
      FetchResult<Boolean> logoutResult = repo.logout(logoutInput);
      log.info("mcorpus LOGOUT WITH INVALID LOGIN TEST result: {}", logoutResult);
      
      assertNotNull(logoutResult);
      assertNotNull(logoutResult.getErrorMsg());
      assertTrue(logoutResult.hasErrorMsg());
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
