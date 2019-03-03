package com.tll.validate;

import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNotNullOrEmpty;
import static com.tll.validate.VldtnErr.verr;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

  private final Set<VldtnErr> errs;

  private final ResourceBundle vmsgBundle;

  /**
   * Constructor.
   * 
   * @param vldtnBundleName the ROOT name of the validation messages property file (resource bundle root name)
   * @param entity the entity instance to be validated
   */
  public VldtnBuilder(final String vldtnBundleName, final E entity) {
    this.vmsgBundle = ResourceBundle.getBundle(vldtnBundleName);
    this.entity = entity;
    this.errs = new HashSet<>();
  }

  /**
   * @return the entity instance under validation 
   *         by way of this {@link VldtnBuilder} instance.
   */
  public E getTarget() { return entity; }

  /**
   * Validation check for a required entity field.
   * <p>
   * If a field is NULL the field is considered invalid.<br>
   * If a field is NON-NULL the field IS validated.
   * 
   * @param p the validation predicate
   * @param fval the entity accessor method ref for the field under validation
   * @param vmk the validation message key used when the validation fails
   * @param fname the entity field name used when the validation fails
   */
  public <T> VldtnBuilder<E> vrqd(Predicate<T> p, Function<E, T> fval, String vmk, String fname) {
    final T fv = fval.apply(entity);
    if(!p.test(fv)) {
      vaddErr(vmk, fname, fv);
    }
    return this;
  }

  /**
   * Validation check for an optional entity field.
   * <p>
   * If a field is NON-NULL it IS validated.<br>
   * If a field is NULL it IS NOT validated.
   * 
   * @param p the validation check
   * @param fval the entity accessor method ref for the field under validation
   * @param vmk the validation message key used to obtain the validation error message 
   *            when the validation check (param <code>p</code>) fails.
   * @param fname the entity field name used when the validation fails
   */
  public <T> VldtnBuilder<E> vopt(Predicate<T> p, Function<E, T> fval, String vmk, String fname) {
    final T fv = fval.apply(entity);
    if(isNotNull(fv) && !p.test(fv)) {
      vaddErr(vmk, fname, fv);
    }
    return this;
  }

  /**
   * Validation check for a string type entity field.
   * <p>
   * If the string is NON-NULL and NOT-EMPTY it IS validated.<br>
   * If the string is NULL or EMPTY it IS NOT validated.
   * 
   * @param p the validation check
   * @param fval the entity accessor method ref for the field under validation
   * @param vmk the validation message key used to obtain the validation error message 
   *            when the validation check (param <code>p</code>) fails.
   * @param fname the entity field name used when the validation fails
   */
  public VldtnBuilder<E> vtok(Predicate<String> p, Function<E, String> fval, String vmk, String fname) {
    final String fv = fval.apply(entity);
    if(isNotNullOrEmpty(fv) && !p.test(fv)) {
      vaddErr(vmk, fname, fv);
    }
    return this;
  }

  /**
   * Validation check when a given condition is met (validate when..).
   * 
   * @param c the condition for which the validation <code>p</code> check is run
   * @param p the validation check
   * @param fval the entity accessor method ref for the field under validation
   * @param vmk the validation message key used to obtain the validation error message 
   *            when the validation check (param <code>p</code>) fails.
   * @param fname the entity field name used when the validation fails
   */
  public <T> VldtnBuilder<E> vwhn(Supplier<Boolean> c, Predicate<T> p, Function<E, T> fval, String vmk, String fname) {
    if(c.get() == Boolean.TRUE) {
      final T fv = fval.apply(entity);
      if(isNotNull(fv) && !p.test(fv)) {
        vaddErr(vmk, fname, fv);
      }
    }
    return this;
  }

  /**
   * @return true when no validation errors are present.
   */
  public boolean isValid() { return errs.isEmpty(); }

  /**
   * @return Never-null set of validation errors which may be empty.
   */
  public Set<VldtnErr> getErrors() { return errs; }

  /**
   * Add a single error.
   * <p>
   * The held resource bundle is accessed to resolve the validation error message 
   * from the given validation messaage key <code>vmk</code>.
   * 
   * @param vmk the required validation message key
   * @param fname the optional name of the field in error
   * @param fval the optional value of the field in error
   */
  protected <T> void vaddErr(String vmk, String fname, T fval) {
    final String vmsg = vmsgBundle.getString(vmk);
    errs.add(verr(vmsg, fname, fval, entity.getClass()));
  }
}