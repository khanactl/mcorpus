package com.tll.mcorpus.repo.model;

import com.google.common.base.MoreObjects;
import com.tll.mcorpus.db.enums.Location;

import java.util.Objects;
import java.util.UUID;

public class MemberRef implements IMemberKey {

  private final UUID mid;
  private final String empId;
  private final Location location;

  /**
   * Constructor.
   *
   * @param mid the unique member Primary Key (PK) UUID token
   * @param empId the member emp id
   * @param location the member location
   */
  public MemberRef(UUID mid, String empId, Location location) {
    this.mid = mid;
    this.empId = empId;
    this.location = location;
  }

  public boolean hasMid() {
    return mid != null;
  }

  public UUID getMid() {
    return mid;
  }

  @Override
  public String getEmpId() {
    return empId;
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public boolean isSet() {
    return hasMid() || (empId != null && location != null);
  }

  @Override
  public String refToken() {
    return String.format("mid: %s, empId: %s, loc: %s", mid, empId, location);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MemberRef memberRef = (MemberRef) o;
    return Objects.equals(mid, memberRef.mid) &&
      Objects.equals(empId, memberRef.empId) &&
      location == memberRef.location;
  }

  @Override
  public int hashCode() {
    return Objects.hash(mid, empId, location);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("mid", mid)
      .add("empId", empId)
      .add("location", location)
      .toString();
  }
}
