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
    assertFalse(CsrfGuardHandler.PTRN_NO_RST.matcher("index.html").matches());
    assertFalse(CsrfGuardHandler.PTRN_NO_RST.matcher("index").matches());
    assertFalse(CsrfGuardHandler.PTRN_NO_RST.matcher("index.").matches());
    assertFalse(CsrfGuardHandler.PTRN_NO_RST.matcher("index.j").matches());

    assertTrue(CsrfGuardHandler.PTRN_NO_RST.matcher("index.js").matches());
    assertTrue(CsrfGuardHandler.PTRN_NO_RST.matcher("index.css").matches());
    assertTrue(CsrfGuardHandler.PTRN_NO_RST.matcher("index.ico").matches());
    assertTrue(CsrfGuardHandler.PTRN_NO_RST.matcher("index.png").matches());
    assertTrue(CsrfGuardHandler.PTRN_NO_RST.matcher("index.jpg").matches());
    assertTrue(CsrfGuardHandler.PTRN_NO_RST.matcher("index.jpeg").matches());
    assertTrue(CsrfGuardHandler.PTRN_NO_RST.matcher("index.mpeg").matches());

    assertTrue(CsrfGuardHandler.PTRN_NO_RST.matcher("index.JS").matches());
    assertTrue(CsrfGuardHandler.PTRN_NO_RST.matcher("index.CSS").matches());
    assertTrue(CsrfGuardHandler.PTRN_NO_RST.matcher("index.ICO").matches());
  }
}