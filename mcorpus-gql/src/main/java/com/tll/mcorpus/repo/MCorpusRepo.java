package com.tll.mcorpus.repo;

import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.nflatten;
import static com.tll.core.Util.not;
import static com.tll.mcorpus.db.Tables.MADDRESS;
import static com.tll.mcorpus.db.Tables.MAUTH;
import static com.tll.mcorpus.db.Tables.MEMBER;
import static com.tll.mcorpus.repo.MCorpusRepoUtil.fputWhenNotNull;
import static com.tll.mcorpus.repo.MCorpusRepoUtil.fval;
import static com.tll.repo.FetchResult.fetchrslt;

import java.io.Closeable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.routines.InsertMember;
import com.tll.mcorpus.db.routines.MemberLogin;
import com.tll.mcorpus.db.routines.MemberLogout;
import com.tll.mcorpus.db.routines.MemberPswd;
import com.tll.mcorpus.db.tables.pojos.Maddress;
import com.tll.mcorpus.db.tables.pojos.Mauth;
import com.tll.mcorpus.db.tables.pojos.Member;
import com.tll.mcorpus.db.tables.records.MaddressRecord;
import com.tll.mcorpus.db.tables.records.MauthRecord;
import com.tll.mcorpus.db.udt.pojos.Mref;
import com.tll.mcorpus.db.udt.records.MrefRecord;
import com.tll.mcorpus.dmodel.MemberAndMaddresses;
import com.tll.mcorpus.dmodel.MemberAndMauth;
import com.tll.mcorpus.dmodel.MemberSearch;
import com.tll.repo.FetchResult;

import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.Record9;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderKeywordCase;
import org.jooq.conf.RenderNameCase;
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

  static MemberAndMauth mapToMemberAndMauth(final Map<String, Object> mmap) {
      // map to pojo
      return new MemberAndMauth(
        new Member(
          fval(MEMBER.MID, mmap),
          fval(MEMBER.CREATED, mmap),
          fval(MEMBER.MODIFIED, mmap),
          fval(MEMBER.EMP_ID, mmap),
          fval(MEMBER.LOCATION, mmap),
          fval(MEMBER.NAME_FIRST, mmap),
          fval(MEMBER.NAME_MIDDLE, mmap),
          fval(MEMBER.NAME_LAST, mmap),
          fval(MEMBER.DISPLAY_NAME, mmap),
          fval(MEMBER.STATUS, mmap)
        ),
        new Mauth(
          fval(MEMBER.MID, mmap),
          fval(MAUTH.MODIFIED, mmap),
          fval(MAUTH.DOB, mmap),
          fval(MAUTH.SSN, mmap),
          fval(MAUTH.EMAIL_PERSONAL, mmap),
          fval(MAUTH.EMAIL_WORK, mmap),
          fval(MAUTH.MOBILE_PHONE, mmap),
          fval(MAUTH.HOME_PHONE, mmap),
          fval(MAUTH.WORK_PHONE, mmap),
          fval(MAUTH.FAX, mmap),
          fval(MAUTH.USERNAME, mmap),
          null
        )
      );
  }

  static MemberAndMaddresses mapToOneMemberAndMaddresses(List<Map<String, Object>> mlist) {
    return new MemberAndMaddresses(
      mapToMemberAndMauth(mlist.get(0)),
      mlist.stream().map(map -> {
        return new Maddress(
          fval(MADDRESS.MID, map),
          fval(MADDRESS.ADDRESS_NAME, map),
          fval(MADDRESS.MODIFIED, map),
          fval(MADDRESS.ATTN, map),
          fval(MADDRESS.STREET1, map),
          fval(MADDRESS.STREET2, map),
          fval(MADDRESS.CITY, map),
          fval(MADDRESS.STATE, map),
          fval(MADDRESS.POSTAL_CODE, map),
          fval(MADDRESS.COUNTRY, map)
        );
      }).filter(ma -> isNotNull(ma.getAddressName())).collect(Collectors.toList())
    );
  }

  protected final Logger log = LoggerFactory.getLogger("MCorpusRepo");

  protected final DSLContext dsl;

  /**
   * Constructor.
   *
   * @param ds the data source
   * @param vldtr optional entity validator
   */
  public MCorpusRepo(DataSource ds) {
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
   * The member login routine which fetches the member ref whose username and
   * password matches the ones given.
   * <p>
   * Blocking!
   *
   * @param username the member username
   * @param pswd the member password
   * @param requestInstant the sourcing request timestamp
   * @param clientOrigin the sourcing request client origin token
   * @return Never null {@link FetchResult} object holding the {@link Mref} ref if successful.
   */
  public FetchResult<Mref> memberLogin(final String username, final String pswd, final Instant requestInstant, final String clientOrigin) {
    try {
      final MemberLogin mlogin = new MemberLogin();
      mlogin.setMemberUsername(username);
      mlogin.setMemberPassword(pswd);
      mlogin.setInRequestTimestamp(OffsetDateTime.ofInstant(requestInstant, ZoneId.systemDefault()));
      mlogin.setInRequestOrigin(clientOrigin);
      mlogin.execute(dsl.configuration());
      final MrefRecord rec = mlogin.getReturnValue();
      if(rec != null && rec.getMid() != null) {
        // login success
        final Mref mref = rec.into(Mref.class);
        return fetchrslt(mref, null);
      }
    }
    catch(Throwable t) {
      log.error("Member login error: {}.", t.getMessage());
    }
    // default - login fail
    return fetchrslt(null, "Member login failed.");
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
      mlogout.setInRequestTimestamp(OffsetDateTime.ofInstant(requestInstant, ZoneId.systemDefault()));
      mlogout.setInRequestOrigin(clientOrigin);
      mlogout.execute(dsl.configuration());
      final UUID rmid = mlogout.getReturnValue();
      if(rmid != null) {
        // logout successful - convey by returning the mid
        return fetchrslt(rmid, null);
      }
    }
    catch(Throwable t) {
      log.error("Member logout error: {}.", t.getMessage());
    }
    // default - logout failed
    return fetchrslt(null, "Member logout failed.");
  }

  /**
   * Fetch the {@link Mref} by member id.
   *
   * @param mid the member id
   * @return newly created {@link FetchResult} wrapping a property map.
   */
  public FetchResult<Mref> fetchMRefByMid(final UUID mid) {
    if(mid == null) return fetchrslt(null, "No member id provided.");
    String emsg;
    try {
      final Record3<UUID, String, Location> mrefRec = dsl
        .select(MEMBER.MID, MEMBER.EMP_ID, MEMBER.LOCATION)
        .from(MEMBER)
        .where(MEMBER.MID.eq(mid))
        .fetchOne();

      if(isNotNull(mrefRec)) {
        // success
        final Mref mref = mrefRec.into(Mref.class);
        return fetchrslt(mref);
      } else {
        // in error
        emsg = "No mref found with provided mid.";
      }
    }
    catch(DataAccessException dae) {
      log.error(dae.getMessage());
      emsg = "A data access exception occurred fetching mref by mid.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching mref by mid.";
    }
    // error
    return fetchrslt(null, emsg);
  }

  /**
   * Fetch the {@link Mref} by emp id and location.
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
        Record3<UUID, String, Location> mrefRec = dsl
          .select(MEMBER.MID, MEMBER.EMP_ID, MEMBER.LOCATION)
          .from(MEMBER)
          .where(MEMBER.EMP_ID.eq(empId).and(MEMBER.LOCATION.eq(loc)))
          .fetchOne();

        if(isNotNull(mrefRec)) {
          // success
          final Mref mref = mrefRec.into(Mref.class);
          return fetchrslt(mref);
        } else {
          // in error
          emsg = "No mref found with provided empId and location.";
        }
      }
      catch(DataAccessException dae) {
        log.error(dae.getMessage());
        emsg = "A data access exception occurred fetching mref by empid and location.";
      }
      catch(Throwable t) {
        log.error(t.getMessage());
        emsg = "A technical error occurred fetching mref by by empid and location.";
      }
    }
    else {
      emsg = "Invalid emp id and/or location for mref fetch.";
    }
    // error
    return fetchrslt(null, emsg);
  }

  /**
   * Fetch all matching {@link Mref}s having the given emp id.
   *
   * @param empId the member emp id
   * @return newly created {@link FetchResult} wrapping a list of property map mref elements upon success
   *          or wrapping an error message upon a fetch error.
   */
  public FetchResult<List<Mref>> fetchMRefsByEmpId(final String empId) {
    if(empId == null) return fetchrslt(null, "No emp id provided.");
    String emsg;
    try {
      final List<Mref> mref = dsl
        .select(MEMBER.MID, MEMBER.EMP_ID, MEMBER.LOCATION)
        .from(MEMBER)
        .where(MEMBER.EMP_ID.eq(empId))
        .fetchInto(Mref.class);
      return fetchrslt(mref, null);
    }
    catch(DataAccessException dae) {
      log.error(dae.getMessage());
      emsg = "A data access exception occurred fetching mrefs by emp id.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching mrefs by emp id.";
    }
    // error
    return fetchrslt(null, emsg);
  }

  public FetchResult<MemberAndMauth> fetchMember(final UUID mid) {
    if(mid == null) return fetchrslt(null, "No member id provided.");
    String emsg;
    try {
      final Map<String, Object> mmap = dsl
        .select(
          MEMBER.MID, MEMBER.CREATED, MEMBER.MODIFIED, MEMBER.EMP_ID, MEMBER.LOCATION, MEMBER.NAME_FIRST, MEMBER.NAME_MIDDLE, MEMBER.NAME_LAST, MEMBER.DISPLAY_NAME, MEMBER.STATUS,
          MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.FAX, MAUTH.USERNAME
        )
        .from(MEMBER).join(MAUTH).onKey()
        .where(MEMBER.MID.eq(mid))
        .fetchOneMap();

      if(isNull(mmap)) return fetchrslt(null, String.format("No member found with mid: '%s'.", mid));

      // map to pojo
      final MemberAndMauth manda = mapToMemberAndMauth(mmap);

      // success
      return fetchrslt(manda, null);
    }
    catch(DataAccessException dae) {
      log.error(dae.getMessage());
      emsg = "A data access exception occurred fetching member.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching member.";
    }
    // error
    return fetchrslt(null, emsg);
  }

  /**
   * Fetch a member and all related member addresses (if any) given a member id.
   *
   * @param mid the member id
   * @return newly created {@link FetchResult} wrapping a {@link MemberAndMaddresses} domain object
   *         or wrapping an error message upon a fetch error.
   */
  public FetchResult<MemberAndMaddresses> fetchMemberAndAddresses(final UUID mid) {
    if(mid == null) return fetchrslt(null, "No member id provided.");
    String emsg;
    try {
      final List<Map<String, Object>> mlist = dsl
        .select(
          MEMBER.MID, MEMBER.CREATED, MEMBER.MODIFIED, MEMBER.EMP_ID, MEMBER.LOCATION, MEMBER.NAME_FIRST, MEMBER.NAME_MIDDLE, MEMBER.NAME_LAST, MEMBER.DISPLAY_NAME, MEMBER.STATUS,
          MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.FAX, MAUTH.USERNAME,
          MADDRESS.ADDRESS_NAME, MADDRESS.ATTN, MADDRESS.STREET1, MADDRESS.STREET2, MADDRESS.CITY, MADDRESS.STATE, MADDRESS.POSTAL_CODE, MADDRESS.COUNTRY
        )
        .from(MEMBER).join(MAUTH).onKey().leftJoin(MADDRESS).on(MEMBER.MID.eq(MADDRESS.MID))
        .where(MEMBER.MID.eq(mid))
        .fetchMaps();

      if(isNullOrEmpty(mlist)) return fetchrslt(null, String.format("No member and addresses found with mid: '%s'.", mid));

      // map to pojo
      final MemberAndMaddresses mandaddresses = mapToOneMemberAndMaddresses(mlist);

      // success
      return fetchrslt(mandaddresses, null);
    }
    catch(DataAccessException dae) {
      log.error(dae.getMessage());
      emsg = "A data access exception occurred fetching member and addresses.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching member and addresses.";
    }
    // error
    return fetchrslt(null, emsg);
  }

  /**
   * Fetch all held member addresses for a given member.
   *
   * @param mid the member id for which to get addresses
   * @return the potentially empty list of member addresses
   *          wrapped in a {@link FetchResult} to enclose an
   *          error message when the fetch fails
   */
  public FetchResult<List<Maddress>> fetchMemberAddresses(final UUID mid) {
    if(mid == null) return fetchrslt(null, "No member id provided.");
    String emsg;
    try {
      final List<Maddress> maddressList = dsl
        .select()
        .from(MADDRESS)
        .where(MADDRESS.MID.eq(mid))
        .fetchInto(Maddress.class);

      // success
      return fetchrslt(maddressList, null);
    }
    catch(DataAccessException dae) {
      log.error(dae.getMessage());
      emsg = "A data access exception occurred fetching member address.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching member addresses.";
    }
    // error
    return fetchrslt(null, emsg);
  }

  /**
   * Member search function with optional filtering and paging offsets.
   *
   * @param msearch the member search conditions object
   * @return FetchResult for a list of property maps representing member entities.
   */
  public FetchResult<List<MemberAndMauth>> memberSearch(final MemberSearch msearch) {
    String emsg;
    try {
      final List<Map<String, Object>> mmap;
      if(not(msearch.hasSearchConditions())) {
        // NO filter
        mmap = dsl
          .select(
            MEMBER.MID, MEMBER.CREATED, MEMBER.MODIFIED, MEMBER.EMP_ID, MEMBER.LOCATION, MEMBER.NAME_FIRST, MEMBER.NAME_MIDDLE, MEMBER.NAME_LAST, MEMBER.DISPLAY_NAME, MEMBER.STATUS,
            MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME
          )
          .from(MEMBER).join(MAUTH).onKey()
          .orderBy(msearch.orderBys)
          .offset(msearch.offset).limit(msearch.limit)
          .fetch().intoMaps();
      } else {
        // filter
        mmap = dsl
          .select(
            MEMBER.MID, MEMBER.CREATED, MEMBER.MODIFIED, MEMBER.EMP_ID, MEMBER.LOCATION, MEMBER.NAME_FIRST, MEMBER.NAME_MIDDLE, MEMBER.NAME_LAST, MEMBER.DISPLAY_NAME, MEMBER.STATUS,
            MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME
          )
          .from(MEMBER).join(MAUTH).onKey()
          .where(msearch.conditions)
          .orderBy(msearch.orderBys)
          .offset(msearch.offset).limit(msearch.limit)
          .fetch().intoMaps();
      }

      if(isNullOrEmpty(mmap)) {
        // no matches
        return fetchrslt(Collections.emptyList(), null);
      }

      final List<MemberAndMauth> mlist = mmap.stream()
        .map(MCorpusRepo::mapToMemberAndMauth)
        .collect(Collectors.toList());

      return fetchrslt(mlist, null);
    }
    catch(DataAccessException dae) {
      log.error(dae.getMessage());
      emsg = "A data access error occurred fetching members by search.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching members by search.";
    }
    // error
    return fetchrslt(null, emsg);
  }

  /**
   * Add a member.
   *
   * @param memberToAdd the member to insert in the backend.
   *
   * @return newly created fetch result of the added {@link MemberAndMauth} upon success
   *         -OR- an error message otherwise.
   */
  public FetchResult<MemberAndMauth> addMember(final MemberAndMauth memberToAdd) {
    if(isNull(memberToAdd)) return fetchrslt(null, "No member provided.");

    final List<String> emsgs = new ArrayList<>();
    final List<MemberAndMauth> rlist = new ArrayList<>(1);

    try {
      dsl.transaction(configuration -> {
        final DSLContext trans = DSL.using(configuration);

        final InsertMember im = new InsertMember();
        im.setInEmpId(memberToAdd.dbMember.getEmpId());
        im.setInLocation(memberToAdd.dbMember.getLocation());
        im.setInNameFirst(memberToAdd.dbMember.getNameFirst());
        im.setInNameMiddle(memberToAdd.dbMember.getNameMiddle());
        im.setInNameLast(memberToAdd.dbMember.getNameLast());
        im.setInDisplayName(memberToAdd.dbMember.getDisplayName());
        im.setInStatus(memberToAdd.dbMember.getStatus());
        im.setInDob(memberToAdd.dbMauth.getDob());
        im.setInSsn(memberToAdd.dbMauth.getSsn());
        im.setInEmailPersonal(memberToAdd.dbMauth.getEmailPersonal());
        im.setInEmailWork(memberToAdd.dbMauth.getEmailWork());
        im.setInMobilePhone(memberToAdd.dbMauth.getMobilePhone());
        im.setInHomePhone(memberToAdd.dbMauth.getHomePhone());
        im.setInWorkPhone(memberToAdd.dbMauth.getWorkPhone());
        im.setInFax(memberToAdd.dbMauth.getFax());
        im.setInUsername(memberToAdd.dbMauth.getUsername());
        im.setInPswd(memberToAdd.dbMauth.getPswd());

        im.execute(trans.configuration());

        final MemberAndMauth added = new MemberAndMauth(
          new Member(
            im.getOutMid(),
            im.getOutCreated(),
            im.getOutModified(),
            im.getOutEmpId(),
            im.getOutLocation(),
            im.getOutNameFirst(),
            im.getOutNameMiddle(),
            im.getOutNameLast(),
            im.getOutDisplayName(),
            im.getOutStatus()
          ),
          new Mauth(
            im.getOutMid(),
            im.getOutModified(),
            im.getOutDob(),
            im.getOutSsn(),
            im.getOutEmailPersonal(),
            im.getOutEmailWork(),
            im.getOutMobilePhone(),
            im.getOutHomePhone(),
            im.getOutWorkPhone(),
            im.getOutFax(),
            im.getOutUsername(),
            null
          )
        );

        // success
        rlist.add(added);
      });
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsgs.add("A data access exception occurred adding member.");
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsgs.add("A technical error occurred adding member.");
    }

    final MemberAndMauth added = rlist.size() == 1 ? rlist.get(0) : null;
    return fetchrslt(added, nflatten(emsgs, ","));
  }

  public FetchResult<MemberAndMauth> updateMember(final MemberAndMauth memberToUpdate) {
    if(isNull(memberToUpdate)) return fetchrslt(null, "No member provided.");

    final List<String> emsgs = new ArrayList<>();

    final UUID mid = memberToUpdate.dbMember.getMid();

    final List<MemberAndMauth> rlist = new ArrayList<>(1);

    try {
      dsl.transaction(configuration -> {

        final DSLContext trans = DSL.using(configuration);

        final Map<String, Object> mmap;

        // update mauth
        if(memberToUpdate.hasMauthTableVals()) {

          // update mauth record
          final Map<String, Object> fmapMauth = new HashMap<>();
          fputWhenNotNull(MAUTH.DOB, memberToUpdate.dbMauth.getDob(), fmapMauth);
          fputWhenNotNull(MAUTH.SSN, memberToUpdate.dbMauth.getSsn(), fmapMauth);
          fputWhenNotNull(MAUTH.EMAIL_PERSONAL, memberToUpdate.dbMauth.getEmailPersonal(), fmapMauth);
          fputWhenNotNull(MAUTH.EMAIL_WORK, memberToUpdate.dbMauth.getEmailWork(), fmapMauth);
          fputWhenNotNull(MAUTH.MOBILE_PHONE, memberToUpdate.dbMauth.getMobilePhone(), fmapMauth);
          fputWhenNotNull(MAUTH.HOME_PHONE, memberToUpdate.dbMauth.getHomePhone(), fmapMauth);
          fputWhenNotNull(MAUTH.WORK_PHONE, memberToUpdate.dbMauth.getWorkPhone(), fmapMauth);
          fputWhenNotNull(MAUTH.USERNAME, memberToUpdate.dbMauth.getUsername(), fmapMauth);

          MauthRecord mauthRec = trans
                    .update(MAUTH)
                    .set(fmapMauth)
                    .where(MAUTH.MID.eq(mid))
                    .returning(MAUTH.MID, MAUTH.MODIFIED, MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME) // :o
                    .fetchOne();

          if(isNull(mauthRec)) {
            // mauth insert failed (rollback)
            emsgs.add("No member found to update with provided id.");
            throw new DataAccessException(String.format("Member with mid '%s' not found to update.", mid));
          }

          mmap = mauthRec.intoMap();
        } else {
          // otherwise select mauth to get current snapshot
          Record9<OffsetDateTime, LocalDate, String, String, String, String, String, String, String> mauthRecFetch = trans
                    .select(MAUTH.MODIFIED, MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME)
                    .from(MAUTH)
                    .where(MAUTH.MID.eq(mid))
                    .fetchOne();

          if(isNull(mauthRecFetch)) {
            // mauth select failed (rollback)
            emsgs.add("No member found to update with provided id.");
            throw new DataAccessException(String.format("Member with mid '%s' not found for update.", mid));
          }

          mmap = mauthRecFetch.intoMap();
        }


        // update member record (always to maintain modified integrity)
        final Map<String, Object> fmapMember = new HashMap<>();
        // fput(MEMBER.MODIFIED, OffsetDateTime.now(), fmapMember); // done in backend!
        fputWhenNotNull(MEMBER.EMP_ID, memberToUpdate.dbMember.getEmpId(), fmapMember);
        fputWhenNotNull(MEMBER.LOCATION, memberToUpdate.dbMember.getLocation(), fmapMember);
        fputWhenNotNull(MEMBER.NAME_FIRST, memberToUpdate.dbMember.getNameFirst(), fmapMember);
        fputWhenNotNull(MEMBER.NAME_MIDDLE, memberToUpdate.dbMember.getNameMiddle(), fmapMember);
        fputWhenNotNull(MEMBER.NAME_LAST, memberToUpdate.dbMember.getNameLast(), fmapMember);
        fputWhenNotNull(MEMBER.DISPLAY_NAME, memberToUpdate.dbMember.getDisplayName(), fmapMember);
        fputWhenNotNull(MEMBER.STATUS, memberToUpdate.dbMember.getStatus(), fmapMember);

        mmap.putAll(trans
                .update(MEMBER)
                .set(fmapMember)
                .where(MEMBER.MID.eq(mid))
                .returning()
                .fetchOne().intoMap());

        // success
        rlist.add(mapToMemberAndMauth(mmap));

        // commit happens upon falling out of scope
      });
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsgs.add("A data access error occurred updating member.");
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsgs.add("A technical error occurred updating member.");
    }

    // success
    final MemberAndMauth updated = rlist.size() == 1 ? rlist.get(0) : null;
    return fetchrslt(updated, nflatten(emsgs, ","));
  }

  /**
   * Delete a member from the system (physical record removal).
   *
   * @param mid the id of the member to delete
   * @return FetchResult of the member id upon successful deletion,
   *                     otherwise an error message.
   */
  public FetchResult<Boolean> deleteMember(final UUID mid) {
    if(mid == null) return fetchrslt(null, "No member id provided.");

    String emsg;
    try {
      final int numDeleted =
        dsl
          .delete(MEMBER)
          .where(MEMBER.MID.eq(mid))
          .execute();

      if(numDeleted != 1) throw new DataAccessException("Invalid member delete return value.");

      // success
      return fetchrslt(Boolean.TRUE, null);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsg = "A data access exception occurred deleting member.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred deleting member.";
    }

    // fail
    return fetchrslt(null, emsg);
  }

  /**
   * Set/reset a member pswd.
   *
   * @param mid the id of the member
   * @param pswd the pswd to set
   */
  public FetchResult<Boolean> setMemberPswd(final UUID mid, final String pswd) {
    String emsg = null;
    try {
      final MemberPswd sp = new MemberPswd();
      sp.setInMid(mid);
      sp.setInPswd(pswd);
      sp.execute(dsl.configuration());

      // success
      return fetchrslt(Boolean.TRUE, null);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsg = "A data access exception occurred setting member pswd.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred setting member pswd.";
    }

    // fail
    return fetchrslt(Boolean.FALSE, emsg);
  }

  public FetchResult<Maddress> addMemberAddress(final Maddress memberAddressToAdd) {
    if(isNull(memberAddressToAdd)) return fetchrslt(null, "No member address provided.");

    final List<String> emsgs = new ArrayList<>();

    final UUID mid = memberAddressToAdd.getMid();

    try {
      // add record
      final Maddress maddress =
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
            mid,
            memberAddressToAdd.getAddressName(),
            memberAddressToAdd.getAttn(),
            memberAddressToAdd.getStreet1(),
            memberAddressToAdd.getStreet2(),
            memberAddressToAdd.getCity(),
            memberAddressToAdd.getState(),
            memberAddressToAdd.getPostalCode(),
            memberAddressToAdd.getCountry()
          )
          .returning()
          .fetchOne().into(Maddress.class);

      if(isNull(maddress) || isNull(maddress.getMid()))
        throw new DataAccessException("Invalid member address insert return value.");

      // success
      return fetchrslt(maddress, null);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsgs.add("A data access exception occurred adding member address.");
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsgs.add("A technical error occurred adding member address.");
    }

    // fail
    return fetchrslt(null, nflatten(emsgs, ","));
  }

  public FetchResult<Maddress> updateMemberAddress(final Maddress maddressToUpdate) {
    if(isNull(maddressToUpdate)) return fetchrslt(null, "No member address provided.");

    final List<String> emsgs = new ArrayList<>();

    final UUID mid = maddressToUpdate.getMid();
    final Addressname addressname = maddressToUpdate.getAddressName();

    try {
      // update
      final Map<String, Object> fmap = new HashMap<>();
      fputWhenNotNull(MADDRESS.ATTN, maddressToUpdate.getAttn(), fmap);
      fputWhenNotNull(MADDRESS.STREET1, maddressToUpdate.getStreet1(), fmap);
      fputWhenNotNull(MADDRESS.STREET2, maddressToUpdate.getStreet2(), fmap);
      fputWhenNotNull(MADDRESS.CITY, maddressToUpdate.getCity(), fmap);
      fputWhenNotNull(MADDRESS.STATE, maddressToUpdate.getState(), fmap);
      fputWhenNotNull(MADDRESS.POSTAL_CODE, maddressToUpdate.getPostalCode(), fmap);
      fputWhenNotNull(MADDRESS.COUNTRY, maddressToUpdate.getCountry(), fmap);

      final MaddressRecord maddressRec =
        dsl
          .update(MADDRESS)
          .set(fmap)
          .where(MADDRESS.MID.eq(mid)).and(MADDRESS.ADDRESS_NAME.eq(addressname))
          .returning() // :o
          .fetchOne();

      if(isNotNull(maddressRec)) {
        // success
        final Maddress maddress = maddressRec.into(Maddress.class);
        return fetchrslt(maddress, null);
      } else {
        // in error
        final String emsg = String.format("No %s member address found to update.", addressname.getLiteral());
        log.error(emsg);
        emsgs.add(emsg);
      }
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsgs.add("A data access exception occurred updating member address.");
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsgs.add("A technical error occurred updating member address.");
    }

    // fail
    return fetchrslt(null, nflatten(emsgs, ","));
  }

  /**
   * Delete a member address.
   *
   * @param mid the id of the member "owning" the address to delete
   * @param addressname the address name identifying the member address to delete
   * @return FetchResult<TRUE> upon successful deletion, or an error message.
   */
  public FetchResult<Boolean> deleteMemberAddress(final UUID mid, Addressname addressname) {
    if(mid == null) return fetchrslt(null, "No member id provided.");
    if(addressname == null) return fetchrslt(null, "No address name provided.");
    String emsg;
    try {
      final int numDeleted = dsl
                .delete(MADDRESS)
                .where(MADDRESS.MID.eq(mid).and(MADDRESS.ADDRESS_NAME.eq(addressname)))
                .execute();

      if(numDeleted != 1) throw new DataAccessException("Invalid member address delete return value.");

      // success
      return fetchrslt(Boolean.TRUE);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsg = "A data access exception occurred deleting member address.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred deleting member address.";
    }

    // member delete fail
    return fetchrslt(null, emsg);
  }
}
