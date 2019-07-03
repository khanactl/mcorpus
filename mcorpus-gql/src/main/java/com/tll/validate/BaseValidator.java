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
   * @return The presentation-worthy name of the entity type being validated.
   *         <p>
   *         This name is used, if present, when constructing validation error messages.
   */
  protected abstract String getEntityTypeName();
  
  @Override
  public final VldtnResult validate(final T e) {
    return validate(e, this::validate);
  }

  @Override
  public final VldtnResult validateForAdd(final T e) {
    return validate(e, this::validateForAdd);
  }

  @Override
  public final VldtnResult validateForUpdate(final T e) {
    return validate(e, this::validateForUpdate);
  }

  /**
   * Validate the given entity for a backend fetch/query op or 
   * as a way to validate an input argument type for a backend operation.
   * <p>
   * Concrete sub-classes may elect to implment or not 
   * where the default behavior throws {@link UnsupportedOperationException}.
   * 
   * @param e the entity to validate
   * @param vldtn the validation builder to use to validate
   */
  protected void validate(final VldtnBuilder<T> vldtn) {
    throw new UnsupportedOperationException();
  }

  /**
   * Validate the given entity for a backend add/insert op.
   * <p>
   * Concrete sub-classes may elect to implment or not 
   * where the default behavior throws {@link UnsupportedOperationException}.
   
   * @param vldtn the validation builder to use to validate
   */
  protected void validateForAdd(final VldtnBuilder<T> vldtn) {
    throw new UnsupportedOperationException();
  }

  /**
   * Validate the given entity instance for a backend update op.
   * <p>
   * Concrete sub-classes may elect to implment or not 
   * where the default behavior throws {@link UnsupportedOperationException}.
   * 
   * @param vldtn the validation builder to use to validate
   */
  protected void validateForUpdate(final VldtnBuilder<T> vldtn) {
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
   * @return Newly created {@link VldtnResult} instance conveying the validation results.
   */
  protected VldtnResult validate(final T e, final Consumer<VldtnBuilder<T>> vfunc) {
    try {
      final VldtnBuilder<T> vldtn = new VldtnBuilder<>(getValidationMsgsRootName(), e, getEntityTypeName());
      vfunc.accept(vldtn); // do validation
      return new VldtnResult(vldtn.getErrors());
    } catch(Exception ex) {
      final String vmsg = String.format("Validation processing error ('%s').", ex.getMessage());
      return new VldtnResult(vmsg);
    }
  }
}