package com.tll.mcorpus.repo.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;

public class OrderBy {

  public static OrderBy fromMap(final Map<String, Object> map) {
    OrderBy ob = null;
    if(map != null && !map.isEmpty()) {
      ob = new OrderBy();
      for(final Entry<String, Object> entry : map.entrySet()) {
        String key = entry.getKey();
        switch(key) {
          case "token":
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
  @JsonCreator
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
  public String toString() { return String.format("'%s' %s", token, asc() ? "asc" : "desc"); }
}
