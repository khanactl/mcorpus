package com.tll.mcorpus.validateapi;

import static com.tll.core.Util.asString;

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

  public static VldtnErr verr(String vmsg, String fname, Object fval, Class<?> etype) {
    return new VldtnErr(vmsg, fname, fval, etype);
  }

  private final String vmsg;
  private final String fname;
  private final Object fval;
  private final Class<?> etype;

  /**
   * Constructor.
   * 
   * @param vmsg the UI display-ready validation error message
   * @param fname the name of the invalid entity field
   * @param fval optional field value
   * @param etype optional parent entity type
   */
  private VldtnErr(final String vmsg, final String fname, final Object fval, final Class<?> etype) {
    this.vmsg = vmsg;
    this.fname = fname;
    this.fval = fval;
    this.etype = etype;
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
  public Class<?> etype() { return etype; }

  @Override
  public String toString() {
    return String.format("VldtnErr[vmsg: %s, fname: %s, fval: '%s', etype: %s]", 
      vmsg, fname, asString(fval), etype == null ? "null" : etype.getSimpleName() );
  }
}