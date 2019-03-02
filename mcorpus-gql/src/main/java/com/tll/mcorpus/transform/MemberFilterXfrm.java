package com.tll.mcorpus.transform;

import static com.tll.core.Util.asString;
import static com.tll.core.Util.isBlank;
import static com.tll.core.Util.isNotNullOrEmpty;
import static com.tll.mcorpus.db.Tables.MEMBER;
import static com.tll.mcorpus.transform.MemberXfrm.locationFromString;
import static java.util.Collections.singletonList;
import static org.jooq.impl.DSL.not;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.dmodel.MemberSearch;
import com.tll.mcorpus.gmodel.MemberFilter;
import com.tll.mcorpus.gmodel.MemberFilter.DatePredicate;
import com.tll.mcorpus.gmodel.MemberFilter.DatePredicate.DateOp;
import com.tll.mcorpus.gmodel.MemberFilter.LocationPredicate;
import com.tll.mcorpus.gmodel.MemberFilter.OrderBy;
import com.tll.mcorpus.gmodel.MemberFilter.OrderBy.OrderByClause;
import com.tll.mcorpus.gmodel.MemberFilter.StringPredicate;
import com.tll.mcorpus.gmodel.MemberFilter.StringPredicate.Operation;
import com.tll.mcorpus.transformapi.BaseTransformer;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SortField;

public class MemberFilterXfrm extends BaseTransformer<MemberFilter, MemberSearch> {

  private static DatePredicate datePredicateFromGraphQLMap(final Map<String, Object> gqlMap) {
    DatePredicate dp = null;
    if(gqlMap != null && !gqlMap.isEmpty()) {
      dp = new DatePredicate();
      for(final Entry<String, Object> entry : gqlMap.entrySet()) {
        String key = entry.getKey();
        switch(key) {
          case "dateOp":
            dp.setDateOp(DateOp.valueOf((String) entry.getValue()));
            break;
          case "a":
            dp.setA((Date) entry.getValue());
            break;
          case "b":
            dp.setB((Date) entry.getValue());
            break;
        }
      }
    }
    return dp;
  }

  private static StringPredicate stringPredicatefromGraphQLMap(final Map<String, Object> gqlMap) {
    StringPredicate tp = null;
    if(gqlMap != null && !gqlMap.isEmpty()) {
      tp = new StringPredicate();
      for(final Entry<String, Object> entry : gqlMap.entrySet()) {
        String key = entry.getKey();
        switch(key) {
          case "value":
            tp.setValue((String) entry.getValue());
            break;
          case "ignoreCase":
            tp.setIgnoreCase((Boolean) entry.getValue());
            break;
          case "operation":
            tp.setOperation(Operation.valueOf(asString(entry.getValue())));
            break;
        }
      }
    }
    return tp;
  }

  private static LocationPredicate locationPredicatefromGraphQLMap(final Map<String, Object> gqlMap) {
    LocationPredicate lp = null;
    if(gqlMap !=  null && !gqlMap.isEmpty()) {
      lp = new LocationPredicate();
      for(final Entry<String, Object> entry : gqlMap.entrySet()) {
        String key = entry.getKey();
        switch (key) {
          case "locations":
            @SuppressWarnings("unchecked") List<String> sloclist = (List<String>) entry.getValue();
            if(sloclist != null && !sloclist.isEmpty()) {
             for(final String sloc : sloclist) lp.addLocation(sloc);
            }
            break;
          case "negate":
            lp.setNegate(Boolean.valueOf(asString(entry.getValue())).booleanValue());
            break;
        }
      }
    }
    return lp;
  }

  private static OrderBy orderByFromGraphQLMap(final Map<String, Object> gqlMap) {
    OrderBy ob = null;
    if(gqlMap != null && !gqlMap.isEmpty()) {
      ob = new OrderBy();
      for(final Entry<String, Object> entry : gqlMap.entrySet()) {
        String key = entry.getKey();
        switch(key) {
          case "value":
            ob.token = (String) entry.getValue();
            break;
          case "direction":
            ob.direction = OrderByClause.valueOf((String) entry.getValue());
            break;
        }
      }
    }
    return ob;
  }

  private static Condition[] asJooqCondition(final MemberFilter mf) {
    if(!mf.isSet()) return new Condition[0];
    final List<Condition> conditions = new ArrayList<>(8);
    if(mf.hasCreated()) conditions.add(datePredicateAsJooqCondition(mf.getCreated(), MEMBER.CREATED));
    if(mf.hasModified()) conditions.add(datePredicateAsJooqCondition(mf.getCreated(), MEMBER.MODIFIED));
    if(mf.hasEmpId()) conditions.add(stringPredicateAsJooqCondition(mf.getEmpId(), MEMBER.EMP_ID));
    if(mf.hasLocation()) conditions.add(locationPredicateAsJooqCondition(mf.getLocation(), MEMBER.LOCATION));
    if(mf.hasNameFirst()) conditions.add(stringPredicateAsJooqCondition(mf.getNameFirst(), MEMBER.NAME_FIRST));
    if(mf.hasNameMiddle()) conditions.add(stringPredicateAsJooqCondition(mf.getNameMiddle(), MEMBER.NAME_MIDDLE));
    if(mf.hasNameLast()) conditions.add(stringPredicateAsJooqCondition(mf.getNameLast(), MEMBER.NAME_LAST));
    if(mf.hasDisplayName()) conditions.add(stringPredicateAsJooqCondition(mf.getDisplayName(), MEMBER.DISPLAY_NAME));
    return conditions.toArray(new Condition[conditions.size()]);
  }

  private static SortField<?>[] generateJooqSortFields(final MemberFilter mf) {
    final List<OrderBy> orderBys = mf.getOrderByList();
    return orderBys == null || orderBys.isEmpty() ? defaultJooqSorting : generateJooqSortFields(orderBys);
  }

  private static Condition datePredicateAsJooqCondition(final DatePredicate dp, final Field<Timestamp> f) {
    final Condition c;
    switch(dp.getDateOp()) {
      default:
      case EQUAL_TO:
        c = f.eq(new Timestamp(dp.getA().getTime()));
        break;
      case NOT_EQUAL_TO:
        c = f.notEqual(new Timestamp(dp.getA().getTime()));
        break;
      case LESS_THAN:
        c = f.lessThan(new Timestamp(dp.getA().getTime()));
        break;
      case NOT_LESS_THAN:
        c = not(f.lessThan(new Timestamp(dp.getA().getTime())));
        break;
      case LESS_THAN_OR_EQUAL_TO:
        c = f.lessOrEqual(new Timestamp(dp.getA().getTime()));
        break;
      case NOT_LESS_THAN_OR_EQUAL_TO:
        c = not(f.lessOrEqual(new Timestamp(dp.getA().getTime())));
        break;
      case GREATER_THAN:
        c = f.greaterThan(new Timestamp(dp.getA().getTime()));
        break;
      case NOT_GREATER_THAN:
        c = not(f.greaterThan(new Timestamp(dp.getA().getTime())));
        break;
      case GREATER_THAN_OR_EQUAL_TO:
        c = f.greaterOrEqual(new Timestamp(dp.getA().getTime()));
        break;
      case NOT_GREATER_THAN_OR_EQUAL_TO:
        c = not(f.greaterOrEqual(new Timestamp(dp.getA().getTime())));
        break;
      case BETWEEN:
        c = f.between(new Timestamp(dp.getA().getTime()), new Timestamp(dp.getB().getTime()));
        break;
      case NOT_BETWEEN:
        c = not(f.between(new Timestamp(dp.getA().getTime()), new Timestamp(dp.getB().getTime())));
        break;
    }
    return c;
  }

  private static Condition stringPredicateAsJooqCondition(final StringPredicate sp, final Field<String> f) {
    final Condition c;
    switch(sp.getOperation()) {
      default:
      case EQUALS:
        c = sp.isIgnoreCase() ? f.equalIgnoreCase(sqlEqualsStatement(sp.getValue())) : f.eq(sqlEqualsStatement(sp.getValue()));
        break;
      case LIKE:
        c = sp.isIgnoreCase() ? f.likeIgnoreCase(sqlLikeStatement(sp.getValue())) : f.like(sqlLikeStatement(sp.getValue()));
        break;
    }
    return c;
  }

  private static String sqlEqualsStatement(final String value) {
    return value.replaceAll("[\\*|%]", "");
  }

  private static String sqlLikeStatement(final String value) {
    return value.replaceAll("\\*", "%");
  }

  private static Condition locationPredicateAsJooqCondition(final LocationPredicate lp, final Field<Location> f) {
    final List<Location> dlocations = lp.getLocations().stream().map(sloc -> locationFromString(sloc)).collect(Collectors.toList());
    return lp.isNegate() ? not(f.in(dlocations)) : f.in(dlocations);
  }

  /**
   * Given a list of 'native' {@link OrderBy} elements,
   * generate the complimenting JooQ {@link SortField} array.
   *
   * @param obl the native order by list
   * @return Array of JOOQ {@link SortField} objects.
   */
  private static SortField<?>[] generateJooqSortFields(final List<OrderBy> obl) {
    final List<SortField<?>> jlist = new ArrayList<>(obl.size());
    for(final OrderBy orderBy : obl) {
      switch (orderBy.getToken()) {
        case "created":
          jlist.add(orderBy.asc() ? MEMBER.CREATED.asc() : MEMBER.CREATED.desc());
          break;
        case "modified":
          jlist.add(orderBy.asc() ? MEMBER.MODIFIED.asc() : MEMBER.MODIFIED.desc());
          break;
        case "empId":
          jlist.add(orderBy.asc() ? MEMBER.EMP_ID.asc() : MEMBER.EMP_ID.desc());
          break;
        case "location":
          jlist.add(orderBy.asc() ? MEMBER.LOCATION.asc() : MEMBER.LOCATION.desc());
          break;
        case "nameFirst":
          jlist.add(orderBy.asc() ? MEMBER.NAME_FIRST.asc() : MEMBER.NAME_FIRST.desc());
          break;
        case "nameMiddle":
          jlist.add(orderBy.asc() ? MEMBER.NAME_MIDDLE.asc() : MEMBER.NAME_MIDDLE.desc());
          break;
        case "nameLast":
          jlist.add(orderBy.asc() ? MEMBER.NAME_LAST.asc() : MEMBER.NAME_LAST.desc());
          break;
        case "displayName":
          jlist.add(orderBy.asc() ? MEMBER.DISPLAY_NAME.asc() : MEMBER.DISPLAY_NAME.desc());
          break;
      }
    }
    return jlist.toArray(new SortField[jlist.size()]);
  }

  /**
   * @return the default member search result ordering for Jooq.
   */
  public static SortField<?>[] getDefaultJooqSortFields() {
    // keep internal array safe from mutations by returning a copy
    final int len = defaultJooqSorting.length;
    final SortField<?>[] rval = new SortField[len]; 
    System.arraycopy(defaultJooqSorting, 0, rval, 0, len);
    return rval;
  }

  private static final SortField<?>[] defaultJooqSorting;

  static {
    defaultJooqSorting = generateJooqSortFields(singletonList(new OrderBy("created", OrderBy.OrderByClause.DESCENDING)));
  }

  @Override @SuppressWarnings("unchecked")
  protected MemberFilter fromNotEmptyGraphQLMap(Map<String, Object> gqlMap) {
    MemberFilter mf = null;
    if(isNotNullOrEmpty(gqlMap)) {
      mf = new MemberFilter();
      for(Entry<String, Object> entry : gqlMap.entrySet()) {
        String key = entry.getKey();
        if(!isBlank(key)) {
          Map<String, Object> submap;
          List<Map<String, Object>> sublist;
          switch(key) {
            case "created":
              submap = (Map<String, Object>) entry.getValue();
              mf.setCreated(datePredicateFromGraphQLMap(submap));
              break;
            case "modified":
              submap = (Map<String, Object>) entry.getValue();
              mf.setModified(datePredicateFromGraphQLMap(submap));
              break;
            case "empId":
              submap = (Map<String, Object>) entry.getValue();
              mf.setEmpId(stringPredicatefromGraphQLMap(submap));
              break;
            case "location":
              submap = (Map<String, Object>) entry.getValue();
              mf.setLocation(locationPredicatefromGraphQLMap(submap));
              break;
            case "nameFirst":
              submap = (Map<String, Object>) entry.getValue();
              mf.setNameFirst(stringPredicatefromGraphQLMap(submap));
              break;
            case "nameMiddle":
              submap = (Map<String, Object>) entry.getValue();
              mf.setNameMiddle(stringPredicatefromGraphQLMap(submap));
              break;
            case "nameLast":
              submap = (Map<String, Object>) entry.getValue();
              mf.setNameLast(stringPredicatefromGraphQLMap(submap));
              break;
            case "displayName":
              submap = (Map<String, Object>) entry.getValue();
              mf.setDisplayName(stringPredicatefromGraphQLMap(submap));
              break;
            case "orderByList":
              sublist = (List<Map<String, Object>>) gqlMap.get(key);
              if(sublist != null && !sublist.isEmpty()) {
                for(final Map<String, Object> sublistmap : sublist) {
                  OrderBy ob = orderByFromGraphQLMap(sublistmap);
                  if(ob != null) mf.addOrderBy(ob);
                }
              }
          }
        }
      }
    }
    return mf;
  }

  @Override
  protected MemberSearch toBackendFromNonNull(MemberFilter e) {
    return new MemberSearch(
      asJooqCondition(e), 
      generateJooqSortFields(e) 
    );
  }

}