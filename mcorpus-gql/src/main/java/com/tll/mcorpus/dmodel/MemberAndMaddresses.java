package com.tll.mcorpus.dmodel;

import static com.tll.core.Util.isNull;

import java.util.Collections;
import java.util.List;

import com.tll.mcorpus.db.tables.pojos.Maddress;

/**
 * Encapsulation POJO of member, mauth and related many maddress for a given member.
 */
public class MemberAndMaddresses {

  public final MemberAndMauth member;
  public final List<Maddress> addresses;

  /**
   * Constructor.
   *
   * @param member the backend {@link MemberAndMauth} pojo
   * @param addresses the backend {@link Maddress} pojo
   */
  public MemberAndMaddresses(MemberAndMauth member, List<Maddress> addresses) {
    this.member = member;
    this.addresses = isNull(addresses) ? Collections.emptyList() : addresses;
  }

}