package com.tll.mcorpus.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import com.tll.UnitTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class BaseMcorpusTransformerTest {

  /**
   * Verifies both {@link BaseMcorpusTransformer#uuidToToken(UUID)} and 
   * {@link BaseMcorpusTransformer#uuidFromToken(String)} methods.
   */
  @Test
  public void testUuidTokenTranslation() {
    final UUID testUuid = UUID.randomUUID();
    final String token = BaseMcorpusTransformer.uuidToToken(testUuid);
    assertNotNull(token);
    assertEquals(24, token.length());
    final UUID reTestUuid = BaseMcorpusTransformer.uuidFromToken(token);
    assertNotNull(reTestUuid);
    assertEquals(testUuid, reTestUuid);
  }

}