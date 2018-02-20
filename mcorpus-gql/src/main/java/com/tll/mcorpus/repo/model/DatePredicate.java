package com.tll.mcorpus.repo.model;

import static org.jooq.impl.DSL.not;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.jooq.Condition;
import org.jooq.Field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates a logical date comparison operation.
 *
 * <p>Used to capture user-specified query constraints.</p>
 */
public class DatePredicate implements IFieldPredicate<Timestamp> {

  public static DatePredicate fromMap(final Map<String, Object> map) {
    DatePredicate dp = null;
    if(map != null && !map.isEmpty()) {
      dp = new DatePredicate();
      for(final Entry<String, Object> entry : map.entrySet()) {
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

  public static DatePredicate isEqualTo(Date a) {
    return new DatePredicate(DateOp.EQUAL_TO, a, null);
  }

  public static DatePredicate isNotEqualTo(Date a) {
    return new DatePredicate(DateOp.NOT_EQUAL_TO, a, null);
  }

  public static DatePredicate isLessThan(Date a) {
    return new DatePredicate(DateOp.LESS_THAN, a, null);
  }

  public static DatePredicate isNotLessThan(Date a) {
    return new DatePredicate(DateOp.NOT_LESS_THAN, a, null);
  }

  public static DatePredicate isLessThanOrEqualTo(Date a) {
    return new DatePredicate(DateOp.LESS_THAN_OR_EQUAL_TO, a, null);
  }

  public static DatePredicate isNotLessThanOrEqualTo(Date a) {
    return new DatePredicate(DateOp.NOT_LESS_THAN_OR_EQUAL_TO, a, null);
  }

  public static DatePredicate isGreaterThan(Date a) {
    return new DatePredicate(DateOp.GREATER_THAN, a, null);
  }

  public static DatePredicate isNotGreaterThan(Date a) {
    return new DatePredicate(DateOp.NOT_GREATER_THAN, a, null);
  }

  public static DatePredicate isGreaterThanOrEqualTo(Date a) {
    return new DatePredicate(DateOp.GREATER_THAN_OR_EQUAL_TO, a, null);
  }

  public static DatePredicate isNotGreaterThanOrEqualTo(Date a) {
    return new DatePredicate(DateOp.NOT_GREATER_THAN_OR_EQUAL_TO, a, null);
  }

  public static DatePredicate isBetween(Date a, Date b) {
    return new DatePredicate(DateOp.BETWEEN, a, b);
  }

  public static DatePredicate isNotBetween(Date a, Date b) {
    return new DatePredicate(DateOp.NOT_BETWEEN, a, b);
  }

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
  @JsonCreator
  public DatePredicate(@JsonProperty("dateOp") DateOp dateOp, @JsonProperty("a") Date a, @JsonProperty("b") Date b) {
    this.dateOp = dateOp;
    this.a = a;
    this.b = b;
  }

  public DateOp getDateOp() {
    return dateOp;
  }

  public void setDateOp(DateOp dateOp) {
    this.dateOp = dateOp;
  }

  public Date getA() {
    return a;
  }

  public void setA(Date d) {
    this.a = d;
  }

  public Date getB() {
    return b;
  }

  public void setB(Date d) {
    this.b = d;
  }

  @Override
  public Condition asJooqCondition(final Field<Timestamp> f) {
    final Condition c;
    switch(dateOp) {
      default:
      case EQUAL_TO:
        c = f.eq(new Timestamp(a.getTime()));
        break;
      case NOT_EQUAL_TO:
        c = f.notEqual(new Timestamp(a.getTime()));
        break;
      case LESS_THAN:
        c = f.lessThan(new Timestamp(a.getTime()));
        break;
      case NOT_LESS_THAN:
        c = not(f.lessThan(new Timestamp(a.getTime())));
        break;
      case LESS_THAN_OR_EQUAL_TO:
        c = f.lessOrEqual(new Timestamp(a.getTime()));
        break;
      case NOT_LESS_THAN_OR_EQUAL_TO:
        c = not(f.lessOrEqual(new Timestamp(a.getTime())));
        break;
      case GREATER_THAN:
        c = f.greaterThan(new Timestamp(a.getTime()));
        break;
      case NOT_GREATER_THAN:
        c = not(f.greaterThan(new Timestamp(a.getTime())));
        break;
      case GREATER_THAN_OR_EQUAL_TO:
        c = f.greaterOrEqual(new Timestamp(a.getTime()));
        break;
      case NOT_GREATER_THAN_OR_EQUAL_TO:
        c = not(f.greaterOrEqual(new Timestamp(a.getTime())));
        break;
      case BETWEEN:
        c = f.between(new Timestamp(a.getTime()), new Timestamp(b.getTime()));
        break;
      case NOT_BETWEEN:
        c = not(f.between(new Timestamp(a.getTime()), new Timestamp(b.getTime())));
        break;
    }
    return c;
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
  public String toString() { return String.format("Op: %s, argA: %s, argB: %s", dateOp, a, b); }
}
