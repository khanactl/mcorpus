package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.isNull;
import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;

import java.util.Map;

import com.tll.mcorpus.web.JWT.JWTStatusInstance;

/**
 * Immutable encapsulation of a GraphQL query request
 * for use in the app web layer.
 * <p>
 * This is the GraphQL context object for http requests.
 * 
 * @author jkirton
 */
public class GraphQLWebQuery {

  private final String query;
  private final Map<String, Object> vmap;
  private final RequestSnapshot requestSnapshot;
  private final JWTStatusInstance jwtStatus;
  
  /**
   * Constructor.
   *
   * @param query the GraphQL query string
   * @param vmap optional query variables expressed as a name/value map
   * @param requestSnapshot snapshot of the sourcing http request
   * @param jwtStatus the status of the JWT of the sourcing http request
   */
  public GraphQLWebQuery(String query, Map<String, Object> vmap, RequestSnapshot requestSnapshot, JWTStatusInstance jwtStatus) {
    super();
    this.query = query;
    this.vmap = vmap;
    this.requestSnapshot = requestSnapshot;
    this.jwtStatus = jwtStatus;
  }
  
  /**
   * Is this a valid GraphQL query ready to be handed off to further processing?
   * 
   * @return true/false
   */
  public boolean isValid() { 
    return 
        not(isNullOrEmpty(query))
        && not(isNull(requestSnapshot))
        && not(isNull(jwtStatus));
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
  
  /**
   * @return the JWT status instance of the sourcing http request.
   */
  public JWTStatusInstance getJwtStatus() { return jwtStatus; }
  
  @Override
  public String toString() {
    return String.format("%n%s%n", query);
  }
}
