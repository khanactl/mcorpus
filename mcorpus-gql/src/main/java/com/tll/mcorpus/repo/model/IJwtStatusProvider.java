package com.tll.mcorpus.repo.model;

import java.util.UUID;

import com.tll.mcorpus.db.enums.JwtStatus;

/**
 * Contract for obtaining the status of a JWT from a known-about 
 * remote data store to allow for multiple or stackable 
 * implmentation strategies.<br>
 * Time-based caching, for example, can be achieved with this generic contract.
 * <p>
 * Since: 1/10/2019
 * 
 * @author jkirton
 */
public interface IJwtStatusProvider {

  /**
   * Get the status of a known JWT from a 
   * remotely held datastore given its jwt id.
   * 
   * @param jwtId id of the target JWT
   * @return the possibly absent JwtStatus wrapped in a 
   *         FetchResult type that holds an error message 
   *         if the jwt status is not provided.
   */
  FetchResult<JwtStatus> getJwtStatus(UUID jwtId);
}