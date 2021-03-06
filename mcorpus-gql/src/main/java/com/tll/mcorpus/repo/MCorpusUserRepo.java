package com.tll.mcorpus.repo;

import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.nflatten;
import static com.tll.mcorpus.db.Tables.MCUSER;
import static com.tll.mcorpus.db.Tables.MCUSER_AUDIT;
import static com.tll.mcorpus.repo.MCorpusRepoUtil.fputWhenNotNull;
import static com.tll.repo.FetchResult.fetchrslt;

import java.io.Closeable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import com.tll.mcorpus.db.enums.JwtStatus;
import com.tll.mcorpus.db.enums.McuserAuditType;
import com.tll.mcorpus.db.enums.McuserRole;
import com.tll.mcorpus.db.enums.McuserStatus;
import com.tll.mcorpus.db.routines.BlacklistJwtIdsFor;
import com.tll.mcorpus.db.routines.GetJwtStatus;
import com.tll.mcorpus.db.routines.GetNumActiveLogins;
import com.tll.mcorpus.db.routines.InsertMcuser;
import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.db.routines.McuserPswd;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.dmodel.McuserHistoryDomain;
import com.tll.mcorpus.dmodel.McuserHistoryDomain.LoginEventDomain;
import com.tll.mcorpus.dmodel.McuserHistoryDomain.LogoutEventDomain;
import com.tll.repo.FetchResult;

import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Record9;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderKeywordCase;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mcuser (those who access and mutate mcorpus data) repository.
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
    s.setRenderNameCase(RenderNameCase.LOWER);
    s.setRenderKeywordCase(RenderKeywordCase.UPPER);
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
    if(uid == null) return fetchrslt(null, "No mcuser id provided.");
    try {
      final GetNumActiveLogins backend = new GetNumActiveLogins();
      backend.setMcuserId(uid);
      backend.execute(dsl.configuration());
      return fetchrslt(backend.getReturnValue(), null);
    }
    catch(Throwable e) {
      log.info("JWT number of active logins call error: {}", e.getMessage());
      return fetchrslt(null, "JWT number of active logins call failed.");
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
  public FetchResult<JwtStatus> getBackendJwtStatus(final UUID jwtId) {
    if(jwtId == null) return fetchrslt(null, "JWT status check failed: bad input.");
    try {
      final GetJwtStatus backend = new GetJwtStatus();
      backend.setJwtId(jwtId);
      backend.execute(dsl.configuration());
      final JwtStatus rval = backend.getReturnValue();
      return fetchrslt(rval);
    }
    catch(Throwable e) {
      log.info("JWT id validation check error: {}", e.getMessage());
      return fetchrslt(null, "JWT id validation check failed.");
    }
  }

  /**
   * Fetch the login and logout history for an mcuser.
   *
   * @param uid the mcuser id
   */
  public FetchResult<McuserHistoryDomain> mcuserHistory(final UUID uid) {
    if(uid == null) return fetchrslt(null, "No mcuser id provided.");
    try {
      final Result<Record4<UUID, OffsetDateTime, String, McuserAuditType>> result = dsl
        .select(MCUSER_AUDIT.JWT_ID, MCUSER_AUDIT.CREATED, MCUSER_AUDIT.REQUEST_ORIGIN, MCUSER_AUDIT.TYPE)
        .from(MCUSER_AUDIT)
        .where(MCUSER_AUDIT.UID.eq(uid))
        .orderBy(MCUSER_AUDIT.CREATED.desc())
        .fetch();
      if(result.isNotEmpty()) {
        final List<LoginEventDomain> logins = new ArrayList<>();
        final List<LogoutEventDomain> logouts = new ArrayList<>();
        final Iterator<Record4<UUID, OffsetDateTime, String, McuserAuditType>> itr = result.iterator();
        while(itr.hasNext()) {
          Record4<UUID, OffsetDateTime, String, McuserAuditType> rec = itr.next();
          UUID jwtId = rec.get(MCUSER_AUDIT.JWT_ID);
          OffsetDateTime created = rec.get(MCUSER_AUDIT.CREATED);
          String requestOrigin = rec.get(MCUSER_AUDIT.REQUEST_ORIGIN);
          switch(rec.get(MCUSER_AUDIT.TYPE)) {
            case LOGIN:
              logins.add(new LoginEventDomain(jwtId, created, requestOrigin));
              break;
            case LOGOUT:
              logouts.add(new LogoutEventDomain(jwtId, created, requestOrigin));
              break;
            default:
              throw new Exception("Unhandled mcuser audit type.");
          }
        }
        return fetchrslt(new McuserHistoryDomain(uid, logins, logouts), null);
      } else {
        // no history
        return fetchrslt(new McuserHistoryDomain(uid), null);
      }
    }
    catch(Throwable e) {
      log.error("Error fetching mcuser ('{}') history: {}", uid, e.getMessage());
    }

    // fail
    return fetchrslt(null, "Mcuser history fetch failed.");
  }

  /**
   * The mcorpus mcuser login routine which fetches the mcuser record ref
   * whose username and password matches the ones given.
   *
   * @param username the mcuser username
   * @param pswd the mcuser password
   * @param pendingJwtId the pending JWT id (the login must succeed first)
   * @param jwtExpiration the JWT expiration date
   * @param requestInstant the instant the sourcing http request reached the server
   * @param clientOriginToken the client origin token gnerated from the sourcing http request
   *
   * @return Never null {@link FetchResult} object<br>
   *         holding the {@link Mcuser} ref if successful<br>
   *         -OR- a null Mcuser ref and a non-null error message if unsuccessful.
   */
  public FetchResult<Mcuser> login(final String username, final String pswd, final UUID pendingJwtId, Instant jwtExpiration, Instant requestInstant, final String clientOriginToken) {
    try {
      final McuserLogin mcuserLogin = new McuserLogin();
      mcuserLogin.setMcuserUsername(username);
      mcuserLogin.setMcuserPassword(pswd);
      mcuserLogin.setInJwtId(pendingJwtId);
      mcuserLogin.setInLoginExpiration(OffsetDateTime.ofInstant(jwtExpiration, ZoneId.systemDefault()));
      mcuserLogin.setInRequestOrigin(clientOriginToken);
      mcuserLogin.setInRequestTimestamp(OffsetDateTime.ofInstant(requestInstant, ZoneId.systemDefault()));

      mcuserLogin.execute(dsl.configuration());
      final Mcuser rval = mcuserLogin.getReturnValue().into(Mcuser.class);
      if(rval != null && rval.getUid() != null) {
        // login success
        return fetchrslt(rval, null);
      } else {
        // login fail - no record returned
        return fetchrslt(null, "Mcuser login unsuccessful.");
      }
    }
    catch(Throwable t) {
      log.error("mcuser login error: {}.", t.getMessage());
    }
    // default - login fail
    return fetchrslt(null, "Mcuser login failed.");
  }

  /**
   * Log an mcuser out.
   *
   * @param mcuserId the mcuser id (jwt user id)
   * @param jwtId the JWT id
   * @param requestInstant the instant the sourcing http request reached the server
   * @param clientOriginToken the client origin token gnerated from the sourcing http request
   *
   * @return Never null {@link FetchResult} object
   *         holding an error message if unsuccessful.
   */
  public FetchResult<Boolean> logout(final UUID mcuserId, final UUID jwtId, Instant requestInstant, final String clientOriginToken) {
    try {
      final McuserLogout mcuserLogout = new McuserLogout();
      mcuserLogout.setMcuserUid(mcuserId);
      mcuserLogout.setJwtId(jwtId);
      mcuserLogout.setRequestTimestamp(OffsetDateTime.ofInstant(requestInstant, ZoneId.systemDefault()));
      mcuserLogout.setRequestOrigin(clientOriginToken);

      mcuserLogout.execute(dsl.configuration());
      if(Boolean.TRUE.equals(mcuserLogout.getReturnValue())) {
        // logout success
        return fetchrslt(Boolean.TRUE, null);
      }
    }
    catch(Throwable t) {
      log.error("Logout error: {}.", t.getMessage());
    }
    // default - logout failed
    return fetchrslt(Boolean.FALSE, "Logout failed.");
  }

  /**
   * Fetch an mcuser.
   *
   * @param uid the mcuser id
   * @return Never null fetch result containing the fetched
   *         {@link Mcuser} or an error message if unsuccessful.
   */
  public FetchResult<Mcuser> fetchMcuser(final UUID uid) {
    if(uid == null) return fetchrslt(null, "No mcuser id provided.");
    String emsg;
    try {
      final Mcuser mcuser = dsl
              .select(
                MCUSER.UID,
                MCUSER.CREATED,
                MCUSER.MODIFIED,
                MCUSER.NAME,
                MCUSER.EMAIL,
                MCUSER.USERNAME,
                DSL.val((String) null),
                MCUSER.STATUS,
                MCUSER.ROLES
              )
              .from(MCUSER)
              .where(MCUSER.UID.eq(uid))
              .fetchOneInto(Mcuser.class);

      if(isNotNull(mcuser)) {
        return fetchrslt(mcuser, null);
      } else {
        return fetchrslt(null, String.format("No mcuser found with uid %s.", uid));
      }
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
    return fetchrslt(null, emsg);
  }

  /**
   * Add an mcuser.
   *
   * @param mcuserToAdd the mcuser to be added with optional set of roles
   * @return Never null fetch result containing the added {@link Mcuser}
   *         instance if successful.
   */
  public FetchResult<Mcuser> addMcuser(final Mcuser mcuserToAdd) {
    if(isNull(mcuserToAdd)) return fetchrslt(null, "No mcuser provided.");

    final List<String> emsgs = new ArrayList<>();
    final List<Mcuser> rlist = new ArrayList<>(1);

    try {
      dsl.transaction(configuration -> {
        final DSLContext trans = DSL.using(configuration);

        final InsertMcuser routine = new InsertMcuser();
        routine.setInName(mcuserToAdd.getName());
        routine.setInEmail(mcuserToAdd.getEmail());
        routine.setInUsername(mcuserToAdd.getUsername());
        routine.setInPswd(mcuserToAdd.getPswd());
        routine.setInStatus(mcuserToAdd.getStatus());
        routine.setInRoles(mcuserToAdd.getRoles());
        routine.execute(trans.configuration());
        final Mcuser addedMcuser = routine.getReturnValue().into(Mcuser.class);
        if(isNull(addedMcuser) || isNull(addedMcuser.getUid()))
          throw new DataAccessException("No post-insert mcuser record returned.");
        rlist.add(addedMcuser);
      });
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsgs.add("A data access exception occurred adding mcuser.");
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsgs.add("A technical error occurred adding mcuser.");
    }

    final Mcuser addedMcuser = rlist.size() == 1 ? rlist.get(0) : null;
    return fetchrslt(addedMcuser, nflatten(emsgs, ","));
  }

  /**
   * Update an mcuser.
   *
   * @param mcuserToUpdate the mcuser and optional roles to be updated.
   *                       <p>
   *                       If no roles are present, no roles updating happens
   * @return Never null fetch result containing the updated {@link Mcuser} instance
   *         if unsuccessful.
   */
  public FetchResult<Mcuser> updateMcuser(final Mcuser mcuserToUpdate) {
    if(isNull(mcuserToUpdate)) return fetchrslt(null, "No mcuser provided.");

    final List<String> emsgs = new ArrayList<>();
    final List<Mcuser> rlist = new ArrayList<>(1);

    // create update map of fields that are present only
    final Map<String, Object> fmap = new HashMap<>();
    fputWhenNotNull(MCUSER.NAME, mcuserToUpdate.getName(), fmap);
    fputWhenNotNull(MCUSER.EMAIL, mcuserToUpdate.getEmail(), fmap);
    fputWhenNotNull(MCUSER.USERNAME, mcuserToUpdate.getUsername(), fmap);
    fputWhenNotNull(MCUSER.STATUS, mcuserToUpdate.getStatus(), fmap);
    fputWhenNotNull(MCUSER.ROLES, mcuserToUpdate.getRoles(), fmap);

    try {
      dsl.transaction(configuration -> {

        final DSLContext trans = DSL.using(configuration);

        // update mcuser
        final Record9<UUID, OffsetDateTime, OffsetDateTime, String, String, String, String, McuserStatus, McuserRole[]>
        mcuserUpatedRec = trans
          .update(MCUSER)
          .set(fmap)
          .where(MCUSER.UID.eq(mcuserToUpdate.getUid()))
          .returningResult(
            MCUSER.UID,
            MCUSER.CREATED,
            MCUSER.MODIFIED,
            MCUSER.NAME,
            MCUSER.EMAIL,
            MCUSER.USERNAME,
            DSL.val((String) null),
            MCUSER.STATUS,
            MCUSER.ROLES
          ).fetchOne();

        if(isNotNull(mcuserUpatedRec)) {
          // successful update at this point
          final Mcuser mcuserUpdated = mcuserUpatedRec.into(Mcuser.class);
          rlist.add(mcuserUpdated);
        } else {
          emsgs.add("No mcuser found to update.");
        }
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

    final Mcuser updated = rlist.size() == 1 ? rlist.get(0) : null;
    return fetchrslt(updated, nflatten(emsgs, ","));
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
      if(numd != 1) return fetchrslt(Boolean.FALSE, "Invalid mcuser record delete count: " + Integer.toString(numd));
      // success
      return fetchrslt(Boolean.TRUE, null);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsg = "A data access exception occurred deleting mcuser.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred deleting mcuser.";
    }

    // fail
    return fetchrslt(Boolean.FALSE, emsg);
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
      return fetchrslt(Boolean.TRUE, null);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsg = "A data access exception occurred setting mcuser pswd.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred setting mcuser pswd.";
    }

    // fail
    return fetchrslt(Boolean.FALSE, emsg);
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
      sp.setInUid(uid);
      sp.setInRequestTimestamp(OffsetDateTime.ofInstant(requestInstant, ZoneId.systemDefault()));
      sp.setInRequestOrigin(clientOrigin);
      sp.execute(dsl.configuration());

      // success
      return fetchrslt(Boolean.TRUE, null);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsg = "A data access exception occurred invalidating jwts.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred invalidating jwts.";
    }

    // fail
    return fetchrslt(Boolean.FALSE, emsg);
  }
}
