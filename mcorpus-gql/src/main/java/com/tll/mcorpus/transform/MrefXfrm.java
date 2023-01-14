package com.tll.mcorpus.transform;

import static com.tll.mcorpus.transform.MemberXfrm.locationFromString;
import static com.tll.mcorpus.transform.MemberXfrm.locationToString;

import java.util.Map;

import com.tll.mcorpus.gmodel.Mref;
import com.tll.transform.BaseTransformer;

public class MrefXfrm extends BaseTransformer<Mref, com.tll.mcorpus.db.udt.pojos.Mref> {

	@Override
	protected Mref fromNotEmptyGraphQLMapForAdd(final Map<String, Object> gqlMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Mref fromNotEmptyGraphQLMapForUpdate(final Map<String, Object> gqlMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Mref fromNonNullBackend(final com.tll.mcorpus.db.udt.pojos.Mref d) {
		return new Mref(
			d.getMid(),
			d.getEmpId(),
			locationToString(d.getLocation())
		);
	}

	@Override
	protected com.tll.mcorpus.db.udt.pojos.Mref toBackendFromNonNull(final Mref e) {
		return new com.tll.mcorpus.db.udt.pojos.Mref(
			e.memberPk.getUUID(),
			e.empId,
			locationFromString(e.location));
	}
}