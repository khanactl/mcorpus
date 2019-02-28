package com.tll.mcorpus.gmodel.mcuser;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.tll.mcorpus.gmodel.BaseEntity;
import com.tll.mcorpus.gmodel.IKey;

public class Mcuser extends BaseEntity<Mcuser, IKey> {

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
      this.created = created;
      this.modified = modified;
      this.name = name;
      this.email = email;
      this.username = username;
      this.pswd = pswd;
      this.status = status;
      this.roles = roles;
  }

  @Override
  public IKey getPk() { return pk; }

  public UUID getUid() {
    return uid;
  }

  public Date getCreated() {
    return created;
  }

  public Date getModified() {
    return modified;
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