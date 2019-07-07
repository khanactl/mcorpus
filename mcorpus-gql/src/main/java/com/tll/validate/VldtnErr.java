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
   * The parent entity type name of the field in error.
   */
  public String getParentType() { return etype; }

  /**
   * @return [{parent type}.]{field name} never null.
   */
  public String getFieldPath() { 
    final boolean hasEType = isNotNullOrEmpty(etype);
    final boolean hasField = isNotNullOrEmpty(fname);
    if(hasEType && hasField) 
      return String.format("%s.%s", etype, fname);
    else if(hasField) 
      return fname;
    return "";
  }

  @Override
  public String toString() {
    return String.format("VldtnErr[vmsg: %s, fname: %s, etype: %s]", 
      vmsg, fname, etype == null ? "null" : etype );
  }
}