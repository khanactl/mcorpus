package com.tll.mcorpus.jwtgen;

import static org.junit.Assert.*;

import com.tll.mcorpus.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(UnitTest.class)
public class JwtGenTest {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(JwtGenTest.class);

  @Test
  public void tesNullArgs() throws Exception {
    assertEquals(
        "No input arguments provided.",
        JwtGen.processInput((String[]) null)
    );
  }

  @Test
  public void tesAllArgsNull() throws Exception {
    assertEquals(
        "Invalid input.",
        JwtGen.processInput(null, null, null)
    );
  }

  @Test
  public void tesHelpTextOnInvalidNumArgs() throws Exception {
    assertEquals(
        JwtGen.Input.helpText(),
        JwtGen.processInput("a", "b")
    );
  }

  @Test
  public void testNullDuration() throws Exception {
    assertFalse(JwtGen.processInput("test", "test", null).contains("JWT"));
  }

  @Test
  public void testEmptyDuration() throws Exception {
    assertFalse(JwtGen.processInput("test", "test", "").contains("JWT"));
  }

  @Test
  public void testZeroDuration() throws Exception {
    assertEquals(
        "Invalid duration amount.",
        JwtGen.processInput("test", "test", "0d")
    );
  }

  @Test
  public void testTooBigDuration() throws Exception {
    assertEquals(
        "Invalid duration amount.",
        JwtGen.processInput("test", "test", "100d")
    );
  }

  @Test
  public void testOneHourDuration() throws Exception {
    assertTrue(JwtGen.processInput("test", "jackson", "1h").contains("JWT"));
  }

  @Test
  public void testOneDayDuration() throws Exception {
    assertTrue(JwtGen.processInput("test", "jackson", "1d").contains("JWT"));
  }

  @Test
  public void test20DaysDuration() throws Exception {
    assertTrue(JwtGen.processInput("test", "jackson", "20d").contains("JWT"));
  }

  @Test
  public void test20HoursDuration() throws Exception {
    assertTrue(JwtGen.processInput("test", "jackson", "20h").contains("JWT"));
  }
}
