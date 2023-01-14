package com.tll.mcorpus.gmodel;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

public class Mlogout {
	private final UUID mid;
	private final Instant requestInstant;
	private final InetAddress requestOrigin;

	public Mlogout(UUID mid, Instant requestInstant, InetAddress requestOrigin) {
		this.mid = mid;
		this.requestInstant = requestInstant;
		this.requestOrigin = requestOrigin;
	}

	public UUID getMid() { return mid; }

	public Instant getRequestInstant() { return requestInstant; }

	public InetAddress getRequestOrigin() { return requestOrigin; }
}