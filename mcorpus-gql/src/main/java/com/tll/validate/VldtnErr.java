package com.tll.validate;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotNullOrEmpty;

/**
 * A single validation error.
 * 
 * @author jpk
 */
public class VldtnErr {

  public static VldtnErr verr(String vmsg) {
    return verr(vmsg, null, null);
  }

  public static VldtnErr verr(String vmsg, String fname) {
    return verr(vmsg, fname, null);
  }

  public static VldtnErr verr(String vmsg, String fname, String etype) {
    return new VldtnErr(vmsg, fname, etype);
  }

  private final String vmsg;
  private final String fname;
  private final String etype;

  /**
   * Constructor.
   * 
   * @param vmsg the UI display-ready validation error message
   * @param fname the name of the invalid entity field
   * @param fval optional field value
   * @param etype optional parent entity type
   */
  private VldtnErr(final String vmsg, final String fname, final String etype) {
    this.vmsg = clean(vmsg);
    this.fname = clean(fname);
    this.etype = clean(etype);
  }

  /**
   * @return the validation error message
   */
  public String getVldtnErrMsg() { return vmsg; }

  /**
   * @return the entity field name in error which may be null.
   */
  public String getFieldName() { return fname; }

  /**
   * The parent entity {@link Class} ref to convey the entity type in error
   */
  public String etype() { return etype; }

  /**
   * @return A presentation-worthy validation error message conveying all relevant
   *         aspects of the error.
   *         <p>
   *         Possible display formats: 
   * <pre>
   * etype.fname: vmsg
   * fname: vmsg
   * fname
   * vmsg
   * </pre>
   */
  public String formalErrMsg() {
    final boolean hasEType = isNotNullOrEmpty(etype);
    final boolean hasField = isNotNullOrEmpty(fname);
    final boolean hasVmsg = isNotNullOrEmpty(vmsg);
    final boolean hasAll = hasEType && hasField && hasVmsg;

    if(hasAll) {
      return String.format("%s.%s: %s", etype, fname, vmsg);
    }
    else if(hasField && hasVmsg) {
      return String.format("%s: %s", fname, vmsg);
    }
    else if(hasField) {
      return String.format("%s", fname);
    }
    else if(hasVmsg) {
      return String.format("%s", vmsg);
    }

    // default
    return "";
  }

  @Override
  public String toString() {
    return String.format("VldtnErr[vmsg: %s, fname: %s, etype: %s]", 
      vmsg, fname, etype == null ? "null" : etype );
  }
}