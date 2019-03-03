package com.tll.mcorpus.validate;

import static com.tll.mcorpus.TestUtil.strN;
import static com.tll.mcorpus.validate.MemberValidator.displayNameValid;
import static com.tll.mcorpus.validate.MemberValidator.empIdValid;
import static com.tll.mcorpus.validate.MemberValidator.locationValid;
import static com.tll.mcorpus.validate.MemberValidator.nameFirstValid;
import static com.tll.mcorpus.validate.MemberValidator.nameLastValid;
import static com.tll.mcorpus.validate.MemberValidator.nameMiddleValid;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tll.UnitTest;
import com.tll.mcorpus.db.enums.Location;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class MemberValidatorTest {

  @Test
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

  @Test
  public void testLocationValid() {
    assertTrue(locationValid(Location._01.getLiteral()));
    assertFalse(locationValid(null));
  }

  @Test
  public void testNameFirstValid() {
    assertTrue(nameFirstValid("Jake"));
    assertFalse(nameFirstValid(null));
    assertFalse(nameFirstValid(""));
    assertFalse(nameFirstValid(" "));
    assertFalse(nameFirstValid(strN(65)));
  }

  @Test
  public void testNameMiddleValid() {
    assertTrue(nameMiddleValid("Diddle"));
    assertTrue(nameMiddleValid(null));
    assertTrue(nameMiddleValid(""));
    assertFalse(nameMiddleValid(" "));
    assertFalse(nameMiddleValid(strN(65)));
  }

  @Test
  public void testNameLastValid() {
    assertTrue(nameLastValid("Last"));
    assertFalse(nameLastValid(null));
    assertFalse(nameLastValid(""));
    assertFalse(nameLastValid(" "));
    assertFalse(nameLastValid(strN(65)));
  }

  @Test
  public void testDisplayNameValid() {
    assertTrue(displayNameValid("display"));
    assertTrue(displayNameValid(null));
    assertTrue(displayNameValid(""));
    assertFalse(displayNameValid(" "));
    assertFalse(displayNameValid(strN(65)));
  }


}