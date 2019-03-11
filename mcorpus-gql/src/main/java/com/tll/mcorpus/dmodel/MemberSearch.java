package com.tll.mcorpus.dmodel;

import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.not;

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
  public final int offset;
  public final int limit;

  public MemberSearch(final Condition[] conditions, final SortField<?>[] orderBys, int offset, int limit) {
    this.conditions = conditions;
    this.orderBys = orderBys;
    this.offset = offset;
    this.limit = limit;
  }

  public boolean hasSearchConditions() { return not(isNullOrEmpty(conditions)); }
}