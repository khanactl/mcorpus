package com.tll.web;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RequestSnapshotTest {

  @Test
  public void stringQSTest() {
    assertEquals(null, RequestSnapshot.stripQS(null));
    assertEquals("", RequestSnapshot.stripQS(""));
    assertEquals(" ", RequestSnapshot.stripQS(" "));
    assertEquals("domain.com", RequestSnapshot.stripQS("domain.com"));
    assertEquals("domain.com", RequestSnapshot.stripQS("domain.com?"));
    assertEquals("domain.com", RequestSnapshot.stripQS("domain.com?a=b&c=d"));
  }
}