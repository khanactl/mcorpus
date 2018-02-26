package com.tll.mcorpus.repo;

import static com.tll.mcorpus.Util.flatten;
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
import static com.tll.mcorpus.repo.RepoUtil.fput;
import static com.tll.mcorpus.repo.RepoUtil.fval;

import java.io.Closeable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.routines.MemberLogin;
import com.tll.mcorpus.db.routines.MemberLogout;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.db.tables.records.MaddressRecord;
import com.tll.mcorpus.db.tables.records.MemberRecord;
import com.tll.mcorpus.db.udt.pojos.Mref;
import com.tll.mcorpus.db.udt.records.MrefRecord;
import com.tll.mcorpus.repo.model.FetchResult;
import com.tll.mcorpus.repo.model.LoginInput;
import com.tll.mcorpus.repo.model.MemberFilter;
import com.tll.mcorpus.repo.model.MemberRef;

/**
 * MCorpus Repository (data access).
 * <p>
 * All public methods are <em>blocking</em>!
 *
 * @author jpk
 */
public class MCorpusRepo implements Closeable {
  protected static final Logger log = LoggerFactory.getLogger("MCorpusRepo");

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
   * The member login routine which fetches the member ref whose username and password matches the ones given.
   * <p>
   * Blocking!
   *
   * @param memberLoginInput the member login input credentials along with http header captures
   * @return Never null {@link FetchResult<Mcuser>} object holding the {@link Mcuser} ref if successful<br>
   *         -OR- a null Mcuser ref and a non-null error message if unsuccessful.
   */
  public FetchResult<Mref> memberLogin(final LoginInput memberLoginInput) {
    if(memberLoginInput != null && memberLoginInput.isValid()) {
      log.debug("member logging in {}..", memberLoginInput);
      try {
        final MemberLogin mlogin = new MemberLogin();
        mlogin.setMemberUsername(memberLoginInput.getUsername());
        mlogin.setMemberPassword(memberLoginInput.getPassword());
        mlogin.setMemberWebSessionId(memberLoginInput.getWebSessionId());
        mlogin.setMemberIp(memberLoginInput.getIp());
        mlogin.setMemberHost(memberLoginInput.getHttpHost());
        mlogin.setMemberOrigin(memberLoginInput.getHttpOrigin());
        mlogin.setMemberReferer(memberLoginInput.getHttpReferer());
        mlogin.setMemberForwarded(memberLoginInput.getHttpForwarded());
        mlogin.execute(dsl.configuration());
        final MrefRecord rec = mlogin.getReturnValue();
        if(rec != null && rec.getMid() != null) {
          // login success
          final Mref mref = rec.into(Mref.class);
          log.info("MEMBER LOGGED IN: {}", mref);
          return new FetchResult<>(mref, null);
        } else {
          // login fail - no record returned
          return new FetchResult<>(null, "Member login unsuccessful.");
        }
      }
      catch(Throwable t) {
        log.error("Member login error: {}.", t.getMessage());
      }
    }
    // default - login fail
    return new FetchResult<>(null, "Member login failed.");
  }

  /**
   * Log a member out.
   * <p>
   * Blocking!
   *
   * @param mid the member id
   * @param webSessionId the member's current web session id
   * @return Never null {@link FetchResult<Void>} object holding an error message if unsuccessful.
   */
  public FetchResult<Void> memberLogout(final UUID mid, final String webSessionId) {
    if(mid != null && webSessionId != null) {
      try {
        final MemberLogout logout = new MemberLogout();
        logout.setMid(mid);
        logout.setMemberWebSessionId(webSessionId);
        logout.execute(dsl.configuration());
        // logout successful (no exception)
        return new FetchResult<>(null, null);
      }
      catch(Throwable t) {
        log.error("Member logout error: {}.", t.getMessage());
      }
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
      emsg = dae.getMessage();
    }
    catch(Throwable t) {
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
        emsg = dae.getMessage();
      }
      catch(Throwable t) {
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
      emsg = dae.getMessage();
    }
    catch(Throwable t) {
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
      emsg = dae.getMessage();
    }
    catch(Throwable t) {
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
      emsg = dae.getMessage();
    }
    catch(Throwable t) {
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
      emsg = dae.getMessage();
    }
    catch(Throwable t) {
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
    if(memberMap == null || memberMap.isEmpty())
      return new FetchResult<>(null, "No member properties provided.");

    // validate the member properties
    final List<String> emsgs = new ArrayList<>();
    validateMemberToAdd(memberMap, emsgs);
    if(not(emsgs.isEmpty())) {
      return new FetchResult<>(null, flatten(emsgs, ","));
    }

    // transform member property map to serve as a data cleanse/prep operation before record insert
    final List<Map<String, Object>> cmapList = transformMember(memberMap);
    final Map<String, Object> cmapMember = cmapList.get(0);
    final Map<String, Object> cmapMauth = cmapList.get(1);

    final StringBuilder emsg = new StringBuilder();
    final List<FetchResult<Map<String, Object>>> fetchResultList = new ArrayList<>(1);
    try {
      dsl.transaction(configuration -> {
        // add member record
        final Result<MemberRecord> result =
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
            .returning(MEMBER.MID, MEMBER.CREATED) // :o
            .fetch();

        // acquire the inserted member record's PK and created timestamp
        final MemberRecord memberRecord = result == null ? null : (MemberRecord) result.get(0);
        if(memberRecord == null) throw new DataAccessException("No post-insert member key present.");

        final UUID mid = memberRecord.getMid();
        final Timestamp created = memberRecord.getCreated();

        if(mid == null || created == null) {
          // bad insert return values (force rollback)
          throw new DataAccessException("Bad member insert return values.");
        }

        // create mauth record
        final int mauthNumInserted =
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
            .execute();

        if(mauthNumInserted != 1) {
          // mauth insert failed (rollback)
          throw new DataAccessException("Downstream member insert failed.");
        }

        final Map<String, Object> rmap = new HashMap<>(cmapMember.size() + cmapMauth.size());
        rmap.putAll(cmapMember);
        rmap.putAll(cmapMauth);
        rmap.remove(MAUTH.PSWD.getName());
        // add returned new member PK and created timestamp to the cleaned member map
        fput(MEMBER.MID, mid, rmap);
        fput(MEMBER.CREATED, created, rmap);

        // successful member add
        fetchResultList.add(new FetchResult<>(rmap, null));
      });
    }
    catch(DataAccessException e) {
      emsg.append(e.getMessage());
    }
    catch(Throwable t) {
      emsg.append("A technical error occurred adding member.");
    }

    if(emsg.length() > 0) {
      // member add fail
      return new FetchResult<>(null, emsg.toString());
    }

    // success (presuming actually)
    return fetchResultList.get(0);
  }

  public FetchResult<Map<String, Object>> updateMember(final Map<String, Object> memberMap) {
    // validate the member properties
    final List<String> emsgs = new ArrayList<>();
    validateMemberToUpdate(memberMap, emsgs);
    if(not(emsgs.isEmpty())) {
      return new FetchResult<>(null, flatten(emsgs, ","));
    }

    // transform member property map to serve as a data cleanse/prep operation before record insert
    final List<Map<String, Object>> cmaps = transformMember(memberMap);
    final Map<String, Object> cmapMember = cmaps.get(0);
    final Map<String, Object> cmapMauth = cmaps.get(1);

    final UUID mid = (UUID) memberMap.get(MEMBER.MID.getName());

    final StringBuilder emsg = new StringBuilder();

    try {
      dsl.transaction(configuration -> {

        if(not(cmapMember.isEmpty())) {
          // update member record
          final int numMemberRecordsUpdated =
            DSL.using(configuration)
                    .update(MEMBER)
                    .set(cmapMember)
                    .where(MEMBER.MID.eq(mid))
                    .execute();

          // acquire the inserted member record's modified timestamp
          if (numMemberRecordsUpdated != 1) {
            // bad insert return values (force rollback)
            throw new DataAccessException("Bad member update return values.");
          }
        }

        if(not(cmapMauth.isEmpty())) {
          // update mauth record
          final int numMauthRecordsUpdated =
            DSL.using(configuration)
                    .update(MAUTH)
                    .set(cmapMauth)
                    .where(MAUTH.MID.eq(mid))
                    .execute();

          if (numMauthRecordsUpdated != 1) {
            // mauth insert failed (rollback)
            throw new DataAccessException("Downstream member update failed - mauth record insert failed.");
          }
        }
        // successful member update at this point
        // implicit commit happens now
      });
    }
    catch(DataAccessException e) {
      emsg.append(e.getMessage());
    }
    catch(Throwable t) {
      emsg.append("A technical error occurred updating member.");
    }

    if(emsg.length() > 0) {
      // member add fail
      return new FetchResult<>(null, emsg.toString());
    }

    // now fetch updated member for return value
    final FetchResult<Map<String, Object>> memberFetchResult = fetchMember(mid);
    if(memberFetchResult.isSuccess()) {
      return memberFetchResult;
    }

    // error fetching updated member record(s)
    throw new DataAccessException("Member fetch failed after successful update.");
  }

  /**
   * Delete a member from the sytem (physical record removal).
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
      emsg = e.getMessage();
    }
    catch(Throwable t) {
      emsg = "A technical error occurred deleting member.";
    }

    // fail
    return new FetchResult<>(null, emsg);
  }

  public FetchResult<Map<String, Object>> addMemberAddress(final Map<String, Object> maddressMap) {
    if(maddressMap == null) return new FetchResult<>(null, "No member id provided.");

    // validate the member properties
    final List<String> emsgs = new ArrayList<>();
    validateMemberAddressToAdd(maddressMap, emsgs);
    if(not(emsgs.isEmpty())) {
      return new FetchResult<>(null, flatten(emsgs, ","));
    }

    // transform property map param to serve as a data cleanse/prep operation before record insert
    final Map<String, Object> cmap = transformMemberAddressForAdd(maddressMap);
    String emsg;
    try {
      // add record
      final int numAdded =
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
          .execute();

      if(numAdded != 1) throw new DataAccessException("Invalid member address insert return value.");

      // successful
      return new FetchResult<>(cmap, null);
    }
    catch(DataAccessException e) {
      emsg = e.getMessage();
    }
    catch(Throwable t) {
      emsg = "A technical error occurred adding member address.";
    }

    // fail
    return new FetchResult<>(null, emsg);
  }

  public FetchResult<Map<String, Object>> updateMemberAddress(final Map<String, Object> maddressMap) {
    if(maddressMap == null || maddressMap.isEmpty())
      return new FetchResult<>(null, "No member address properties provided.");

    // validate
    final List<String> emsgs = new ArrayList<>();
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

    final StringBuilder emsg = new StringBuilder();
    @SuppressWarnings("unchecked")
    final FetchResult<Map<String, Object>>[] arrFetchResult = new FetchResult[1];
    try {
      dsl.transaction(configuration -> {
        // update
        final MaddressRecord record =
          DSL.using(configuration)
            .update(MADDRESS)
            .set(cmap)
            .where(MADDRESS.MID.eq(mid)).and(MADDRESS.ADDRESS_NAME.eq(addressname))
            .returning(MADDRESS.MODIFIED) // :o
            .fetchOne();

        // get the inserted record's modified timestamp
        final Date modified = record == null ? null : record.getModified();
        if(record == null || modified == null) {
          // bad insert return values (force rollback)
          throw new DataAccessException("Bad member address update return value.");
        }

        // add returned new member modified timestamp to the cleaned member map
        cmap.put(MEMBER.MODIFIED.getName(), modified);

        // successful member update
        arrFetchResult[0] = new FetchResult<>(cmap, null);
      });
    }
    catch(DataAccessException e) {
      emsg.append(e.getMessage());
    }
    catch(Throwable t) {
      emsg.append("A technical error occurred updating member address.");
    }

    if(emsg.length() > 0) {
      return new FetchResult<>(null, emsg.toString());
    }
    else if(arrFetchResult[0] == null) {
      // sanity check: verify non-null
      return new FetchResult<>(null, "");
    }

    // success
    return arrFetchResult[0];
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
      emsg = e.getMessage();
    }
    catch(Throwable t) {
      emsg = "A technical error occurred deleting member address.";
    }

    // member delete fail
    return new FetchResult<>(null, emsg);
  }
}
