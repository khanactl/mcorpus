package com.tll.mcorpus.web.ratpack;

/**
 * The MCorpus GraphQL server config properties.
 * <p>
 * All needed startup properties shall be explicitly declared herein.
 */
public class MCorpusServerConfig {

  /**
   * The fully qualified JDBC data source class name.
   */
  public String dbDataSourceClassName;

  /**
   * The JDBC db connection URL which includes the db username and password.
   */
  public String dbUrl;

  /**
   * The Request Sync Token name to use (http cookie name and http header name).
   */
  public String rstTokenName = "rst";

  /**
   * The Request Sync Token time to live in seconds on the client browser.
   * <p>
   * Default is 30 minutes (1800 seconds).
   */
  public long rstTtlInSeconds = 1800L;

  /**
   * RegEx that identifies the http request paths subject to RST server handling.
   */
  public String rstRegExRequestPaths = "^(graphql\\/index|graphql)\\/?$";

  /**
   * The JWT salt value.
   */
  public String jwtSalt;

  /**
   * The JWT time to live in minutes.
   * <p>
   * Default is 15 minutes.
   */
  public int jwtTtlInMinutes = 15;

  /**
   * The name to use for jwt refresh token cookies.
   */
  public String jwtRefreshTokenName = "jrt";

  /**
   * The JWT refresh token time to live in minutes.
   * <p>
   * Default is 2 weeks.
   */
  public int jwtRefreshTokenTtlInMinutes = 20160;

  /**
   * The GraphQL schema method name for JWT-based user logins.
   */
  public String jwtUserLoginGraphqlMethodName = "jwtLogin";

  /**
   * The GraphQL schema method name for JWT-based user *refresh* logins.
   */
  public String jwtUserLoginRefreshGraphqlMethodName = "jwtRefresh";

  /**
   * The number of minutes the status of JWTs should be held in-memory
   * before re-fetching them from the bakend data store.
   * <p>
   * A value of zero or less means do NOT cache and always fetch jwt
   * status from backend.
   * <p>
   * The default is 10 minutes.
   */
  public int jwtStatusCacheTimeoutInMinutes = 10;

  /**
   * The max number of JWT status instances to cache at any one time.
   * <p>
   * The default is 5.
   */
  public int jwtStatusCacheMaxSize = 5;

  /**
   * Flag for whether to send http cookies in the clear (http) or only over https.
   * <p>
   * <b>CAUTION</b>: This flag should ALWAYS be <code>true</code>
   * in production environments!
   * <p>
   * The default is <code>true</code>.
   */
  public boolean cookieSecure = true;

  /**
   * Capture app metrics?
   * <p>
   * The default is <code>true</code>.
   */
  public boolean metricsOn = true;

  /**
   * Serve GraphiQL requests?
   * <p>
   * The default is <code>false</code> (production mode).
   */
  public boolean graphiql = false;

  /**
   * The optional http client origin enabling CORS functionality.
   * <p>
   * When this property is specified,
   * CORS will be configured to allow http communication between
   * this graphql server and the http client origin specified by this property.
   * <p>
   * E.g.:  <code>https://clientAppDomain.net</code>
   *        <code>http://localhost:8080</code>
   * <p>
   * The default is null (not specified) and NO CORS http headers will be issued.
   */
  public String httpClientOrigin;
}