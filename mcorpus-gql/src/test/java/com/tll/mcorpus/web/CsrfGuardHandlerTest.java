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
    assertFalse(CsrfGuardHandler.ptrnNoRst.matcher("index.html").matches());
    assertFalse(CsrfGuardHandler.ptrnNoRst.matcher("index").matches());
    assertFalse(CsrfGuardHandler.ptrnNoRst.matcher("index.").matches());
    assertFalse(CsrfGuardHandler.ptrnNoRst.matcher("index.j").matches());

    assertTrue(CsrfGuardHandler.ptrnNoRst.matcher("index.js").matches());
    assertTrue(CsrfGuardHandler.ptrnNoRst.matcher("index.css").matches());
    assertTrue(CsrfGuardHandler.ptrnNoRst.matcher("index.ico").matches());
    assertTrue(CsrfGuardHandler.ptrnNoRst.matcher("index.png").matches());
    assertTrue(CsrfGuardHandler.ptrnNoRst.matcher("index.jpg").matches());
    assertTrue(CsrfGuardHandler.ptrnNoRst.matcher("index.jpeg").matches());
    assertTrue(CsrfGuardHandler.ptrnNoRst.matcher("index.mpeg").matches());

    assertTrue(CsrfGuardHandler.ptrnNoRst.matcher("index.JS").matches());
    assertTrue(CsrfGuardHandler.ptrnNoRst.matcher("index.CSS").matches());
    assertTrue(CsrfGuardHandler.ptrnNoRst.matcher("index.ICO").matches());
  }
}