package com.tll.mcorpus.repo.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.jooq.Condition;
import org.jooq.Field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenPredicate implements IFieldPredicate<String> {

  public static TokenPredicate fromMap(final Map<String, Object> map) {
    TokenPredicate tp = null;
    if(map != null && !map.isEmpty()) {
      tp = new TokenPredicate();
      for(final Entry<String, Object> entry : map.entrySet()) {
        String key = entry.getKey();
        switch(key) {
          case "token":
            tp.setToken((String) entry.getValue());
            break;
          case "ignoreCase":
            tp.setIgnoreCase((Boolean) entry.getValue());
            break;
          case "tokenOp":
            tp.setTokenOp(TokenOp.valueOf((String) entry.getValue()));
            break;
        }
      }
    }
    return tp;
  }

  public static enum TokenOp {
    EQUALS,
    LIKE
  }

  private String token;
  private boolean ignoreCase;
  private TokenOp tokenOp;

  /**
   * Constructor.
   */
  public TokenPredicate() {
    this("", false, TokenOp.EQUALS);
  }

  /**
   * Constructor.
   *
   * @param token the allowed characters enforced with a regex replacement before field assignment:<br>
   *              <code>a-z, A-Z, 0-9, *, %, -space-</code>
   * @param ignoreCase
   * @param tokenOp
   */
  @JsonCreator
  public TokenPredicate(@JsonProperty("token") String token, @JsonProperty("ignoreCase") boolean ignoreCase, @JsonProperty("tokenOp") TokenOp tokenOp) {
    setToken(token);
    setIgnoreCase(ignoreCase);
    setTokenOp(tokenOp);
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token == null ? "" : token.replaceAll("[^a-zA-Z|\\d| |\\*|%]", "");
  }

  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  public void setIgnoreCase(boolean b) {
    this.ignoreCase = b;
  }

  public TokenOp getTokenOp() {
    return tokenOp;
  }

  public void setTokenOp(TokenOp tokenOp) {
    this.tokenOp = tokenOp;
  }

  String sqlEqualsToken() {
    return token.replaceAll("[\\*|%]", "");
  }

  String sqlLikeToken() {
    return token.replaceAll("\\*", "%");
  }

  @Override
  public Condition asJooqCondition(final Field<String> f) {
    final Condition c;
    switch(tokenOp) {
      default:
      case EQUALS:
        c = ignoreCase ? f.equalIgnoreCase(sqlEqualsToken()) : f.eq(sqlEqualsToken());
        break;
      case LIKE:
        c = ignoreCase ? f.likeIgnoreCase(sqlLikeToken()) : f.like(sqlLikeToken());
        break;
    }
    return c;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TokenPredicate that = (TokenPredicate) o;
    return ignoreCase == that.ignoreCase &&
      Objects.equals(token, that.token) &&
      tokenOp == that.tokenOp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(token, ignoreCase, tokenOp);
  }

  @Override
  public String toString() {
    return String.format("token: '%s', ignoreCase? %s, tokenOp: %s", token, ignoreCase, tokenOp);
  }
}
