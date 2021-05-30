package com.tll.mcorpus.transform;

import static com.tll.core.Util.asString;
import static com.tll.core.Util.clean;
import static com.tll.core.Util.isBlank;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNotNullOrEmpty;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.upper;
import static com.tll.mcorpus.db.Tables.MAUTH;
import static com.tll.mcorpus.db.Tables.MEMBER;
import static com.tll.mcorpus.transform.MemberXfrm.locationFromString;
import static com.tll.mcorpus.transform.MemberXfrm.memberStatusFromString;
import static com.tll.transform.TransformUtil.dateToLocalDate;
import static com.tll.transform.TransformUtil.odtFromDate;
import static java.util.Collections.singletonList;
import static org.jooq.impl.DSL.not;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.MemberStatus;
import com.tll.mcorpus.dmodel.MemberSearch;
import com.tll.mcorpus.gmodel.MemberFilter;
import com.tll.mcorpus.gmodel.MemberFilter.DatePredicate;
import com.tll.mcorpus.gmodel.MemberFilter.DatePredicate.DateOp;
import com.tll.mcorpus.gmodel.MemberFilter.LocationPredicate;
import com.tll.mcorpus.gmodel.MemberFilter.OrderBy;
import com.tll.mcorpus.gmodel.MemberFilter.StringPredicate;
import com.tll.mcorpus.gmodel.MemberFilter.StringPredicate.Operation;
import com.tll.transform.BaseTransformer;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SortField;

public class MemberFilterXfrm extends BaseTransformer<MemberFilter, MemberSearch> {

  private static DatePredicate datePredicateFromGraphQLMap(final Map<String, Object> gqlMap) {
    DateOp dop = null;
    Date a = null;
    Date b = null;
    if(gqlMap != null && !gqlMap.isEmpty()) {
      for(final Entry<String, Object> entry : gqlMap.entrySet()) {
        String key = entry.getKey();
        switch(key) {
          case "op":
            dop = DateOp.valueOf(upper(clean((String) entry.getValue())));
            break;
          case "argA":
            a = (Date) entry.getValue();
            break;
          case "argB":
            b = (Date) entry.getValue();
            break;
        }
      }
    }
    return new DatePredicate(dop, a, b);
  }

  private static StringPredicate stringPredicatefromGraphQLMap(final Map<String, Object> gqlMap) {
    String value = null;
    boolean ignoreCase = false;
    Operation opValue = Operation.EQUALS; // default op
    Operation opIsNull = null;
    if(gqlMap != null && !gqlMap.isEmpty()) {
      for(final Entry<String, Object> entry : gqlMap.entrySet()) {
        String key = entry.getKey();
        switch(key) {
          case "isNull":
            Boolean bin = (Boolean) entry.getValue();
            opIsNull = (isNull(bin) || bin.booleanValue()) ? Operation.IS_NULL : Operation.IS_NOT_NULL;
            break;
          case "value":
            final String sval = clean((String) entry.getValue());
            value = sval.replaceAll("[^a-zA-Z|\\d| |\\*|%|_|\\-]", "");
            opValue = sval.matches("^.*?[*|%].*?$") ? Operation.LIKE : Operation.EQUALS;
            break;
          case "ignoreCase":
            Boolean bic = (Boolean) entry.getValue();
            ignoreCase = isNotNull(bic) ? bic.booleanValue() : false;
            break;
        }
      }
    }
    return new StringPredicate(
      isNotNull(opIsNull) ? opIsNull : opValue, // nullness check takes precedence
      isNotNull(opIsNull) ? null : value,
      ignoreCase
    );
  }

  private static LocationPredicate locationPredicatefromGraphQLMap(final Map<String, Object> gqlMap) {
    List<String> locations = null;
    boolean negate = false;
    if(gqlMap !=  null && !gqlMap.isEmpty()) {
      for(final Entry<String, Object> entry : gqlMap.entrySet()) {
        String key = entry.getKey();
        switch (key) {
          case "locations":
            @SuppressWarnings("unchecked")
            final List<String> sloclist = (List<String>) entry.getValue();
            locations = isNotNullOrEmpty(sloclist) ?
              sloclist.stream().filter(e -> isNotNull(e)).map(e -> clean(e)).collect(Collectors.toList())
              : Collections.emptyList();
            break;
          case "negate":
            negate = Boolean.valueOf(asString(entry.getValue())).booleanValue();
            break;
        }
      }
    }
    return new LocationPredicate(locations, negate);
  }

  private static OrderBy.Dir orderByDirFromToken(final String dirtok) {
    return OrderBy.Dir.valueOf(upper(clean(dirtok)));
  }

  private static OrderBy orderByFromToken(final String token) {
    final String[] parts = clean(token).split(" ");
    if(isNotNullOrEmpty(parts)) {
      switch(parts.length) {
        case 1:  // field only
          return new OrderBy(clean(parts[0]));
        case 2:  // field and dir
          return new OrderBy(clean(parts[0]), orderByDirFromToken(parts[1]));
        default: // unhandled
          break;
      }
    }
    // default
    return null;
  }

  private static List<OrderBy> orderByListFromToken(final String orderByToken) {
    return isNotNullOrEmpty(orderByToken) ?
      Arrays.stream(orderByToken.split(","))
        .map(elm -> orderByFromToken(elm))
        .filter(ob -> isNotNull(ob))
        .collect(Collectors.toList())
      : Collections.emptyList();
  }

  private static Condition[] asJooqCondition(final MemberFilter mf) {
    if(!mf.isSet()) return new Condition[0];
    final List<Condition> conditions = new ArrayList<>(8);
    if(mf.hasCreated()) conditions.add(timestampPredicateAsJooqCondition(mf.getCreated(), MEMBER.CREATED));
    if(mf.hasModified()) conditions.add(timestampPredicateAsJooqCondition(mf.getModified(), MEMBER.MODIFIED));
    if(mf.hasEmpId()) conditions.add(stringPredicateAsJooqCondition(mf.getEmpId(), MEMBER.EMP_ID));
    if(mf.hasLocation()) conditions.add(locationPredicateAsJooqCondition(mf.getLocation(), MEMBER.LOCATION));
    if(mf.hasNameFirst()) conditions.add(stringPredicateAsJooqCondition(mf.getNameFirst(), MEMBER.NAME_FIRST));
    if(mf.hasNameMiddle()) conditions.add(stringPredicateAsJooqCondition(mf.getNameMiddle(), MEMBER.NAME_MIDDLE));
    if(mf.hasNameLast()) conditions.add(stringPredicateAsJooqCondition(mf.getNameLast(), MEMBER.NAME_LAST));
    if(mf.hasDisplayName()) conditions.add(stringPredicateAsJooqCondition(mf.getDisplayName(), MEMBER.DISPLAY_NAME));
    if(mf.hasStatus()) conditions.add(memberStatusAsJooqCondition(mf.getStatus(), MEMBER.STATUS));
    if(mf.hasDob()) conditions.add(datePredicateAsJooqCondition(mf.getDob(), MAUTH.DOB));
    if(mf.hasUsername()) conditions.add(stringPredicateAsJooqCondition(mf.getUsername(), MAUTH.USERNAME));

    return conditions.toArray(new Condition[conditions.size()]);
  }

  private static SortField<?>[] generateJooqSortFields(final MemberFilter mf) {
    final List<OrderBy> orderBys = mf.getOrderByList();
    return isNotNullOrEmpty(orderBys) ? generateJooqSortFields(orderBys) : defaultJooqSorting;
  }

  private static Condition datePredicateAsJooqCondition(final DatePredicate dp, final Field<java.time.LocalDate> f) {
    final Condition c;
    switch(dp.getDateOp()) {
      case IS_NULL:
        c = f.isNull();
        break;
      case IS_NOT_NULL:
        c = f.isNotNull();
        break;
      default:
      case EQUAL_TO:
        c = f.eq(dateToLocalDate(dp.getA()));
        break;
      case NOT_EQUAL_TO:
        c = f.notEqual(dateToLocalDate(dp.getA()));
        break;
      case LESS_THAN:
        c = f.lessThan(dateToLocalDate(dp.getA()));
        break;
      case NOT_LESS_THAN:
        c = not(f.lessThan(dateToLocalDate(dp.getA())));
        break;
      case LESS_THAN_OR_EQUAL_TO:
        c = f.lessOrEqual(dateToLocalDate(dp.getA()));
        break;
      case NOT_LESS_THAN_OR_EQUAL_TO:
        c = not(f.lessOrEqual(dateToLocalDate(dp.getA())));
        break;
      case GREATER_THAN:
        c = f.greaterThan(dateToLocalDate(dp.getA()));
        break;
      case NOT_GREATER_THAN:
        c = not(f.greaterThan(dateToLocalDate(dp.getA())));
        break;
      case GREATER_THAN_OR_EQUAL_TO:
        c = f.greaterOrEqual(dateToLocalDate(dp.getA()));
        break;
      case NOT_GREATER_THAN_OR_EQUAL_TO:
        c = not(f.greaterOrEqual(dateToLocalDate(dp.getA())));
        break;
      case BETWEEN:
        c = f.between(dateToLocalDate(dp.getA()), dateToLocalDate(dp.getB()));
        break;
      case NOT_BETWEEN:
        c = not(f.between(dateToLocalDate(dp.getA()), dateToLocalDate(dp.getB())));
        break;
    }
    return c;
  }

  private static Condition timestampPredicateAsJooqCondition(final DatePredicate dp, final Field<OffsetDateTime> f) {
    final Condition c;
    switch(dp.getDateOp()) {
      case IS_NULL:
        c = f.isNull();
        break;
      case IS_NOT_NULL:
        c = f.isNotNull();
        break;
      default:
      case EQUAL_TO:
        c = f.eq(odtFromDate(dp.getA()));
        break;
      case NOT_EQUAL_TO:
        c = f.notEqual(odtFromDate(dp.getA()));
        break;
      case LESS_THAN:
        c = f.lessThan(odtFromDate(dp.getA()));
        break;
      case NOT_LESS_THAN:
        c = not(f.lessThan(odtFromDate(dp.getA())));
        break;
      case LESS_THAN_OR_EQUAL_TO:
        c = f.lessOrEqual(odtFromDate(dp.getA()));
        break;
      case NOT_LESS_THAN_OR_EQUAL_TO:
        c = not(f.lessOrEqual(odtFromDate(dp.getA())));
        break;
      case GREATER_THAN:
        c = f.greaterThan(odtFromDate(dp.getA()));
        break;
      case NOT_GREATER_THAN:
        c = not(f.greaterThan(odtFromDate(dp.getA())));
        break;
      case GREATER_THAN_OR_EQUAL_TO:
        c = f.greaterOrEqual(odtFromDate(dp.getA()));
        break;
      case NOT_GREATER_THAN_OR_EQUAL_TO:
        c = not(f.greaterOrEqual(odtFromDate(dp.getA())));
        break;
      case BETWEEN:
        c = f.between(odtFromDate(dp.getA()), odtFromDate(dp.getB()));
        break;
      case NOT_BETWEEN:
        c = not(f.between(odtFromDate(dp.getA()), odtFromDate(dp.getB())));
        break;
    }
    return c;
  }

  private static Condition stringPredicateAsJooqCondition(final StringPredicate sp, final Field<String> f) {
    final Condition c;
    switch(sp.getOperation()) {
      case IS_NULL:
        c = f.isNull();
        break;
      case IS_NOT_NULL:
        c = f.isNotNull();
        break;
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

  private static Condition memberStatusAsJooqCondition(final MemberStatus status, final Field<MemberStatus> f) {
    return f.eq(status);
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
        case "status":
          jlist.add(orderBy.asc() ? MEMBER.STATUS.asc() : MEMBER.STATUS.desc());
          break;
        case "dob":
          jlist.add(orderBy.asc() ? MAUTH.DOB.asc() : MAUTH.DOB.desc());
          break;
        case "username":
          jlist.add(orderBy.asc() ? MAUTH.USERNAME.asc() : MAUTH.USERNAME.desc());
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
    defaultJooqSorting = generateJooqSortFields(singletonList(new OrderBy("created", OrderBy.Dir.DESC)));
  }

  @Override @SuppressWarnings("unchecked")
  protected MemberFilter fromNotEmptyGraphQLMap(Map<String, Object> gqlMap) {
    MemberFilter mf = null;
    if(isNotNullOrEmpty(gqlMap)) {
      mf = new MemberFilter();
      int offset = 0; // default if not specified
      int limit = 10; // default if not specified
      StringPredicate sp = null;
      LocationPredicate lp = null;
      DatePredicate dp = null;
      MemberStatus status = null;
      List<OrderBy> obl = null;
      for(final Entry<String, Object> entry : gqlMap.entrySet()) {
        String key = entry.getKey();
        if(!isBlank(key)) {
          Map<String, Object> submap;
          switch(key) {
            case "offset":
              offset = isNull(entry.getValue()) ? offset : ((Integer)entry.getValue()).intValue();
              mf.setOffset(offset);
              break;
            case "limit":
              limit = isNull(entry.getValue()) ? limit : ((Integer)entry.getValue()).intValue();
              mf.setLimit(limit);
              break;
            case "created":
              submap = (Map<String, Object>) entry.getValue();
              dp = datePredicateFromGraphQLMap(submap);
              if(isNotNull(dp)) mf.setCreated(dp);
              break;
            case "modified":
              submap = (Map<String, Object>) entry.getValue();
              dp = datePredicateFromGraphQLMap(submap);
              if(isNotNull(dp)) mf.setModified(dp);
              break;
            case "empId":
              submap = (Map<String, Object>) entry.getValue();
              sp = stringPredicatefromGraphQLMap(submap);
              if(isNotNull(sp)) mf.setEmpId(sp);
              break;
            case "location":
              submap = (Map<String, Object>) entry.getValue();
              lp = locationPredicatefromGraphQLMap(submap);
              if(isNotNull(lp)) mf.setLocation(lp);
              break;
            case "nameFirst":
              submap = (Map<String, Object>) entry.getValue();
              sp = stringPredicatefromGraphQLMap(submap);
              if(isNotNull(sp)) mf.setNameFirst(sp);
              break;
            case "nameMiddle":
              submap = (Map<String, Object>) entry.getValue();
              sp = stringPredicatefromGraphQLMap(submap);
              if(isNotNull(sp)) mf.setNameMiddle(sp);
              break;
            case "nameLast":
              submap = (Map<String, Object>) entry.getValue();
              sp = stringPredicatefromGraphQLMap(submap);
              if(isNotNull(sp)) mf.setNameLast(sp);
              break;
            case "displayName":
              submap = (Map<String, Object>) entry.getValue();
              sp = stringPredicatefromGraphQLMap(submap);
              if(isNotNull(sp)) mf.setDisplayName(sp);
              break;
            case "status":
              status = memberStatusFromString((String) gqlMap.get(key));
              if(isNotNull(status)) mf.setStatus(status);
              break;
            case "dob":
              submap = (Map<String, Object>) entry.getValue();
              dp = datePredicateFromGraphQLMap(submap);
              if(isNotNull(dp)) mf.setDob(dp);
              break;
            case "username":
              submap = (Map<String, Object>) entry.getValue();
              sp = stringPredicatefromGraphQLMap(submap);
              if(isNotNull(sp)) mf.setUsername(sp);
              break;
            case "orderBy":
              obl = orderByListFromToken((String) entry.getValue());
              if(isNotNullOrEmpty(obl)) mf.setOrderByList(obl);
              break;
            default:
              break;
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
      generateJooqSortFields(e),
      e.getOffset(),
      e.getLimit()
    );
  }

}