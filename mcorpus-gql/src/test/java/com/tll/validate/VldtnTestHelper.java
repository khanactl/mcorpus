package com.tll.validate;

import static org.junit.Assert.assertEquals;

public class VldtnTestHelper {

  public static void verify(VldtnResult vresult, boolean expectValid, int expectErrSize) {
    assertEquals(expectValid, vresult.isValid());
    assertEquals(expectErrSize, vresult.getErrors().size());
  }
}