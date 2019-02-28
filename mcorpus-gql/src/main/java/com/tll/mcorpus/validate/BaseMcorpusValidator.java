package com.tll.mcorpus.validate;

import com.tll.mcorpus.validateapi.BaseValidator;

public abstract class BaseMcorpusValidator<E> extends BaseValidator<E> {

  protected String getValidationMsgsRootName() { return "validate"; }
}