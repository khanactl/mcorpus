package com.tll.gql;

import static com.tll.core.Util.isNull;

import java.util.HashMap;
import java.util.Map;

import com.tll.validate.VldtnResult;

import graphql.execution.ExecutionPath;

/**
 * Sanitized graphql data fetch error specifically for conveying entity validation errors.
 * <p>
 * No stack trace will get serialized to JSON.
 * 
 * @author jpk
 */
public class GraphQLDataValidationError extends GraphQLDataFetchError {
  private static final long serialVersionUID = 1L;

  public static GraphQLDataValidationError inst(final ExecutionPath epath, VldtnResult vrslt) {
    return new GraphQLDataValidationError(
      (isNull(epath) ? ExecutionPath.rootPath() : epath).toString(), 
      vrslt.getSummaryMsg(),
      vrslt.getMappedFieldErrors()
    );
  }

  private final Map<String, String> verrs;

  /**
   * Constructor
   * 
   * @param epath the graphql execution path associated with the error(s)
   * @param emsg the error message
   * @param verrs the validation errors
   */
  private GraphQLDataValidationError(String epath, String emsg, Map<String, String> verrs) {
    super(epath, emsg);
    this.verrs = new HashMap<>(verrs);
  }

  public Map<String, String> getValidationErrors() { return verrs; }
}