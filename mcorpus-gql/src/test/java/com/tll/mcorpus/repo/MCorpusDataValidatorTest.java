package com.tll.mcorpus.repo;

import com.tll.mcorpus.UnitTest;
import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.enums.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Date;

import static com.tll.mcorpus.TestUtil.strN;
import static com.tll.mcorpus.TestUtil.toDate;
import static com.tll.mcorpus.repo.MCorpusDataValidator.*;
import static org.junit.Assert.*;

@Category(UnitTest.class)
public class MCorpusDataValidatorTest {

  @SuppressWarnings("unused")
  private static final Logger log = LogManager.getLogger();

  @Test // emp id REQUIRED
  public void testEmpIdValid() {
    assertTrue(empIdValid("12-3456789"));
    assertFalse(empIdValid("12-34567890"));
    assertFalse(empIdValid(null));
    assertFalse(empIdValid(""));
    assertFalse(empIdValid(" "));
    assertFalse(empIdValid("- "));
    assertFalse(empIdValid("12-a"));
    assertFalse(empIdValid("12-123456b"));
    assertFalse(empIdValid("b2-1234567"));
    assertFalse(empIdValid("221234567"));
  }

  @Test // location REQUIRED
  public void testLocationValid() {
    assertTrue(locationValid(Location._01));
    assertFalse(locationValid(null));
  }

  @Test // first name REQUIRED
  public void testNameFirstValid() {
    assertTrue(nameFirstValid("Jake"));
    assertFalse(nameFirstValid(null));
    assertFalse(nameFirstValid(""));
    assertFalse(nameFirstValid(" "));
    assertFalse(nameFirstValid(strN(65)));
  }

  @Test // middle name OPTIONAL
  public void testNameMiddleValid() {
    assertTrue(nameMiddleValid("Diddle"));
    assertTrue(nameMiddleValid(null));
    assertTrue(nameMiddleValid(""));
    assertFalse(nameMiddleValid(" "));
    assertFalse(nameMiddleValid(strN(65)));
  }

  @Test // last name REQUIRED
  public void testNameLastValid() {
    assertTrue(nameLastValid("Last"));
    assertFalse(nameLastValid(null));
    assertFalse(nameLastValid(""));
    assertFalse(nameLastValid(" "));
    assertFalse(nameLastValid(strN(65)));
  }

  @Test // display name OPTIONAL
  public void testDisplayNameValid() {
    assertTrue(displayNameValid("display"));
    assertTrue(displayNameValid(null));
    assertTrue(displayNameValid(""));
    assertFalse(displayNameValid(" "));
    assertFalse(displayNameValid(strN(65)));
  }

  @Test // dob REQUIRED
  public void testDobValid() {
    assertTrue(dobValid(new Date()));
    assertFalse(dobValid(null));
    assertFalse(dobValid(toDate("2020-01-01")));
    assertTrue(dobValid(toDate("1965-01-01")));
  }

  @Test // ssn REQUIRED
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

  @Test // email OPTIONAL
  public void testEmailValid() {
    // required
    assertTrue(emailValid("smog@bokely.com", true));
    assertFalse(emailValid(null, true));
    assertFalse(emailValid("", true));
    assertFalse(emailValid("we", true));
    // not required
    assertTrue(emailValid("smog@bokely.com", false));
    assertTrue(emailValid(null, false));
    assertTrue(emailValid("", false));
    assertFalse(emailValid("a.", false));
    assertFalse(emailValid("we", false));
  }

  @Test // phone OPTIONAL
  public void testPhoneValid() {
    // required
    assertTrue(phoneValid("323-3323", true));
    assertTrue(phoneValid("415-323-3323", true));
    assertFalse(phoneValid(null, true));
    assertFalse(phoneValid("", true));
    assertFalse(phoneValid("aaaaaaa", true));
    // not required
    assertTrue(phoneValid("323-3323", false));
    assertTrue(phoneValid("415-323-3323", false));
    assertTrue(phoneValid(null, false));
    assertTrue(phoneValid("", false));
    assertFalse(phoneValid("aaaaaaa", false));
  }

  @Test // username REQUIRED
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

  @Test // pswd REQUIRED
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

  @Test
  public void testAddressNameValid() {
    assertFalse(addressNameValid(null));
    assertTrue(addressNameValid(Addressname.home));
  }

  @Test
  public void testAddressAttnValid() {
    assertTrue(addressAttnValid(null));
    assertTrue(addressAttnValid(""));
    assertTrue(addressAttnValid("attn"));
  }

  @Test
  public void testAddressStreet1Valid() {
    assertFalse(addressStreet1Valid(null));
    assertFalse(addressStreet1Valid(""));
    assertTrue(addressStreet1Valid("street 22 court. #1"));
    assertTrue(addressStreet1Valid(strN(120)));
    assertFalse(addressStreet1Valid(strN(121)));
  }

  @Test
  public void testAddressStreet2Valid() {
    assertTrue(addressStreet2Valid(null));
    assertTrue(addressStreet2Valid(""));
    assertTrue(addressStreet1Valid("street 22 court. #1"));
    assertTrue(addressStreet1Valid(strN(120)));
    assertFalse(addressStreet1Valid(strN(121)));
  }

  @Test
  public void testAddressCityValid() {
    assertFalse(addressCityValid(null));
    assertFalse(addressCityValid(""));
    assertTrue(addressCityValid("st. petersberg"));
    assertTrue(addressCityValid(strN(120)));
    assertFalse(addressCityValid(strN(121)));
  }

  @Test
  public void testAddressStateValid() {
    assertFalse(addressStateValid(null));
    assertFalse(addressStateValid(""));
    assertTrue(addressStateValid("MI"));
    assertTrue(addressStateValid(strN(2)));
    assertFalse(addressStateValid(strN(3)));
  }

  @Test
  public void testAddressPostalCodeValid() {
    assertFalse(addressPostalCodeValid(null));
    assertFalse(addressPostalCodeValid(""));
    assertTrue(addressPostalCodeValid("33443"));
    assertTrue(addressPostalCodeValid(strN(16)));
    assertFalse(addressPostalCodeValid(strN(17)));
  }

  @Test
  public void testAddressCountryValid() {
    assertFalse(addressCountryValid(null));
    assertFalse(addressCountryValid(""));
    assertTrue(addressCountryValid("USA"));
    assertTrue(addressCountryValid(strN(120)));
    assertFalse(addressCountryValid(strN(121)));
  }

}
