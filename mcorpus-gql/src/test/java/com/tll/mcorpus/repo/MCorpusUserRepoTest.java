package com.tll.mcorpus.repo;

import static com.tll.mcorpus.TestUtil.ds_mcweb;
import static com.tll.mcorpus.TestUtil.isTestDslMcwebTestLoaded;
import static com.tll.mcorpus.TestUtil.testDslMcweb;
import static com.tll.mcorpus.TestUtil.testDslMcwebTest;
import static com.tll.mcorpus.TestUtil.testRequestOrigin;
import static com.tll.mcorpus.db.Tables.MCUSER;
import static com.tll.mcorpus.db.Tables.MCUSER_AUDIT;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.tll.mcorpus.UnitTest;
import com.tll.mcorpus.db.enums.JwtIdStatus;
import com.tll.mcorpus.db.enums.McuserAuditType;
import com.tll.mcorpus.db.enums.McuserRole;
import com.tll.mcorpus.db.enums.McuserStatus;
import com.tll.mcorpus.db.routines.InsertMcuser;
import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.db.tables.pojos.McuserAudit;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for {@link com.tll.mcorpus.repo.MCorpusUserRepo}.
 */
@Category(UnitTest.class)
public class MCorpusUserRepoTest {
  private static final Logger log = LoggerFactory.getLogger(MCorpusUserRepoTest.class);

  static MCorpusUserRepo mcuserRepo() { return new MCorpusUserRepo(ds_mcweb()); }

  @AfterClass
  public static void clearBackend() {
    try {
      // delete mcuser_audit recs if any
      log.info("Num mcuser_audit records deleted after test: {}.",
          testDslMcwebTest().delete(MCUSER_AUDIT).where(MCUSER_AUDIT.REQUEST_ORIGIN.eq(testRequestOrigin)).execute());
      
      // delete test mcuser recs if any
      log.info("Num mcuser records deleted after test: {}.",
          testDslMcwebTest().delete(MCUSER).where(MCUSER.USERNAME.eq(TEST_MCUSER_USERNAME)).execute());
    } catch (Exception e) {
      log.error(e.getMessage());
    } finally {
      if (isTestDslMcwebTestLoaded())
        testDslMcwebTest().close();
    }
  }

  static final String TEST_MCUSER_NAME = "testerMcuser";

  static final String TEST_MCUSER_EMAIL = "testerMcuser@domain.com";

  static final String TEST_MCUSER_USERNAME = "testermcuser";

  static final String TEST_MCUSER_PSWD = "pswd33*7yuI";

  static final McuserStatus TEST_MCUSER_STATUS = McuserStatus.ACTIVE;
  
  static final McuserRole[] TEST_MCUSER_ROLES = new McuserRole[] { McuserRole.MCORPUS, McuserRole.MPII };

  /**
   * @return Newly created {@link McuserAndRoles} instance 
   *         suitable for insert into mcuser table 
   *         temporarily for testing.
   */
  static Mcuser testNewMcuserAndRoles() {
    final Mcuser mcuser = new Mcuser(
      null, 
      null, 
      null, 
      TEST_MCUSER_NAME, 
      TEST_MCUSER_EMAIL, 
      TEST_MCUSER_USERNAME, 
      TEST_MCUSER_PSWD, 
      TEST_MCUSER_STATUS,
      TEST_MCUSER_ROLES
    );
    return mcuser;
  }

  static Mcuser insertTestMcuser() {
    try {
      InsertMcuser imu = new InsertMcuser();
      imu.setInName(TEST_MCUSER_NAME);
      imu.setInEmail(TEST_MCUSER_EMAIL);
      imu.setInUsername(TEST_MCUSER_USERNAME);
      imu.setInPswd(TEST_MCUSER_PSWD);
      imu.setInStatus(TEST_MCUSER_STATUS);
      imu.setInRoles(TEST_MCUSER_ROLES);
      imu.execute(testDslMcwebTest().configuration());
      final Mcuser mcuser = imu.getReturnValue().into(Mcuser.class);
      log.info("mcuser test record added: {}", mcuser);
      assertNotNull(mcuser);
      assertNotNull(mcuser.getUid());
      return mcuser;
    }
    catch(Exception e) {
      log.error(e.getMessage());
      throw e;
    }
    finally {
      if(isTestDslMcwebTestLoaded()) testDslMcwebTest().close();
    }
  }
  
  static void deleteTestMcuser(final UUID uid) {
    try {
     log.info("Num mcuser records deleted for uid {}: {}.", 
         uid,
         testDslMcwebTest().delete(MCUSER).where(MCUSER.UID.eq(uid)).execute());
    }
    catch(Exception e) {
      log.error(e.getMessage());
    }
    finally {
      if(isTestDslMcwebTestLoaded()) testDslMcwebTest().close();
    }
  }
  
  /**
   * Add a test mcuser_audit record.
   * 
   * @return Newly created McuserAudit pojo corresponding to the added
   *         MCUSER_AUDIT test record.
   * @throws Exception upon test record insert failure
   */
  static McuserAudit addTestMcuserAuditRecord(final UUID uid) throws Exception {
    long lnow = Instant.now().toEpochMilli();
    final long expiry = lnow + Duration.ofMinutes(30).toMillis();
    
    McuserAudit e = new McuserAudit(
        UUID.randomUUID(),
        null,
        McuserAuditType.LOGIN,
        new Timestamp(lnow),
        testRequestOrigin,
        new Timestamp(expiry),
        UUID.randomUUID(),
        JwtIdStatus.OK);
    
    final int numInserted = testDslMcweb().insertInto(MCUSER_AUDIT,
        MCUSER_AUDIT.TYPE,
        MCUSER_AUDIT.JWT_ID, 
        MCUSER_AUDIT.JWT_ID_STATUS,
        MCUSER_AUDIT.UID,
        MCUSER_AUDIT.REQUEST_TIMESTAMP,
        MCUSER_AUDIT.REQUEST_ORIGIN,
        MCUSER_AUDIT.LOGIN_EXPIRATION
      ).values(
          McuserAuditType.LOGIN,
          e.getJwtId(),
          e.getJwtIdStatus(),
          uid,
          e.getRequestTimestamp(),
          e.getRequestOrigin(),
          e.getLoginExpiration()
      ).execute();
    
    if(numInserted != 1) throw new Exception("Num inserted MCUSER_AUDIT records: " + numInserted);
    
    return e;
  }

  /**
   * @return New modified copy of mcuser to use to verify mcuser update op.
   */
  static Mcuser alteredCopy(final Mcuser mcuser) {
    return new Mcuser(
      mcuser.getUid(),
      mcuser.getCreated(),
      mcuser.getModified(),
      mcuser.getUsername(),
      null,
      "nameUPDATED",
      mcuser.getEmail(),
      McuserStatus.INVALIDATED,
      new McuserRole[0] // i.e. nix roles
    );
  }

  /**
   * @return newly created {@link McuserLogin} instance for an mcuser for testing purposes.
   */
  public static McuserLogin testMcuserLoginInput() {
    final long lnow = System.currentTimeMillis();
    final long expiry = lnow + Duration.ofMinutes(30).toMillis();
    McuserLogin mcuserLogin = new McuserLogin();
    mcuserLogin.setMcuserUsername(TEST_MCUSER_USERNAME);
    mcuserLogin.setMcuserPassword(TEST_MCUSER_PSWD);
    mcuserLogin.setInJwtId(UUID.randomUUID());
    mcuserLogin.setInRequestTimestamp(new Timestamp(lnow));
    mcuserLogin.setInRequestOrigin(testRequestOrigin);
    mcuserLogin.setInLoginExpiration(new Timestamp(expiry));
    return mcuserLogin;
  }

  /**
   * @return newly created {@link McuserLogout} instance for an mcuser for testing purposes.
   */
  public static McuserLogout testMcuserLogoutInput() {
    final long lnow = System.currentTimeMillis();
    McuserLogout mcuserLogout = new McuserLogout();
    mcuserLogout.setJwtId((UUID)null);
    mcuserLogout.setMcuserUid((UUID)null);
    mcuserLogout.setRequestTimestamp(new Timestamp(lnow));
    mcuserLogout.setRequestOrigin(testRequestOrigin);
    return mcuserLogout;
  }

  /**
   * Test {MCorpusUserRepo#login} with valid user credentials.
   */
  @Test
  public void testMcuserLogin_AuthSuccess() {
    MCorpusUserRepo repo = null;
    UUID uid = null;
    try {
      final Mcuser mar = insertTestMcuser();
      uid = mar.getUid();
      
      repo = mcuserRepo();

      McuserLogin loginInput = testMcuserLoginInput();
      FetchResult<Mcuser> loginResult = repo.login(loginInput);
      log.info("mcorpus LOGIN WITH AUTH SUCCESS TEST result: {}", loginResult);
      
      Mcuser mcuser = loginResult.get();
      log.info("mcuser and roles: {}", mcuser);
      
      assertNotNull(loginResult);
      assertNull(loginResult.getErrorMsg());
      assertFalse(loginResult.hasErrorMsg());
      
      assertNotNull(mcuser);
      assertNotNull(mcuser.getUsername());
      assertNull(mcuser.getPswd());
      
      assertNotNull(mcuser.getRoles());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(uid != null) deleteTestMcuser(uid);
        repo.close();
      }
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
      repo = mcuserRepo();
      McuserLogin loginInput = testMcuserLoginInput();
      loginInput.setMcuserPassword("bunko");
      FetchResult<Mcuser> loginResult = repo.login(loginInput);
      log.info("mcorpus LOGIN WITH BAD PASSWORD TEST result: {}", loginResult);
      
      assertNotNull(loginResult);
      assertNotNull(loginResult.getErrorMsg());
      assertTrue(loginResult.hasErrorMsg());
      assertNull(loginResult.get());
      assertFalse(loginResult.isSuccess());
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
      repo = mcuserRepo();
      McuserLogin loginInput = testMcuserLoginInput();
      loginInput.setMcuserUsername("unknown");
      FetchResult<Mcuser> loginResult = repo.login(loginInput);
      log.info("mcorpus LOGIN WITH UNKNOWN USERNAME TEST result: {}", loginResult);
      
      assertNotNull(loginResult);
      assertFalse(loginResult.isSuccess());
      assertTrue(loginResult.hasErrorMsg());
      assertNotNull(loginResult.getErrorMsg());
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
    UUID uid = null;
    try {
      // establish the condition that a target test mcuser is validly logged in on the backend
      final Mcuser mar = insertTestMcuser();
      uid = mar.getUid();
      McuserAudit mcuserAudit = addTestMcuserAuditRecord(uid);
      
      repo = mcuserRepo();

      McuserLogout logoutInput = testMcuserLogoutInput();
      logoutInput.setJwtId(mcuserAudit.getJwtId());
      logoutInput.setMcuserUid(uid);
      logoutInput.setRequestOrigin(testRequestOrigin);
      logoutInput.setRequestTimestamp(new Timestamp(Instant.now().toEpochMilli()));
      FetchResult<Boolean> logoutResult = repo.logout(logoutInput);
      log.info("mcorpus LOGOUT WITH VALID LOGIN TEST result: {}", logoutResult);
      
      assertNotNull(logoutResult);
      assertTrue(logoutResult.isSuccess());
      assertTrue(logoutResult.get());
      assertNull(logoutResult.getErrorMsg());
      assertFalse(logoutResult.hasErrorMsg());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(uid != null) deleteTestMcuser(uid);
        repo.close();
      }
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
    UUID uid = null;
    try {
      final Mcuser mar = insertTestMcuser();
      uid = mar.getUid();
      
      repo = mcuserRepo();

      // create a logout input instance where the jwt id is not in the backend system
      // this is expected to fail the logout on the backend
      McuserLogout logoutInput = testMcuserLogoutInput();
      logoutInput.setJwtId(UUID.randomUUID());
      logoutInput.setMcuserUid(uid);
      logoutInput.setRequestOrigin(testRequestOrigin);
      logoutInput.setRequestTimestamp(new Timestamp(Instant.now().toEpochMilli()));
      FetchResult<Boolean> logoutResult = repo.logout(logoutInput);
      log.info("mcorpus LOGOUT WITH INVALID LOGIN TEST result: {}", logoutResult);
      
      assertNotNull(logoutResult);
      assertNotNull(logoutResult.getErrorMsg());
      assertTrue(logoutResult.hasErrorMsg());
      assertFalse(logoutResult.isSuccess());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(uid != null) deleteTestMcuser(uid);
        repo.close();
      }
    }
  }

  @Test
  public void testMcuserFetch() throws Exception {
    UUID uid = null;
    MCorpusUserRepo repo = null;
    try {
      repo = mcuserRepo();

      // insert test mcuser record
      Mcuser mcuser = insertTestMcuser();
      uid = mcuser.getUid();
      assertNotNull(uid);

      FetchResult<Mcuser> fr = repo.fetchMcuser(uid);
      assertNotNull(fr);
      assertTrue("mcuser fetch test failed", fr.isSuccess());
      assertNotNull("mcuser fetch test - no mcuser", fr.get());
      assertNotNull("mcuser fetch test - no mcuser.uid", fr.get().getUid());
      assertNotNull("mcuser fetch test - no mcuser.created", fr.get().getCreated());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(uid != null) deleteTestMcuser(uid);
        repo.close();
      }
    }
  }

  @Test
  public void testMcuserAdd() throws Exception {
    MCorpusUserRepo repo = null;
    UUID uid = null;
    try {
      repo = mcuserRepo();
      
      final Mcuser testMcuser = testNewMcuserAndRoles();
      final FetchResult<Mcuser> fr = repo.addMcuser(testMcuser);
      try { uid = fr.get().getUid(); } catch(Exception e) {}
      
      assertNotNull(fr);
      assertTrue("mcuser add test failed", fr.isSuccess());
      assertNotNull("mcuser add test - no mcuser", fr.get());
      assertNotNull("mcuser add test - no mcuser.uid", fr.get().getUid());
      assertNotNull("mcuser add test - no mcuser.created", fr.get().getCreated());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(uid != null) deleteTestMcuser(uid);
        repo.close();
      }
    }
  }

  @Test
  public void testMcuserUpdate() throws Exception {
    UUID uid = null;
    MCorpusUserRepo repo = null;
    try {
      repo = mcuserRepo();

      // insert test mcuser record
      final Mcuser mcuser = insertTestMcuser();
      try { uid = mcuser.getUid(); } catch(Exception e) {}

      // update
      final Mcuser altered = alteredCopy(mcuser);

      final FetchResult<Mcuser> fr = repo.updateMcuser(altered);
      assertNotNull(fr);
      assertTrue("mcuser update test failed", fr.isSuccess());
      assertNotNull("mcuser add test - no mcuser", fr.get());
      assertNotNull("mcuser add test - no mcuser.uid", fr.get().getUid());
      assertNotNull("mcuser add test - no mcuser.created", fr.get().getCreated());
      assertNotNull("mcuser add test - no mcuser.modified", fr.get().getModified());

      final Mcuser updated = fr.get();
      assertEquals(altered.getName(), updated.getName());
      assertEquals(altered.getStatus(), updated.getStatus());
      assertArrayEquals(altered.getRoles(), updated.getRoles());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(uid != null) deleteTestMcuser(uid);
        repo.close();
      }
    }
  }

  @Test
  public void testMcuserDelete() throws Exception {
    MCorpusUserRepo repo = null;
    UUID uid = null;
    try {
      repo = mcuserRepo();
      Mcuser testMcuser = insertTestMcuser();
      try { uid = testMcuser.getUid(); } catch(Exception e) {}

      FetchResult<Boolean> fetchResult = repo.deleteMcuser(uid);
      assertNotNull(fetchResult);
      assertNull(fetchResult.getErrorMsg());
      assertTrue(fetchResult.get());
    }
    catch(Exception e) {
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(uid != null) deleteTestMcuser(uid);
        repo.close();
      }
    }
  }

  @Test
  public void testInvalidateJwtsFor() throws Exception {
    MCorpusUserRepo repo = null;
    UUID uid = null;
    try {
      repo = mcuserRepo();
      
      Mcuser testMcuser = insertTestMcuser();
      uid = testMcuser.getUid();
      
      Instant requestInstant = Instant.now();
      String clientOrigin = testRequestOrigin;

      FetchResult<Boolean> fr = repo.invalidateJwtsFor(uid, requestInstant, clientOrigin);
      assertNotNull(fr);
      assertTrue(fr.isSuccess());
      assertTrue(fr.get());
    }
    catch(Exception e) {
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(uid != null) deleteTestMcuser(uid);
        repo.close();
      }
    }
  }
}
