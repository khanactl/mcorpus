package com.tll.mcorpus.validate;

import static com.tll.TestUtil.strN;
import static com.tll.mcorpus.validate.MemberAddressValidator.addressAttnValid;
import static com.tll.mcorpus.validate.MemberAddressValidator.addressCityValid;
import static com.tll.mcorpus.validate.MemberAddressValidator.addressCountryValid;
import static com.tll.mcorpus.validate.MemberAddressValidator.addressNameValid;
import static com.tll.mcorpus.validate.MemberAddressValidator.addressPostalCodeValid;
import static com.tll.mcorpus.validate.MemberAddressValidator.addressStateValid;
import static com.tll.mcorpus.validate.MemberAddressValidator.addressStreet1Valid;
import static com.tll.mcorpus.validate.MemberAddressValidator.addressStreet2Valid;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tll.UnitTest;
import com.tll.mcorpus.db.enums.Addressname;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class MemberAddressValidatorTest {

  @Test
  public void testAddressNameValid() {
    assertFalse(addressNameValid(null));
    assertTrue(addressNameValid(Addressname.home.getLiteral()));
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