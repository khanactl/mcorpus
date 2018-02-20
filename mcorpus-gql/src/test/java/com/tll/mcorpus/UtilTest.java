package com.tll.mcorpus;

import static com.tll.mcorpus.TestUtil.cpr;
import static com.tll.mcorpus.TestUtil.jsonStringToMap;
import static com.tll.mcorpus.Util.strlen;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Simple unit tests for {@link Util} methods.
 */
@Category(UnitTest.class)
public class UtilTest {

  private static final String base64EncodedUuid = "mnU9EDlhSzaid3_hApEoNA==";

  private static final UUID testUuid = UUID.fromString("9a753d10-3961-4b36-a277-7fe102912834");

  @Test
  public void testUuidToToken() {
    final String token = Util.uuidToToken(testUuid);
    assertEquals(base64EncodedUuid, token);
    assertEquals(24, strlen(token));
  }

  @Test
  public void testUuidFromToken() {
    final UUID uuid = Util.uuidFromToken(base64EncodedUuid);
    assertEquals(testUuid, uuid);
  }

  @Test
  public void testJsonStringToMap() throws Exception {
    final String json = cpr("introspect.gql");
    Map<String, Object> rmap = jsonStringToMap(json);
    assertNotNull(rmap);
    assertTrue(rmap.size() == 1);
    assertTrue(rmap.containsKey("query"));
  }
}
