package com.tll.mcorpus.gmodel;

import java.net.InetAddress;
import java.time.Instant;

public class Mlogin {
	private final String username;
	private final String pswd;
	private final Instant requestInstant;
	private final InetAddress requestOrigin;

	public Mlogin(String username, String pswd, Instant requestInstant, InetAddress requestOrigin) {
		this.username = username;
		this.pswd = pswd;
		this.requestInstant = requestInstant;
		this.requestOrigin = requestOrigin;
	}

	public String getUsername() {
		return username;
	}

	public String getPswd() {
		return pswd;
	}

	public Instant getRequestInstant() {
		return requestInstant;
	}

	public InetAddress getRequestOrigin() {
		return requestOrigin;
	}

}