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
   * The maximum number of web sessions allowed to exist at any instant of time.
   */
  public int maxWebSessions;
  /**
   * The duration of a web session in minutes.
   */
  public int webSessionDurationInMinutes;
  
  /**
   * The JWT salt value.
   */
  public String jwtSalt;
  /**
   * The JWT time to live in milli-seconds.
   */
  public long jwtTtlInMillis;

  /**
   * The number of minutes the status of JWTs should be held in-memory 
   * before re-fetching them from the bakend data store.
   * <p>
   * A value of zero or less means do NOT cache and always fetch jwt 
   * status from backend.
   */
  public int jwtStatusCacheTimeoutInMinutes;
  
  /**
   * Flag for whether to send http cookies in the clear (http) or only over https.
   * <p>
   * <b>CAUTION</b>: This flag should ALWAYS be <code>true</code> 
   * in production environments!
   */
  public boolean cookieSecure;
}