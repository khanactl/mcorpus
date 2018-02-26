package com.tll.mcorpus.repo;

import java.io.Closeable;
import java.util.UUID;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.db.tables.records.McuserRecord;
import com.tll.mcorpus.repo.model.FetchResult;
import com.tll.mcorpus.repo.model.LoginInput;

/**
 * MCorpus User Repository (data access).
 * <p>
 * All public methods are <em>blocking</em>!
 *
 * @author jpk
 */
public class MCorpusUserRepo implements Closeable {
  protected static final Logger log = LoggerFactory.getLogger("MCorpusUserRepo");

  protected final DSLContext dsl;

  /**
   * Constructor
   * @param ds the data source
   */
  public MCorpusUserRepo(DataSource ds) {
    Settings s = new Settings();
    s.setRenderSchema(false);
    s.setRenderNameStyle(RenderNameStyle.LOWER);
    s.setRenderKeywordStyle(RenderKeywordStyle.UPPER);
    this.dsl = DSL.using(ds, SQLDialect.POSTGRES, s);
  }

  @Override
  public void close() {
    if(dsl != null) {
      log.debug("Closing..");
      dsl.close();
      log.info("Closed.");
    }
  }

  /**
   * The mcorpus mcuser login routine which fetches the mcuser record ref 
   * whose username and password matches the ones given.
   * <p>
   * Blocking!
   *
   * @param mcuserLoginInput the mcuser login input credentials along with http header captures
   * @return Never null {@link FetchResult} object<br> 
   *         holding the {@link Mcuser} ref if successful<br>
   *         -OR- a null Mcuser ref and a non-null error message if unsuccessful.
   */
  public FetchResult<Mcuser> login(final LoginInput mcuserLoginInput) {
    if(mcuserLoginInput != null && mcuserLoginInput.isValid()) {
      log.debug("logging in {}..", mcuserLoginInput);
      try {
        final McuserLogin login = new McuserLogin();
        login.setMcuserUsername(mcuserLoginInput.getUsername());
        login.setMcuserPassword(mcuserLoginInput.getPassword());
        login.setMcuserSessionId(mcuserLoginInput.getWebSessionId());
        login.setMcuserIp(mcuserLoginInput.getIp());
        login.setMcuserHost(mcuserLoginInput.getHttpHost());
        login.setMcuserOrigin(mcuserLoginInput.getHttpOrigin());
        login.setMcuserReferer(mcuserLoginInput.getHttpReferer());
        login.setMcuserForwarded(mcuserLoginInput.getHttpForwarded());
        login.execute(dsl.configuration());
        final McuserRecord rec = login.getReturnValue();
        if(rec != null && rec.getUid() != null) {
          // login success
          final Mcuser mcuser = rec.into(Mcuser.class);
          log.info("LOGGED IN: {}", mcuser);
          return new FetchResult<>(mcuser, null);
        } else {
          // login fail - no record returned
          return new FetchResult<>(null, "Login unsuccessful.");
        }
      }
      catch(Throwable t) {
        log.error("User login error: {}.", t.getMessage());
      }
    }
    // default - login fail
    return new FetchResult<>(null, "Login failed.");
  }

  /**
   * Log an mcuser out.
   * <p>
   * Blocking!
   *
   * @param mcuserId the mcuser id
   * @param webSessionId the user's current web session id token
   * @return Never null {@link FetchResult} object 
   *         holding an error message if unsuccessful.
   */
  public FetchResult<Void> logout(final UUID mcuserId, final String webSessionId) {
    if(mcuserId != null && webSessionId != null) {
      try {
        final McuserLogout logout = new McuserLogout();
        logout.setMcuserUid(mcuserId);
        logout.setMcuserSessionId(webSessionId);
        logout.execute(dsl.configuration());
        // logout successful (no exception)
        return new FetchResult<>(null, null);
      }
      catch(Throwable t) {
        log.error("User logout error: {}.", t.getMessage());
      }
    }
    // default - logout failed
    return new FetchResult<>(null, "Logout failed.");
  }
}
