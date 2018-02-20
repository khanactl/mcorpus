package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;

import java.util.Map;

import com.google.common.base.MoreObjects;

/**
 * Simple, immutable encapsulation of the needed GraphQL query parameters
 * intended for use in the app web layer.
 * 
 * @author jkirton
 */
public class GraphQLWebQuery {

  /**
   * Parse the given raw GraphQL query into its constituent sub-components.
   * 
   * @param qmap map of inbound JSON tokens
   * @throws Exception upon any parsing error
   */
  public static GraphQLWebQuery parse(final Map<String, Object> qmap) throws Exception {
    final String query = ((String) qmap.get("query"));
    @SuppressWarnings("unchecked")
    final Map<String, Object> vmap = (Map<String, Object>) qmap.get("variables");
    return new GraphQLWebQuery(query, vmap);
  }
  
  private final String query;
  private final Map<String, Object> vmap;
  
  /**
   * Constructor.
   *
   * @param query the GraphQL query string
   * @param vmap optional query variables expressed as a name/value map
   */
  public GraphQLWebQuery(String query, Map<String, Object> vmap) {
    super();
    this.query = query;
    this.vmap = vmap;
  }
  
  /**
   * Is this a valid GraphQL query ready to be handed off to further processing?
   * 
   * @return true/false
   */
  public boolean isValid() { return not(isNullOrEmpty(query)); }
  
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
  
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("query", query)
        .add("variables", vmap)
        .toString();
  }
}
