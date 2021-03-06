package com.tll.validate;

import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.not;
import static com.tll.validate.VldtnErr.verr;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
   * @return true when at least one error exists, false otherwise.
   */
  public boolean hasErrors() { return not(errors.isEmpty()); }

  /**
   * @return Never-null set of errors which may be empty.
   */
  public Set<VldtnErr> getErrors() { return errors; }

  /**
   * @return the number of held validation errors which may be zero.
   */
  public int getNumErrors() { return errors.size(); }

  /**
   * @return A presentation-worthy validation summary message.
   */
  public String getSummaryMsg() {
    return String.format("%d validation error%s.", 
      getNumErrors(), 
      getNumErrors() == 1 ? "" : "s"
    );
  }

  /**
   * @return map of field validation messages keyed by {@link VldtnErr#getFieldPath}.
   */
  public Map<String, String> getMappedFieldErrors() {
    return errors.stream().collect(Collectors.toMap(
            ve -> ve.getFieldPath(), 
            ve -> ve.getVldtnErrMsg()) );
  }

  @Override
  public String toString() { return String.format("VldtnResult[isValid: %b, numErrors: %d", isValid(), getNumErrors()); }
}