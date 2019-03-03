package com.tll.validate;

/**
 * Contract for validating objects bound for backend persistence.
 * 
 * @param <T> the target type subject to validation.
 * 
 * @author jpk
 */
public interface IValidator<T> {

  /**
   * Validate an entity or supporting object type 
   * for a backend fetch/query operation 
   * or as a way to validate a required input argument for a backend operation.
   * 
   * @return newly created {@link VldtnResult} holding any found validation errors.
   */
  VldtnResult validate(final T e);

  /**
   * Validate an entity for a backend add operation (db insert usually).
   * 
   * @return newly created {@link VldtnResult} holding any found validation errors.
   */
  VldtnResult validateForAdd(final T e);

  /**
   * Validate an entity for a backend update operation.
   * 
   * @return newly created {@link VldtnResult} holding any found validation errors.
   */
  VldtnResult validateForUpdate(final T e);
}