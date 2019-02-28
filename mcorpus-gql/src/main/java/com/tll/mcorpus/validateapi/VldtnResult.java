package com.tll.mcorpus.validateapi;

import static com.tll.mcorpus.validateapi.VldtnErr.verr;
import static com.tll.mcorpus.Util.isNull;
import static com.tll.mcorpus.Util.isNullOrEmpty;

import java.util.Collections;
import java.util.Set;

/**
 * Houses the results of a validation check on a target entity object.
 * 
 * @author jpk
 */
public class VldtnResult {

  /**
   * Constant for a valid validation result.
   */
  public static final VldtnResult VALID = new VldtnResult();

  private final Set<VldtnErr> errors;

  /**
   * Constructor.
   * 
   * @param errMsg the sole error message
   */
  public VldtnResult(final String errMsg) {
    this.errors = isNullOrEmpty(errMsg) ? Collections.emptySet() : Collections.singleton(verr(errMsg));
  }

  /**
   * Constructor.
   * 
   * @param errors the set of validation errors if any
   */
  public VldtnResult(final Set<VldtnErr> errors) {
    this.errors = isNull(errors) ? Collections.emptySet() : errors;
  }

  /**
   * Constructor (no errors).
   */
  private VldtnResult() { this((String) null); }

  /**
   * @return true when no validation errors exist
   */
  public boolean isValid() { return errors.isEmpty(); }

  /**
   * @return Never-null set of errors which may be empty.
   */
  public Set<VldtnErr> getErrors() { return errors; }

  /**
   * @return the number of held validation errors which may be zero.
   */
  public int getNumErrors() { return errors.size(); }

  @Override
  public String toString() { return String.format("VldtnResult[isValid: %b, numErrors: %d", isValid(), getNumErrors()); }
}