package com.tll.mcorpus.repo;

import static com.tll.mcorpus.Util.flatten;
import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;
import static com.tll.mcorpus.db.Tables.MADDRESS;
import static com.tll.mcorpus.db.Tables.MAUTH;
import static com.tll.mcorpus.db.Tables.MEMBER;
import static com.tll.mcorpus.repo.MCorpusDataTransformer.transformMember;
import static com.tll.mcorpus.repo.MCorpusDataTransformer.transformMemberAddressForAdd;
import static com.tll.mcorpus.repo.MCorpusDataTransformer.transformMemberAddressForUpdate;
import static com.tll.mcorpus.repo.MCorpusDataValidator.validateMemberAddressToAdd;
import static com.tll.mcorpus.repo.MCorpusDataValidator.validateMemberAddressToUpdate;
import static com.tll.mcorpus.repo.MCorpusDataValidator.validateMemberToAdd;
import static com.tll.mcorpus.repo.MCorpusDataValidator.validateMemberToUpdate;
import static com.tll.mcorpus.repo.RepoUtil.fval;

import java.io.Closeable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.routines.MemberLogin;
import com.tll.mcorpus.db.routines.MemberLogout;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.db.udt.pojos.Mref;
import com.tll.mcorpus.db.udt.records.MrefRecord;
import com.tll.mcorpus.repo.model.FetchResult;
import com.tll.mcorpus.repo.model.MemberFilter;
import com.tll.mcorpus.repo.model.MemberRef;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MCorpus Repository (data access).
 * <p>
 * All public methods are <em>blocking</em>!
 *
 * @author jpk
 */
public class MCorpusRepo implements Closeable {
  protected final Logger log = LoggerFactory.getLogger("MCorpusRepo");

  protected final DSLContext dsl;

  /**
   * Constructor
   * @param ds the data source
   */
  public MCorpusRepo(DataSource ds) {
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
   * The member login routine which fetches the member ref whose username and
   * password matches the ones given.
   * <p>
   * Blocking!
   *
   * @param username the member username
   * @param pswd the member password
   * @param requestInstant the sourcing request timestamp
   * @param clientOrigin the sourcing request client origin token
   * @return Never null {@link FetchResult} object holding the {@link Mcuser} ref
   *         if successful<br>
   *         -OR- a null Mcuser ref and a non-null error message if unsuccessful.
   */
  public FetchResult<Mref> memberLogin(final String username, final String pswd, final Instant requestInstant, final String clientOrigin) {
    try {
      final MemberLogin mlogin = new MemberLogin();
      mlogin.setMemberUsername(username);
      mlogin.setMemberPassword(pswd);
      mlogin.setInRequestTimestamp(new Timestamp(requestInstant.toEpochMilli()));
      mlogin.setInRequestOrigin(clientOrigin);
      mlogin.execute(dsl.configuration());
      final MrefRecord rec = mlogin.getReturnValue();
      if(rec != null && rec.getMid() != null) {
        // login success
        final Mref mref = rec.into(Mref.class);
        return new FetchResult<>(mref, null);
      }
    }
    catch(Throwable t) {
      log.error("Member login error: {}.", t.getMessage());
    }
    // default - login fail
    return new FetchResult<>(null, "Member login failed.");
  }

  /**
   * Log a member out.
   * <p>
   * Blocking!
   *
   * @param mid the member id of the member to log out
   * @param requestInstant the sourcing request timestamp
   * @param clientOrigin the sourcing request client origin token
   * @return Never null {@link FetchResult} object holding the given member id upon successful member logout 
   *          -OR- a null member id and a non-null error message if unsuccessful.
   */
  public FetchResult<UUID> memberLogout(final UUID mid, final Instant requestInstant, final String clientOrigin) {
    try {
      final MemberLogout mlogout = new MemberLogout();
      mlogout.setMid(mid);
      mlogout.setInRequestTimestamp(new Timestamp(requestInstant.toEpochMilli()));
      mlogout.setInRequestOrigin(clientOrigin);
      mlogout.execute(dsl.configuration());
      final UUID rmid = mlogout.getReturnValue();
      if(rmid != null) {
        // logout successful - convey by returning the mid
        return new FetchResult<>(rmid, null);
      }
    }
    catch(Throwable t) {
      log.error("Member logout error: {}.", t.getMessage());
    }
    // default - logout failed
    return new FetchResult<>(null, "Member logout failed.");
  }

  /**
   * Fetch the {@link MemberRef} by member id.
   *
   * @param mid the member id
   * @return newly created {@link FetchResult} wrapping a property map.
   */
  public FetchResult<Mref> fetchMRefByMid(final UUID mid) {
    if(mid == null) return new FetchResult<>(null, "No member id provided.");
    String emsg;
    try {
      final Mref mref = dsl
        .select(MEMBER.MID, MEMBER.EMP_ID, MEMBER.LOCATION)
        .from(MEMBER)
        .where(MEMBER.MID.eq(mid))
        .fetchOne().into(Mref.class);
      return new FetchResult<>(mref, null);
    }
    catch(DataAccessException dae) {
      log.error(dae.getMessage());
      emsg = "A data access exception occurred.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching member ref by member id.";
    }
    // error
    return new FetchResult<>(null, emsg);
  }

  /**
   * Fetch the {@link MemberRef} by emp id and location.
   *
   * @param empId the emp id
   * @param loc the location
   * @return newly created {@link FetchResult} wrapping a property map upon success
   *          or wrapping an error message upon a fetch error.
   */
  public FetchResult<Mref> fetchMRefByEmpIdAndLoc(final String empId, final Location loc) {
    String emsg;
    if(empId != null && loc != null) {
      try {
        Mref mref = dsl
          .select(MEMBER.MID, MEMBER.EMP_ID, MEMBER.LOCATION)
          .from(MEMBER)
          .where(MEMBER.EMP_ID.eq(empId).and(MEMBER.LOCATION.eq(loc)))
          .fetchOne().into(Mref.class);
        return new FetchResult<>(mref, null);
      }
      catch(DataAccessException dae) {
        log.error(dae.getMessage());
        emsg = "A data access exception occurred.";
      }
      catch(Throwable t) {
        log.error(t.getMessage());
        emsg = "A technical error occurred fetching member ref by member id.";
      }
    }
    else {
      emsg = "Invalid emp id and/or location.";
    }
    // error
    return new FetchResult<>(null, emsg);
  }

  /**
   * Fetch all matching {@link MemberRef}s having the given emp id.
   *
   * @param empId the member emp id
   * @return newly created {@link FetchResult} wrapping a list of property map mref elements upon success
   *          or wrapping an error message upon a fetch error.
   */
  public FetchResult<List<Mref>> fetchMRefsByEmpId(final String empId) {
    if(empId == null) return new FetchResult<>(null, "No emp id provided.");
    String emsg;
    try {
      final List<Mref> mref = dsl
        .select(MEMBER.MID, MEMBER.EMP_ID, MEMBER.LOCATION)
        .from(MEMBER)
        .where(MEMBER.EMP_ID.eq(empId))
        .fetchInto(Mref.class);
      return new FetchResult<>(mref, null);
    }
    catch(DataAccessException dae) {
      log.error(dae.getMessage());
      emsg = "A data access exception occurred.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching member refs by emp id.";
    }
    // error
    return new FetchResult<>(null, emsg);
  }

  public FetchResult<Map<String, Object>> fetchMember(final UUID mid) {
    if(mid == null) return new FetchResult<>(null, "No member id provided.");
    String emsg;
    try {
      final Map<String, Object> mmap = dsl
              .select(MEMBER.MID, MEMBER.CREATED, MEMBER.MODIFIED, MEMBER.EMP_ID, MEMBER.LOCATION, MEMBER.NAME_FIRST, MEMBER.NAME_MIDDLE, MEMBER.NAME_LAST, MEMBER.DISPLAY_NAME, MEMBER.STATUS, MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME)
              .from(MEMBER).join(MAUTH).onKey()
              .where(MEMBER.MID.eq(mid))
              .fetchOneMap();
      if(mmap != null) {
        // found
        return new FetchResult<>(mmap, null);
      }
      // not found
      emsg = String.format("No member found with mid: '%s'.", mid);
      return new FetchResult<>(null, emsg);
    }
    catch(DataAccessException dae) {
      log.error(dae.getMessage());
      emsg = "A data access exception occurred.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching member.";
    }
    // error
    return new FetchResult<>(null, emsg);
  }

  /**
   * Fetch all held member addresses for a given member.
   *
   * @param mid the member id for which to get addresses
   * @return the potentially empty list of member addresses
   *          wrapped in a {@link FetchResult} to enclose an
   *          error message when the fetch fails
   */
  public FetchResult<List<Map<String, Object>>> fetchMemberAddresses(final UUID mid) {
    if(mid == null) return new FetchResult<>(null, "No member id provided.");
    String emsg;
    try {
      final List<Map<String, Object>> maddressList = dsl
        .select()
        .from(MADDRESS)
        .where(MADDRESS.MID.eq(mid))
        .fetchMaps();
      if(maddressList != null && !maddressList.isEmpty()) {
        return new FetchResult<>(maddressList, null);
      }
      // no member addresses exist
      return new FetchResult<>(Collections.emptyList(), null);
    }
    catch(DataAccessException dae) {
      log.error(dae.getMessage());
      emsg = "A data access exception occurred.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching member addresses.";
    }
    // error
    return new FetchResult<>(null, emsg);
  }

  /**
   * Member search function with optional filtering and paging offsets.
   *
   * @param filter the optional filter object
   * @param offset the sql query offset
   * @param limit the sql query fetch size
   * @return FetchResult for a list of property maps representing member entities.
   */
  public FetchResult<List<Map<String, Object>>> memberSearch(final MemberFilter filter, int offset, int limit) {
    String emsg;
    try {
      final List<Map<String, Object>> members;
      if(filter == null || !filter.isSet()) {
        // NO filter
        members = dsl
          .select(MEMBER.MID, MEMBER.CREATED, MEMBER.MODIFIED, MEMBER.EMP_ID, MEMBER.LOCATION, MEMBER.NAME_FIRST, MEMBER.NAME_MIDDLE, MEMBER.NAME_LAST, MEMBER.DISPLAY_NAME, MEMBER.STATUS, MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME)
          .from(MEMBER).join(MAUTH).onKey()
          .orderBy(MemberFilter.getDefaultJooqSortFields())
          .offset(offset).limit(limit)
          .fetch().intoMaps();
      }
      else {
        // filter
        members = dsl
          .select(MEMBER.MID, MEMBER.CREATED, MEMBER.MODIFIED, MEMBER.EMP_ID, MEMBER.LOCATION, MEMBER.NAME_FIRST, MEMBER.NAME_MIDDLE, MEMBER.NAME_LAST, MEMBER.DISPLAY_NAME, MEMBER.STATUS, MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME)
          .from(MEMBER).join(MAUTH).onKey()
          .where(filter.asJooqCondition())
          .orderBy(filter.generateJooqSortFields())
          .offset(offset).limit(limit)
          .fetch().intoMaps();
      }
      if(members == null || members.isEmpty()) {
        // no matches
        return new FetchResult<>(Collections.emptyList(), null);
      }
      return new FetchResult<>(members, null);
    }
    catch(DataAccessException dae) {
      log.error(dae.getMessage());
      emsg = "A data access error occurred fetching members.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching members.";
    }
    // error
    return new FetchResult<>(null, emsg);
  }

  /**
   * Add a member.
   *
   * @param memberMap a map of property name keys and values.<br>
   *                  The expected member properties are:
   * <pre>
   * emp_id         the member emp id
   * location       the member location
   * name_first     first name of the member
   * name_middle    middle name of the member
   * name_last last name of the member
   * display_name   member display name
   * dob member     date of birth
   * ssn member     SSN
   * email_personal member personal email address
   * email_work     member work email address
   * mobile_phone   member mobile phone number
   * home_phone     member home phone number
   * workPhone      member work phone number
   * username       member username
   * pswd           password un-encrypted
   * </pre>
   *
   * @return newly created {@link FetchResult} of a map of the newly added member:<br>
   *          mid: the member's unique UUID,<br>
   *          created: the member's record created timestamp
   */
  public FetchResult<Map<String, Object>> addMember(final Map<String, Object> memberMap) {
    if(isNullOrEmpty(memberMap)) return new FetchResult<>(null, "No member properties provided.");

    final List<String> emsgs = new ArrayList<>();

    // validate the member properties
    validateMemberToAdd(memberMap, emsgs);
    if(not(emsgs.isEmpty())) {
      return new FetchResult<>(null, flatten(emsgs, ","));
    }

    // transform member property map to serve as a data cleanse/prep operation before record insert
    final List<Map<String, Object>> cmapList = transformMember(memberMap);
    final Map<String, Object> cmapMember = cmapList.get(0);
    final Map<String, Object> cmapMauth = cmapList.get(1);

    final Map<String, Object> rmap = new HashMap<>();
    
    try {
      dsl.transaction(configuration -> {
        // add member record
        final Map<String, Object> rmapMember =
          DSL.using(configuration)
            .insertInto(MEMBER, MEMBER.EMP_ID, MEMBER.LOCATION, MEMBER.NAME_FIRST, MEMBER.NAME_MIDDLE, MEMBER.NAME_LAST, MEMBER.DISPLAY_NAME, MEMBER.STATUS)
            .values(
              fval(MEMBER.EMP_ID, cmapMember),
              fval(MEMBER.LOCATION, cmapMember),
              fval(MEMBER.NAME_FIRST, cmapMember),
              fval(MEMBER.NAME_MIDDLE, cmapMember),
              fval(MEMBER.NAME_LAST, cmapMember),
              fval(MEMBER.DISPLAY_NAME, cmapMember),
              fval(MEMBER.STATUS, cmapMember)
            )
            .returning() // :o
            .fetchOne().intoMap();

        // acquire the inserted member record's PK and created timestamp
        if(rmapMember == null) throw new DataAccessException("No post-insert member record returned.");

        final UUID mid = fval(MEMBER.MID, rmapMember);
        final Timestamp created = fval(MEMBER.CREATED, rmapMember);

        if(mid == null || created == null) {
          // bad insert return values (force rollback)
          throw new DataAccessException("Bad member insert return values.");
        }

        // create mauth record
        final Map<String, Object> rmapMauth =
          DSL.using(configuration)
            .insertInto(MAUTH,
              MAUTH.MID, MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME, MAUTH.PSWD)
            .values(
              mid,
              fval(MAUTH.DOB, cmapMauth),
              fval(MAUTH.SSN, cmapMauth),
              fval(MAUTH.EMAIL_PERSONAL, cmapMauth),
              fval(MAUTH.EMAIL_WORK, cmapMauth),
              fval(MAUTH.MOBILE_PHONE, cmapMauth),
              fval(MAUTH.HOME_PHONE, cmapMauth),
              fval(MAUTH.WORK_PHONE, cmapMauth),
              fval(MAUTH.USERNAME, cmapMauth),
              fval(MAUTH.PSWD, cmapMauth)
            )
            .returning(MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME) // :o
            .fetchOne().intoMap();

        if(rmapMauth == null) {
          // mauth insert failed (rollback)
          throw new DataAccessException("No post-insert member record returned.");
        }

        // success
        // NOTE: the order of adding the maps the returning map is important
        rmap.putAll(rmapMauth);
        rmap.putAll(rmapMember);
      });
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsgs.add("A data access exception occurred.");
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsgs.add("A technical error occurred adding member.");
    }

    if(not(isNullOrEmpty(emsgs))) {
      // member add fail
      return new FetchResult<>(null, flatten(emsgs, ","));
    }

    // success (presuming actually)
    return new FetchResult<>(rmap, null);
  }

  public FetchResult<Map<String, Object>> updateMember(final Map<String, Object> memberMap) {
    if(isNullOrEmpty(memberMap)) return new FetchResult<>(null, "No member properties provided.");
    
    final List<String> emsgs = new ArrayList<>();
    
    // validate the member properties
    validateMemberToUpdate(memberMap, emsgs);
    if(not(emsgs.isEmpty())) {
      return new FetchResult<>(null, flatten(emsgs, ","));
    }

    // transform member property map to serve as a data cleanse/prep operation before record insert
    final List<Map<String, Object>> cmaps = transformMember(memberMap);
    final Map<String, Object> cmapMember = cmaps.get(0);
    final Map<String, Object> cmapMauth = cmaps.get(1);

    final UUID mid = fval(MEMBER.MID, memberMap);

    final Map<String, Object> rmap = new HashMap<>();

    try {
      dsl.transaction(configuration -> {

        final Map<String, Object> rmapMauth;
        
        // update mauth
        if(not(cmapMauth.isEmpty())) {
          // update mauth record
          rmapMauth = 
            DSL.using(configuration)
                    .update(MAUTH)
                    .set(cmapMauth)
                    .where(MAUTH.MID.eq(mid))
                    .returning(MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME) // :o
                    .fetchOne().intoMap();
        }
        else {
          // otherwise select to get current snapshot
          rmapMauth = 
            DSL.using(configuration)
                    .select(MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME)
                    .from(MAUTH)
                    .where(MAUTH.MID.eq(mid))
                    .fetchOne().intoMap();
        }
        
        if (rmapMauth == null) {
          // mauth insert failed (rollback)
          throw new DataAccessException("No post-update mauth record returned.");
        }
        rmap.putAll(rmapMauth);

        final Map<String, Object> rmapMember;

        // update member record (we are guaranteed at least one field to update)
        rmapMember = 
          DSL.using(configuration)
                  .update(MEMBER)
                  .set(cmapMember)
                  .where(MEMBER.MID.eq(mid))
                  .returning()
                  .fetchOne().intoMap();

        // acquire the inserted member record's modified timestamp
        if (rmapMember == null) {
          // bad insert return values (force rollback)
          throw new DataAccessException("No post-update member record returned.");
        }
        rmap.putAll(rmapMember);

        // successful member update at this point
        // implicit commit happens now
      });
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsgs.add("A data access exception occurred.");
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsgs.add("A technical error occurred updating member.");
    }

    if(not(isNullOrEmpty(emsgs))) {
      // error fetching updated member
      return new FetchResult<>(null, flatten(emsgs, ","));
    }

    // success
    return new FetchResult<>(rmap, null);
  }

  /**
   * Delete a member from the system (physical record removal).
   *
   * @param mid the id of the member to delete
   * @return FetchResult of the member id upon successful deletion,
   *                     otherwise an error message.
   */
  public FetchResult<UUID> deleteMember(final UUID mid) {
    if(mid == null) return new FetchResult<>(null, "No member id provided.");
    
    String emsg;
    try {
      final int numDeleted =
        dsl
          .delete(MEMBER)
          .where(MEMBER.MID.eq(mid))
          .execute();

      if(numDeleted != 1) throw new DataAccessException("Invalid member delete return value.");

      // success
      return new FetchResult<>(mid, null);
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
    return new FetchResult<>(null, emsg);
  }

  public FetchResult<Map<String, Object>> addMemberAddress(final Map<String, Object> maddressMap) {
    if(isNullOrEmpty(maddressMap)) return new FetchResult<>(null, "No member address properties provided.");

    final List<String> emsgs = new ArrayList<>();
    
    // validate the member properties
    validateMemberAddressToAdd(maddressMap, emsgs);
    if(not(emsgs.isEmpty())) {
      return new FetchResult<>(null, flatten(emsgs, ","));
    }

    // transform property map param to serve as a data cleanse/prep operation before record insert
    final Map<String, Object> cmap = transformMemberAddressForAdd(maddressMap);
    
    try {
      // add record
      final Map<String, Object> rmap =
        dsl
          .insertInto(MADDRESS,
            MADDRESS.MID,
            MADDRESS.ADDRESS_NAME,
            MADDRESS.ATTN,
            MADDRESS.STREET1,
            MADDRESS.STREET2,
            MADDRESS.CITY,
            MADDRESS.STATE,
            MADDRESS.POSTAL_CODE,
            MADDRESS.COUNTRY)
          .values(
            fval(MADDRESS.MID, cmap),
            fval(MADDRESS.ADDRESS_NAME, cmap),
            fval(MADDRESS.ATTN, cmap),
            fval(MADDRESS.STREET1, cmap),
            fval(MADDRESS.STREET2, cmap),
            fval(MADDRESS.CITY, cmap),
            fval(MADDRESS.STATE, cmap),
            fval(MADDRESS.POSTAL_CODE, cmap),
            fval(MADDRESS.COUNTRY, cmap)
          )
          .returning()
          .fetchOne().intoMap();

      if(isNullOrEmpty(rmap)) 
        throw new DataAccessException("Invalid member address insert return value.");

      // successful
      return new FetchResult<>(rmap, null);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsgs.add("A data access exception occurred.");
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsgs.add("A technical error occurred adding member address.");
    }

    // fail
    return new FetchResult<>(null, flatten(emsgs, ","));
  }

  public FetchResult<Map<String, Object>> updateMemberAddress(final Map<String, Object> maddressMap) {
    if(isNullOrEmpty(maddressMap)) return new FetchResult<>(null, "No member address properties provided.");

    final List<String> emsgs = new ArrayList<>();
    
    // validate
    validateMemberAddressToUpdate(maddressMap, emsgs);
    if(not(emsgs.isEmpty())) {
      return new FetchResult<>(null, flatten(emsgs, ","));
    }

    // transform member property map to serve as a data cleanse/prep operation before record insert
    final List<Object> clist = transformMemberAddressForUpdate(maddressMap);
    final UUID mid = (UUID) clist.get(0);
    final Addressname addressname = (Addressname) clist.get(1);
    @SuppressWarnings("unchecked")
    final Map<String, Object> cmap = (Map<String, Object>) clist.get(2);
    
    try {
      // update
      final Map<String, Object> rmap =
        dsl
          .update(MADDRESS)
          .set(cmap)
          .where(MADDRESS.MID.eq(mid)).and(MADDRESS.ADDRESS_NAME.eq(addressname))
          .returning() // :o
          .fetchOne().intoMap();

      if(isNullOrEmpty(rmap))
        throw new DataAccessException("Bad member address update return value.");

      // successful
      return new FetchResult<>(rmap, null);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsgs.add("A data access exception occurred.");
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsgs.add("A technical error occurred updating member address.");
    }

    // fail
    return new FetchResult<>(null, flatten(emsgs, ","));
  }

  /**
   * Delete a member address.
   *
   * @param mid the id of the member "owning" the address to delete
   * @param addressname the address name identifying the member address to delete
   * @return FetchResult of the member id upon successful deletion,
   *                     otherwise an error message.
   */
  public FetchResult<UUID> deleteMemberAddress(final UUID mid, Addressname addressname) {
    if(mid == null) return new FetchResult<>(null, "No member id provided.");
    String emsg;
    try {
      // update member record
      final int numDeleted = dsl
                .delete(MADDRESS)
                .where(MADDRESS.MID.eq(mid).and(MADDRESS.ADDRESS_NAME.eq(addressname)))
                .execute();

      if(numDeleted != 1) throw new DataAccessException("Invalid member address delete return value.");

      // successful member address delete
      return new FetchResult<>(mid, null);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsg = "A data access exception occurred.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred deleting member address.";
    }

    // member delete fail
    return new FetchResult<>(null, emsg);
  }
}
