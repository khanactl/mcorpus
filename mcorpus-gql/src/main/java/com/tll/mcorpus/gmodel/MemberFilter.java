package com.tll.mcorpus.gmodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Member search filter GraphQL entity type.
 * <p>
 * Encapsulates member search criteria backing the types 
 * defined in the associated GraphQL schema.
 * 
 * @author jpk
 */
public class MemberFilter {

  public static class StringPredicate {

    public static enum Operation {
      EQUALS,
      LIKE
    }
  
    private String value;
    private boolean ignoreCase;
    private Operation operation;
  
    /**
     * Constructor.
     */
    public StringPredicate() {
      this("", false, Operation.EQUALS);
    }
  
    /**
     * Constructor.
     *
     * @param value the string value for which to filter against.
     *              Allowed characters (<code>a-z, A-Z, 0-9, *, %, -space-</code>)
     *              are enforced with a regex replacement before field assignment.
     *
     * @param ignoreCase ignore case when comparing?
     * @param operation compare value by equals or like
     */
    public StringPredicate(String value, boolean ignoreCase, Operation operation) {
      setValue(value);
      setIgnoreCase(ignoreCase);
      setOperation(operation);
    }
  
    public String getValue() {
      return value;
    }
  
    public void setValue(String value) {
      this.value = value == null ? "" : value.replaceAll("[^a-zA-Z|\\d| |\\*|%]", "");
    }
  
    public boolean isIgnoreCase() {
      return ignoreCase;
    }
  
    public void setIgnoreCase(boolean b) {
      this.ignoreCase = b;
    }
  
    public Operation getOperation() {
      return operation;
    }
  
    public void setOperation(Operation operation) {
      this.operation = operation;
    }
  
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      StringPredicate that = (StringPredicate) o;
      return ignoreCase == that.ignoreCase &&
        Objects.equals(value, that.value) &&
        operation == that.operation;
    }
  
    @Override
    public int hashCode() {
      return Objects.hash(value, ignoreCase, operation);
    }
  
    @Override
    public String toString() { return String.format("StringPredicate [value: '%s', ignoreCase? %s, operation: %s]", value, ignoreCase, operation); }
  }

  public static class DatePredicate {

    public static enum DateOp {
      EQUAL_TO,
      NOT_EQUAL_TO,
      LESS_THAN,
      NOT_LESS_THAN,
      LESS_THAN_OR_EQUAL_TO,
      NOT_LESS_THAN_OR_EQUAL_TO,
      GREATER_THAN,
      NOT_GREATER_THAN,
      GREATER_THAN_OR_EQUAL_TO,
      NOT_GREATER_THAN_OR_EQUAL_TO,
      BETWEEN,
      NOT_BETWEEN;
    }
  
    private DateOp dateOp;
    private Date a;
    private Date b;
  
    /**
     * Constructor.
     */
    public DatePredicate() {
  
    }
  
    /**
     * Constructor.
     *
     * @param dateOp the date op
     * @param a the first instant argument
     * @param b the second instant argument which may not be used (depends on the date op)
     */
    public DatePredicate(DateOp dateOp, Date a, Date b) {
      this.dateOp = dateOp;
      setA(a);
      setB(b);
    }
  
    public DateOp getDateOp() {
      return dateOp;
    }
  
    public void setDateOp(DateOp dateOp) {
      this.dateOp = dateOp;
    }
  
    public Date getA() {
      return a == null ? null : new Date(a.getTime());
    }
  
    public void setA(Date d) {
      this.a = d == null ? null : new Date(d.getTime());
    }
  
    public Date getB() {
      return b == null ? null : new Date(b.getTime());
    }
  
    public void setB(Date d) {
      this.b = d == null ? null : new Date(d.getTime());
    }
  
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DatePredicate that = (DatePredicate) o;
      return dateOp == that.dateOp &&
        Objects.equals(a, that.a) &&
        Objects.equals(b, that.b);
    }
  
    @Override
    public int hashCode() {
      return Objects.hash(dateOp, a, b);
    }
  
    @Override
    public String toString() { return String.format("DatePredicate [Op: %s, argA: %s, argB: %s]", dateOp, a, b); }
  }

  public static class LocationPredicate {

    private final Set<String> locations = new HashSet<>(5);
  
    private boolean negate;
  
    public Set<String> getLocations() { return locations; }
  
    /**
     * Add a location to the set of locations in this predicate.
     *
     * <p>NOTE: if the location is already present, nothing happens. </p>
     *
     * @param loc the [unique] location to add
     */
    public void addLocation(String loc) { locations.add(loc); }
  
    /**
     * @return true when the held set of Locations is to be considered as "not in",
     *         false -> "in"
     */
    public boolean isNegate() { return negate; }
  
    public void setNegate(boolean b) { this.negate = b; }
  
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      LocationPredicate that = (LocationPredicate) o;
      return negate == that.negate &&
        Objects.equals(locations, that.locations);
    }
  
    @Override
    public int hashCode() {
      return Objects.hash(locations, negate);
    }
  
    @Override
    public String toString() { return String.format("LocationPredicate[locations: %s, negate?: %s]", locations, negate); }
  }
  
  public static class OrderBy {

    public static enum OrderByClause {
      ASCENDING,
      DESCENDING;
    }
  
    public String token;
    public OrderByClause direction;
  
    /**
     * Constructor.
     */
    public OrderBy() {
  
    }
  
    /**
     * Constructor.
     *
     * @param token the order by token
     * @param direction asc or desc?
     */
    public OrderBy(String token, OrderByClause direction) {
      this.token = token;
      this.direction = direction;
    }
  
    public String getToken() { return token; }
  
    public OrderByClause getDirection() { return direction; }
  
    /**
     * @return true when ascending,
     *         false when descending.
     */
    public boolean asc() { return direction == OrderByClause.ASCENDING; }
  
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      OrderBy orderBy = (OrderBy) o;
      return Objects.equals(token, orderBy.token) &&
        direction == orderBy.direction;
    }
  
    @Override
    public int hashCode() {
      return Objects.hash(token, direction);
    }
  
    @Override
    public String toString() { return String.format("OrderBy['%s' %s]", token, asc() ? "asc" : "desc"); }
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

  public List<OrderBy> getOrderByList() { return orderByList; }

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

  /*
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
  */
}
