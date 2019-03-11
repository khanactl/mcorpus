package com.tll.mcorpus.gmodel;

import static com.tll.core.Util.*;

import com.tll.gmodel.IKey;

/**
 * Emp Id and Location Business Key.
 * 
 * @author jpk
 */
public class EmpIdAndLocationKey implements IKey {

  private final String empId;
  private final String location;

  public EmpIdAndLocationKey(String empId, String location) {
    this.empId = clean(empId);
    this.location = clean(location);
  }

  public String empId() { return empId; }

  public String location() { return location; }
  
  @Override
  public boolean isSet() {
    return isNotNullOrEmpty(empId) && isNotNullOrEmpty(location);
  }

  @Override
  public String refToken() {
    return String.format("empIdAndLoc[empId: %s, location: %s]", empId, location);
  }

}