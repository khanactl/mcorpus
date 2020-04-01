package com.tll.mcorpus;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import javax.sql.DataSource;

import com.tll.jwt.IJwtBackendHandler;
import com.tll.jwt.IJwtHttpResponseAction;
import com.tll.jwt.IJwtUser;
import com.tll.jwt.JWT;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.mcorpus.web.MCorpusJwtBackendHandler;
import com.tll.repo.FetchResult;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderKeywordCase;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.postgresql.ds.PGSimpleDataSource;

public class MCorpusTestUtil {

  public static final InetAddress testRequestOrigin;

  public static final String testServerPublicAddress = "https://mcorpus.d2d";

  private static DSLContext dslMcweb = null;

  private static DSLContext dslMcwebtest = null;

  static {
    try {
      testRequestOrigin = InetAddress.getByName("127.0.0.1");
    } catch(Exception e) {
      throw new Error(e);
    }
  }

  /**
   * @return A newly created {@link DataSource} to the test database intended for
   *         testing.
   */
  public static DataSource ds_mcweb() {
    PGSimpleDataSource ds = new PGSimpleDataSource();
    final String jdbcUrl = System.getenv("MCORPUS_DB_URL");
    ds.setUrl(jdbcUrl);
    return ds;
  }

  /**
   * @return A newly created {@link DataSource} to the test database intended for
   *         testing.
   */
  public static DataSource ds_mcwebtest() {
    PGSimpleDataSource ds = new PGSimpleDataSource();
    final String jdbcUrl = System.getenv("MCORPUS_TEST_DB_URL");
    ds.setUrl(jdbcUrl);
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
      s.setRenderNameCase(RenderNameCase.LOWER);
      s.setRenderKeywordCase(RenderKeywordCase.UPPER);
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
      s.setRenderNameCase(RenderNameCase.LOWER);
      s.setRenderKeywordCase(RenderKeywordCase.UPPER);
      dslMcwebtest = DSL.using(ds_mcwebtest(), SQLDialect.POSTGRES, s);
    }
    return dslMcwebtest;
  }

  public static IJwtUser mockJwtUser() {
    return new IJwtUser() {

      @Override
      public String[] getJwtUserRoles() {
        return new String[] { "ADMIN" };
      }

      @Override
      public UUID getJwtUserId() {
        return UUID.randomUUID();
      }
    };
  }

  public static IJwtBackendHandler mockJwtBackendHandler() {
    return new IJwtBackendHandler() {

      final IJwtUser jwtUser = mockJwtUser();

      @Override
      public FetchResult<Boolean> jwtInvalidateAllForUser(UUID jwtUserId, InetAddress requestOrigin,
          Instant requestInstant) {
        return FetchResult.fetchrslt(Boolean.TRUE);
      }

      @Override
      public FetchResult<Boolean> jwtBackendLogout(UUID jwtUserId, UUID jwtId, InetAddress requestOrigin,
          Instant requestInstant) {
          return FetchResult.fetchrslt(Boolean.TRUE);
      }

      @Override
      public FetchResult<IJwtUser> jwtBackendLogin(String username, String pswd, UUID pendingJwtId,
          InetAddress requestOrigin, Instant requestInstant, Instant jwtExpiration) {
        return FetchResult.fetchrslt(jwtUser);
      }

      @Override
      public FetchResult<Integer> getNumActiveJwtLogins(UUID jwtUserId) {
        return FetchResult.fetchrslt(2);
      }

      @Override
      public FetchResult<JwtBackendStatus> getBackendJwtStatus(UUID jwtId) {
        return FetchResult.fetchrslt(JwtBackendStatus.NOT_PRESENT);
      }
    };
  }

  /**
   * @return Newly created {@link JWT} instance suitable for testing.
   */
  public static JWT jwt() {
    byte[] jwtSharedSecret = JWT.generateJwtSharedSecret();
    Duration jwtTtl = Duration.ofDays(2);
    return new JWT(mockJwtBackendHandler(), jwtTtl, jwtSharedSecret, testServerPublicAddress);
  }

  /**
   * @return Newly created {@link IJwtBackendHandler} instance suitable for testing.
   */
  public static IJwtBackendHandler testJwtBackendHandler() {
    return new MCorpusJwtBackendHandler(new MCorpusUserRepo(ds_mcweb()));
  }

  /**
   * @return Newly created {@link IJwtHttpResponseAction} instance
   *         whose implementation methods are no-ops (they do nothing).
   */
  public static IJwtHttpResponseAction testJwtResponseProvider() {
    return new IJwtHttpResponseAction(){

      @Override
      public void setJwtClientside(String jwt, Duration jwtTimeToLive) {
        // testing no-op
      }

      @Override
      public void expireJwtClientside() {
        // testing no-op
      }
    };
  }

  private MCorpusTestUtil() {}
}
