package com.tll.mcorpus.transform;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.nclean;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.lower;
import static com.tll.core.Util.neclean;
import static com.tll.core.Util.upper;
import static com.tll.transform.TransformUtil.fval;
import static com.tll.transform.TransformUtil.odtToDate;
import static com.tll.transform.TransformUtil.uuidFromToken;

import java.util.Map;

import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.tables.pojos.Maddress;
import com.tll.mcorpus.gmodel.MemberAddress;

public class MemberAddressXfrm extends BaseMcorpusTransformer<MemberAddress, Maddress> {

	public static Addressname addressnameFromString(final String s) {
		try {
			return Addressname.valueOf(lower(clean(s)));
		} catch(Exception e) {
			return null;
		}
	}

	public static String addressnameToString(final Addressname enm) {
		return isNull(enm) ? null : enm.getLiteral();
	}

	@Override
	protected MemberAddress fromNotEmptyGraphQLMapForAdd(final Map<String, Object> gqlMap) {
		return new MemberAddress(
			uuidFromToken(fval("mid", gqlMap)),
			clean(fval("addressName", gqlMap)),
			null,
			nclean(fval("attn", gqlMap)),
			clean(fval("street1", gqlMap)),
			nclean(fval("street2", gqlMap)),
			clean(fval("city", gqlMap)),
			clean(fval("state", gqlMap)),
			nclean(fval("postalCode", gqlMap)),
			clean(fval("country", gqlMap))
		);
	}

	@Override
	public MemberAddress fromNotEmptyGraphQLMapForUpdate(final Map<String, Object> gqlMap) {
		return new MemberAddress(
			uuidFromToken(fval("mid", gqlMap)),
			clean(fval("addressName", gqlMap)),
			null,
			nclean(fval("attn", gqlMap)),
			neclean(fval("street1", gqlMap)),
			nclean(fval("street2", gqlMap)),
			neclean(fval("city", gqlMap)),
			neclean(fval("state", gqlMap)),
			neclean(fval("postalCode", gqlMap)),
			neclean(fval("country", gqlMap))
		);
	}

	@Override
	protected MemberAddress fromNonNullBackend(final Maddress d) {
		return new MemberAddress(
			d.getMid(),
			upper(addressnameToString(d.getAddressName())),
			odtToDate(d.getModified()),
			d.getAttn(),
			d.getStreet1(),
			d.getStreet2(),
			d.getCity(),
			d.getState(),
			d.getPostalCode(),
			d.getCountry()
		);
	}

	@Override
	protected Maddress toBackendFromNonNull(final MemberAddress g) {
		return new Maddress(
			g.getMid(),
			addressnameFromString(g.getAddressName()),
			null,
			g.getAttn(),
			g.getStreet1(),
			g.getStreet2(),
			g.getCity(),
			g.getState(),
			g.getPostalCode(),
			g.getCountry()
		);
	}

}