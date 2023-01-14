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
	 * Enum-ification of the 3 declared validation use-cases.
	 */
	public static enum VldtnOp { 
		/**
		 * Corresponds to {@link #validate} case.
		 */
		INPUT, 
		/**
		 * Corresponds to {@link #validateForAdd} case.
		 */
		ADD, 
		/**
		 * Corresponds to {@link #validateForUpdate} case.
		 */
		UPDATE; 
	}

	/**
	 * @return The presentation-worthy name of the entity type being validated.
	 *				 <p>
	 *				 This name is used, if present, when constructing validation error messages.
	 */
	String getEntityTypeName();
	
	/**
	 * Validate an entity or supporting object type 
	 * for a backend fetch/query operation 
	 * or, more generally, as a way to validate a required input argument 
	 * for a backend operation.
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