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

import com.tll.mcorpus.db.routines.JwtIdOk;
import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.db.tables.records.McuserRecord;
import com.tll.mcorpus.repo.model.FetchResult;

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
   * Is the given JWT id ok by way of these conditions:
   * <ul>
   * <li>JWT id is known and has 'OK' status on backend (db)
   * <li>the associated mcuser's status is valid
   * </ul>
   * @param jwtId the JWT id to check
   * @return fetch result around a Boolean indicating JWT id validity 
   */
  public FetchResult<Boolean> isJwtIdValid(final UUID jwtId) {
    if(jwtId == null) return new FetchResult<>(null, "JWT status check failed: bad input.");
    try {
      final JwtIdOk backend = new JwtIdOk();
      backend.setJwtId(jwtId);
      backend.execute(dsl.configuration());
      return new FetchResult<>(backend.getReturnValue(), null);
    }
    catch(Throwable e) {
      log.info("JWT id validation check error: {}", e.getMessage());
      return new FetchResult<>(null, "JWT id validation check failed.");
    }
  }

  /**
   * The mcorpus mcuser login routine which fetches the mcuser record ref 
   * whose username and password matches the ones given.
   * <p>
   * Blocking!
   *
   * @param mcuserLogin the mcuser login input credentials
   * @return Never null {@link FetchResult} object<br> 
   *         holding the {@link Mcuser} ref if successful<br>
   *         -OR- a null Mcuser ref and a non-null error message if unsuccessful.
   */
  public FetchResult<Mcuser> login(final McuserLogin mcuserLogin) {
    if(mcuserLogin != null) {
      try {
        mcuserLogin.execute(dsl.configuration());
        final McuserRecord rec = mcuserLogin.getReturnValue();
        if(rec != null && rec.getUid() != null) {
          // login success
          final Mcuser mcuser = rec.into(Mcuser.class);
          log.info("LOGGED IN: {}", mcuser.getUid());
          return new FetchResult<>(mcuser, null);
        } else {
          // login fail - no record returned
          return new FetchResult<>(null, "Login unsuccessful.");
        }
      }
      catch(Throwable t) {
        log.error("Login error: {}.", t.getMessage());
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
   * @param mcuserLogoutInput the mcuser logout input data object along with request snapshot
   * @return Never null {@link FetchResult} object 
   *         holding an error message if unsuccessful.
   */
  public FetchResult<Boolean> logout(final McuserLogout mcuserLogout) {
    if(mcuserLogout != null) {
      try {
        mcuserLogout.execute(dsl.configuration());
        // logout successful (no exception)
        log.info("LOGGED OUT.");
        return new FetchResult<>(Boolean.TRUE, null);
      }
      catch(Throwable t) {
        log.error("Logout error: {}.", t.getMessage());
      }
    }
    // default - logout failed
    return new FetchResult<>(Boolean.FALSE, "Logout failed.");
  }
}
