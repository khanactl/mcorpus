package com.tll.gql;

import com.fasterxml.jackson.annotation.JsonIgnore;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;

/**
 * Sanitized graphql data fetch error type.
 * <p>
 * No stack trace will get serialized to JSON.
 * 
 * @author jpk
 */
public class GraphQLDataFetchError extends ExceptionWhileDataFetching {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor - simple error message case.
   * 
   * @param emsg the error message
   */
  public GraphQLDataFetchError(String emsg) {
    this(ExecutionPath.rootPath(), new Exception(emsg), null);
  }

  /**
   * Constructor - Exception instance case.
   * 
   * @param emsg the error message
   */
  public GraphQLDataFetchError(final Throwable exception) {
    this(ExecutionPath.rootPath(), exception, null);
  }

  /**
   * Constructor - Full details case.
   * 
   * @param emsg path the GraphQL execution path
   * @param exception the data fetching related exception
   * @param sourceLocation the optional source location
   */
  public GraphQLDataFetchError(final ExecutionPath path, final Throwable exception, final SourceLocation sourceLocation) {
    super(path, exception, sourceLocation);
  }

  /**
   * {@inheritDoc}
   * <p>
   * Deny stack trace visibility when serialized to JSON!
   */
  @Override @JsonIgnore
  public Throwable getException() { return super.getException(); }
}