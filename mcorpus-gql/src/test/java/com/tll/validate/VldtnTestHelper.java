package com.tll.validate;

import static org.junit.Assert.assertEquals;

public class VldtnTestHelper {

  public static void verify(VldtnResult vresult, boolean expectValid, int expectSize) {
    assertEquals(expectValid, vresult.isValid());
    assertEquals(expectSize, vresult.getErrors().size());
  }
}