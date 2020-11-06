package com.tll.mcorpus.repo;

import static com.tll.core.Util.isNotNullOrEmpty;
import static com.tll.mcorpus.MCorpusTestUtil.ds_mcweb;
import static com.tll.mcorpus.MCorpusTestUtil.testDslMcweb;
import static com.tll.mcorpus.MCorpusTestUtil.testDslMcwebTest;
import static com.tll.mcorpus.MCorpusTestUtil.testRequestOrigin;
import static com.tll.mcorpus.db.Tables.MCUSER;
import static com.tll.mcorpus.db.Tables.MCUSER_AUDIT;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.tll.UnitTest;
import com.tll.mcorpus.db.enums.JwtIdStatus;
import com.tll.mcorpus.db.enums.McuserAuditType;
import com.tll.mcorpus.db.enums.McuserRole;
import com.tll.mcorpus.db.enums.McuserStatus;
import com.tll.mcorpus.db.routines.InsertMcuser;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.db.tables.pojos.McuserAudit;
import com.tll.mcorpus.dmodel.ActiveLoginDomain;
import com.tll.mcorpus.dmodel.McuserHistoryDomain;
import com.tll.repo.FetchResult;

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
    }
  }

  static final String TEST_MCUSER_NAME = "testerMcuser";

  static final String TEST_MCUSER_EMAIL = "testerMcuser@domain.com";

  static final String TEST_MCUSER_USERNAME = "testermcuser";

  static final String TEST_MCUSER_USERNAME_UNKNOWN = "unknown";

  static final String TEST_MCUSER_PSWD = "pswd33*7yuI";

  static final String TEST_MCUSER_BAD_PSWD = "bunko";

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
  }

  /**
   * Add a test mcuser_audit record.
   *
   * @return Newly created McuserAudit pojo corresponding to the added
   *         MCUSER_AUDIT test record.
   * @throws Exception upon test record insert failure
   */
  static McuserAudit addTestMcuserAuditRecord(final UUID uid) throws Exception {
    Instant now = Instant.now();
    final Instant expiry = now.plus(Duration.ofMinutes(30));
    final ZoneOffset zo = ZoneOffset.systemDefault().getRules().getOffset(now);

    McuserAudit e = new McuserAudit(
        UUID.randomUUID(),
        null,
        McuserAuditType.LOGIN,
        now.atOffset(zo),
        testRequestOrigin,
        expiry.atOffset(zo),
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
      McuserStatus.INACTIVE,
      new McuserRole[0] // i.e. nix roles
    );
  }

  static FetchResult<Mcuser> mcuserLogin(MCorpusUserRepo repo, String username, String pswd, UUID pendingJwtId) {
    final Instant lnow = Instant.now();
    final Instant expiry = lnow.plus(Duration.ofMinutes(30));
    FetchResult<Mcuser> fr = repo.login(
      username,
      pswd,
      pendingJwtId,
      expiry,
      lnow,
      testRequestOrigin
    );
    return fr;
  }

  static FetchResult<Mcuser> mcuserRefresh(MCorpusUserRepo repo, UUID oldJwtId, UUID newJwtId) {
    final Instant lnow = Instant.now();
    final Instant expiry = lnow.plus(Duration.ofMinutes(30));
    FetchResult<Mcuser> fr = repo.loginRefresh(
      oldJwtId,
      newJwtId,
      expiry,
      lnow,
      testRequestOrigin
    );
    return fr;
  }

  static FetchResult<Boolean> mcuserLogout(MCorpusUserRepo repo, UUID mcuserId, UUID jwtId) {
    final Instant lnow = Instant.now();
    FetchResult<Boolean> fr = repo.logout(
      mcuserId,
      jwtId,
      lnow,
      testRequestOrigin
    );
    return fr;
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

      UUID pendingJwtId = UUID.randomUUID();

      FetchResult<Mcuser> loginResult = mcuserLogin(repo, TEST_MCUSER_USERNAME, TEST_MCUSER_PSWD, pendingJwtId);
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
      UUID pendingJwtId = UUID.randomUUID();
      FetchResult<Mcuser> loginResult = mcuserLogin(repo, TEST_MCUSER_USERNAME, TEST_MCUSER_BAD_PSWD, pendingJwtId);
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
      UUID pendingJwtId = UUID.randomUUID();
      FetchResult<Mcuser> loginResult = mcuserLogin(repo, TEST_MCUSER_USERNAME_UNKNOWN, TEST_MCUSER_BAD_PSWD, pendingJwtId);
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
  }

  @Test
  public void testMcuserLoginRefresh() {
    MCorpusUserRepo repo = null;
    try {
      repo = mcuserRepo();
      UUID oldJwtId = UUID.randomUUID();
      UUID newJwtId = UUID.randomUUID();
      mcuserLogin(repo, TEST_MCUSER_USERNAME_UNKNOWN, TEST_MCUSER_BAD_PSWD, oldJwtId);
      FetchResult<Mcuser> loginRefreshResult = mcuserRefresh(repo, oldJwtId, newJwtId);
      assertNotNull(loginRefreshResult);
      assertFalse(loginRefreshResult.isSuccess());
      assertTrue(loginRefreshResult.hasErrorMsg());
      assertNotNull(loginRefreshResult.getErrorMsg());
      assertNull(loginRefreshResult.get());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
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
      FetchResult<Boolean> logoutResult = mcuserLogout(repo, uid, mcuserAudit.getJwtId());
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
      FetchResult<Boolean> logoutResult = mcuserLogout(repo, uid, UUID.randomUUID());
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
      }
    }
  }

  @Test
  public void testMcuserSetPswd() throws Exception {
    MCorpusUserRepo repo = null;
    UUID uid = null;
    try {
      repo = mcuserRepo();
      Mcuser testMcuser = insertTestMcuser();
      try { uid = testMcuser.getUid(); } catch(Exception e) {}

      FetchResult<Boolean> fetchResult = repo.setPswd(uid, "test123");
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
      InetAddress clientOrigin = testRequestOrigin;

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
      }
    }
  }

  @Test
  public void testGetActiveLogins() throws Exception {
    MCorpusUserRepo repo = null;
    UUID uid = null;
    try {
      // establish the condition that a target test mcuser is validly logged in on the backend
      final Mcuser mar = insertTestMcuser();
      uid = mar.getUid();

      addTestMcuserAuditRecord(uid);

      repo = mcuserRepo();
      FetchResult<List<ActiveLoginDomain>> fr = repo.getActiveLogins(uid);

      assertNotNull(fr);
      assertTrue(fr.isSuccess());
      assertTrue(isNotNullOrEmpty(fr.get()));
      assertNull(fr.getErrorMsg());
      assertFalse(fr.hasErrorMsg());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(uid != null) deleteTestMcuser(uid);
      }
    }
  }

  @Test
  public void testMcuserHistory() throws Exception {
    MCorpusUserRepo repo = null;
    UUID uid = null;
    try {
      // establish the condition that a target test mcuser is validly logged in on the backend
      final Mcuser mar = insertTestMcuser();
      uid = mar.getUid();
      addTestMcuserAuditRecord(uid);

      repo = mcuserRepo();
      FetchResult<McuserHistoryDomain> fr = repo.mcuserHistory(uid);

      assertNotNull(fr);
      assertTrue(fr.isSuccess());
      assertTrue(fr.get() != null);
      assertNotNull(fr.get().logins);
      assertTrue(fr.get().logins.size() == 1);
      assertNull(fr.getErrorMsg());
      assertFalse(fr.hasErrorMsg());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(uid != null) deleteTestMcuser(uid);
      }
    }
  }
}
