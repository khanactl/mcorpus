package com.tll.mcorpus.repo;

import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.nflatten;
import static com.tll.core.Util.not;
import static com.tll.mcorpus.db.Tables.MADDRESS;
import static com.tll.mcorpus.db.Tables.MAUTH;
import static com.tll.mcorpus.db.Tables.MEMBER;
import static com.tll.mcorpus.repoapi.RepoUtil.fput;
import static com.tll.mcorpus.repoapi.RepoUtil.fputWhenNotNull;
import static com.tll.mcorpus.repoapi.RepoUtil.fval;

import java.io.Closeable;
import java.sql.Timestamp;
import java.time.Instant;
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
import com.tll.mcorpus.db.tables.pojos.Maddress;
import com.tll.mcorpus.db.tables.pojos.Mauth;
import com.tll.mcorpus.db.tables.pojos.Member;
import com.tll.mcorpus.db.udt.pojos.Mref;
import com.tll.mcorpus.db.udt.records.MrefRecord;
import com.tll.mcorpus.dmodel.MemberAndMauth;
import com.tll.mcorpus.dmodel.MemberSearch;
import com.tll.mcorpus.repoapi.FetchResult;

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

  private static MemberAndMauth mapToMemberAndMauth(final Map<String, Object> mmap) {
      // map to pojo
      final MemberAndMauth manda = new MemberAndMauth(
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
          null,
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
      return manda;
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
   * @return Never null {@link FetchResult} object holding the {@link Mref} ref if successful.
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
   * Fetch the {@link Mref} by member id.
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
      emsg = "A data access exception occurred fetching mref by mid.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching mref by mid.";
    }
    // error
    return new FetchResult<>(null, emsg);
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
        Mref mref = dsl
          .select(MEMBER.MID, MEMBER.EMP_ID, MEMBER.LOCATION)
          .from(MEMBER)
          .where(MEMBER.EMP_ID.eq(empId).and(MEMBER.LOCATION.eq(loc)))
          .fetchOne().into(Mref.class);
        return new FetchResult<>(mref, null);
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
    return new FetchResult<>(null, emsg);
  }

  /**
   * Fetch all matching {@link Mref}s having the given emp id.
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
      emsg = "A data access exception occurred fetching mrefs by emp id.";
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsg = "A technical error occurred fetching mrefs by emp id.";
    }
    // error
    return new FetchResult<>(null, emsg);
  }

  public FetchResult<MemberAndMauth> fetchMember(final UUID mid) {
    if(mid == null) return new FetchResult<>(null, "No member id provided.");
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

      if(isNull(mmap)) return new FetchResult<>(null, String.format("No member found with mid: '%s'.", mid));
      
      // map to pojo
      final MemberAndMauth manda = mapToMemberAndMauth(mmap);
      
      // success
      return new FetchResult<>(manda, null);
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
  public FetchResult<List<Maddress>> fetchMemberAddresses(final UUID mid) {
    if(mid == null) return new FetchResult<>(null, "No member id provided.");
    String emsg;
    try {
      final List<Maddress> maddressList = dsl
        .select()
        .from(MADDRESS)
        .where(MADDRESS.MID.eq(mid))
        .fetchInto(Maddress.class);
      
      // success
      return new FetchResult<>(maddressList, null);
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
    return new FetchResult<>(null, emsg);
  }

  /**
   * Member search function with optional filtering and paging offsets.
   *
   * @param msearch the member search conditions object
   * @param offset the member search db offset index
   * @param limit the max number of members to return
   * @return FetchResult for a list of property maps representing member entities.
   */
  public FetchResult<List<MemberAndMauth>> memberSearch(final MemberSearch msearch, int offset, int limit) {
    String emsg;
    try {
      final List<Map<String, Object>> members;
      if(msearch == null || not(msearch.hasSearchConditions())) {
        // NO filter
        members = dsl
          .select(
            MEMBER.MID, MEMBER.CREATED, MEMBER.MODIFIED, MEMBER.EMP_ID, MEMBER.LOCATION, MEMBER.NAME_FIRST, MEMBER.NAME_MIDDLE, MEMBER.NAME_LAST, MEMBER.DISPLAY_NAME, MEMBER.STATUS, 
            MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME
          )
          .from(MEMBER).join(MAUTH).onKey()
          .orderBy(msearch.orderBys)
          .offset(offset).limit(limit)
          .fetch().intoMaps();
      } else {
        // filter
        members = dsl
          .select(
            MEMBER.MID, MEMBER.CREATED, MEMBER.MODIFIED, MEMBER.EMP_ID, MEMBER.LOCATION, MEMBER.NAME_FIRST, MEMBER.NAME_MIDDLE, MEMBER.NAME_LAST, MEMBER.DISPLAY_NAME, MEMBER.STATUS, 
            MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME
          )
          .from(MEMBER).join(MAUTH).onKey()
          .where(msearch.conditions)
          .orderBy(msearch.orderBys)
          .offset(offset).limit(limit)
          .fetch().intoMaps();
      }
      
      if(members == null || members.isEmpty()) {
        // no matches
        return new FetchResult<>(Collections.emptyList(), null);
      }

      final List<MemberAndMauth> mlist = members.stream()
        .map(MCorpusRepo::mapToMemberAndMauth)
        .collect(Collectors.toList());

      return new FetchResult<>(mlist, null);
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
    return new FetchResult<>(null, emsg);
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
    if(isNull(memberToAdd)) return new FetchResult<>(null, "No member provided.");

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
            null,
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
    return new FetchResult<>(added, nflatten(emsgs, ","));
  }

  public FetchResult<MemberAndMauth> updateMember(final MemberAndMauth memberToUpdate) {
    if(isNull(memberToUpdate)) return new FetchResult<>(null, "No member provided.");
    
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
          mmap = trans
                    .update(MAUTH)
                    .set(fmapMauth)
                    .where(MAUTH.MID.eq(mid))
                    .returning(MAUTH.MID, MAUTH.MODIFIED, MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME) // :o
                    .fetchOne().intoMap();
        } else {
          // otherwise select to get current snapshot
          mmap = trans
                    .select(MAUTH.DOB, MAUTH.SSN, MAUTH.EMAIL_PERSONAL, MAUTH.EMAIL_WORK, MAUTH.MOBILE_PHONE, MAUTH.HOME_PHONE, MAUTH.WORK_PHONE, MAUTH.USERNAME)
                    .from(MAUTH)
                    .where(MAUTH.MID.eq(mid))
                    .fetchOne().intoMap();
        }
        
        if (isNullOrEmpty(mmap)) {
          // mauth insert failed (rollback)
          throw new DataAccessException("No post-update mauth record returned.");
        }

        // update member record (always to maintain modified integrity)
        final Map<String, Object> fmapMember = new HashMap<>();
        fput(MEMBER.MODIFIED, Timestamp.from(Instant.now()), fmapMember); // force
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
    return new FetchResult<>(updated, nflatten(emsgs, ","));
  }

  /**
   * Delete a member from the system (physical record removal).
   *
   * @param mid the id of the member to delete
   * @return FetchResult of the member id upon successful deletion,
   *                     otherwise an error message.
   */
  public FetchResult<Boolean> deleteMember(final UUID mid) {
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
      return new FetchResult<>(Boolean.TRUE, null);
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
    return new FetchResult<>(null, emsg);
  }

  public FetchResult<Maddress> addMemberAddress(final Maddress memberAddressToAdd) {
    if(isNull(memberAddressToAdd)) return new FetchResult<>(null, "No member address provided.");

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
      return new FetchResult<>(maddress, null);
    }
    catch(DataAccessException e) {
      log.error(e.getMessage());
      emsgs.add("A data access exception occurred addming member address.");
    }
    catch(Throwable t) {
      log.error(t.getMessage());
      emsgs.add("A technical error occurred adding member address.");
    }

    // fail
    return new FetchResult<>(null, nflatten(emsgs, ","));
  }

  public FetchResult<Maddress> updateMemberAddress(final Maddress maddressToUpdate) {
    if(isNull(maddressToUpdate)) return new FetchResult<>(null, "No member address provided.");

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

      final Maddress maddress =
        dsl
          .update(MADDRESS)
          .set(fmap)
          .where(MADDRESS.MID.eq(mid)).and(MADDRESS.ADDRESS_NAME.eq(addressname))
          .returning() // :o
          .fetchOne().into(Maddress.class);

      if(isNull(maddress))
        throw new DataAccessException("Bad member address update return value.");

      // success
      return new FetchResult<>(maddress, null);
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
    return new FetchResult<>(null, nflatten(emsgs, ","));
  }

  /**
   * Delete a member address.
   *
   * @param mid the id of the member "owning" the address to delete
   * @param addressname the address name identifying the member address to delete
   * @return FetchResult<TRUE> upon successful deletion, or an error message.
   */
  public FetchResult<Boolean> deleteMemberAddress(final UUID mid, Addressname addressname) {
    if(mid == null) return new FetchResult<>(null, "No member id provided.");
    if(addressname == null) return new FetchResult<>(null, "No address name provided.");
    String emsg;
    try {
      final int numDeleted = dsl
                .delete(MADDRESS)
                .where(MADDRESS.MID.eq(mid).and(MADDRESS.ADDRESS_NAME.eq(addressname)))
                .execute();

      if(numDeleted != 1) throw new DataAccessException("Invalid member address delete return value.");

      // success
      return new FetchResult<>(Boolean.TRUE);
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
    return new FetchResult<>(null, emsg);
  }
}
