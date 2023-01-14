package com.tll.mcorpus.validate;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isBlank;
import static com.tll.core.Util.isNotBlank;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.not;
import static com.tll.core.Util.upper;
import static com.tll.validate.VldtnCore.lenchk;
import static com.tll.validate.VldtnCore.namePattern;

import java.util.regex.Pattern;

import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.MemberStatus;
import com.tll.mcorpus.gmodel.Member;
import com.tll.validate.VldtnBuilder;
import com.tll.validate.VldtnCore;

public class MemberValidator extends BaseMcorpusValidator<Member> {

	@Override
	public String getEntityTypeName() { return "Member"; }

	@Override
	protected void doValidateForAdd(final VldtnBuilder<Member> vldtn) {
		vldtn
			// member
			.vrqd(MemberValidator::empIdValid, Member::getEmpId, "member.empId.emsg", "empId")
			.vrqd(MemberValidator::locationValid, Member::getLocation, "member.location.emsg", "location")
			.vrqd(MemberValidator::nameFirstValid, Member::getNameFirst, "member.nameFirst.emsg", "nameFirst")
			.vtok(MemberValidator::nameMiddleValid, Member::getNameMiddle, "member.nameMiddle.emsg", "nameMiddle")
			.vrqd(MemberValidator::nameLastValid, Member::getNameLast, "member.nameLast.emsg", "nameLast")
			.vtok(MemberValidator::displayNameValid, Member::getDisplayName, "member.displayName.emsg", "displayName")
			.vrqd(MemberValidator::statusValid, Member::getStatus, "member.status.emsg", "status")
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
	protected void doValidateForUpdate(final VldtnBuilder<Member> vldtn) {
		vldtn
			// require pk
			.vrqd(t -> t.isSet(), Member::getPk, "member.nopk.emsg", "pk")

			// member
			.vtok(MemberValidator::empIdValid, Member::getEmpId, "member.empId.emsg", "empId")
			.vtok(MemberValidator::locationValid, Member::getLocation, "member.location.emsg", "location")
			.vtok(MemberValidator::nameFirstValid, Member::getNameFirst, "member.nameFirst.emsg", "nameFirst")
			.vtok(MemberValidator::nameMiddleValid, Member::getNameMiddle, "member.nameMiddle.emsg", "nameMiddle")
			.vtok(MemberValidator::nameLastValid, Member::getNameLast, "member.nameLast.emsg", "nameLast")
			.vtok(MemberValidator::displayNameValid, Member::getDisplayName, "member.displayName.emsg", "displayName")
			.vtok(MemberValidator::statusValid, Member::getStatus, "member.status.emsg", "status")
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

	@Override
	protected boolean hasAnyUpdatableFields(Member e) {
		return
			// member
			isNotBlank(e.getEmpId()) ||
			isNotBlank(e.getLocation()) ||
			isNotBlank(e.getNameFirst()) ||
			isNotNull(e.getNameMiddle()) ||
			isNotBlank(e.getNameLast()) ||
			isNotNull(e.getDisplayName()) ||
			isNotBlank(e.getStatus()) ||
			// mauth
			isNotNull(e.getDob()) ||
			isNotBlank(e.getSsn()) ||
			isNotNull(e.getPersonalEmail()) ||
			isNotNull(e.getWorkEmail()) ||
			isNotNull(e.getMobilePhone()) ||
			isNotNull(e.getHomePhone()) ||
			isNotNull(e.getWorkPhone()) ||
			isNotBlank(e.getUsername())
			;
	}

	@Override
	protected String getVmkForNoUpdateFieldsPresent() {
		return "member.noupdatefields.emsg";
	}

	/**
	 * empIdPattern: RegEx for strict enforcement of member emp id format:
	 *							 <code>dd-ddddddd</code> where d is 0-9.
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

	public static boolean statusValid(final String status) {
		return isNotNull(statusFromString(status));
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

	private static MemberStatus statusFromString(final String status) {
		if(status != null) {
			final String cstatus = upper(clean(status));
			for(final MemberStatus enmStatus : MemberStatus.values()) {
				if(enmStatus.getLiteral().equals(cstatus)) return enmStatus;
			}
		}
		// default
		return null;
	}

}