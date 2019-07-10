package com.tll.mcorpus.transform;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.neclean;
import static com.tll.core.Util.upper;
import static com.tll.transform.TransformUtil.fval;
import static com.tll.transform.TransformUtil.odtFromDate;
import static com.tll.transform.TransformUtil.odtToDate;
import static com.tll.transform.TransformUtil.uuidFromToken;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.tll.mcorpus.db.enums.McuserRole;
import com.tll.mcorpus.db.enums.McuserStatus;
import com.tll.mcorpus.gmodel.mcuser.Mcuser;

public class McuserXfrm extends BaseMcorpusTransformer<Mcuser, com.tll.mcorpus.db.tables.pojos.Mcuser> {

  public static McuserStatus mcuserStatusFromString(final String s) {
    try {
      return McuserStatus.valueOf(upper(clean(s)));
    } catch(Exception e) {
      return null;
    }
  }
  
  public static String mcuserStatusToString(final McuserStatus enm) {
    return isNull(enm) ? null : enm.getLiteral();
  }
  
  public static McuserRole mcuserRoleFromString(final String s) {
    try {
      return McuserRole.valueOf(upper(clean(s)));
    } catch(Exception e) {
      return null;
    }
  }
  
  public static String mcuserRoleToString(final McuserRole enm) {
    return isNull(enm) ? null : enm.getLiteral();
  }

  public static McuserRole[] mcuserRolesArrayFromStringCollection(final Collection<String> sroles) {
    return isNull(sroles) ? null : sroles.stream()
        .map(srole -> mcuserRoleFromString(srole))
        .filter(mr -> isNotNull(mr))
        .collect(Collectors.toSet())
        .toArray(new McuserRole[0]);
  }

  public static Set<String> mcuserRolesArrayToStringSet(final McuserRole[] roles) {
    return isNull(roles) ? null : Arrays.stream(roles)
        .map(role -> mcuserRoleToString(role))
        .filter(mr -> isNotNull(mr))
        .collect(Collectors.toSet())
        ;
  }

  public static Set<String> rolesListToSet(final List<String> rlist) {
    return isNull(rlist) ? null : new HashSet<>(rlist);
  }
  
  @Override
  protected Mcuser fromNotEmptyGraphQLMapForAdd(final Map<String, Object> gqlMap) {
    return new Mcuser(
      null,
      null,
      fval("name", gqlMap),
      fval("email", gqlMap),
      fval("username", gqlMap),
      fval("pswd", gqlMap),
      fval("status", gqlMap),
      fval("initialStatus", gqlMap),
      rolesListToSet(fval("roles", gqlMap))
    );
  }

  @Override
  public Mcuser fromNotEmptyGraphQLMapForUpdate(final Map<String, Object> gqlMap) {
    return new Mcuser(
      uuidFromToken(fval("uid", gqlMap)),
      null,
      null,
      fval("name", gqlMap),
      fval("email", gqlMap),
      fval("username", gqlMap),
      null,
      fval("status", gqlMap),
      rolesListToSet(fval("roles", gqlMap))
    );
  }

  @Override
  protected Mcuser fromNonNullBackend(final com.tll.mcorpus.db.tables.pojos.Mcuser d) {
    return new Mcuser(
      d.getUid(),
      odtToDate(d.getCreated()),
      odtToDate(d.getModified()),
      d.getName(),
      d.getEmail(),
      d.getUsername(),
      d.getPswd(),
      mcuserStatusToString(d.getStatus()),
      mcuserRolesArrayToStringSet(d.getRoles())
    );
  }

  @Override
  protected com.tll.mcorpus.db.tables.pojos.Mcuser toBackendFromNonNull(final Mcuser g) {
    return new com.tll.mcorpus.db.tables.pojos.Mcuser(
      g.getUid(),
      odtFromDate(g.getCreated()),
      odtFromDate(g.getModified()),
      neclean(g.getName()),
      neclean(g.getEmail()),
      neclean(g.getUsername()),
      neclean(g.getPswd()),
      mcuserStatusFromString(g.getStatus()),
      mcuserRolesArrayFromStringCollection(g.getRoles())
    );
  }
}
