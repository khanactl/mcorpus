package com.tll.validate;

import java.util.function.Consumer;

/**
 * Base validation class intended for all concrete {@link IValidator} implementations.
 * 
 * @param <T> the object type under validation
 * 
 * @author jpk
 */
public abstract class BaseValidator<T> implements IValidator<T> {

  /**
   * @return the root name of the validation resource bundle property file.
   */
  protected abstract String getValidationMsgsRootName();

  /**
   * Are there any updatable fields present?
   * <p>
   * This is used to assess whether or not we have at least one field to update to
   * ensure we have a valid update operation to pass on.
   * 
   * @param e the entity under validation
   * @return true if at least one updatable field value is present, false otherwise
   */
  protected abstract boolean hasAnyUpdatableFields(final T e);

  /**
   * @return the validation message key for the case of 
   *         'no (non-pk) update fields present for updating'.
   */
  protected abstract String getVmkForNoUpdateFieldsPresent();

  @Override
  public final VldtnResult validate(final T e) {
    return validate(e, this::validate, VldtnOp.INPUT);
  }

  @Override
  public final VldtnResult validateForAdd(final T e) {
    return validate(e, this::validateForAdd, VldtnOp.ADD);
  }

  @Override
  public final VldtnResult validateForUpdate(final T e) {
    return validate(e, this::validateForUpdate, VldtnOp.UPDATE);
  }

  private void validate(final VldtnBuilder<T> vbldr) {
    doValidate(vbldr);
  }

  /**
   * Validate the given entity for a backend fetch/query op or 
   * as a way to validate an input argument type for a backend operation.
   * <p>
   * Concrete sub-classes may elect to implment or not 
   * where the default behavior throws {@link UnsupportedOperationException}.
   * 
   * @param e the entity to validate
   * @param vbldr the validation builder to use to validate
   */
  protected void doValidate(final VldtnBuilder<T> vbldr) {
    throw new UnsupportedOperationException();
  }

  private void validateForAdd(final VldtnBuilder<T> vbldr) {
    doValidateForAdd(vbldr);
  }

  /**
   * Validate the given entity for a backend add/insert op.
   * <p>
   * Concrete sub-classes may elect to implment or not 
   * where the default behavior throws {@link UnsupportedOperationException}.
   
   * @param vbldr the validation builder to use to validate
   */
  protected void doValidateForAdd(final VldtnBuilder<T> vbldr) {
    throw new UnsupportedOperationException();
  }

  private void validateForUpdate(final VldtnBuilder<T> vbldr) {
    vbldr.vchk(e -> hasAnyUpdatableFields(e), getVmkForNoUpdateFieldsPresent());
    if(vbldr.isValid()) doValidateForUpdate(vbldr); // only do update validation when a field is present
  }

  /**
   * Validate the given entity instance for a backend update op.
   * <p>
   * Concrete sub-classes may elect to implment or not 
   * where the default behavior throws {@link UnsupportedOperationException}.
   * 
   * @param vbldr the validation builder to use to validate
   */
  protected void doValidateForUpdate(final VldtnBuilder<T> vbldr) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * The sole validation work-horse method.
   * <p>
   * This method instantiates a new {@link VldtnBuilder} 
   * then performs the validation.
   * 
   * @param e the target object to be validated
   * @param vfunc the validation method to call to perform the validation
   * @param vop the explicit validation op (needed for nested object(s) validation case)
   * @return Newly created {@link VldtnResult} instance conveying the validation results.
   */
  protected VldtnResult validate(final T e, final Consumer<VldtnBuilder<T>> vfunc, final VldtnOp vop) {
    try {
      final VldtnBuilder<T> vbldr = new VldtnBuilder<>(getValidationMsgsRootName(), e, getEntityTypeName(), vop);
      vfunc.accept(vbldr); // do validation
      return new VldtnResult(vbldr.getErrors());
    } catch(Exception ex) {
      final String vmsg = String.format("Validation processing error ('%s').", ex.getMessage());
      return new VldtnResult(vmsg);
    }
  }
}