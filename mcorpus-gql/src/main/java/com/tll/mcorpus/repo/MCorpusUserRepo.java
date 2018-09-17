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

import com.tll.mcorpus.db.routines.GetJwtStatus;
import com.tll.mcorpus.db.routines.GetNumActiveLogins;
import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.db.tables.records.McuserRecord;
import com.tll.mcorpus.db.udt.pojos.JwtStatusMcuserRole;
import com.tll.mcorpus.repo.model.FetchResult;

/**
 * MCorpus User Repository (data access).
 * <p>
 * All public methods are <em>blocking</em>!
 *
 * @author jpk
 */
public class MCorpusUserRepo implements Closeable {
  protected final Logger log = LoggerFactory.getLogger("MCorpusUserRepo");

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
   * Call the backend to get the number of valid, non-expired JWT IDs issued for
   * the given mcuser id.
   * 
   * @param uid the mcuser id
   * @return the number of valid, non-expired JWT IDs held in the backend system
   */
  public FetchResult<Integer> getNumActiveLogins(final UUID uid) {
    if(uid == null) return new FetchResult<>(null, "No mcuser id provided.");
    try {
      final GetNumActiveLogins backend = new GetNumActiveLogins();
      backend.setMcuserId(uid);
      backend.execute(dsl.configuration());
      return new FetchResult<>(backend.getReturnValue(), null);
    }
    catch(Throwable e) {
      log.info("JWT number of active logins call error: {}", e.getMessage());
      return new FetchResult<>(null, "JWT number of active logins call failed.");
    }
  }
  
  /**
   * Is the given JWT id ok by way of these conditions:
   * <ul>
   * <li>JWT id is known and has 'OK' status on backend (db)
   * <li>the associated mcuser's status is valid
   * <li>fetch the mcuser's role
   * </ul>
   * @param jwtId the JWT id to check
   * @return fetch result for the JWT status and mcuser role
   */
  public FetchResult<JwtStatusMcuserRole> getJwtStatus(final UUID jwtId) {
    if(jwtId == null) return new FetchResult<>(null, "JWT status check failed: bad input.");
    try {
      final GetJwtStatus backend = new GetJwtStatus();
      backend.setJwtId(jwtId);
      backend.execute(dsl.configuration());
      JwtStatusMcuserRole rval = backend.getReturnValue().into(JwtStatusMcuserRole.class);
      return new FetchResult<>(rval, null);
    }
    catch(Throwable e) {
      log.info("JWT id validation check error: {}", e.getMessage());
      return new FetchResult<>(null, "JWT id validation check failed.");
    }
  }

  /**
   * The mcorpus mcuser login routine which fetches the mcuser record ref 
   * whose username and password matches the ones given.
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
   *
   * @param mcuserLogoutInput the mcuser logout input data object along with request snapshot
   * @return Never null {@link FetchResult} object 
   *         holding an error message if unsuccessful.
   */
  public FetchResult<Boolean> logout(final McuserLogout mcuserLogout) {
    if(mcuserLogout != null) {
      try {
        mcuserLogout.execute(dsl.configuration());
        if(mcuserLogout.getReturnValue() == Boolean.TRUE) {
          // logout success
          return new FetchResult<>(Boolean.TRUE, null);
        }
      }
      catch(Throwable t) {
        log.error("Logout error: {}.", t.getMessage());
      }
    }
    // default - logout failed
    return new FetchResult<>(Boolean.FALSE, "Logout failed.");
  }
}
