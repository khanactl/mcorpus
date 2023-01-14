package com.tll.mcorpus.gmodel.mcuser;

import static com.tll.core.Util.isNotBlank;
import static com.tll.core.Util.isNotNull;

import java.util.UUID;

import com.tll.gmodel.IKey;

public class McuserIdAndPswdKey implements IKey {
	private final UUID uid;
	private final String pswd;

	public McuserIdAndPswdKey(UUID uid, String pswd) {
		this.uid = uid;
		this.pswd = pswd;
	}

	public UUID getUid() { return uid; }

	public String getPswd() { return pswd; }

	@Override
	public boolean isSet() { return isNotNull(uid) && isNotBlank(pswd); }

	@Override
	public String refToken() { return String.format("McuserIdAndPswdKey[uid: %s]", uid); }

	

}