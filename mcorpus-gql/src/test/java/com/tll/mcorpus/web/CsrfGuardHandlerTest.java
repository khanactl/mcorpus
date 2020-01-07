package com.tll.mcorpus.web;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import com.tll.UnitTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

@Category(UnitTest.class)
public class CsrfGuardHandlerTest {
  // private static final Logger log = LoggerFactory.getLogger(CsrfGuardHandlerTest.class);

  @Test
  public void testNoRstPattern() {
    assertTrue(CsrfGuardHandler.PTRN_GEN_RST.matcher("graphql").matches());
    assertTrue(CsrfGuardHandler.PTRN_GEN_RST.matcher("graphql/").matches());
    assertTrue(CsrfGuardHandler.PTRN_GEN_RST.matcher("graphql/index").matches());
    assertTrue(CsrfGuardHandler.PTRN_GEN_RST.matcher("graphql/index/").matches());
    assertTrue(CsrfGuardHandler.PTRN_GEN_RST.matcher("sub/graphql/index").matches());
    assertTrue(CsrfGuardHandler.PTRN_GEN_RST.matcher("sub/graphql/index/").matches());

    assertFalse(CsrfGuardHandler.PTRN_GEN_RST.matcher("index.html").matches());
    assertFalse(CsrfGuardHandler.PTRN_GEN_RST.matcher("index.js").matches());
    assertFalse(CsrfGuardHandler.PTRN_GEN_RST.matcher("index.css").matches());
    assertFalse(CsrfGuardHandler.PTRN_GEN_RST.matcher("favicon.ico").matches());
  }
}