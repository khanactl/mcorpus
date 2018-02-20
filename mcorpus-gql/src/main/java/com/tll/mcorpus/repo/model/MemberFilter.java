package com.tll.mcorpus.repo.model;

import static com.tll.mcorpus.Util.isBlank;
import static com.tll.mcorpus.db.Tables.MEMBER;
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.jooq.Condition;
import org.jooq.SortField;

import com.google.common.base.MoreObjects;

/**
 * Encapsulates search criteria for member searches.
 */
public class MemberFilter implements IFetchFilter {

  @SuppressWarnings("unchecked")
  public static MemberFilter fromMap(final Map<String, Object> map) {
    MemberFilter mf = null;
    if(map != null) {
      mf = new MemberFilter();
      for(Entry<String, Object> entry : map.entrySet()) {
        String key = entry.getKey();
        if(!isBlank(key)) {
          Map<String, Object> submap;
          List<Map<String, Object>> sublist;
          switch(key) {
            case "created":
              submap = (Map<String, Object>) entry.getValue();
              mf.setCreated(DatePredicate.fromMap(submap));
              break;
            case "modified":
              submap = (Map<String, Object>) entry.getValue();
              mf.setModified(DatePredicate.fromMap(submap));
              break;
            case "empId":
              submap = (Map<String, Object>) entry.getValue();
              mf.setEmpId(StringPredicate.fromMap(submap));
              break;
            case "location":
              submap = (Map<String, Object>) entry.getValue();
              mf.setLocation(LocationPredicate.fromMap(submap));
              break;
            case "nameFirst":
              submap = (Map<String, Object>) entry.getValue();
              mf.setNameFirst(StringPredicate.fromMap(submap));
              break;
            case "nameMiddle":
              submap = (Map<String, Object>) entry.getValue();
              mf.setNameMiddle(StringPredicate.fromMap(submap));
              break;
            case "nameLast":
              submap = (Map<String, Object>) entry.getValue();
              mf.setNameLast(StringPredicate.fromMap(submap));
              break;
            case "displayName":
              submap = (Map<String, Object>) entry.getValue();
              mf.setDisplayName(StringPredicate.fromMap(submap));
              break;
            case "orderByList":
              sublist = (List<Map<String, Object>>) map.get(key);
              if(sublist != null && !sublist.isEmpty()) {
                for(final Map<String, Object> sublistmap : sublist) {
                  OrderBy ob = OrderBy.fromMap(sublistmap);
                  if(ob != null) mf.addOrderBy(ob);
                }
              }
          }
        }
      }
    }
    return mf;
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
    return defaultJooqSorting;
  }

  private static final SortField<?>[] defaultJooqSorting;

  static {
    defaultJooqSorting = generateJooqSortFields(singletonList(new OrderBy("created", OrderBy.OrderByClause.DESCENDING)));
  }

  private DatePredicate created;
  private DatePredicate modified;
  private StringPredicate empId;
  private LocationPredicate location;
  private StringPredicate nameFirst;
  private StringPredicate nameMiddle;
  private StringPredicate nameLast;
  private StringPredicate displayName;

  private List<OrderBy> orderByList;

  /**
   * Constructor.
   */
  public MemberFilter() { }

  /**
   * @return true if at least one member filter constraint is set,
   *         false otherwise.
   */
  public boolean isSet() {
    return hasCreated()
      || hasModified()
      || hasEmpId()
      || hasLocation()
      || hasNameFirst()
      || hasNameMiddle()
      || hasNameLast()
      || hasDisplayName();
  }

  public boolean hasCreated() { return created != null; }

  public DatePredicate getCreated() {
    return created;
  }

  public void setCreated(DatePredicate created) {
    this.created = created;
  }

  public boolean hasModified() { return modified != null; }

  public DatePredicate getModified() {
    return modified;
  }

  public void setModified(DatePredicate modified) {
    this.modified = modified;
  }

  public boolean hasEmpId() { return empId != null; }

  public StringPredicate getEmpId() {
    return empId;
  }

  public void setEmpId(StringPredicate empId) {
    this.empId = empId;
  }

  public boolean hasLocation() { return location != null; }

  public LocationPredicate getLocation() {
    return location;
  }

  public void setLocation(LocationPredicate location) {
    this.location = location;
  }

  public boolean hasNameFirst() { return nameFirst != null; }

  public StringPredicate getNameFirst() {
    return nameFirst;
  }

  public void setNameFirst(StringPredicate nameFirst) {
    this.nameFirst = nameFirst;
  }

  public boolean hasNameMiddle() { return nameMiddle != null; }

  public StringPredicate getNameMiddle() {
    return nameMiddle;
  }

  public void setNameMiddle(StringPredicate nameMiddle) {
    this.nameMiddle = nameMiddle;
  }

  public boolean hasNameLast() { return nameLast != null; }

  public StringPredicate getNameLast() {
    return nameLast;
  }

  public void setNameLast(StringPredicate nameLast) {
    this.nameLast = nameLast;
  }

  public boolean hasDisplayName() { return displayName != null; }

  public StringPredicate getDisplayName() {
    return displayName;
  }

  public void setDisplayName(StringPredicate displayName) {
    this.displayName = displayName;
  }

  /**
   * Add an order by directive to the internal list.
   *
   * @param orderBy the order by directive to add
   */
  public void addOrderBy(final OrderBy orderBy) {
    if(orderBy != null) {
      if (orderByList == null) orderByList = new ArrayList<>(5);
      orderByList.add(orderBy);
    }
  }

  @Override
  public Condition[] asJooqCondition() {
    if(!isSet()) return null;
    final List<Condition> conditions = new ArrayList<>(8);
    if(hasCreated()) conditions.add(getCreated().asJooqCondition(MEMBER.CREATED));
    if(hasModified()) conditions.add(getModified().asJooqCondition(MEMBER.MODIFIED));
    if(hasEmpId()) conditions.add(getEmpId().asJooqCondition(MEMBER.EMP_ID));
    if(hasLocation()) conditions.add(getLocation().asJooqCondition(MEMBER.LOCATION));
    if(hasNameFirst()) conditions.add(getNameFirst().asJooqCondition(MEMBER.NAME_FIRST));
    if(hasNameMiddle()) conditions.add(getNameMiddle().asJooqCondition(MEMBER.NAME_MIDDLE));
    if(hasNameLast()) conditions.add(getNameLast().asJooqCondition(MEMBER.NAME_LAST));
    if(hasDisplayName()) conditions.add(getDisplayName().asJooqCondition(MEMBER.DISPLAY_NAME));
    return conditions.toArray(new Condition[conditions.size()]);
  }

  @Override
  public SortField<?>[] generateJooqSortFields() {
    return orderByList == null || orderByList.isEmpty() ? defaultJooqSorting : generateJooqSortFields(orderByList);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MemberFilter that = (MemberFilter) o;
    return Objects.equals(created, that.created) &&
      Objects.equals(modified, that.modified) &&
      Objects.equals(location, that.location) &&
      Objects.equals(nameFirst, that.nameFirst) &&
      Objects.equals(nameMiddle, that.nameMiddle) &&
      Objects.equals(nameLast, that.nameLast) &&
      Objects.equals(displayName, that.displayName) &&
      Objects.equals(orderByList, that.orderByList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(created, modified, location, nameFirst, nameMiddle, nameLast, displayName, orderByList);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("created", created)
      .add("modified", modified)
      .add("location", location)
      .add("nameFirst", nameFirst)
      .add("nameMiddle", nameMiddle)
      .add("nameLast", nameLast)
      .add("displayName", displayName)
      .toString();
  }
}
