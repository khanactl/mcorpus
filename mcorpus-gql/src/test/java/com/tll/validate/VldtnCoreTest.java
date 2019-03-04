package com.tll.validate;

import static com.tll.TestUtil.strN;
import static com.tll.TestUtil.toDate;
import static com.tll.validate.VldtnCore.dobValid;
import static com.tll.validate.VldtnCore.emailValid;
import static com.tll.validate.VldtnCore.phoneValid;
import static com.tll.validate.VldtnCore.pswdValid;
import static com.tll.validate.VldtnCore.ssnValid;
import static com.tll.validate.VldtnCore.usernameValid;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tll.UnitTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class VldtnCoreTest {

  @SuppressWarnings("unused")
  private static final Logger log = LogManager.getLogger();

  @Test
  public void testDobValid() {
    assertFalse(dobValid(null));
    assertFalse(dobValid(toDate("2080-01-01"))); // future
    assertTrue(dobValid(toDate("1965-01-01")));  // past
  }

  @Test
  public void testSsnValid() {
    // legit ssns
    assertTrue(ssnValid("123-45-6789"));
    assertTrue(ssnValid("123456789"));
    // ill ssns
    assertFalse(ssnValid("1234567890"));
    assertFalse(ssnValid(null));
    assertFalse(ssnValid(""));
    assertFalse(ssnValid(" "));
    assertFalse(ssnValid("we"));
  }

  @Test
  public void testEmailValid() {
    assertTrue(emailValid("smog@bokely.com"));
    assertFalse(emailValid(null));
    assertFalse(emailValid("we"));
  }

  @Test
  public void testPhoneValid() {
    // required
    assertTrue(phoneValid("323-3323"));
    assertTrue(phoneValid("415-323-3323"));
    assertFalse(phoneValid(null));
    assertFalse(phoneValid(""));
    assertFalse(phoneValid("aaaaaaa"));
  }

  @Test
  public void testUsernameValid() {
    assertTrue(usernameValid("uname"));
    assertFalse(usernameValid("a."));
    assertFalse(usernameValid(null));
    assertFalse(usernameValid(""));
    assertFalse(usernameValid(" "));
    assertFalse(usernameValid(strN(3)));
    assertTrue(usernameValid(strN(4)));
    assertTrue(usernameValid(strN(5)));
    assertTrue(usernameValid(strN(26)));
    assertFalse(usernameValid(strN(27)));
  }

  @Test
  public void testPasswordValid() {
    assertTrue(pswdValid("pswdpswd"));
    assertFalse(pswdValid(strN(7)));
    assertTrue(pswdValid(strN(8)));
    assertTrue(pswdValid(strN(50)));
    assertFalse(pswdValid(strN(51)));
    assertFalse(pswdValid("a."));
    assertFalse(pswdValid(null));
    assertFalse(pswdValid(""));
    assertFalse(pswdValid(" "));
    assertFalse(pswdValid("\r\n\t"));
  }

}
