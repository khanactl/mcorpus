package com.tll.mcorpus.validateapi;

import static org.junit.Assert.*;

public class VldtnTestHelper {

  public static void verify(VldtnResult vresult, boolean expectValid, int expectSize) {
    assertEquals(expectValid, vresult.isValid());
    assertEquals(expectSize, vresult.getErrors().size());
  }
}