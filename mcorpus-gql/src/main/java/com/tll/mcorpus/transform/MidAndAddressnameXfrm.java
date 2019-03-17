package com.tll.mcorpus.transform;

import static com.tll.core.Util.upper;
import static com.tll.mcorpus.transform.MemberAddressXfrm.addressnameFromString;
import static com.tll.mcorpus.transform.MemberAddressXfrm.addressnameToString;

import com.tll.mcorpus.dmodel.MidAndAddressname;
import com.tll.mcorpus.gmodel.MidAndAddressnameKey;

public class MidAndAddressnameXfrm extends BaseMcorpusTransformer<MidAndAddressnameKey, MidAndAddressname> {

  @Override
  protected MidAndAddressnameKey fromNonNullBackend(final MidAndAddressname d) {
    return new MidAndAddressnameKey(
      d.getMid(),
      upper(addressnameToString(d.getAddressname()))
    );
  }

  @Override
  protected MidAndAddressname toBackendFromNonNull(final MidAndAddressnameKey g) {
    return new MidAndAddressname(
      g.getMid(),
      addressnameFromString(g.getAddressname()) 
    );
  }

}