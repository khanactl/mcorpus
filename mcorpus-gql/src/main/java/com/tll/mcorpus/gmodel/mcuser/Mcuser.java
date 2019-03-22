package com.tll.mcorpus.gmodel.mcuser;

import static com.tll.core.Util.copy;
import static com.tll.core.Util.isNull;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.tll.gmodel.BaseEntity;
import com.tll.gmodel.IKey;
import com.tll.jwt.IJwtUser;

public class Mcuser extends BaseEntity<Mcuser, IKey> implements IJwtUser {

  private final IKey         pk;
  private final UUID         uid;
  private final Date         created;
  private final Date         modified;
  private final String       name;
  private final String       email;
  private final String       username;
  private final String       pswd;
  private final String       status;
  private final Set<String>  roles;

  public Mcuser(
      UUID         uid,
      Date         created,
      Date         modified,
      String       name,
      String       email,
      String       username,
      String       pswd,
      String       status,
      Set<String>  roles
  ) {
      this.pk = IKey.uuid("Mcuser", uid);
      this.uid = uid;
      this.created = copy(created);
      this.modified = copy(modified);
      this.name = name;
      this.email = email;
      this.username = username;
      this.pswd = pswd;
      this.status = status;
      this.roles = roles;
  }

  @Override
  public UUID getJwtUserId() { return uid; }

  @Override
  public String[] getJwtUserRoles() {
    return isNull(roles) ? new String[0] : roles.toArray(new String[0]);
  }
  
  @Override
  public IKey getPk() { return pk; }

  public UUID getUid() {
    return uid;
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