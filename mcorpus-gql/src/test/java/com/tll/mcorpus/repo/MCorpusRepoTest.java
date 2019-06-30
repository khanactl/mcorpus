package com.tll.mcorpus.repo;


import static com.tll.TestUtil.toSqlDate;
import static com.tll.mcorpus.MCorpusTestUtil.ds_mcweb;
import static com.tll.mcorpus.MCorpusTestUtil.isTestDslMcwebTestLoaded;
import static com.tll.mcorpus.MCorpusTestUtil.testDslMcweb;
import static com.tll.mcorpus.MCorpusTestUtil.testDslMcwebTest;
import static com.tll.mcorpus.MCorpusTestUtil.testRequestOrigin;
import static com.tll.mcorpus.db.Tables.MADDRESS;
import static com.tll.mcorpus.db.Tables.MAUTH;
import static com.tll.mcorpus.db.Tables.MEMBER;
import static com.tll.mcorpus.db.Tables.MEMBER_AUDIT;
import static com.tll.transform.TransformUtil.asSqlDate;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.tll.UnitTest;
import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.MemberStatus;
import com.tll.mcorpus.db.routines.PassHash;
import com.tll.mcorpus.db.tables.pojos.Maddress;
import com.tll.mcorpus.db.tables.pojos.Mauth;
import com.tll.mcorpus.db.tables.pojos.Member;
import com.tll.mcorpus.db.tables.records.MemberRecord;
import com.tll.mcorpus.db.udt.pojos.Mref;
import com.tll.mcorpus.dmodel.MemberAndMauth;
import com.tll.repo.FetchResult;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for {@link MCorpusRepo}.
 */
@Category(UnitTest.class)
public class MCorpusRepoTest {

  private static final Logger log = LoggerFactory.getLogger(MCorpusRepoTest.class);

  /*
  private static final Random rand = new Random();
  
  public static String randomEmpId() {
    String s = String.format("%02d-%07d",
      Integer.valueOf(rand.nextInt(100)),
      Integer.valueOf(rand.nextInt(10000000))
    );
    return s;
  }

  public static Location randomLocation() {
    return Location.values()[rand.nextInt(Location.values().length)];
  }
  */

  static final String           TEST_MEMBER_EMP_ID = "00-0000000";
  static final Location         TEST_MEMBER_LOCATION = Location._01;
  static final String           TEST_MEMBER_FIRST_NAME = "Jake";
  static final String           TEST_MEMBER_MIDDLE_NAME = "Arthur";
  static final String           TEST_MEMBER_LAST_NAME = "McGidrich";
  static final String           TEST_MEMBER_DISPLAY_NAME = "JAMFMan";
  static final MemberStatus     TEST_MEMBER_STATUS = MemberStatus.ACTIVE;
  
  static final String           TEST_MEMBER_FIRST_NAME_U = "JakeU";
  static final String           TEST_MEMBER_MIDDLE_NAME_U = "ArthurU";
  static final String           TEST_MEMBER_LAST_NAME_U = "McGidrichU";
  static final MemberStatus     TEST_MEMBER_STATUS_U = MemberStatus.INACTIVE;
  
  static final java.sql.Date    TEST_MAUTH_DOB = toSqlDate("1977-09-04");
  static final String           TEST_MAUTH_SSN = "101010101";
  static final String           TEST_MAUTH_EMAIL_HOME = "jam@ggl.com";
  static final String           TEST_MAUTH_EMAIL_WORK = "jam-work@ggl.com";
  static final String           TEST_MAUTH_MOBILE_PHONE = "4156747832";
  static final String           TEST_MAUTH_HOME_PHONE = "4156747833";
  static final String           TEST_MAUTH_WORK_PHONE = "4156747834";
  static final String           TEST_MAUTH_FAX = "4156747835";
  static final String           TEST_MAUTH_USERNAME = "jamuser";
  static final String           TEST_MAUTH_PSWD = "nixem567ert";
  
  static final java.sql.Date    TEST_MAUTH_DOB_U = toSqlDate("1977-09-05");
  static final String           TEST_MAUTH_SSN_U = "101010102";
  static final String           TEST_MAUTH_EMAIL_HOME_U = "jamU@ggl.com";
  static final String           TEST_MAUTH_EMAIL_WORK_U = "jam-workU@ggl.com";
  static final String           TEST_MAUTH_MOBILE_PHONE_U = "4156747833";
  static final String           TEST_MAUTH_HOME_PHONE_U = "4156747834";
  static final String           TEST_MAUTH_WORK_PHONE_U = "4156747835";
  
  static MemberAndMauth generateMemberToAdd() {
    return new MemberAndMauth(
      new Member(
        null, 
        null, 
        null, 
        TEST_MEMBER_EMP_ID, 
        TEST_MEMBER_LOCATION, 
        TEST_MEMBER_FIRST_NAME, 
        TEST_MEMBER_MIDDLE_NAME, 
        TEST_MEMBER_LAST_NAME, 
        TEST_MEMBER_DISPLAY_NAME, 
        TEST_MEMBER_STATUS 
      ),
      new Mauth(
        null,
        null,
        TEST_MAUTH_DOB, 
        TEST_MAUTH_SSN, 
        TEST_MAUTH_EMAIL_HOME, 
        TEST_MAUTH_EMAIL_WORK, 
        TEST_MAUTH_MOBILE_PHONE, 
        TEST_MAUTH_HOME_PHONE, 
        TEST_MAUTH_WORK_PHONE, 
        TEST_MAUTH_FAX, 
        TEST_MAUTH_USERNAME, 
        TEST_MAUTH_PSWD 
      )
    );
  }

  static MemberAndMauth generateMemberToUpdate(final UUID mid) {
    return new MemberAndMauth(
      new Member(
        mid, 
        null, 
        null, 
        null, 
        null, 
        TEST_MEMBER_FIRST_NAME_U, 
        TEST_MEMBER_MIDDLE_NAME_U, 
        TEST_MEMBER_LAST_NAME_U, 
        null, 
        MemberStatus.INACTIVE 
      ),
      new Mauth(
        null,
        null,
        TEST_MAUTH_DOB_U, 
        TEST_MAUTH_SSN_U, 
        TEST_MAUTH_EMAIL_HOME_U, 
        TEST_MAUTH_EMAIL_WORK_U, 
        TEST_MAUTH_MOBILE_PHONE_U, 
        TEST_MAUTH_HOME_PHONE_U, 
        TEST_MAUTH_WORK_PHONE_U, 
        null, 
        null, 
        null
      )
    );
  }

  static Maddress generateMemberAddressToAdd(UUID mid, Addressname addressname) {
    return new Maddress(
      mid,
      addressname,
      null,
      "attn",
      "88 bway",
      "#3",
      "city",
      "MS",
      "99876",
      "USA"
    );
  }

  static Maddress generateMemberAddressToUpdate(UUID mid, Addressname addressname) {
    return new Maddress(
      mid,
      addressname,
      null,
      "attnU",
      "88 bwayU",
      "#3U",
      "cityU",
      "MI",
      "99877",
      "USA"
    );
  }

  static UUID insertTestMember() {
    MemberAndMauth maa = generateMemberToAdd();

    // add member record
    MemberRecord memberRecord = testDslMcweb()
      .insertInto(MEMBER,
        MEMBER.EMP_ID,
        MEMBER.LOCATION,
        MEMBER.NAME_FIRST,
        MEMBER.NAME_MIDDLE,
        MEMBER.NAME_LAST,
        MEMBER.DISPLAY_NAME,
        MEMBER.STATUS
      )
      .values(
        maa.dbMember.getEmpId(),
        maa.dbMember.getLocation(),
        maa.dbMember.getNameFirst(),
        maa.dbMember.getNameMiddle(),
        maa.dbMember.getNameLast(),
        maa.dbMember.getDisplayName(),
        maa.dbMember.getStatus()
      )
      .returning(MEMBER.MID)
      .fetchOne();
    
    assertNotNull(memberRecord);
    UUID mid = memberRecord.getMid();
    assertNotNull(mid);

    // pswd
    final PassHash ph = new PassHash();
    ph.setPswd(TEST_MAUTH_PSWD);
    ph.execute(testDslMcweb().configuration());
    final String phash = ph.getReturnValue();

    // add mauth record
    testDslMcweb()
      .insertInto(MAUTH, 
        MAUTH.MID,
        MAUTH.DOB,
        MAUTH.SSN,
        MAUTH.EMAIL_PERSONAL,
        MAUTH.EMAIL_WORK,
        MAUTH.MOBILE_PHONE,
        MAUTH.HOME_PHONE,
        MAUTH.WORK_PHONE,
        MAUTH.USERNAME,
        MAUTH.PSWD
      )
      .values(
        mid,
        asSqlDate(maa.dbMauth.getDob()),
        maa.dbMauth.getSsn(),
        maa.dbMauth.getEmailPersonal(),
        maa.dbMauth.getEmailWork(),
        maa.dbMauth.getMobilePhone(),
        maa.dbMauth.getHomePhone(),
        maa.dbMauth.getWorkPhone(),
        maa.dbMauth.getUsername(),
        phash
      )
      .execute();

    return mid;
  }

  static void deleteTestMember(UUID mid) {
    if(mid != null) testDslMcweb().delete(MEMBER).where(MEMBER.MID.eq(mid)).execute();
  }

  static void insertTestMemberAddress(UUID mid, Addressname addressname) {
    Maddress maddress = generateMemberAddressToAdd(mid, addressname);

    // add member address record
    testDslMcweb().insertInto(MADDRESS, 
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
      addressname,
      maddress.getAttn(),
      maddress.getStreet1(),
      maddress.getStreet2(),
      maddress.getCity(),
      maddress.getState(),
      maddress.getPostalCode(),
      maddress.getCountry()
    )
    .execute();
  }

  static void deleteTestMemberAddress(UUID mid, Addressname addressname) {
    if(mid != null && addressname != null) 
      testDslMcweb().delete(MADDRESS).where(MADDRESS.MID.eq(mid).and(MADDRESS.ADDRESS_NAME.eq(addressname))).execute();
  }

  @AfterClass
  public static void clearBackend() {
    try {
      // delete test member_audit records
      log.info("Num member_audit records deleted after test: {}.", 
        testDslMcwebTest().deleteFrom(MEMBER_AUDIT).where(MEMBER_AUDIT.REQUEST_ORIGIN.eq(testRequestOrigin)).execute());

      // delete test member records
      log.info("Num member records deleted after test: {}.", 
        testDslMcwebTest().deleteFrom(MEMBER).where(MEMBER.DISPLAY_NAME.eq("JAMFMan")).execute());
    }
    catch(Exception e) {
      log.error(e.getMessage());
    }
    finally {
      if(isTestDslMcwebTestLoaded()) testDslMcwebTest().close();
    }
  }
  
  static MCorpusRepo mcorpusRepo() { return new MCorpusRepo(ds_mcweb()); }

  @Test
  public void testMemberLogin() {
    MCorpusRepo repo = null;
    UUID mid = null;
    try {
      repo = mcorpusRepo();
      mid = insertTestMember();

      FetchResult<Mref> mrefFetch = repo.memberLogin(
        TEST_MAUTH_USERNAME, 
        TEST_MAUTH_PSWD,
        Instant.now(),
        testRequestOrigin
      );
      assertNotNull(mrefFetch);
      assertNotNull(mrefFetch.get());
      assertNull(mrefFetch.getErrorMsg());
      assertNotNull(mrefFetch.get());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(mid != null) deleteTestMember(mid);
        repo.close();
      }
    }
  }
  
  @Test
  public void testMemberLogout() {
    MCorpusRepo repo = null;
    UUID mid = null;
    try {
      repo = mcorpusRepo();
      mid = insertTestMember();
      
      FetchResult<UUID> memberLogoutResult = repo.memberLogout(
        mid,
        Instant.now(),
        testRequestOrigin
      );
      assertNotNull(memberLogoutResult);
      assertTrue(memberLogoutResult.isSuccess());
      assertFalse(memberLogoutResult.hasErrorMsg());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        deleteTestMember(mid);
        repo.close();
      }
    }
  }

  @Test
  public void testFetchMRefByMid() {
    MCorpusRepo repo = null;
    UUID mid = null;
    try {
      repo = mcorpusRepo();
      mid = insertTestMember();

      FetchResult<Mref> mrefFetch = repo.fetchMRefByMid(mid);
      assertNotNull(mrefFetch);
      assertTrue(mrefFetch.isSuccess());
      assertNotNull(mrefFetch.get());
      assertNull(mrefFetch.getErrorMsg());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        deleteTestMember(mid);
        repo.close();
      }
    }
  }

  @Test
  public void testFetchMember() {
    MCorpusRepo repo = null;
    UUID mid = null;
    try {
      repo = mcorpusRepo();
      mid = insertTestMember();

      FetchResult<MemberAndMauth> memberFetch = repo.fetchMember(mid);
      assertNotNull(memberFetch);
      assertTrue(memberFetch.isSuccess());
      assertNotNull(memberFetch.get());
      assertNull(memberFetch.getErrorMsg());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        deleteTestMember(mid);
        repo.close();
      }
    }
  }

  @Test
  public void testFetchMemberAddresses() {
    MCorpusRepo repo = null;
    UUID mid = null;
    Addressname addressname = Addressname.other;
    try {
      repo = mcorpusRepo();
      
      mid = insertTestMember();
      insertTestMemberAddress(mid, addressname);
      
      FetchResult<List<Maddress>> fetchResult = repo.fetchMemberAddresses(mid);
      assertNotNull(fetchResult);
      assertNotNull(fetchResult.get());
      assertNull(fetchResult.getErrorMsg());

      List<Maddress> malist = fetchResult.get();
      assertTrue(malist.size() == 1);
      assertTrue(malist.get(0).getAddressName() == Addressname.other);
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        deleteTestMember(mid); // NOTE: maddress are cascade deleted
        repo.close();
      }
    }
  }

  @Test
  public void testAddMember() {
    MCorpusRepo repo = null;
    UUID mid = null;
    try {
      repo = mcorpusRepo();

      MemberAndMauth memberToAdd = generateMemberToAdd();

      FetchResult<MemberAndMauth> fetchResult = repo.addMember(memberToAdd);

      assertNotNull(fetchResult);
      assertNull(fetchResult.getErrorMsg());
      assertNotNull(fetchResult.get());

      mid = fetchResult.get().dbMember.getMid();
      assertNotNull(mid);

      assertNotNull(fetchResult.get().dbMember.getCreated());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(mid != null) {
          deleteTestMember(mid);
        }
        repo.close();
      }
    }
  }

  @Test
  public void testUpdateMember() {
    MCorpusRepo repo = null;
    UUID mid = null;
    try {
      repo = mcorpusRepo();

      mid = insertTestMember();

      MemberAndMauth memberToUpdate = generateMemberToUpdate(mid);

      FetchResult<MemberAndMauth> fetchResult = repo.updateMember(memberToUpdate);

      assertNotNull(fetchResult);
      assertNotNull(fetchResult.get());
      assertNull(fetchResult.getErrorMsg());
      
      // verify we have an mid and modified timestamp present
      assertNotNull(fetchResult.get().dbMember.getMid());
      assertNotNull(fetchResult.get().dbMember.getCreated());
      assertNotNull(fetchResult.get().dbMember.getModified());
      
      assertNotNull(fetchResult.get().dbMauth);
    }
    catch(Exception e) {
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        deleteTestMember(mid);
        repo.close();
      }
    }
  }

  @Test
  public void testDeleteMember() {
    MCorpusRepo repo = null;
    UUID mid = insertTestMember();
    try {
      repo = mcorpusRepo();

      FetchResult<Boolean> fetchResult = repo.deleteMember(mid);

      assertNotNull(fetchResult);
      assertEquals(Boolean.TRUE, fetchResult.get());
      assertNull(fetchResult.getErrorMsg());
    }
    catch(Exception e) {
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        deleteTestMember(mid);
        repo.close();
      }
    }
  }

  @Test
  public void testMemberSetPswd() throws Exception {
    MCorpusRepo repo = null;
    UUID mid = null;
    try {
      repo = mcorpusRepo();
      mid = insertTestMember();

      FetchResult<Boolean> fetchResult = repo.setMemberPswd(mid, "test123");
      assertNotNull(fetchResult);
      assertNull(fetchResult.getErrorMsg());
      assertTrue(fetchResult.get());
    }
    catch(Exception e) {
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(mid != null) deleteTestMember(mid);
        repo.close();
      }
    }
  }

  @Test
  public void testAddMemberAddress() {
    MCorpusRepo repo = null;
    UUID mid = insertTestMember();
    try {
      repo = mcorpusRepo();

      Maddress maddress = generateMemberAddressToAdd(mid, Addressname.other);

      FetchResult<Maddress> fetchResult = repo.addMemberAddress(maddress);

      assertNotNull(fetchResult);
      assertNotNull(fetchResult.get());
      assertNull(fetchResult.getErrorMsg());
      assertEquals(mid, fetchResult.get().getMid());
      assertNotNull(fetchResult.get().getModified());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        deleteTestMember(mid); // NOTE: maddress records are cascade deleted
        repo.close();
      }
    }
  }

  @Test
  public void testUpdateMemberAddress() {
    MCorpusRepo repo = null;
    UUID mid = insertTestMember();
    insertTestMemberAddress(mid, Addressname.other);
    try {
      repo = mcorpusRepo();

      Maddress ma = generateMemberAddressToUpdate(mid, Addressname.other);
      
      FetchResult<Maddress> fetchResult = repo.updateMemberAddress(ma);

      assertNotNull(fetchResult);
      assertNotNull(fetchResult.get());
      assertNull(fetchResult.getErrorMsg());
      assertNotNull(fetchResult.get().getModified());
    }
    catch(Exception e) {
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        deleteTestMember(mid); // NOTE: maddress records are cascade deleted
        repo.close();
      }
    }
  }

  @Test
  public void testDeleteMemberAddress() {
    MCorpusRepo repo = null;
    UUID mid = null;
    try {
      repo = mcorpusRepo();

      mid = insertTestMember();
      insertTestMemberAddress(mid, Addressname.other);
      
      FetchResult<Boolean> fetchResult = repo.deleteMemberAddress(mid, Addressname.other);

      assertNotNull(fetchResult);
      assertEquals(Boolean.TRUE, fetchResult.get());
      assertNull(fetchResult.getErrorMsg());

    } catch (Exception e) {
      fail(e.getMessage());
    } finally {
      if (repo != null) {
        deleteTestMember(mid); // NOTE: maddress records are cascade deleted
        repo.close();
      }
    }
  }
}
