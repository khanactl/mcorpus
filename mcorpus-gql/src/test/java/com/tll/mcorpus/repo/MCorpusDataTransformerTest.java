package com.tll.mcorpus.repo;

import com.tll.mcorpus.UnitTest;
import com.tll.mcorpus.db.enums.Addressname;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tll.mcorpus.TestUtil.generateMaddressToAddPropertyMap;
import static com.tll.mcorpus.TestUtil.generateMaddressToUpdatePropertyMap;
import static com.tll.mcorpus.TestUtil.generateMemberToAddPropertyMap;
import static com.tll.mcorpus.db.Tables.MAUTH;
import static com.tll.mcorpus.db.Tables.MEMBER;
import static com.tll.mcorpus.repo.MCorpusDataTransformer.*;
import static org.junit.Assert.*;

@Category(UnitTest.class)
public class MCorpusDataTransformerTest {

  @Test
  public void testMemberTransform() {
    final Map<String, Object> memberMap = generateMemberToAddPropertyMap();

    final List<Map<String, Object>> mapList = transformMember(memberMap);
    final Map<String, Object> cmapMember = mapList.get(0);
    final Map<String, Object> cmapMauth = mapList.get(1);

    assertEquals("JAKE", cmapMember.get(MEMBER.NAME_FIRST.getName()));
    assertEquals("ARTHUR", cmapMember.get(MEMBER.NAME_MIDDLE.getName()));
    assertEquals("MCGIDRICH", cmapMember.get(MEMBER.NAME_LAST.getName()));

    assertEquals("4156747832", cmapMauth.get(MAUTH.MOBILE_PHONE.getName()));
    assertEquals("4154741080", cmapMauth.get(MAUTH.HOME_PHONE.getName()));
    assertEquals("4153742231", cmapMauth.get(MAUTH.WORK_PHONE.getName()));

    assertEquals("101010101", cmapMauth.get(MAUTH.SSN.getName()));
  }

  @Test
  public void testMemberAddreessTransformForAdd() {
    UUID mid = UUID.randomUUID();
    Addressname addressname = Addressname.home;

    Map<String, Object> maddressMap = generateMaddressToAddPropertyMap(mid, addressname);

    Map<String, Object> tmap = transformMemberAddressForAdd(maddressMap);

    assertNotNull(maddressMap);
    assertNotNull(tmap);
    assertEquals(maddressMap.size(), tmap.size());
  }

  @Test
  public void testMemberAddreessTransformForUpdate() {
    UUID mid = UUID.randomUUID();
    Addressname addressname = Addressname.home;

    Map<String, Object> maddressMap = generateMaddressToUpdatePropertyMap(mid, addressname);

    List<Object> tmap = transformMemberAddressForUpdate(maddressMap);
    assertNotNull(tmap);
    assertEquals(3, tmap.size());
    assertTrue(tmap.get(0) instanceof UUID);
    assertTrue(tmap.get(1) instanceof Addressname);
    assertTrue(tmap.get(2) instanceof Map);
    assertTrue(((Map<?, ?>)tmap.get(2)).size() > 0);
  }
}
