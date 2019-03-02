package com.tll.mcorpus.gmodel;

import java.util.Date;
import java.util.UUID;

import com.tll.mcorpus.gmodelapi.BaseEntity;
import com.tll.mcorpus.gmodelapi.IKey;

/**
 * Member gql entity.
 * 
 * @author jpk
 */
public class Member extends BaseEntity<Member, IKey> {
  
  private final IKey pk;
  private final UUID mid;

  private final Date created;
  private final Date modified;

  private final String empId;
  private final String location;

  private final String nameFirst;
  private final String nameMiddle;
  private final String nameLast;

  private final String displayName;

  private final String status;

  private final Date dob;
  private final String ssn;

  private final String personalEmail;
  private final String workEmail;

  private final String mobilePhone;
  private final String homePhone;
  private final String workPhone;
  
  private final String username;
  private final String pswd;

  public Member(UUID mid, Date created, Date modified, String empId, String location, String nameFirst, String nameMiddle, String nameLast, String displayName, String status, Date dob, String ssn, String personalEmail, String workEmail, String mobilePhone, String homePhone, String workPhone, String username, String pswd) {
    this.pk = IKey.uuid("Member", mid);
    this.mid = mid;
    this.created = created;
    this.modified = modified;
    this.empId = empId;
    this.location = location;
    this.nameFirst = nameFirst;
    this.nameMiddle = nameMiddle;
    this.nameLast = nameLast;
    this.displayName = displayName;
    this.status = status;
    this.dob = dob;
    this.ssn = ssn;
    this.personalEmail = personalEmail;
    this.workEmail = workEmail;
    this.mobilePhone = mobilePhone;
    this.homePhone = homePhone;
    this.workPhone = workPhone;
    this.username = username;
    this.pswd = pswd;
  }

  @Override
  public IKey getPk() { return pk; }

  public UUID getMid() {
    return mid;
  }

  public Date getCreated() {
    return created;
  }

  public Date getModified() {
    return modified;
  }

  public String getEmpId() {
    return this.empId;
  }

  public String getLocation() {
    return this.location;
  }

  public String getNameFirst() {
    return this.nameFirst;
  }

  public String getNameMiddle() {
    return this.nameMiddle;
  }

  public String getNameLast() {
    return this.nameLast;
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public String getStatus() {
    return this.status;
  }

  public Date getDob() {
    return this.dob;
  }

  public String getSsn() {
    return this.ssn;
  }

  public String getPersonalEmail() {
    return this.personalEmail;
  }

  public String getWorkEmail() {
    return this.workEmail;
  }

  public String getMobilePhone() {
    return this.mobilePhone;
  }

  public String getHomePhone() {
    return this.homePhone;
  }

  public String getWorkPhone() {
    return this.workPhone;
  }

  public String getUsername() {
    return this.username;
  }

  public String getPswd() {
    return this.pswd;
  }

}