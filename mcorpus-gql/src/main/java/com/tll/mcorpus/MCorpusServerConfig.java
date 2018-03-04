package com.tll.mcorpus;

import com.google.common.base.MoreObjects;

/**
 * The MCorpus GraphQL server config type.
 * <p>
 * All needed startup properties shall be explicitly defined here.
 */
public class MCorpusServerConfig {
  public String dataSourceClassName;
  public String dbUsername;
  public String dbPassword;
  public String dbName;
  public String dbSchema;
  public String dbServerName;
  public String dbPortNumber;

  public String serverDomainName;

  public String jwtSalt;
  public long jwtTtlInMillis;
  
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("serverDomainName", serverDomainName)
        .toString();
  }
}