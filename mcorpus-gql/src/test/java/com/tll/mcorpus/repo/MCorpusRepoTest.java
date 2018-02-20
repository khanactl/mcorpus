package com.tll.mcorpus.repo;

import com.tll.mcorpus.UnitTest;
import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.tables.records.MemberRecord;
import com.tll.mcorpus.repo.model.FetchResult;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.*;

import static com.tll.mcorpus.TestUtil.*;
import static com.tll.mcorpus.db.Tables.MADDRESS;
import static com.tll.mcorpus.db.Tables.MAUTH;
import static com.tll.mcorpus.db.Tables.MEMBER;
import static com.tll.mcorpus.repo.MCorpusDataTransformer.transformMember;
import static com.tll.mcorpus.repo.MCorpusDataTransformer.transformMemberAddressForAdd;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.*;

/**
 * Unit test for {@link MCorpusRepo}.
 */
@Category(UnitTest.class)
public class MCorpusRepoTest {

  private static final Logger log = LoggerFactory.getLogger(MCorpusRepoTest.class);

  private static DSLContext dsl = null;

  private static DataSource ds() {
    PGSimpleDataSource ds = new PGSimpleDataSource();
    ds.setUrl("jdbc:postgresql://localhost:5432/mcorpus");
    ds.setUser("mcweb");
    ds.setPassword("YAcsR6*-L;djIaX1~%zBa");
    ds.setCurrentSchema("public");
    return ds;
  }

  private static DSLContext dsl() {
    if(dsl == null) {
      Settings s = new Settings();
      s.setRenderSchema(false);
      s.setRenderNameStyle(RenderNameStyle.LOWER);
      s.setRenderKeywordStyle(RenderKeywordStyle.UPPER);
      dsl = DSL.using(ds(), SQLDialect.POSTGRES, s);
    }
    return dsl;
  }

  private static UUID insertTestMember() {
    Map<String, Object> memberMap = new HashMap<>();

    addMemberTableProperties(memberMap);
    addMauthTableProperties(memberMap);

    List<Map<String, Object>> cmaps = transformMember(memberMap);
    Map<String, Object> cmapMember = cmaps.get(0);
    Map<String, Object> cmapMauth = cmaps.get(1);

    // add member record
    MemberRecord memberRecord = dsl().insertInto(MEMBER).set(cmapMember).returning(MEMBER.MID).fetchOne();
    UUID mid = memberRecord.getMid();

    // add mauth record
    cmapMauth.put(MAUTH.MID.getName(), mid);
    dsl().insertInto(MAUTH).set(cmapMauth).execute();

    return mid;
  }

  private static void deleteTestMember(UUID mid) {
    if(mid != null) dsl().delete(MEMBER).where(MEMBER.MID.eq(mid)).execute();
  }

  private static void insertTestMemberAddress(UUID mid) {
    Map<String, Object> maddressMap = new HashMap<>();

    addMaddressTableProperties(maddressMap, mid, Addressname.other);

    Map<String, Object> cmap = transformMemberAddressForAdd(maddressMap);
    cmap.put(MADDRESS.MID.getName(), mid);

    // add member address record
    dsl().insertInto(MADDRESS).set(cmap).returning(MADDRESS.MID).execute();
  }

  private static void deleteTestMemberAddress(UUID mid) {
    if(mid != null) dsl().delete(MADDRESS).where(MADDRESS.MID.eq(mid).and(MADDRESS.ADDRESS_NAME.eq(Addressname.other))).execute();
  }

  @Test
  public void testFetchMRefByMid() {
    MCorpusRepo repo = null;
    try {
      repo = new MCorpusRepo(ds());
      FetchResult<Map<String, Object>> mrefFetch = repo.fetchMRefByMid(UUID.fromString("3e983661-62b6-440c-96e9-f2637fa8b4e8"));
      assertNotNull(mrefFetch);
      assertNotNull(mrefFetch.get());
      assertNull(mrefFetch.getErrorMsg());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) repo.close();
    }
  }

  @Test
  public void testFetchMember() {
    MCorpusRepo repo = null;
    try {
      repo = new MCorpusRepo(ds());
      FetchResult<Map<String, Object>> memberFetch = repo.fetchMember(UUID.fromString("3e983661-62b6-440c-96e9-f2637fa8b4e8"));
      assertNotNull(memberFetch);
      assertNotNull(memberFetch.get());
      assertNull(memberFetch.getErrorMsg());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) repo.close();
    }
  }

  @Test
  public void testFetchMemberAddresses() {
    MCorpusRepo repo = null;
    try {
      repo = new MCorpusRepo(ds());
      FetchResult<List<Map<String, Object>>> fetchResult = repo.fetchMemberAddresses(UUID.fromString("3e983661-62b6-440c-96e9-f2637fa8b4e8"));
      assertNotNull(fetchResult);
      assertNotNull(fetchResult.get());
      assertNull(fetchResult.getErrorMsg());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) repo.close();
    }
  }

  @Test
  public void testAddMember() {
    MCorpusRepo repo = null;
    UUID mid = null;
    try {
      repo = new MCorpusRepo(ds());

      Map<String, Object> memberMap = generateMemberToAddPropertyMap();

      FetchResult<Map<String, Object>> fetchResult = repo.addMember(memberMap);

      if(fetchResult.isSuccess()) mid = (UUID) fetchResult.get().get(MEMBER.MID.getName());

      assertNotNull(fetchResult);
      assertNotNull(fetchResult.get());
      assertNull(fetchResult.getErrorMsg());
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
      repo = new MCorpusRepo(ds());

      mid = insertTestMember();

      Map<String, Object> memberMap = generateMemberToUpdatePropertyMap();
      memberMap.put(MEMBER.MID.getName(), mid);

      FetchResult<Map<String, Object>> fetchResult = repo.updateMember(memberMap);

      assertNotNull(fetchResult);
      assertNotNull(fetchResult.get());
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
  public void testDeleteMember() {
    MCorpusRepo repo = null;
    UUID mid = insertTestMember();
    try {
      repo = new MCorpusRepo(ds());

      FetchResult<UUID> fetchResult = repo.deleteMember(mid);

      assertNotNull(fetchResult);
      assertNotNull(fetchResult.get());
      assertNull(fetchResult.getErrorMsg());

      UUID midDeleted;
      if(fetchResult.isSuccess()) {
        midDeleted = fetchResult.get();
        assertEquals(mid, midDeleted);
      }
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
  public void testAddMemberAddress() {
    MCorpusRepo repo = null;
    UUID mid = insertTestMember();
    try {
      repo = new MCorpusRepo(ds());

      Map<String, Object> maddressMap = generateMaddressToAddPropertyMap(mid, Addressname.other);

      FetchResult<Map<String, Object>> fetchResult = repo.addMemberAddress(maddressMap);

      if(fetchResult.isSuccess()) mid = (UUID) fetchResult.get().get(MADDRESS.MID.getName());

      assertNotNull(fetchResult);
      assertNotNull(fetchResult.get());
      assertNull(fetchResult.getErrorMsg());
    }
    catch(Exception e) {
      log.error(e.getMessage());
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        if(mid != null) {
          deleteTestMemberAddress(mid);
          deleteTestMember(mid);
        }
        repo.close();
      }
    }
  }

  @Test
  public void testUpdateMemberAddress() {
    MCorpusRepo repo = null;
    UUID mid = insertTestMember();
    insertTestMemberAddress(mid);
    try {
      repo = new MCorpusRepo(ds());

      Map<String, Object> maddressMap = generateMaddressToUpdatePropertyMap(mid, Addressname.other);
      maddressMap.put(MEMBER.MID.getName(), mid);

      FetchResult<Map<String, Object>> fetchResult = repo.updateMemberAddress(maddressMap);

      assertNotNull(fetchResult);
      assertNotNull(fetchResult.get());
      assertNull(fetchResult.getErrorMsg());
    }
    catch(Exception e) {
      fail(e.getMessage());
    }
    finally {
      if(repo != null) {
        deleteTestMemberAddress(mid);
        deleteTestMember(mid);
        repo.close();
      }
    }
  }

  @Test
  public void testDeleteMemberAddress() {
    MCorpusRepo repo = null;
    UUID mid = insertTestMember();
    insertTestMemberAddress(mid);
    try {
      repo = new MCorpusRepo(ds());

      FetchResult<UUID> fetchResult = repo.deleteMemberAddress(mid, Addressname.other);

      assertNotNull(fetchResult);
      assertNotNull(fetchResult.get());
      assertNull(fetchResult.getErrorMsg());

      UUID midDeleted;
      if (fetchResult.isSuccess()) {
        midDeleted = fetchResult.get();
        assertEquals(mid, midDeleted);
      }
    } catch (Exception e) {
      fail(e.getMessage());
    } finally {
      if (repo != null) {
        deleteTestMemberAddress(mid);
        deleteTestMember(mid);
        repo.close();
      }
    }
  }
}
