package com.tll.validate;

import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNotNullOrEmpty;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.not;
import static com.tll.validate.VldtnErr.verr;

import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.tll.validate.IValidator.VldtnOp;

/**
 * The prescribed way to implmenent entity validation.
 * <p>
 * This class has use-case specific methods for declaring multiple 
 * validation rules/constraints for a single target type.
 * 
 * @author jpk
 */
public class VldtnBuilder<E> {

  private final E entity;
  private final String entityTypeName;
  private final VldtnOp vop;

  private final Set<VldtnErr> errs;

  private final ResourceBundle vmsgBundle;

  /**
   * Constructor.
   * 
   * @param vldtnBundleName the ROOT name of the validation messages property file (resource bundle root name)
   * @param entity the entity instance to be validated
   * @param entityTypeName the name of the entity type
   * @param vop the validation op
   */
  public VldtnBuilder(final String vldtnBundleName, final E entity, String entityTypeName, VldtnOp vop) {
    this.vmsgBundle = ResourceBundle.getBundle(vldtnBundleName);
    this.entity = entity;
    this.entityTypeName = entityTypeName;
    this.vop = vop;
    this.errs = new LinkedHashSet<>();
  }

  /**
   * @return the held entity instance under validation.
   */
  public E getTarget() { return entity; }

  /**
   * Do an entity-level validation check.
   * 
   * @param vldtn the validation check
   * @param vmk the associated validation message key used when the check fails
   * @return this builder instance
   */
  public VldtnBuilder<E> vchk(Predicate<E> vldtn, String vmk) {
    if(not(vldtn.test(entity))) {
      vaddErr(vmk, null);
    }
    return this;
  }

  /**
   * Require a field be non-null.
   * 
   * @param fval the entity accessor method ref for the field under validation
   * @param vmk the validation message key used when the field value is found to be null
   * @param fname the entity field name used when the validation fails
   * @return this builder instance
   */
  public <T> VldtnBuilder<E> vrqd(Function<E, T> fval, String vmk, String fname) {
    if(isNull(fval.apply(entity))) {
      vaddErr(vmk, fname);
    }
    return this;
  }

  /**
   * Validation check for a required entity field.
   * <p>
   * If a field is NULL the field is considered invalid.<br>
   * If a field is NON-NULL the field IS validated.
   * 
   * @param vchk the validation check
   * @param fval the entity accessor method ref for the field under validation
   * @param vmk the validation message key used when the validation fails
   * @param fname the entity field name used when the validation fails
   * @return this builder instance
   */
  public <T> VldtnBuilder<E> vrqd(Predicate<T> vchk, Function<E, T> fval, String vmk, String fname) {
    final T fv = fval.apply(entity);
    if(not(vchk.test(fv))) {
      vaddErr(vmk, fname);
    }
    return this;
  }

  /**
   * Validation check for an optional entity field.
   * <p>
   * If a field is NON-NULL it IS validated.<br>
   * If a field is NULL it IS NOT validated.
   * 
   * @param vchk the validation check
   * @param fval the entity accessor method ref for the field under validation
   * @param vmk the validation message key used when the validation fails
   * @param fname the entity field name used when the validation fails
   */
  public <T> VldtnBuilder<E> vopt(Predicate<T> vchk, Function<E, T> fval, String vmk, String fname) {
    final T fv = fval.apply(entity);
    if(isNotNull(fv) && not(vchk.test(fv))) {
      vaddErr(vmk, fname);
    }
    return this;
  }

  /**
   * Verify a string field value is non-null and non-empty.
   * 
   * @param fval the entity accessor method ref for the field under validation
   * @param vmk the validation message key used when the validation fails
   * @param fname the entity field name used when the validation fails
   */
  public VldtnBuilder<E> vtok(Function<E, String> fval, String vmk, String fname) {
    if(isNullOrEmpty(fval.apply(entity))) {
      vaddErr(vmk, fname);
    }
    return this;
  }

  /**
   * Validation check for a string type entity field.
   * <p>
   * If the string is NON-NULL and NOT-EMPTY it IS validated.<br>
   * If the string is NULL or EMPTY it IS NOT validated.
   * 
   * @param vchk the validation check
   * @param fval the entity accessor method ref for the field under validation
   * @param vmk the validation message key used to obtain the validation error message 
   *            when the validation check (param <code>p</code>) fails.
   * @param fname the entity field name used when the validation fails
   */
  public VldtnBuilder<E> vtok(Predicate<String> vchk, Function<E, String> fval, String vmk, String fname) {
    final String fv = fval.apply(entity);
    if(isNotNullOrEmpty(fv) && not(vchk.test(fv))) {
      vaddErr(vmk, fname);
    }
    return this;
  }

  /**
   * Validation check when a given condition is met (validate when..).
   * 
   * @param c the condition for which the validation <code>vchk</code> is run
   * @param vchk the validation check
   * @param fval the entity accessor method ref for the field under validation
   * @param vmk the validation message key used to obtain the validation error message 
   *            when the validation check (param <code>p</code>) fails.
   * @param fname the entity field name used when the validation fails
   */
  public <T> VldtnBuilder<E> vwhn(Supplier<Boolean> c, Predicate<T> vchk, Function<E, T> fval, String vmk, String fname) {
    if(Boolean.TRUE.equals(c.get())) {
      final T fv = fval.apply(entity);
      if(isNotNull(fv) && not(vchk.test(fv))) {
        vaddErr(vmk, fname);
      }
    }
    return this;
  }

  /**
   * Validate a nested entity.
   * 
   * @param fval the entity accessor method providing the nested entity instance to validate
   * @param fname the nested entity field name used when the validation fails
   * @param nvalidator the nested entity validator that is invoked when the nested entity is non-null.
   */
  public <NE> VldtnBuilder<E> vnested(
    Function<E, NE> fval, 
    String fname, 
    IValidator<NE> nvalidator
  ) {
    final NE nested = fval.apply(entity);
    if(isNotNull(nested)) {
      final VldtnResult vnr;
      switch(vop) {
      default:
      case INPUT:
        vnr = nvalidator.validate(nested);
        break;
      case ADD:
        vnr = nvalidator.validateForAdd(nested);
        break;
      case UPDATE:
        vnr = nvalidator.validateForUpdate(nested);
        break;
      }
      if(vnr.hasErrors()) {
        // transform the validation errors to prepend the parent path
        errs.addAll(
          vnr.getErrors().stream().map(ve -> verr(
            ve.getVldtnErrMsg(), 
            ve.getFieldName(), 
            nvalidator.getEntityTypeName(), 
            fname
          )).collect(Collectors.toSet())
        );
      }
    }
    return this;
  }

  /**
   * @return true when no validation errors are present.
   */
  public boolean isValid() { return errs.isEmpty(); }

  /**
   * @return the current error count.
   */
  public int getNumErrors() { return errs.size(); }

  /**
   * @return Never-null set of validation errors which may be empty.
   */
  public Set<VldtnErr> getErrors() { return errs; }

  /**
   * Add a single entity-level error.
   * <p>
   * The held resource bundle is accessed to resolve the validation error message 
   * from the given validation messaage key <code>vmk</code>.
   * 
   * @param vmk the required validation message key
   */
  protected <T> void vaddErr(String vmk) {
    final String vmsg = vmsgBundle.getString(vmk);
    errs.add(verr(vmsg, null, entityTypeName));
  }

  /**
   * Add a single field-level error.
   * <p>
   * The held resource bundle is accessed to resolve the validation error message 
   * from the given validation messaage key <code>vmk</code>.
   * 
   * @param vmk the required validation message key
   * @param fname the optional name of the field in error
   */
  protected <T> void vaddErr(String vmk, String fname) {
    final String vmsg = vmsgBundle.getString(vmk);
    errs.add(verr(vmsg, fname, entityTypeName));
  }
}