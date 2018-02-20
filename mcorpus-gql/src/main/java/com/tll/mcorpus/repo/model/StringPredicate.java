package com.tll.mcorpus.repo.model;

import static com.tll.mcorpus.Util.asString;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.jooq.Condition;
import org.jooq.Field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Filter clause for data fields whose type is String.
 */
public class StringPredicate implements IFieldPredicate<String> {

  public static StringPredicate fromMap(final Map<String, Object> map) {
    StringPredicate tp = null;
    if(map != null && !map.isEmpty()) {
      tp = new StringPredicate();
      for(final Entry<String, Object> entry : map.entrySet()) {
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
  @JsonCreator
  public StringPredicate(@JsonProperty("value") String value, @JsonProperty("ignoreCase") boolean ignoreCase, @JsonProperty("operation") Operation operation) {
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

  private String sqlEqualsStatement() {
    return value.replaceAll("[\\*|%]", "");
  }

  private String sqlLikeStatement() {
    return value.replaceAll("\\*", "%");
  }

  @Override
  public Condition asJooqCondition(final Field<String> f) {
    final Condition c;
    switch(operation) {
      default:
      case EQUALS:
        c = ignoreCase ? f.equalIgnoreCase(sqlEqualsStatement()) : f.eq(sqlEqualsStatement());
        break;
      case LIKE:
        c = ignoreCase ? f.likeIgnoreCase(sqlLikeStatement()) : f.like(sqlLikeStatement());
        break;
    }
    return c;
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
  public String toString() {
    return String.format("value: '%s', ignoreCase? %s, operation: %s", value, ignoreCase, operation);
  }
}
