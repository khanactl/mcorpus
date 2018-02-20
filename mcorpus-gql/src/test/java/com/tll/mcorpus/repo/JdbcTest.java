package com.tll.mcorpus.repo;

import com.tll.mcorpus.UnitTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

/**
 * Simple unit test to verify jdbc connectivity to the mcorpus com.tll.mcorpus.db.
 */
@Category(UnitTest.class) @Ignore
public class JdbcTest {

  private static final Logger log = LogManager.getLogger();

  @Test
  public void test() {
    PGSimpleDataSource ds = new PGSimpleDataSource();
    ds.setUrl("jdbc:postgresql://localhost:5432/mcorpus");
    ds.setUser("mcweb");
    ds.setPassword("YAcsR6*-L;djIaX1~%zBa");
    Connection cnc = null;
    try {
      cnc = ds.getConnection();
      assertNotNull(cnc);

      final String schema = cnc.getSchema();
      log.info("mcorpus schema: {}", schema);

      final Properties clientInfo = cnc.getClientInfo();
      final String clientInfoTok = clientInfo.toString();
      log.info("mcorpus clientInfoTok: {}", clientInfoTok);
    }
    catch (SQLException e) {
      log.error(e.getMessage());
    }
    finally {
      if(cnc != null) {
        try { cnc.close(); } catch (SQLException e) {}
        cnc = null;
      }
    }
  }
}
