package com.tll.mcorpus.transform;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.nclean;
import static com.tll.transform.TransformUtil.odtToDate;

import java.util.stream.Collectors;

import com.tll.mcorpus.dmodel.MemberAndMaddresses;
import com.tll.mcorpus.gmodel.MemberAndAddresses;

public class MemberAndAddressesXfrm extends BaseMcorpusTransformer<MemberAndAddresses, MemberAndMaddresses> {

  private final MemberAddressXfrm addressXfrm = new MemberAddressXfrm();

  @Override
  protected MemberAndAddresses fromNonNullBackend(final MemberAndMaddresses d) {
    return new MemberAndAddresses(
      d.member.dbMember.getMid(),
      odtToDate(d.member.dbMember.getCreated()),
      odtToDate(d.member.dbMember.getModified()),
      clean(d.member.dbMember.getEmpId()),
      MemberXfrm.locationToString(d.member.dbMember.getLocation()),
      clean(d.member.dbMember.getNameFirst()),
      nclean(d.member.dbMember.getNameMiddle()),
      clean(d.member.dbMember.getNameLast()),
      clean(d.member.dbMember.getDisplayName()),
      MemberXfrm.memberStatusToString(d.member.dbMember.getStatus()),
      d.member.dbMauth.getDob(),
      clean(d.member.dbMauth.getSsn()),
      clean(d.member.dbMauth.getEmailPersonal()),
      clean(d.member.dbMauth.getEmailWork()),
      clean(d.member.dbMauth.getMobilePhone()),
      clean(d.member.dbMauth.getHomePhone()),
      clean(d.member.dbMauth.getWorkPhone()),
      clean(d.member.dbMauth.getUsername()),
      null, // pswd
      d.addresses.stream()
        .map(ma -> addressXfrm.fromNonNullBackend(ma))
        .collect(Collectors.toList())
    );
  }
}