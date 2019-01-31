package com.tll.mcorpus.repo;

import static com.tll.mcorpus.Util.isNull;
import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;
import static com.tll.mcorpus.Util.flatten;

import static com.tll.mcorpus.repo.MCorpusDataValidator.validateMcuserToAdd;
import static com.tll.mcorpus.repo.MCorpusDataValidator.validateMcuserToUpdate;

import static com.tll.mcorpus.db.Tables.MCUSER;
import static com.tll.mcorpus.db.Tables.MCUSER_AUDIT;

import java.io.Closeable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.db.enums.JwtStatus;
import com.tll.mcorpus.db.enums.McuserAuditType;
import com.tll.mcorpus.db.enums.McuserRole;
import com.tll.mcorpus.db.routines.BlacklistJwtIdsFor;
import com.tll.mcorpus.db.routines.GetJwtStatus;
import com.tll.mcorpus.db.routines.GetNumActiveLogins;
import com.tll.mcorpus.db.routines.InsertMcuser;
import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.db.routines.McuserPswd;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.repo.model.McuserToUpdate;
import com.tll.mcorpus.repo.model.FetchResult;
import com.tll.mcorpus.repo.model.IJwtStatusProvider;
import com.tll.mcorpus.repo.model.McuserHistory;
import com.tll.mcorpus.repo.model.McuserToAdd;
import com.tll.mcorpus.repo.model.McuserHistory.LoginEvent;
import com.tll.mcorpus.repo.model.McuserHistory.LogoutEvent;

/**
 * MCorpus User Repository (data access).
 * <p>
 * All public methods are <em>blocking</em>!
 *
 * @author jpk
 */
public class MCorpusUserRepo implements Closeable, IJwtStatusProvider {
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
  public FetchResult<JwtStatus> getJwtStatus(final UUID jwtId) {
    if(jwtId == null) return new FetchResult<>(null, "JWT status check failed: bad input.");
    try {
      final GetJwtStatus backend = new GetJwtStatus();
      backend.setJwtId(jwtId);
      backend.execute(dsl.configuration());
      JwtStatus rval = backend.getReturnValue();
      return new FetchResult<>(rval, null);
    }
    catch(Throwable e) {
      log.info("JWT id validation check error: {}", e.getMessage());
      return new FetchResult<>(null, "JWT id validation check failed.");
    }
  }

  /**
   * Fetch the login and logout history for an mcuser.
   * 
   * @param uid the mcuser id
   */
  public FetchResult<McuserHistory> mcuserHistory(final UUID uid) {
    if(uid == null) return new FetchResult<>(null, "No mcuser id provided.");
    try {
      final Result<Record3<UUID, Timestamp, McuserAuditType>> result = dsl
        .select(MCUSER_AUDIT.JWT_ID, MCUSER_AUDIT.CREATED, MCUSER_AUDIT.TYPE)
        .from(MCUSER_AUDIT)
        .where(MCUSER_AUDIT.UID.eq(uid))
        .orderBy(MCUSER_AUDIT.CREATED.desc())
        .fetch();
      if(result.isNotEmpty()) {
        final List<LoginEvent> logins = new ArrayList<>();
        final List<LogoutEvent> logouts = new ArrayList<>();
        final Iterator<Record3<UUID, Timestamp, McuserAuditType>> itr = result.iterator();
        while(itr.hasNext()) {
          Record3<UUID, Timestamp, McuserAuditType> rec = itr.next();
          UUID jwtId = rec.get(MCUSER_AUDIT.JWT_ID);
          Timestamp created = rec.get(MCUSER_AUDIT.CREATED);
          switch(rec.get(MCUSER_AUDIT.TYPE)) {
            case LOGIN:
              logins.add(new LoginEvent(jwtId, created));
              break;
            case LOGOUT:
              logouts.add(new LogoutEvent(jwtId, created));
              break;
          }
        }
        return new FetchResult<>(new McuserHistory(uid, logins, logouts), null);
      } else {
        // no history
        return new FetchResult<>(new McuserHistory(uid), null);
      }
    }
    catch(Throwable e) {
      log.error("Error fetching mcuser ('{}') history: {}", uid, e.getMessage());
    }

    // fail
    return new FetchResult<>(null, "mcuser history fetch failed.");
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
        final Mcuser rval = mcuserLogin.getReturnValue().into(Mcuser.class);
        if(rval != null && rval.getUid() != null) {
          // login success
          return new FetchResult<>(rval, null);
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

  /**
   * Fetch an mcuser.
   * 
   * @param uid the mcuser id
   * @return Never null fetch result containing the fetched 
   *         {@link Mcuser} or an error message if unsuccessful.
   */
  public FetchResult<Mcuser> fetchMcuser(final UUID uid) {
    if(uid == null) return new FetchResult<>(null, "No mcuser id provided.");
    String emsg;
    try {
      final Mcuser mcuser = dsl
              .select(MCUSER.UID, MCUSER.CREATED, MCUSER.MODIFIED, MCUSER.NAME, MCUSER.EMAIL, MCUSER.USERNAME, DSL.val((String) null), MCUSER.STATUS, MCUSER.ROLES)
              .from(MCUSER)
              .where(MCUSER.UID.eq(uid))
              .fetchOne().into(Mcuser.class);

      return mcuser != null ? 
        new FetchResult<>(mcuser, null) 
      : new FetchResult<>(null, String.format("No mcuser found with uid: '%s'.", uid));
    }
    catch(DataAccessException dae) {
      log.error(dae.getMessage());
      emsg = "A data access exception occurred fetching mcuser.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching mcuser.";
    }
    // error
    return new FetchResult<>(null, emsg);
  }

  /**
   * Add an mcuser.
   * 
   * @param mcuserToAdd the mcuser to be added with optional set of roles
   * @return Never null fetch result containing an {@link McuserAndRoles} instance
   *         conveying the added mcuser and associated roles if successful
   */
  public FetchResult<Mcuser> addMcuser(final McuserToAdd mcuserToAdd) {
    if(isNull(mcuserToAdd)) return new FetchResult<>(null, "No mcuser provided.");

    final List<String> emsgs = new ArrayList<>();

    // validate the member properties
    validateMcuserToAdd(mcuserToAdd, emsgs);
    if(not(emsgs.isEmpty())) {
      return new FetchResult<>(null, flatten(emsgs, ","));
    }

    final List<Object> rlist = new ArrayList<>(1);
    
    try {
      dsl.transaction(configuration -> {
        final McuserRole[] aroles = 
          isNullOrEmpty(mcuserToAdd.getRoles()) ? 
            null : mcuserToAdd.getRoles().toArray(new McuserRole[0]);

        InsertMcuser routine = new InsertMcuser();
        routine.setInName(mcuserToAdd.getName());
        routine.setInEmail(mcuserToAdd.getEmail());
        routine.setInUsername(mcuserToAdd.getUsername());
        routine.setInPswd(mcuserToAdd.getPswd());
        routine.setInStatus(mcuserToAdd.getInitialStatus());
        routine.setInRoles(aroles);
        routine.execute(configuration);
        final Mcuser mcuserAdded = routine.getReturnValue().into(Mcuser.class);
        if(isNull(mcuserAdded) || isNull(mcuserAdded.getUid())) 
          throw new DataAccessException("No post-insert mcuser record returned.");
        rlist.add(mcuserAdded);
      });
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsgs.add("A data access exception occurred.");
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsgs.add("A technical error occurred adding mcuser.");
    }

    if(not(isNullOrEmpty(emsgs))) {
      // mcuser add fail
      return new FetchResult<>(null, flatten(emsgs, ","));
    }

    // success
    final Mcuser addedMcuser = (Mcuser) rlist.get(0);
    return new FetchResult<Mcuser>(addedMcuser, null);
  }

  /**
   * Update an mcuser.
   * 
   * @param mcuserToUpdate the mcuser and optional roles to be updated.
   *                       <p>
   *                       If no roles are present, no roles updating happens
   * @return Never null fetch result containing an {@link McuserAndRoles} instance
   *         conveying the updated mcuser and associated roles or an error message
   *         if unsuccessful.
   */
  public FetchResult<Mcuser> updateMcuser(final McuserToUpdate mcuserToUpdate) {
    if(isNull(mcuserToUpdate)) return new FetchResult<>(null, "No mcuser provided.");
    
    final List<String> emsgs = new ArrayList<>();
    
    // validate the member properties
    validateMcuserToUpdate(mcuserToUpdate, emsgs);
    if(not(emsgs.isEmpty())) {
      return new FetchResult<>(null, flatten(emsgs, ","));
    }

    // transform for persistence
    final UUID uid = mcuserToUpdate.uid;
    final Map<String, Object> mcuserUpdateMap = mcuserToUpdate.asUpdateMap();

    final List<Object> rlist = new ArrayList<>(1);
    
    try {
      dsl.transaction(configuration -> {

        final DSLContext trans = DSL.using(configuration);

        // update mcuser
        final Mcuser mcuserUpdated = trans
          .update(MCUSER)
          .set(mcuserUpdateMap)
          .where(MCUSER.UID.eq(uid))
          .returningResult(MCUSER.UID, MCUSER.CREATED, MCUSER.MODIFIED, MCUSER.NAME, MCUSER.EMAIL, MCUSER.USERNAME, DSL.val((String) null), MCUSER.STATUS, MCUSER.ROLES).fetchOne().into(Mcuser.class);
        rlist.add(mcuserUpdated);

        // successful update at this point
        // implicit commit happens now
      });
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsgs.add("A data access exception occurred updating mcuser.");
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsgs.add("A technical error occurred updating mcuser.");
    }

    if(not(isNullOrEmpty(emsgs))) {
      // error fetching updated mcuser
      return new FetchResult<>(null, flatten(emsgs, ","));
    }

    // success
    final Mcuser updatedMcuser = (Mcuser) rlist.get(0);
    return new FetchResult<Mcuser>(updatedMcuser, null);
  }

  /**
   * Delete an mcuser.
   * <p>
   * NOTE: the associated mcuser roles are assumed to be cascade deleted at the db level.
   * 
   * @param uid id of the mcuser to delete
   * @return Never null fetch result containing the status of the deletion.
   */
  public FetchResult<Boolean> deleteMcuser(final UUID uid) {
    String emsg = null;
    try {
      final int numd = dsl.delete(MCUSER).where(MCUSER.UID.eq(uid)).execute();
      if(numd != 1) return new FetchResult<>(Boolean.FALSE, "Invalid mcuser record delete count: " + Integer.toString(numd));
      // success
      return new FetchResult<>(Boolean.TRUE, null);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsg = "A data access exception occurred.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred deleting member.";
    }
    
    // fail
    return new FetchResult<>(Boolean.FALSE, emsg);
  }

  /**
   * Set/reset an mcuser pswd.
   * 
   * @param uid the id of the mcuser
   * @param pswd the pswd to set
   */
  public FetchResult<Boolean> setPswd(final UUID uid, final String pswd) {
    String emsg = null;
    try {
      final McuserPswd sp = new McuserPswd();
      sp.setInUid(uid);
      sp.setInPswd(pswd);
      sp.execute(dsl.configuration());

      // success
      return new FetchResult<>(Boolean.TRUE, null);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsg = "A data access exception occurred.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred.";
    }
    
    // fail
    return new FetchResult<>(Boolean.FALSE, emsg);
  }

  /**
   * Invalidate all known, non-expired JWTs bound to a particular mcuser held in the backend.
   * 
   * @param uid the mcuser id
   * @param requestInstant the sourcing request timestamp
   * @param clientOrigin the sourcing request client origin token
   */
  public FetchResult<Boolean> invalidateJwtsFor(final UUID uid, final Instant requestInstant, final String clientOrigin) {
    String emsg = null;
    try {
      final BlacklistJwtIdsFor sp = new BlacklistJwtIdsFor();
      sp.setUid(uid);
      sp.setInRequestTimestamp(Timestamp.from(requestInstant));
      sp.setInRequestOrigin(clientOrigin);
      sp.execute(dsl.configuration());

      // success
      return new FetchResult<>(Boolean.TRUE, null);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsg = "A data access exception occurred while invalidating jwts.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred invalidating jwts.";
    }
    
    // fail
    return new FetchResult<>(Boolean.FALSE, emsg);
  }
}
