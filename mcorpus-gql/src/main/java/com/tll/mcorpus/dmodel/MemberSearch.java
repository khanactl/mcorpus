package com.tll.mcorpus.dmodel;

import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;

import org.jooq.Condition;
import org.jooq.SortField;

/**
 * Domain/backend level member search type.
 * 
 * @author jpk
 */
public class MemberSearch {
  public final Condition[] conditions;
  public final SortField<?>[] orderBys;

  public MemberSearch(final Condition[] conditions, final SortField<?>[] orderBys) {
    this.conditions = conditions;
    this.orderBys = orderBys;
  }

  public boolean hasSearchConditions() { return not(isNullOrEmpty(conditions)); }
}