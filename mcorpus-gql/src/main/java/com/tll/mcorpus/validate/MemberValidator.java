package com.tll.mcorpus.validate;

import static com.tll.mcorpus.validateapi.VldtnCore.clean;
import static com.tll.mcorpus.validateapi.VldtnCore.isBlank;
import static com.tll.mcorpus.validateapi.VldtnCore.isNotNull;
import static com.tll.mcorpus.validateapi.VldtnCore.isNullOrEmpty;
import static com.tll.mcorpus.validateapi.VldtnCore.lenchk;
import static com.tll.mcorpus.validateapi.VldtnCore.namePattern;
import static com.tll.mcorpus.validateapi.VldtnCore.not;

import java.util.regex.Pattern;

import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.gmodel.Member;
import com.tll.mcorpus.validateapi.VldtnBuilder;
import com.tll.mcorpus.validateapi.VldtnCore;

public class MemberValidator extends BaseMcorpusValidator<Member> {

  @Override
  protected void validateForAdd(final VldtnBuilder<Member> vldtn) {
    vldtn
      // member
      .vrqd(MemberValidator::empIdValid, Member::getEmpId, "member.empId.emsg", "empId")
      .vrqd(MemberValidator::locationValid, Member::getLocation, "member.location.emsg", "location")
      .vrqd(MemberValidator::nameFirstValid, Member::getNameFirst, "member.nameFirst.emsg", "nameFirst")
      .vtok(MemberValidator::nameMiddleValid, Member::getNameMiddle, "member.nameMiddle.emsg", "nameMiddle")
      .vrqd(MemberValidator::nameLastValid, Member::getNameLast, "member.nameLast.emsg", "nameLast")
      .vtok(MemberValidator::displayNameValid, Member::getDisplayName, "member.displayName.emsg", "displayName")
      // mauth
      .vrqd(VldtnCore::dobValid, Member::getDob, "member.dob.emsg", "dob")
      .vrqd(VldtnCore::ssnValid, Member::getSsn, "member.ssn.emsg", "ssn")
      .vtok(VldtnCore::emailValid, Member::getPersonalEmail, "member.personalEmail.emsg", "personalEmail")
      .vtok(VldtnCore::emailValid, Member::getWorkEmail, "member.workEmail.emsg", "workEmail")
      .vtok(VldtnCore::phoneValid, Member::getMobilePhone, "member.mobilePhone.emsg", "mobilePhone")
      .vtok(VldtnCore::phoneValid, Member::getHomePhone, "member.homePhone.emsg", "homePhone")
      .vtok(VldtnCore::phoneValid, Member::getWorkPhone, "member.workPhone.emsg", "workPhone")
      .vrqd(VldtnCore::usernameValid, Member::getUsername, "member.username.emsg", "username")
      .vrqd(VldtnCore::pswdValid, Member::getPswd, "member.pswd.emsg", "pswd")
    ;
  }

  @Override
  protected void validateForUpdate(final VldtnBuilder<Member> vldtn) {
    vldtn
      // require pk
      .vrqd(t -> vldtn.getTarget().getPk().isSet(), Member::getPk, "member.nopk.emsg", "pk")
      
      // member
      .vopt(MemberValidator::empIdValid, Member::getEmpId, "member.empId.emsg", "empId")
      .vopt(MemberValidator::locationValid, Member::getLocation, "member.location.emsg", "location")
      .vopt(MemberValidator::nameFirstValid, Member::getNameFirst, "member.nameFirst.emsg", "nameFirst")
      .vopt(MemberValidator::nameMiddleValid, Member::getNameMiddle, "member.nameMiddle.emsg", "nameMiddle")
      .vopt(MemberValidator::nameLastValid, Member::getNameLast, "member.nameLast.emsg", "nameLast")
      .vopt(MemberValidator::displayNameValid, Member::getDisplayName, "member.displayName.emsg", "displayName")
      // mauth
      .vopt(VldtnCore::dobValid, Member::getDob, "member.dob.emsg", "dob")
      .vtok(VldtnCore::ssnValid, Member::getSsn, "member.ssn.emsg", "ssn")
      .vtok(VldtnCore::emailValid, Member::getPersonalEmail, "member.personalEmail.emsg", "personalEmail")
      .vtok(VldtnCore::emailValid, Member::getWorkEmail, "member.workEmail.emsg", "workEmail")
      .vtok(VldtnCore::phoneValid, Member::getMobilePhone, "member.mobilePhone.emsg", "mobilePhone")
      .vtok(VldtnCore::phoneValid, Member::getHomePhone, "member.homePhone.emsg", "homePhone")
      .vtok(VldtnCore::phoneValid, Member::getWorkPhone, "member.workPhone.emsg", "workPhone")
      .vtok(VldtnCore::usernameValid, Member::getUsername, "member.username.emsg", "username")
      // .vopt(t -> pswdValid(t), Member::getPswd, "member.pswd.emsg", "pswd")
    ;
  }

  /**
   * empIdPattern: RegEx for strict enforcement of member emp id format:
   *               <code>dd-ddddddd</code> where d is 0-9.
   *
   * <p>Example: <code>32-1234567</code></p>
   */
  private static final Pattern empIdPattern = Pattern.compile("^\\d{2}-\\d{7}$");

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
  public static boolean locationValid(final String location) {
    return isNotNull(locationFromString(location));
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

  private static Location locationFromString(final String location) {
    if(location != null) {
      final String clocation = clean(location);
      final String sloc = clocation.startsWith("L") ? clocation.substring(1) : location;
      for(final Location enmLoc : Location.values()) {
        if(enmLoc.getLiteral().equals(sloc)) return enmLoc;
      }
    }
    // default
    return null;
  }  
}