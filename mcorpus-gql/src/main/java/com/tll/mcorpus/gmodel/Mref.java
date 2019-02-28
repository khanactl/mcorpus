package com.tll.mcorpus.gmodel;

import static com.tll.mcorpus.Util.uuidToToken;

import java.util.UUID;

public class Mref extends BaseEntity<Mref, IKey> {

  public final IKey memberPk;
  public final UUID mid;
  public final String empId;
  public final String location;

  public Mref(UUID mid, String empId, String location) {
    this.memberPk = IKey.uuid("Mref", mid);
    this.mid = mid;
    this.empId = empId;
    this.location = location;
  }

  @Override
  public IKey getPk() { return memberPk; }

  @Override
  public String toString() {
    return String.format("Mref[mid: %s, empId: %s, location: %s]", uuidToToken(mid), empId, location);
  }
}