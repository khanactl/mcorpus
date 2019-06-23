package com.tll.mcorpus;

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
   * The JWT salt value.
   */
  public String jwtSalt;
  
  /**
   * The JWT time to live in seconds.
   * <p>
   * Default is 2 days (172800 seconds).
   */
  public long jwtTtlInSeconds = 172800L;

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
   * The default is 50.
   */
  public int jwtStatusCacheMaxSize = 50;
  
  /**
   * Flag for whether to send http cookies in the clear (http) or only over https.
   * <p>
   * <b>CAUTION</b>: This flag should ALWAYS be <code>true</code> 
   * in production environments!
   * <p>
   * The default is <code>true</code>.
   */
  public boolean cookieSecure = true;
}