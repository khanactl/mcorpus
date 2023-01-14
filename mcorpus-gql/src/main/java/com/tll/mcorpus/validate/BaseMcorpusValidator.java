package com.tll.mcorpus.validate;

import com.tll.validate.BaseValidator;

public abstract class BaseMcorpusValidator<E> extends BaseValidator<E> {

	protected String getValidationMsgsRootName() { return "validate"; }
}