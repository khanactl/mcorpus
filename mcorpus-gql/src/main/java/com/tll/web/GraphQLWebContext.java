package com.tll.web;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotNullOrEmpty;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Immutable encapsulation of a GraphQL query request for use in the app web
 * layer.
 * <p>
 * This is the GraphQL context object for http requests.
 * <p>
 * Application-specific behavior may be realized by extending this class.  
 * The core ingredients and incoming query parsing capability should exist in this class however.
 * 
 * @author jkirton
 */
public class GraphQLWebContext {

  private static final Pattern gqlOperationName = 
    Pattern.compile("^[\\s|\"]*(mutation|query)\\s+(\\w+)\\s*\\{.*", 
      Pattern.CASE_INSENSITIVE);

  private static final Pattern gqlMethodName = 
    Pattern.compile("^.*?\\{.*?(\\w+).*", 
      Pattern.CASE_INSENSITIVE);

  private static final Pattern gqlIntrospectQuery = 
    Pattern.compile("^\\s*(query)?\\s*(IntrospectionQuery)\\s*\\{.*", 
        Pattern.CASE_INSENSITIVE);

  protected final Logger log = LoggerFactory.getLogger(getClass());

  protected final String query;
  protected final String queryCleaned;
  protected final Map<String, Object> vmap;
  // protected final RequestSnapshot requestSnapshot;

  /**
   * Constructor.
   *
   * @param query           the GraphQL query string
   * @param vmap            optional query variables expressed as a name/value map
   */
  public GraphQLWebContext(String query, Map<String, Object> vmap) {
    super();
    this.query = query;
    this.queryCleaned = clean(query).replaceAll("\\n", "").replaceAll("\n", "");
    this.vmap = vmap;
  }
  
  /**
   * Is this a valid GraphQL query ready to be handed off to further processing?
   * 
   * @return true/false
   */
  public boolean isValid() { 
    return isNotNullOrEmpty(query) && isNotNullOrEmpty(queryCleaned);
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
   * @return the never-null GraphQL query/mutation <em>operation</em> name.
   *         <p>
   *         The operation name is not required and when not present, 
   *         a zero-length string is returned.
   */
  public String getOperationName() {
    final Matcher matcher = gqlOperationName.matcher(queryCleaned);
    final String s = matcher.matches() ? matcher.group(2) : "";
    return s;
  }

  /**
   * @return the never-null GraphQL query/mutation <em>method</em> name.
   *         <p>
   *         The method name is expected to always be present 
   *         in a <em>valid</em> qraphql query string.
   */
  public String getQueryMethodName() {
    final Matcher matcher = gqlMethodName.matcher(queryCleaned);
    final String s = matcher.matches() ? matcher.group(1) : "";
    return s;
  }

  /**
   * @return true when the graphql query is an Introspection query.
   */
  public boolean isIntrospectionQuery() {
    final Matcher matcher = gqlIntrospectQuery.matcher(queryCleaned);
    boolean b = matcher.matches();
    return b;
  }

  @Override
  public String toString() {
    return String.format("qry: %s", getQueryMethodName());
  }
}
