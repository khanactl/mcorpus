package com.tll.validate;

import static com.tll.core.Util.asString;
import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNotNullOrEmpty;

/**
 * A single validation error.
 * 
 * @author jpk
 */
public class VldtnErr {

  public static VldtnErr verr(String vmsg) {
    return verr(vmsg, null, null, null);
  }

  public static VldtnErr verr(String vmsg, String fname) {
    return verr(vmsg, fname, null, null);
  }

  public static VldtnErr verr(String vmsg, String fname, Object fval) {
    return verr(vmsg, fname, fval, null);
  }

  public static VldtnErr verr(String vmsg, String fname, Object fval, String etype) {
    return new VldtnErr(vmsg, fname, fval, etype);
  }

  private final String vmsg;
  private final String fname;
  private final Object fval;
  private final String etype;

  /**
   * Constructor.
   * 
   * @param vmsg the UI display-ready validation error message
   * @param fname the name of the invalid entity field
   * @param fval optional field value
   * @param etype optional parent entity type
   */
  private VldtnErr(final String vmsg, final String fname, final Object fval, final String etype) {
    this.vmsg = clean(vmsg);
    this.fname = clean(fname);
    this.fval = fval;
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
   * @return the entity field value in error which may be null.
   */
  public Object getFieldValue() { return fval; }

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
   * etype.fname(fval): vmsg
   * fname(fval): vmsg
   * fname(fval)
   * vmsg (fval)
   * vmsg
   * </pre>
   */
  public String formalErrMsg() {
    final boolean hasEType = isNotNullOrEmpty(etype);
    final boolean hasFieldValue = isNotNull(fval);
    final boolean hasFieldAndValue = isNotNullOrEmpty(fname) && hasFieldValue;
    final boolean hasVmsg = isNotNullOrEmpty(vmsg);
    final boolean hasAll = hasEType && hasFieldAndValue && hasVmsg;

    if(hasAll) {
      return String.format("%s.%s(%s): %s", etype, fname, fval, vmsg);
    }
    else if(hasFieldAndValue && hasVmsg) {
      return String.format("%s(%s): %s", fname, fval, vmsg);
    }
    else if(hasFieldAndValue) {
      return String.format("%s(%s)", fname, fval);
    }
    else if(hasVmsg && hasFieldValue) {
      return String.format("%s (%s)", vmsg, fval);
    }
    else if(hasVmsg) {
      return String.format("%s", vmsg);
    }

    // default
    return "";
  }

  @Override
  public String toString() {
    return String.format("VldtnErr[vmsg: %s, fname: %s, fval: '%s', etype: %s]", 
      vmsg, fname, asString(fval), etype == null ? "null" : etype );
  }
}