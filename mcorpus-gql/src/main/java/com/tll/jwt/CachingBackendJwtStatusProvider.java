package com.tll.jwt;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caching JWT backed status provider using a time-based cache policy.
 * 
 * @author jpk
 */
public class CachingBackendJwtStatusProvider implements IJwtBackendStatusProvider {

  private final Logger log = LoggerFactory.getLogger(CachingBackendJwtStatusProvider.class);

  private final IJwtBackendStatusProvider provider;

  private final LoadingCache<UUID, JwtBackendStatus> cache;

  /**
   * Constructor.
   * 
   * @param jwtStatusProvider the underlying JWT status provider
   * @param minutesTolive the number of minutes a JWT status object shall live in-memory
   * @param maxCacheSize the max number of JWT status instances to hold in cache at any one time
   */
  public CachingBackendJwtStatusProvider(final IJwtBackendStatusProvider jwtStatusProvider, int minutesTolive, int maxCacheSize) {
    this.provider = jwtStatusProvider;
    this.cache = Caffeine.newBuilder()
                  .expireAfterWrite(minutesTolive, TimeUnit.MINUTES)
                  .maximumSize(maxCacheSize)
                  .build(key -> {
                    log.info("Fetching backend JWT status for {}.", key);
                    return this.provider.getBackendJwtStatus(key);
                  });
    log.info("Caching JWT backend status provider created with Time-to-Live: {} minutes, Max-Cache-Size: {}.", minutesTolive, maxCacheSize);
  }
  
  @Override
  public JwtBackendStatus getBackendJwtStatus(UUID jwtId) {
    return cache.get(jwtId);
  }
}