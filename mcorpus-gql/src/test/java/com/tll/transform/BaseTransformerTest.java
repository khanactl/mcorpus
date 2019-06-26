package com.tll.transform;

import static org.junit.Assert.*;
import static com.tll.transform.BaseTransformer.*;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

import org.junit.Test;

public class BaseTransformerTest {

  @Test
  public void testOdtTranslation() {
    Date d = new Date();
    OffsetDateTime odt = odtFromDate(d);
    Date d2 = odtToDate(odt);
    assertEquals("Date != OffsetDateTime", d, d2);

    odt = OffsetDateTime.now();
    d = odtToDate(odt);
    OffsetDateTime odt2 = odtFromDate(d);
    assertEquals("OffsetDateTime != Date", odt, odt2);
  }

  /**
   * Verifies both {@link BaseTransformer#uuidToToken(UUID)} and 
   * {@link BaseTransformer#uuidFromToken(String)} methods.
   */
  @Test
  public void testUuidTokenTranslation() {
    final UUID testUuid = UUID.randomUUID();
    final String token = BaseTransformer.uuidToToken(testUuid);
    assertNotNull(token);
    assertEquals(24, token.length());
    final UUID reTestUuid = BaseTransformer.uuidFromToken(token);
    assertNotNull(reTestUuid);
    assertEquals(testUuid, reTestUuid);
  }

}