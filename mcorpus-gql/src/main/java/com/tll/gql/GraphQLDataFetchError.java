package com.tll.gql;

import com.fasterxml.jackson.annotation.JsonIgnore;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.ExecutionPath;

/**
 * Sanitized graphql data fetch error that is only concerned with a simple error message.
 * <p>
 * No stack trace will get serialized to JSON.
 * 
 * @author jpk
 */
public class GraphQLDataFetchError extends ExceptionWhileDataFetching {
  private static final long serialVersionUID = 1L;

  public GraphQLDataFetchError(String emsg) {
    super(ExecutionPath.rootPath(), new Exception(emsg), null);
  }

  @Override
  @JsonIgnore
  public Throwable getException() { return super.getException(); }
}