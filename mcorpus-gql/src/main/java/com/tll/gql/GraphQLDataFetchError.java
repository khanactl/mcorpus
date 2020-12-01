package com.tll.gql;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;

/**
 * Sanitized graphql data fetch error type.
 * <p>
 * No stack trace will get serialized to JSON.
 *
 * @author jpk
 */
public class GraphQLDataFetchError implements GraphQLError {
  private static final long serialVersionUID = 1L;

  public static GraphQLDataFetchError inst(final ResultPath epath, String emsg) {
    return new GraphQLDataFetchError(
      (isNull(epath) ? ResultPath.rootPath() : epath).toString(),
      isNullOrEmpty(emsg) ? "Unspecified data fetch error." : clean(emsg)
    );
  }

  public static GraphQLDataFetchError inst(final ResultPath epath, final Throwable exception) {
    return inst(epath, exception.getMessage());
  }

  private final String epath;
  private final String emsg;

  /**
   * Constructor
   *
   * @param epath the graphql execution path associated with the error(s)
   * @param emsg the error message
   */
  protected GraphQLDataFetchError(String epath, String emsg) {
    this.epath = epath;
    this.emsg = emsg;
  }

  @Override
  public String getMessage() { return emsg; }

  @Override @JsonIgnore
  public List<SourceLocation> getLocations() { return null; }

  @Override
  public ErrorClassification getErrorType() { return ErrorType.DataFetchingException; }

  @Override
  public List<Object> getPath() { return Collections.singletonList(epath); }

  @Override @JsonIgnore
  public Map<String, Object> getExtensions() { return null; }

  @Override
  public String toString() { return getMessage(); }
}