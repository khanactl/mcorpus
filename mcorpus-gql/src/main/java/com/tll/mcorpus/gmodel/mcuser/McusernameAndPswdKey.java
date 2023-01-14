package com.tll.mcorpus.gmodel.mcuser;

import static com.tll.core.Util.isNotBlank;

import com.tll.gmodel.IKey;

public class McusernameAndPswdKey implements IKey {

	private final String username;
	private final String pswd;

	public McusernameAndPswdKey(String username, String pswd) {
		this.username = username;
		this.pswd = pswd;
	}

	public String getUsername() { return username; }

	public String getPswd() { return pswd; }

	@Override
	public boolean isSet() { return isNotBlank(username) && isNotBlank(pswd); }

	@Override
	public String refToken() { return String.format("McusernameAndPswdKey[username: %s, pswd: %s]", username, pswd); }
}