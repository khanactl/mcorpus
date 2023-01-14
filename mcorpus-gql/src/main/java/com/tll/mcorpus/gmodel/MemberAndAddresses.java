package com.tll.mcorpus.gmodel;

import static com.tll.core.Util.isNull;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MemberAndAddresses extends Member {

	private final List<MemberAddress> addresses;

	public MemberAndAddresses(UUID mid, Date created, Date modified, String empId, String location, String nameFirst, String nameMiddle, String nameLast, String displayName, String status, Date dob, String ssn, String personalEmail, String workEmail, String mobilePhone, String homePhone, String workPhone, String username, String pswd, List<MemberAddress> addresses) {
		super(mid, created, modified, empId, location, nameFirst, nameMiddle, nameLast, displayName, status, dob, ssn, personalEmail, workEmail, mobilePhone, homePhone, workPhone, username, pswd);
		this.addresses = isNull(addresses) ? Collections.emptyList() : addresses;
	}

	public List<MemberAddress> getAddresses() { return addresses; }

}