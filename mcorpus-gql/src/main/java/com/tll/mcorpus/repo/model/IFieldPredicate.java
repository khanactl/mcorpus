package com.tll.mcorpus.repo.model;

import org.jooq.Condition;
import org.jooq.Field;

/**
 * A local field predicate definition.
 *
 * @param <T> the type being acted on by the predicate
 */
public interface IFieldPredicate<T> {

  /**
   * Create a Jooq Condition from the held state of this {@link IFieldPredicate} implementation.
   *
   * @param f the Jooq field ref
   * @return newly created Jooq {@link Condition} object mirroring the constraint logic
   *         currently held in this object.
   */
  Condition asJooqCondition(final Field<T> f);
}
