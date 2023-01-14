package com.tll.mcorpus.gmodel.mcuser;

import static com.tll.core.Util.copy;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.tll.gmodel.BaseEntity;
import com.tll.gmodel.UUIDKey;

public class Mcuser extends BaseEntity<Mcuser, UUIDKey> /*implements IJwtUser*/ {

	private final UUIDKey			 pk;
	private final Date				 created;
	private final Date				 modified;
	private final String			 name;
	private final String			 email;
	private final String			 username;
	private final String			 pswd;
	private final String			 status;
	private final Set<String>	 roles;

	public Mcuser(
			UUID				 uid,
			Date				 created,
			Date				 modified,
			String			 name,
			String			 email,
			String			 username,
			String			 pswd,
			String			 status,
			Set<String>	 roles
	) {
			this.pk = new UUIDKey(uid, "mcuser");
			this.created = copy(created);
			this.modified = copy(modified);
			this.name = name;
			this.email = email;
			this.username = username;
			this.pswd = pswd;
			this.status = status;
			this.roles = roles;
	}

	/*
	@Override
	public UUID getJwtUserId() { return pk.getUUID(); }

	@Override
	public String[] getJwtUserRoles() {
		return isNull(roles) ? new String[0] : roles.toArray(new String[0]);
	}
	*/

	@Override
	public UUIDKey getPk() { return pk; }

	public UUID getUid() {
		return pk.getUUID();
	}

	public Date getCreated() {
		return copy(created);
	}

	public Date getModified() {
		return copy(modified);
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getUsername() {
		return username;
	}

	public String getPswd() {
		return pswd;
	}

	public String getStatus() {
		return status;
	}

	public Set<String> getRoles() {
		return roles;
	}

}