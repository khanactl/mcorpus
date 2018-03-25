package com.tll.mcorpus.repo;

import static com.tll.mcorpus.Util.isBlank;
import static com.tll.mcorpus.Util.isNotNull;
import static com.tll.mcorpus.Util.isNull;
import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.lenchk;
import static com.tll.mcorpus.Util.not;
import static com.tll.mcorpus.db.Tables.MADDRESS;
import static com.tll.mcorpus.db.Tables.MAUTH;
import static com.tll.mcorpus.db.Tables.MEMBER;
import static com.tll.mcorpus.repo.RepoUtil.fmissing;
import static com.tll.mcorpus.repo.RepoUtil.fval;
import static com.tll.mcorpus.repo.RepoUtil.hasField;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.enums.Location;

/**
 * Non-mutating validation methods for incoming member data.
 */
public class MCorpusDataValidator {

  /**
   * empIdPattern: RegEx for strict enforcement of member emp id format:
   *               <code>dd-ddddddd</code> where d is 0-9.
   *
   * <p>Example: <code>32-1234567</code></p>
   */
  private static final Pattern empIdPattern = Pattern.compile("^\\d{2}-\\d{7}$");

  /**
   * namePattern: RegEx to apply to a 'name'.<br>
   *              Allowed chars: A-Z, a-z, comma(,), dash(-), period(.), space( ), apostrophe(')
   */
  private static final Pattern namePattern = Pattern.compile("^[a-zA-Z ,.'-]+$");

  /**
   * ssnPattern: Enforcement of one of two formats:
   *              1. XXX-XX-XXXX
   *              2. XXXXXXXXX
   *
   */
  private static final Pattern ssnPattern = Pattern.compile("^(\\d{3}-\\d{2}-\\d{4}|\\d{9})$");

  /**
   * emailPattern: RegEx for validating email addresses.  "Works 99% of the time."
   *
   * - min 2 chars long
   * - max 128 chars long
   *
   */
  private static final Pattern emailPattern = Pattern.compile("(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,128}$");

  /**
   * phonePattern: Allowed chars: digits(0-9), dash(-), dot(.), space( ), open-paren( '(' ), close-paren( ')' )
   */
  private static final Pattern phonePattern = Pattern.compile("^[\\d\\-. \\(\\)]+$");

  /**
   * usernamePattern: Allowed chars: alpha-numeric upper and lower.<br>
   *                  min length: 4 chars, max length: 26 chars
   */
  private static final Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9]{4,26}$");

  /**
   * pswdPattern: any non-whitespace character.<br>
   *              min length: 8 chars, max length: 50 chars
   */
  private static final Pattern pswdPattern = Pattern.compile("^\\S{8,50}$");

  // member

  /**
   * empId
   *
   * - required
   * - constrained by XX-XXXXXXX digits
   *
   * @param empId the member empid
   * @return true when valid
   */
  public static boolean empIdValid(final String empId) {
    return not(isNullOrEmpty(empId)) && empIdPattern.matcher(empId).matches();
  }

  /**
   * Location
   *
   * - required
   *
   * @param location the member location
   * @return true when valid
   */
  public static boolean locationValid(final Location location) {
    return not(isNull(location));
  }

  public static boolean nameFirstValid(final String name) {
    return not(isNullOrEmpty(name)) && not(isBlank(name)) && lenchk(name, 64) && namePattern.matcher(name).matches();
  }

  public static boolean nameMiddleValid(final String name) {
    return isNullOrEmpty(name) || not(isBlank(name)) && lenchk(name, 64) && namePattern.matcher(name).matches();
  }

  public static boolean nameLastValid(final String name) {
    return not(isNullOrEmpty(name)) && not(isBlank(name)) && lenchk(name, 64) && namePattern.matcher(name).matches();
  }

  public static boolean displayNameValid(final String name) {
    return isNullOrEmpty(name) || not(isBlank(name)) && lenchk(name, 64) && namePattern.matcher(name).matches();
  }

  // mauth

  /**
   * Verify a birth date satisfies:
   * - not null
   * - not greater than now
   *
   * @param dob any birth date
   * @return true when valid
   */
  public static boolean dobValid(final Date dob) {
    return not(isNull(dob)) && not(dob.getTime() > System.currentTimeMillis());
  }

  /**
   * Verify an SSN adheres to format: <code>XX-XX-XXXX</code> where X is 0-9.
   *
   * - required
   *
   * @param ssn the SSN
   * @return true when valid
   */
  public static boolean ssnValid(final String ssn) {
    return not(isNullOrEmpty(ssn)) && ssnPattern.matcher(ssn).matches();
  }

  /**
   * Verify an email address is valid.
   *
   * @param s any email address
   * @param required if the email address is required (can't be null or empty)
   * @return true when the given email address string is valid
   */
  public static boolean emailValid(final String s, boolean required) {
    return isNullOrEmpty(s) ? not(required) : emailPattern.matcher(s).matches();
  }

  /**
   * Verify a phone number has only digits, spaces, dots or dashes.
   *
   * @param s any phone number
   * @param required if the phone number is required (can't be null or empty)
   * @return true when the given phone number string is valid
   */
  public static boolean phoneValid(final String s, boolean required) {
    return isNullOrEmpty(s) ? not(required) : phonePattern.matcher(s).matches();
  }

  /**
   * Verify a member username satisfies:
   * - min 4 chars
   * - max 26 chars
   *
   * @param username the member username
   * @return true when valid
   */
  public static boolean usernameValid(final String username) {
    return not(isNullOrEmpty(username)) && usernamePattern.matcher(username).matches();
  }

  /**
   * Verify a member password satisfies:
   *  - min 8 chars
   *  - max 50 chars
   *  - any non whitespace char
   *
   * @param pswd member password
   * @return true when valid
   */
  public static boolean pswdValid(final String pswd) {
    return not(isNullOrEmpty(pswd)) && pswdPattern.matcher(pswd).matches();
  }

  // maddress

  /**
   * Verify an address name is specified.
   *
   * - required
   *
   * @param addressname the addressname
   * @return true when valid
   */
  public static boolean addressNameValid(final Addressname addressname) {
    return isNotNull(addressname);
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

  /**
   * Validate member fields for adding a member record to the underlying datastore.
   *
   * @param memberMap map of member properties.
   *
   * @param emsgs list reference to hold any presentation-worthy error messages
   *              resulting from the validation check.
   *
   *              <p>This is how client's calling this function will know if the
   *              validation was successful or not.</p>
   *
   */
  public static void validateMemberToAdd(final Map<String, Object> memberMap, final List<String> emsgs) {
    // member
    if(not(empIdValid(fval(MEMBER.EMP_ID, memberMap)))) emsgs.add("Invalid emp id.");

    if(not(locationValid(fval(MEMBER.LOCATION, memberMap)))) emsgs.add("Invalid member Location.");
    if(not(nameFirstValid(fval(MEMBER.NAME_FIRST, memberMap)))) emsgs.add("Invalid First Name.");
    if(not(nameMiddleValid(fval(MEMBER.NAME_MIDDLE, memberMap)))) emsgs.add("Invalid Middle Name.");
    if(not(nameLastValid(fval(MEMBER.NAME_LAST, memberMap)))) emsgs.add("Invalid Last Name.");
    if(not(displayNameValid(fval(MEMBER.DISPLAY_NAME, memberMap)))) emsgs.add("Invalid Display Name.");
    // mauth
    if(not(dobValid(fval(MAUTH.DOB, memberMap)))) emsgs.add("Invalid Date of Birth.");
    if(not(ssnValid(fval(MAUTH.SSN, memberMap)))) emsgs.add("Invalid SSN.");
    if(not(emailValid(fval(MAUTH.EMAIL_PERSONAL, memberMap), false))) emsgs.add("Invalid personal email address.");
    if(not(emailValid(fval(MAUTH.EMAIL_WORK, memberMap), false))) emsgs.add("Invalid work email address.");
    if(not(phoneValid(fval(MAUTH.MOBILE_PHONE, memberMap), false))) emsgs.add("Invalid mobile phone number.");
    if(not(phoneValid(fval(MAUTH.HOME_PHONE, memberMap), false))) emsgs.add("Invalid home phone number.");
    if(not(phoneValid(fval(MAUTH.WORK_PHONE, memberMap), false))) emsgs.add("Invalid work phone number.");
    if(not(usernameValid(fval(MAUTH.USERNAME, memberMap)))) emsgs.add("Invalid username.");
    if(not(pswdValid(fval(MAUTH.PSWD, memberMap)))) emsgs.add("Invalid password.");
  }

  /**
   * Validate member fields for updating a member record to the underlying datastore.
   *
   * @param memberMap map of member properties.
   *
   * @param emsgs list reference to hold any presentation-worthy error messages
   *              resulting from the validation check.
   *
   *              <p>This is how client's calling this function will know if the
   *              validation was successful or not.</p>
   *
   */
  public static void validateMemberToUpdate(final Map<String, Object> memberMap, final List<String> emsgs) {
    // verify we have at least one member property to update
    if(memberMap.isEmpty()) {
      emsgs.add("No member properties provided.");
      return;
    }

    // member
    if(fmissing(MEMBER.MID, memberMap))
      emsgs.add("No member id present.");

    if(hasField(MEMBER.NAME_FIRST, memberMap) && not(nameFirstValid(fval(MEMBER.NAME_FIRST, memberMap))))
      emsgs.add("Invalid First Name.");

    if(hasField(MEMBER.NAME_MIDDLE, memberMap) && not(nameMiddleValid(fval(MEMBER.NAME_MIDDLE, memberMap))))
      emsgs.add("Invalid Middle Name.");

    if(hasField(MEMBER.NAME_LAST, memberMap) && not(nameLastValid(fval(MEMBER.NAME_LAST, memberMap))))
      emsgs.add("Invalid Last Name.");

    if(hasField(MEMBER.DISPLAY_NAME, memberMap) && not(displayNameValid(fval(MEMBER.DISPLAY_NAME, memberMap))))
      emsgs.add("Invalid Display Name.");

    // mauth
    if(hasField(MAUTH.DOB, memberMap) && not(dobValid(fval(MAUTH.DOB, memberMap))))
      emsgs.add("Invalid Date of Birth.");

    if(hasField(MAUTH.SSN, memberMap) && not(ssnValid(fval(MAUTH.SSN, memberMap))))
      emsgs.add("Invalid SSN.");

    if(hasField(MAUTH.EMAIL_PERSONAL, memberMap) && not(emailValid(fval(MAUTH.EMAIL_PERSONAL, memberMap), false)))
      emsgs.add("Invalid personal email address.");

    if(hasField(MAUTH.EMAIL_WORK, memberMap) && not(emailValid(fval(MAUTH.EMAIL_WORK, memberMap), false)))
      emsgs.add("Invalid work email address.");

    if(hasField(MAUTH.MOBILE_PHONE, memberMap) && not(phoneValid(fval(MAUTH.MOBILE_PHONE, memberMap), false)))
      emsgs.add("Invalid mobile phone number.");

    if(hasField(MAUTH.HOME_PHONE, memberMap) && not(phoneValid(fval(MAUTH.HOME_PHONE, memberMap), false)))
      emsgs.add("Invalid home phone number.");

    if(hasField(MAUTH.WORK_PHONE, memberMap) && not(phoneValid(fval(MAUTH.WORK_PHONE, memberMap), false)))
      emsgs.add("Invalid work phone number.");
  }

  /**
   * Validate member address fields for record insert.
   *
   * @param maddressMap the member address field property map
   * @param emsgs error msg list ref added to upon validation failure
   */
  public static void validateMemberAddressToAdd(final Map<String, Object> maddressMap, final List<String> emsgs) {

    if(not(addressNameValid(fval(MADDRESS.ADDRESS_NAME, maddressMap))))
      emsgs.add("Invalid Address Name.");

    if(not(addressAttnValid(fval(MADDRESS.ATTN, maddressMap))))
      emsgs.add("Invalid Address Attn.");

    if(not(addressStreet1Valid(fval(MADDRESS.STREET1, maddressMap))))
      emsgs.add("Invalid Address Street 1.");

    if(not(addressStreet2Valid(fval(MADDRESS.STREET2, maddressMap))))
      emsgs.add("Invalid Address Street 2.");

    if(not(addressCityValid(fval(MADDRESS.CITY, maddressMap))))
      emsgs.add("Invalid Address City.");

    if(not(addressStateValid(fval(MADDRESS.STATE, maddressMap))))
      emsgs.add("Invalid Address State.");

    if(not(addressCountryValid(fval(MADDRESS.COUNTRY, maddressMap))))
      emsgs.add("Invalid Address Country.");
  }

  /**
   * Validate member address fields for record update.
   *
   * @param maddressMap the member address field property map
   * @param emsgs error msg list ref added to upon validation failure
   */
  public static void validateMemberAddressToUpdate(final Map<String, Object> maddressMap, final List<String> emsgs) {

    // verify we have at least one member property to update
    if(maddressMap.isEmpty()) {
      emsgs.add("No member address properties provided.");
      return;
    }

    if(fmissing(MADDRESS.MID, maddressMap))
      emsgs.add("No member id present.");

    if(fmissing(MADDRESS.ADDRESS_NAME, maddressMap))
      emsgs.add("No Address Name present.");

    if(hasField(MADDRESS.ATTN, maddressMap) && not(addressAttnValid(fval(MADDRESS.ATTN, maddressMap))))
      emsgs.add("Invalid address Attn.");

    if(hasField(MADDRESS.STREET1, maddressMap) && not(addressStreet1Valid(fval(MADDRESS.STREET1, maddressMap))))
      emsgs.add("Invalid address Street 1.");

    if(hasField(MADDRESS.STREET2, maddressMap) && not(addressStreet2Valid(fval(MADDRESS.STREET2, maddressMap))))
      emsgs.add("Invalid address Street 2.");

    if(hasField(MADDRESS.CITY, maddressMap) && not(addressCityValid(fval(MADDRESS.CITY, maddressMap))))
      emsgs.add("Invalid address City.");

    if(hasField(MADDRESS.STATE, maddressMap) && not(addressStateValid(fval(MADDRESS.STATE, maddressMap))))
      emsgs.add("Invalid address State.");

    if(hasField(MADDRESS.POSTAL_CODE, maddressMap) && not(addressPostalCodeValid(fval(MADDRESS.POSTAL_CODE, maddressMap))))
      emsgs.add("Invalid address postal/zip code.");

    if(hasField(MADDRESS.COUNTRY, maddressMap) && not(addressCountryValid(fval(MADDRESS.COUNTRY, maddressMap))))
      emsgs.add("Invalid address Country.");
  }

  private MCorpusDataValidator() {}
}
