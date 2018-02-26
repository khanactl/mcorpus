package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.emptyIfNull;
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
  public static GraphQLWebQuery parse(final Map<String, Object> qmap,
      String remoteAddr,
      String httpHost,
      String httpOrigin,
      String httpReferer,
      String httpForwarded,
      String webSessionid
  ) throws Exception {
    final String query = ((String) qmap.get("query"));
    @SuppressWarnings("unchecked")
    final Map<String, Object> vmap = (Map<String, Object>) qmap.get("variables");
    return new GraphQLWebQuery(query, vmap, 
        emptyIfNull(remoteAddr), 
        emptyIfNull(httpHost), 
        emptyIfNull(httpOrigin), 
        emptyIfNull(httpReferer), 
        emptyIfNull(httpForwarded), 
        emptyIfNull(webSessionid)
    );
  }
  
  private final String query;
  private final Map<String, Object> vmap;
  
  private final String remoteAddr;
  private final String httpHost;
  private final String httpOrigin;
  private final String httpReferer;
  private final String httpForwarded;
  private final String webSessionid;
  
  /**
   * Constructor.
   *
   * @param query the GraphQL query string
   * @param vmap optional query variables expressed as a name/value map
   * @param remoteAddr the http remote ip address
   * @param httpHost the http host value
   * @param httpOrigin the http host origin value
   * @param httpReferer the http host referer value
   * @param httpForwarded the http forwarded header value
   * @param webSessionid the web session id token
   */
  private GraphQLWebQuery(String query, Map<String, Object> vmap,
      String remoteAddr,
      String httpHost,
      String httpOrigin,
      String httpReferer,
      String httpForwarded,
      String webSessionid) {
    super();
    this.query = query;
    this.vmap = vmap;
    
    this.remoteAddr = remoteAddr;
    this.httpHost = httpHost;
    this.httpOrigin = httpOrigin;
    this.httpReferer = httpReferer;
    this.httpForwarded = httpForwarded;
    this.webSessionid = webSessionid;
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
  
  /**
   * @return the remoteAddr
   */
  public String getRemoteAddr() {
    return remoteAddr;
  }

  /**
   * @return the httpHost
   */
  public String getHttpHost() {
    return httpHost;
  }

  /**
   * @return the httpOrigin
   */
  public String getHttpOrigin() {
    return httpOrigin;
  }

  /**
   * @return the httpReferer
   */
  public String getHttpReferer() {
    return httpReferer;
  }

  /**
   * @return the httpForwarded
   */
  public String getHttpForwarded() {
    return httpForwarded;
  }

  /**
   * @return the webSessionid
   */
  public String getWebSessionid() {
    return webSessionid;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("query", query)
        .add("variables", vmap)
        .add("remoteAddr", remoteAddr)
        .add("httpHost", httpHost)
        .add("httpOrigin", httpOrigin)
        .add("httpReferer", httpReferer)
        .add("httpForwarded", httpForwarded)
        .add("webSessionid", webSessionid)
        .toString();
  }
}
