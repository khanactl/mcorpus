package com.tll.web;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isEmpty;
import static com.tll.core.Util.isNotNullOrEmpty;
import static com.tll.core.Util.isNull;
import static com.tll.transform.TransformUtil.uuidToToken;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
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

  /**
   * query or mutation?
   */
  private static final Pattern gqlType = 
    Pattern.compile("^(mutation|query)*.*?\\{.*");

  /**
   * The optional operation name.
   * <p>
   * E.g.: 
   * <pre>
   * query *opName* { method {} }
   * </pre>
   */
  private static final Pattern gqlOperationName = 
    Pattern.compile("^(mutation|query)\\s+(\\w+).*?\\{.*");

  /**
   * The required first query method name.
   * <p>
   * E.g.: 
   * <pre>
   * query opName { *methodName* {} method2Name() {} }
   * </pre>
   */
  private static final Pattern gqlFirstMethodName = 
    Pattern.compile("^(mutation|query)*.*?\\{\\s?(\\w+).*\\}");

  /**
   * All whitespace chars.
   */
  private static final Pattern regxWS = Pattern.compile("\\s+");
  
  /*
   * All NON-ASCII chars.
   */
  private static final Pattern regxNA = Pattern.compile("[^\\x00-\\x7F]");  
  
  protected final Logger log = LoggerFactory.getLogger(getClass());

  protected final String query;
  protected final Map<String, Object> vmap;
  protected final String qtype;
  protected final String opName;
  protected final String firstMethodName;
  protected final String executionId;

  /**
   * Constructor.
   *
   * @param query           the GraphQL query string
   * @param vmap            optional query variables expressed as a name/value map
   */
  public GraphQLWebContext(String query, Map<String, Object> vmap) {
    super();
    
    // clean query: 
    //   null -> ""
    //   trim
    //   remove all non-ascii chars
    final String queryCleaned = regxNA.matcher( clean(query) ).replaceAll("");
    final String queryCleanedOneline = regxWS.matcher(queryCleaned).replaceAll(" ");

    Matcher matcher;
    
    matcher = gqlType.matcher(queryCleanedOneline);
    this.qtype = matcher.matches() ? matcher.group(1) : "";
    
    matcher = gqlOperationName.matcher(queryCleanedOneline);
    this.opName = matcher.matches() ? matcher.group(2) : "";
    
    matcher = gqlFirstMethodName.matcher(queryCleanedOneline);
    this.firstMethodName = matcher.matches() ? matcher.group(2) : "";

    this.query = queryCleaned;
    this.vmap = vmap;
    this.executionId = uuidToToken(UUID.randomUUID());
  }
  
  /**
   * Is this a valid GraphQL query ready to be handed off for further processing?
   * 
   * @return true/false
   */
  public boolean isValid() { 
    return isNotNullOrEmpty(query) && isNotNullOrEmpty(firstMethodName);
  }
  
  /**
   * @return the cleansed GraphQL query string.
   */
  public String getQuery() { return query; }
  
  /**
   * @return true when this GraphQL query has variables, false otherwise.
   */
  public boolean hasVariables() { return isNotNullOrEmpty(vmap); }
  
  /**
   * @return the number of gql variables in the query.
   */
  public int getNumVariables() { return isNotNullOrEmpty(vmap) ? vmap.size() : 0; }
  
  /**
   * @return Never-null, possibly empty, map of name/value pairs representing 
   *         the GraphQL variables associated with this query instance.
   */
  public Map<String, Object> getVariables() { return isNull(vmap) ? Collections.emptyMap() : vmap; }

  /**
   * @return Never-null, possibly empty, query type (query or mutation).
   *         <p>
   *         Empty implies query type.
   */
  public String getQueryType() { return qtype; }

  /**
   * @return Never-null query operation name which may be absent (empty).
   *         <p>
   *         The operation name is not required and when not present, 
   *         a zero-length string is returned.
   */
  public String getOpName() { return opName; }

  /**
   * @return the <em>first</em> >GraphQL query/mutation <em>method</em> name.
   *         <p>
   *         If the query type is a mutation, we expect only one method name
   *         whereas if the query type is query then multiple 'root-level' 
   *         methods are allowed.
   *         <p>
   *         At least one method name is always expected to be present 
   *         in a <em>valid</em> qraphql query string.
   */
  public String getFirstMethodName() { return firstMethodName; }

  public boolean isMutation() { return "mutation".equals(qtype); }

  public boolean isQuery() { return isEmpty(qtype) || "query".equals(qtype); }

  /**
   * @return true when the graphql query is an Introspection query.
   */
  public boolean isIntrospectionQuery() { return "IntrospectionQuery".equals(opName); }

  /**
   * @return the unique 'execution' id ascribed to this object intended to be
   *         handed off to the graphql processor pending validation checks.
   *         <p>
   *         We set the execution id at this point for tracking and logging
   *         purposes.
   */
  public String getExecutionId() { return executionId; }

  @Override
  public String toString() {
    // FORMAT: id (type [opName] firstMethodName)
    return String.format("%s (%s %s %s)", 
      executionId, 
      isNotNullOrEmpty(qtype) ? qtype : "query", 
      isNotNullOrEmpty(opName) ? opName : "-", 
      firstMethodName 
    );
  }
}
