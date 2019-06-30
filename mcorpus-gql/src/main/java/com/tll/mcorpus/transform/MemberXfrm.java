package com.tll.mcorpus.transform;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotNullOrEmpty;
import static com.tll.core.Util.neclean;
import static com.tll.core.Util.upper;
import static com.tll.transform.TransformUtil.asSqlDate;
import static com.tll.transform.TransformUtil.digits;
import static com.tll.transform.TransformUtil.fval;

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
      uuidFromToken(fval("mid", gqlMap)),
      fval("created", gqlMap),
      fval("modified", gqlMap),
      fval("empId", gqlMap),
      fval("location", gqlMap),
      fval("nameFirst", gqlMap),
      fval("nameMiddle", gqlMap),
      fval("nameLast", gqlMap),
      fval("displayName", gqlMap),
      fval("status", gqlMap),
      fval("dob", gqlMap),
      fval("ssn", gqlMap),
      fval("personalEmail", gqlMap),
      fval("workEmail", gqlMap),
      fval("mobilePhone", gqlMap),
      fval("homePhone", gqlMap),
      fval("workPhone", gqlMap),
      fval("username", gqlMap),
      fval("pswd", gqlMap)
    );
  }

  @Override
  public Member fromNotEmptyGraphQLMapForUpdate(final Map<String, Object> gqlMap) {
    return fromNotEmptyGraphQLMapForAdd(gqlMap);
  }

  @Override
  protected Member fromNonNullBackend(final MemberAndMauth d) {
    return new Member(
      d.dbMember.getMid(),
      odtToDate(d.dbMember.getCreated()),
      odtToDate(d.dbMember.getModified()),
      d.dbMember.getEmpId(),
      locationToString(d.dbMember.getLocation()),
      d.dbMember.getNameFirst(),
      d.dbMember.getNameMiddle(),
      d.dbMember.getNameLast(),
      d.dbMember.getDisplayName(),
      memberStatusToString(d.dbMember.getStatus()),
      d.dbMauth.getDob(),
      d.dbMauth.getSsn(),
      d.dbMauth.getEmailPersonal(),
      d.dbMauth.getEmailWork(),
      d.dbMauth.getMobilePhone(),
      d.dbMauth.getHomePhone(),
      d.dbMauth.getWorkPhone(),
      d.dbMauth.getUsername(),
      null // pswd
    );
  }

  @Override
  protected MemberAndMauth toBackendFromNonNull(final Member g) {
    return new MemberAndMauth(
      new com.tll.mcorpus.db.tables.pojos.Member(
        g.getMid(),
        odtFromDate(g.getCreated()),
        odtFromDate(g.getModified()),
        neclean(g.getEmpId()),
        locationFromString(g.getLocation()),
        upper(neclean(g.getNameFirst())),
        upper(neclean(g.getNameMiddle())),
        upper(neclean(g.getNameLast())),
        neclean(g.getDisplayName()),
        memberStatusFromString(g.getStatus())
      ),
      new com.tll.mcorpus.db.tables.pojos.Mauth(
        g.getMid(),
        null, // modified
        asSqlDate(g.getDob()),
        digits(neclean(g.getSsn())),
        neclean(g.getPersonalEmail()),
        neclean(g.getWorkEmail()),
        digits(neclean(g.getMobilePhone())),
        digits(neclean(g.getHomePhone())),
        null, // fax
        digits(neclean(g.getWorkPhone())),
        neclean(g.getUsername()),
        null // pswd
      )
    );
  }

}