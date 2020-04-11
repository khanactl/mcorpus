package com.tll.mcorpus.gmodel;

import java.util.UUID;

import com.tll.gmodel.BaseEntity;
import com.tll.gmodel.UUIDKey;

public class Mref extends BaseEntity<Mref, UUIDKey> {

  public final UUIDKey memberPk;
  public final String empId;
  public final String location;

  public Mref(UUID mid, String empId, String location) {
    this.memberPk = new UUIDKey(mid, "mref");
    this.empId = empId;
    this.location = location;
  }

  @Override
  public UUIDKey getPk() { return memberPk; }

  @Override
  public String toString() {
    return String.format("Mref[key: %s, empId: %s, location: %s]", memberPk.refToken(), empId, location);
  }
}