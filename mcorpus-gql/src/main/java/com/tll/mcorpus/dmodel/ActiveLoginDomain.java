package com.tll.mcorpus.dmodel;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ActiveLoginDomain {

	public final UUID jwtId;
	public final OffsetDateTime expires;
	public final OffsetDateTime requestTimestamp;
	public final InetAddress requestOrigin;

	public ActiveLoginDomain(UUID jwtId, OffsetDateTime expires, OffsetDateTime requestTimestamp, InetAddress requestOrigin) {
		this.jwtId = jwtId;
		this.expires = expires;
		this.requestTimestamp = requestTimestamp;
		this.requestOrigin = requestOrigin;
	}

}