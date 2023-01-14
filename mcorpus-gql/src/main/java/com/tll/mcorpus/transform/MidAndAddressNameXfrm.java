package com.tll.mcorpus.transform;

import static com.tll.core.Util.upper;
import static com.tll.mcorpus.transform.MemberAddressXfrm.addressnameFromString;
import static com.tll.mcorpus.transform.MemberAddressXfrm.addressnameToString;

import com.tll.mcorpus.dmodel.MidAndAddressname;
import com.tll.mcorpus.gmodel.MemberAddress.MidAndAddressNameKey;

public class MidAndAddressNameXfrm extends BaseMcorpusTransformer<MidAndAddressNameKey, MidAndAddressname> {

	@Override
	protected MidAndAddressNameKey fromNonNullBackend(final MidAndAddressname d) {
		return new MidAndAddressNameKey(
			d.getMid(),
			upper(addressnameToString(d.getAddressname()))
		);
	}

	@Override
	protected MidAndAddressname toBackendFromNonNull(final MidAndAddressNameKey g) {
		return new MidAndAddressname(
			g.getMid(),
			addressnameFromString(g.getAddressName()) 
		);
	}

}