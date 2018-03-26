package com.tll.mcorpus;

/**
 * The MCorpus GraphQL server config properties.
 * <p>
 * All needed startup properties shall be explicitly declared herein.
 */
public class MCorpusServerConfig {
  // db
  public String dbDataSourceClassName;
  public String dbUsername;
  public String dbPassword;
  public String dbName;
  public String dbSchema;
  public String dbServerName;
  public String dbPortNumber;
  // jwt
  public String jwtSalt;
  public long jwtTtlInMillis;
}