package com.tll.gql;

import static com.tll.core.Util.isNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
      vrslt.getErrors().stream().map(ve -> ve.formalErrMsg()).collect(Collectors.toList())
    );
  }

  private final List<String> verrs;

  /**
   * Constructor
   * 
   * @param epath the graphql execution path associated with the error(s)
   * @param emsg the error message
   * @param verrs the validation errors
   */
  private GraphQLDataValidationError(String epath, String emsg, List<String> verrs) {
    super(epath, emsg);
    this.verrs = new ArrayList<>(verrs);
  }

  public List<String> getValidationErrors() { return verrs; }
}