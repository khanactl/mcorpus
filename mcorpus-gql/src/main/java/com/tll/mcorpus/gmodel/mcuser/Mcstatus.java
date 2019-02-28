package com.tll.mcorpus.gmodel.mcuser;

import java.util.Date;
import java.util.UUID;

/**
 * Simple DTO for conveying the mcuser login status through the graphql api.
 * 
 * @author jkirton
 */
public final class Mcstatus {
  public final UUID mcuserId;
  public final Date since;
  public final Date expires;
  public final int numActiveJWTs;

  /**
   * Constructor.
   * 
   * @param mcuserId the logged in mcuser id
   * @param since when the bound mcuser logged in
   * @param expires when the JWT expires
   * @param numActiveJWTs the total number of valid (non-expired) JWTs presently issued to the bound mcuser
   */
  public Mcstatus(UUID mcuserId, Date since, Date expires, int numActiveJWTs) {
    this.mcuserId = mcuserId;
    this.since = since;
    this.expires = expires;
    this.numActiveJWTs = numActiveJWTs;
  }

  @Override
  public String toString() {
    return String.format("mcuserId: %s, since: %s, expires: %s, numActiveJWTs: %d", mcuserId, since, expires, numActiveJWTs);
  }
}