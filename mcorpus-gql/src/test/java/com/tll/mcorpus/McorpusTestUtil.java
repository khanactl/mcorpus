package com.tll.mcorpus;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.postgresql.ds.PGSimpleDataSource;

public class McorpusTestUtil {
  
  public static final String testRequestOrigin = "localhost|localhost";

  public static final String testServerPublicAddress = "https://mcorpus.d2d";

  private static DSLContext dslMcweb = null;
  
  private static DSLContext dslMcwebtest = null;

  /**
   * @return A newly created {@link DataSource} to the test database intended for
   *         testing.
   */
  public static DataSource ds_mcweb() {
    PGSimpleDataSource ds = new PGSimpleDataSource();
    ds.setUrl(System.getenv("MCORPUS_DB_URL"));
    return ds;
  }
  
  /**
   * @return A newly created {@link DataSource} to the test database intended for
   *         testing.
   */
  public static DataSource ds_mcwebtest() {
    PGSimpleDataSource ds = new PGSimpleDataSource();
    ds.setUrl(System.getenv("MCORPUS_TEST_DB_URL"));
    return ds;
  }
  
  /**
   * @return true if the internally managed test dsl context has been loaded.
   */
  public static synchronized boolean isTestDslMcwebLoaded() {
    return dslMcweb != null;
  }

  /**
   * @return a JooQ {@link DSLContext} intended for testing with <em>mcweb</em> db
   *         credentials.
   */
  public static synchronized DSLContext testDslMcweb() {
    if(dslMcweb == null) {
      Settings s = new Settings();
      s.setRenderSchema(false);
      s.setRenderNameStyle(RenderNameStyle.LOWER);
      s.setRenderKeywordStyle(RenderKeywordStyle.UPPER);
      dslMcweb = DSL.using(ds_mcweb(), SQLDialect.POSTGRES, s);
    }
    return dslMcweb;
  }

  /**
   * @return true if the internally managed mcwebtest dsl context has been loaded.
   */
  public static synchronized boolean isTestDslMcwebTestLoaded() {
    return dslMcwebtest != null;
  }

  /**
   * @return a JooQ {@link DSLContext} intended for testing with <em>mcwebtest</em> db
   *         credentials.
   */
  public static synchronized DSLContext testDslMcwebTest() {
    if(dslMcwebtest == null) {
      Settings s = new Settings();
      s.setRenderSchema(false);
      s.setRenderNameStyle(RenderNameStyle.LOWER);
      s.setRenderKeywordStyle(RenderKeywordStyle.UPPER);
      dslMcwebtest = DSL.using(ds_mcwebtest(), SQLDialect.POSTGRES, s);
    }
    return dslMcwebtest;
  }

  private McorpusTestUtil() {}
}