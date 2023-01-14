package com.tll.mcorpus.transform;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotNullOrEmpty;
import static com.tll.core.Util.nclean;
import static com.tll.core.Util.nechk;
import static com.tll.core.Util.neclean;
import static com.tll.core.Util.upper;
import static com.tll.transform.TransformUtil.dateToLocalDate;
import static com.tll.transform.TransformUtil.digits;
import static com.tll.transform.TransformUtil.fval;
import static com.tll.transform.TransformUtil.localDateToDate;
import static com.tll.transform.TransformUtil.odtToDate;
import static com.tll.transform.TransformUtil.uuidFromToken;

import java.util.Map;

import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.MemberStatus;
import com.tll.mcorpus.dmodel.MemberAndMauth;
import com.tll.mcorpus.gmodel.Member;

public class MemberXfrm extends BaseMcorpusTransformer<Member, MemberAndMauth> {

	public static String locationToString(Location location) {
		return location == null ? null : "L" + location.getLiteral();
	}

	public static Location locationFromString(final String location) {
		if(isNotNullOrEmpty(location)) {
			final String clocation = clean(location);
			final String sloc = clocation.startsWith("L") ? clocation.substring(1) : clocation;
			for(final Location enmLoc : Location.values()) {
				if(enmLoc.getLiteral().equals(sloc)) return enmLoc;
			}
		}
		// default
		return null;
	}

	public static String memberStatusToString(MemberStatus memberStatus) {
		return memberStatus == null ? null : memberStatus.getLiteral();
	}

	public static MemberStatus memberStatusFromString(String status) {
		if(isNotNullOrEmpty(status)) {
			final String cstatus = clean(status);
			for(final MemberStatus mstat : MemberStatus.values()) {
				if(mstat.getLiteral().equals(cstatus)) return mstat;
			}
		}
		// default
		return null;
	}

	@Override
	protected Member fromNotEmptyGraphQLMapForAdd(final Map<String, Object> gqlMap) {
		return new Member(
			null,
			null,
			null,
			fval("empId", gqlMap),
			fval("location", gqlMap),
			clean(fval("nameFirst", gqlMap)),
			clean(fval("nameMiddle", gqlMap)), // make empty if null for middle name (not null in db ddl)
			clean(fval("nameLast", gqlMap)),
			nclean(fval("displayName", gqlMap)),
			clean(fval("status", gqlMap)),
			fval("dob", gqlMap),
			clean(fval("ssn", gqlMap)),
			nclean(fval("personalEmail", gqlMap)),
			nclean(fval("workEmail", gqlMap)),
			nclean(fval("mobilePhone", gqlMap)),
			nclean(fval("homePhone", gqlMap)),
			nclean(fval("workPhone", gqlMap)),
			clean(fval("username", gqlMap)),
			fval("pswd", gqlMap) // do not clean pswd
		);
	}

	@Override
	public Member fromNotEmptyGraphQLMapForUpdate(final Map<String, Object> gqlMap) {
		return new Member(
			uuidFromToken(fval("mid", gqlMap)),
			null,
			null,
			neclean(fval("empId", gqlMap)),
			neclean(fval("location", gqlMap)),
			neclean(fval("nameFirst", gqlMap)),
			nclean(fval("nameMiddle", gqlMap)),
			neclean(fval("nameLast", gqlMap)),
			nclean(fval("displayName", gqlMap)),
			neclean(fval("status", gqlMap)),
			fval("dob", gqlMap),
			neclean(fval("ssn", gqlMap)),
			nclean(fval("personalEmail", gqlMap)),
			nclean(fval("workEmail", gqlMap)),
			nclean(fval("mobilePhone", gqlMap)),
			nclean(fval("homePhone", gqlMap)),
			nclean(fval("workPhone", gqlMap)),
			neclean(fval("username", gqlMap)),
			nechk(fval("pswd", gqlMap))
		);
	}

	@Override
	protected Member fromNonNullBackend(final MemberAndMauth d) {
		return new Member(
			d.dbMember.getMid(),
			odtToDate(d.dbMember.getCreated()),
			odtToDate(d.dbMember.getModified()),
			clean(d.dbMember.getEmpId()),
			locationToString(d.dbMember.getLocation()),
			clean(d.dbMember.getNameFirst()),
			nclean(d.dbMember.getNameMiddle()),
			clean(d.dbMember.getNameLast()),
			clean(d.dbMember.getDisplayName()),
			memberStatusToString(d.dbMember.getStatus()),
			localDateToDate(d.dbMauth.getDob()),
			clean(d.dbMauth.getSsn()),
			clean(d.dbMauth.getEmailPersonal()),
			clean(d.dbMauth.getEmailWork()),
			clean(d.dbMauth.getMobilePhone()),
			clean(d.dbMauth.getHomePhone()),
			clean(d.dbMauth.getWorkPhone()),
			clean(d.dbMauth.getUsername()),
			null // pswd
		);
	}

	@Override
	protected MemberAndMauth toBackendFromNonNull(final Member g) {
		return new MemberAndMauth(
			new com.tll.mcorpus.db.tables.pojos.Member(
				g.getMid(),
				null,
				null,
				g.getEmpId(),
				locationFromString(g.getLocation()),
				upper(g.getNameFirst()),
				upper(g.getNameMiddle()),
				upper(g.getNameLast()),
				g.getDisplayName(),
				memberStatusFromString(g.getStatus())
			),
			new com.tll.mcorpus.db.tables.pojos.Mauth(
				g.getMid(),
				null, // modified
				dateToLocalDate(g.getDob()),
				digits(g.getSsn()),
				g.getPersonalEmail(),
				g.getWorkEmail(),
				digits(g.getMobilePhone()),
				digits(g.getHomePhone()),
				digits(g.getWorkPhone()),
				null, // fax
				g.getUsername(),
				g.getPswd()
			)
		);
	}

}