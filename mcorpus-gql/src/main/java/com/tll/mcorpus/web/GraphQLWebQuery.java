package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.isNull;
import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;

import java.util.Map;

/**
 * Immutable encapsulation of a GraphQL query request
 * for use in the app web layer.
 * 
 * @author jkirton
 */
public class GraphQLWebQuery {

  private final String query;
  private final Map<String, Object> vmap;
  private final RequestSnapshot requestSnapshot;
  
  /**
   * Constructor.
   *
   * @param query the GraphQL query string
   * @param vmap optional query variables expressed as a name/value map
   * @param requestSnapshot snapshot of the sourcing http request
   */
  public GraphQLWebQuery(String query, Map<String, Object> vmap, RequestSnapshot requestSnapshot) {
    super();
    this.query = query;
    this.vmap = vmap;
    this.requestSnapshot = requestSnapshot;
  }
  
  /**
   * Is this a valid GraphQL query ready to be handed off to further processing?
   * 
   * @return true/false
   */
  public boolean isValid() { 
    return 
        not(isNullOrEmpty(query))
        && not(isNull(requestSnapshot)); 
  }
  
  /**
   * @return true when this GraphQL query has variables, false otherwise.
   */
  public boolean hasQueryVariables() { return vmap != null && !vmap.isEmpty(); }
  
  /**
   * @return the GraphQL query string.
   */
  public String getQuery() { return query; }
  
  /**
   * @return map of name/value pairs representing 
   *          the GraphQL variables associated with this query instance.
   */
  public Map<String, Object> getVariables() { return vmap; }
  
  /**
   * @return the snapshot of the sourcing http request.
   */
  public RequestSnapshot getRequestSnapshot() { return requestSnapshot; }
  
  @Override
  public String toString() {
    return String.format("\n%s\n", query);
  }
}
