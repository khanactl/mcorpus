package com.tll.mcorpus.repo.model;

import org.jooq.Condition;
import org.jooq.SortField;

/**
 * Needed methods for accommodating an object hierarchy of search filters on top of a JOOQ SQL API.
 *
 * @author jpk
 */
public interface IFetchFilter {

  /**
   * @return Array of Jooq {@link Condition} elements that
   *         logically match the current state of this fetch filter object.
   */
  Condition[] asJooqCondition();

  /**
   * @return Jooq-native array of {@link SortField} elements
   *         indicating the desired fetch filter results list ordering.
   */
  SortField<?>[] generateJooqSortFields();
}
