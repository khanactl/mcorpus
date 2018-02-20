package com.tll.mcorpus.repo;

import com.tll.mcorpus.UnitTest;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.repo.model.FetchResult;
import com.tll.mcorpus.repo.model.LoginInput;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.sql.Connection;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.*;

/**
 * Unit test for {@link com.tll.mcorpus.repo.MCorpusUserRepo}.
 */
@Category(UnitTest.class)
public class MCorpusUserRepoTest {
  private static final Logger log = LoggerFactory.getLogger(MCorpusUserRepoTest.class);

  private static DataSource ds() {
    PGSimpleDataSource ds = new PGSimpleDataSource();
    ds.setUrl("jdbc:postgresql://localhost:5432/mcorpus");
    ds.setUser("mcweb");
    ds.setPassword("YAcsR6*-L;djIaX1~%zBa");
    ds.setCurrentSchema("public");
    return ds;
  }

  @Test @Ignore
  public void testGetConnection() {
    Connection cnc = null;
    try {
      DataSource ds = ds();
      assertNotNull(ds);
      cnc = ds.getConnection();
      assertNotNull(cnc);
    }
    catch (Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(cnc != null) try { cnc.close(); } catch(Exception e) {}
    }
  }

  /**
   * Test {MCorpusUserRepo#login} with valid user credentials.
   */
  @Test
  public void testLoginAuthSuccess() {
    MCorpusUserRepo repo = null;
    try {
      DataSource ds = ds();
      repo = new MCorpusUserRepo(ds);

      LoginInput loginInput = new LoginInput("test", "jackson", "webSessionId", "ip", "httpHost", "httpOrigin", "httpReferer", "httpForwarded");
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
  public void testLoginAuthFail() {
    MCorpusUserRepo repo = null;
    try {
      DataSource ds = ds();
      repo = new MCorpusUserRepo(ds);

      LoginInput loginInput = new LoginInput("test", "badpswd", "webSessionId", "ip", "httpHost", "httpOrigin", "httpReferer", "httpForwarded");
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
}
