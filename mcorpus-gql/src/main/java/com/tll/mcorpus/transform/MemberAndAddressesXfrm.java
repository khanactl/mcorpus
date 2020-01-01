package com.tll.mcorpus.transform;

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
      d.member.dbMember.getEmpId(),
      MemberXfrm.locationToString(d.member.dbMember.getLocation()),
      d.member.dbMember.getNameFirst(),
      d.member.dbMember.getNameMiddle(),
      d.member.dbMember.getNameLast(),
      d.member.dbMember.getDisplayName(),
      MemberXfrm.memberStatusToString(d.member.dbMember.getStatus()),
      d.member.dbMauth.getDob(),
      d.member.dbMauth.getSsn(),
      d.member.dbMauth.getEmailPersonal(),
      d.member.dbMauth.getEmailWork(),
      d.member.dbMauth.getMobilePhone(),
      d.member.dbMauth.getHomePhone(),
      d.member.dbMauth.getWorkPhone(),
      d.member.dbMauth.getUsername(),
      null, // pswd
      d.addresses.stream()
        .map(ma -> addressXfrm.fromNonNullBackend(ma))
        .collect(Collectors.toList())
    );
  }
}