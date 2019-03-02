package com.tll.mcorpus.dmodel;

import static com.tll.core.Util.isNotNull;

import com.tll.mcorpus.db.tables.pojos.Mauth;
import com.tll.mcorpus.db.tables.pojos.Member;

/**
 * Simple wrapper around jooQ generated {@link Member} and {@link Mauth} types.
 * 
 * @author jpk
 */
public class MemberAndMauth {

  public final Member dbMember;
  public final Mauth dbMauth;

  /**
   * Constructor.
   * 
   * @param dbMember backend member table pojo
   * @param dbMauth backend mauth table pojo
   */
  public MemberAndMauth(final Member dbMember, final Mauth dbMauth) {
    this.dbMember = dbMember;
    this.dbMauth = dbMauth;
  }

  public boolean hasMemberTableVals() {
    return 
      isNotNull(dbMember.getEmpId())
      || isNotNull(dbMember.getLocation())
      || isNotNull(dbMember.getNameFirst())
      || isNotNull(dbMember.getNameMiddle())
      || isNotNull(dbMember.getNameLast())
      || isNotNull(dbMember.getDisplayName())
      || isNotNull(dbMember.getStatus());
  }

  public boolean hasMauthTableVals() {
    return 
      isNotNull(dbMauth.getDob())
      || isNotNull(dbMauth.getSsn())
      || isNotNull(dbMauth.getEmailPersonal())
      || isNotNull(dbMauth.getEmailWork())
      || isNotNull(dbMauth.getMobilePhone())
      || isNotNull(dbMauth.getHomePhone())
      || isNotNull(dbMauth.getWorkPhone())
      || isNotNull(dbMauth.getUsername())
      || isNotNull(dbMauth.getPswd())
      ;
  }
}
