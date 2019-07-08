package com.tll.mcorpus.validate;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.lower;
import static com.tll.core.Util.not;
import static com.tll.validate.VldtnCore.lenchk;
import static com.tll.validate.VldtnCore.namePattern;

import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.gmodel.MemberAddress;
import com.tll.validate.VldtnBuilder;

public class MemberAddressValidator extends BaseMcorpusValidator<MemberAddress> {

  @Override
  public String getEntityTypeName() { return "MemberAddress"; }
  
  @Override
  protected void validateForAdd(final VldtnBuilder<MemberAddress> vldtn) {
    vldtn
      // require pk
      .vrqd(t -> t.isSet(), MemberAddress::getPk, "maddress.nopk.emsg", "pk")

      .vrqd(MemberAddressValidator::addressAttnValid, MemberAddress::getAttn, "maddress.attn.emsg", "attn")
      .vrqd(MemberAddressValidator::addressStreet1Valid, MemberAddress::getStreet1, "maddress.street1.emsg", "street1")
      .vrqd(MemberAddressValidator::addressStreet2Valid, MemberAddress::getStreet2, "maddress.street2.emsg", "street2")
      .vrqd(MemberAddressValidator::addressCityValid, MemberAddress::getCity, "maddress.city.emsg", "city")
      .vrqd(MemberAddressValidator::addressStateValid, MemberAddress::getState, "maddress.state.emsg", "state")
      .vrqd(MemberAddressValidator::addressPostalCodeValid, MemberAddress::getPostalCode, "maddress.postalCode.emsg", "postalCode")
      .vrqd(MemberAddressValidator::addressCountryValid, MemberAddress::getCountry, "maddress.country.emsg", "country")
    ;
  }

  @Override
  protected void validateForUpdate(final VldtnBuilder<MemberAddress> vldtn) {
    vldtn
      // require pk
      .vrqd(t -> t.isSet(), MemberAddress::getPk, "maddress.nopk.emsg", "pk")
      
      .vtok(MemberAddressValidator::addressAttnValid, MemberAddress::getAttn, "maddress.attn.emsg", "attn")
      .vtok(MemberAddressValidator::addressStreet1Valid, MemberAddress::getStreet1, "maddress.street1.emsg", "street1")
      .vtok(MemberAddressValidator::addressStreet2Valid, MemberAddress::getStreet2, "maddress.street2.emsg", "street2")
      .vtok(MemberAddressValidator::addressCityValid, MemberAddress::getCity, "maddress.city.emsg", "city")
      .vtok(MemberAddressValidator::addressStateValid, MemberAddress::getState, "maddress.state.emsg", "state")
      .vtok(MemberAddressValidator::addressPostalCodeValid, MemberAddress::getPostalCode, "maddress.postalCode.emsg", "postalCode")
      .vtok(MemberAddressValidator::addressCountryValid, MemberAddress::getCountry, "maddress.country.emsg", "country")
    ;
  }

  /**
   * Verify an address name is specified.
   *
   * - required
   *
   * @param addressname the addressname
   * @return true when valid
   */
  public static boolean addressNameValid(final String addressname) {
    return isNotNull(addressnameFromString(addressname));
  }

  /**
   * Verify an address attn is valid.
   *
   * - NOT required
   * - max len: 50 chars
   *
   * <p>NOTE: attn is taken as a 'name' for the imposed RegEx filter.</p>
   *
   * @param attn the address attn
   * @return true when valid
   */
  public static boolean addressAttnValid(final String attn) {
    return isNullOrEmpty(attn) || (lenchk(attn, 50) && namePattern.matcher(attn).matches());
  }

  /**
   * Verify an address street1.
   *
   * - required<br>
   * - Max. length: 120 chars
   *
   * @param street1 the address street1
   * @return true when valid
   */
  public static boolean addressStreet1Valid(final String street1) {
    return not(isNullOrEmpty(street1)) && lenchk(street1, 120);
  }

  /**
   * Verify an address street2.
   *
   *     - NOT required<br>
   *     - Max. length: 120 chars
   *
   * @param street2 the address street1
   * @return true when valid
   */
  public static boolean addressStreet2Valid(final String street2) {
    return isNullOrEmpty(street2) || lenchk(street2, 120);
  }

  /**
   * Verify an address city.
   *
   *     - required<br>
   *     - Max. length: 120 chars
   *
   * @param city the address city
   * @return true when valid
   */
  public static boolean addressCityValid(final String city) {
    return not(isNullOrEmpty(city)) && lenchk(city, 120);
  }

  /**
   * Verify an address state.
   *
   *     - required<br>
   *     - Max. length: 2 chars
   *
   * @param state the address state
   * @return true when valid
   */
  public static boolean addressStateValid(final String state) {
    return not(isNullOrEmpty(state)) && lenchk(state, 2);
  }

  /**
   * Verify an address postal/zip code.
   *
   *     - required<br>
   *     - Max. length: 16 chars
   *
   * @param postalCode the address postalCode
   * @return true when valid
   */
  public static boolean addressPostalCodeValid(final String postalCode) {
    return not(isNullOrEmpty(postalCode)) && lenchk(postalCode, 16);
  }

  /**
   * Verify an address country.
   *
   *     - required<br>
   *     - Max. length: 120 chars
   *     - constrain by name RegEx filter
   *
   * @param country the address country
   * @return true when valid
   */
  public static boolean addressCountryValid(final String country) {
    return not(isNullOrEmpty(country)) && lenchk(country, 120) && namePattern.matcher(country).matches();
  }

  private static Addressname addressnameFromString(final String s) {
    try {
      return Addressname.valueOf(lower(clean(s)));
    } catch(Exception e) {
      return null;
    }
  }  
}