package com.tll.jwt;

import static com.tll.core.Util.isNotNull;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.tll.repo.FetchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caching JWT backed status provider using a time-based cache policy.
 * 
 * @author jpk
 */
public class CachingJwtBackendHandler implements IJwtBackendHandler {

  private final Logger log = LoggerFactory.getLogger(CachingJwtBackendHandler.class);

  private final IJwtBackendHandler targetHandler;

  private final LoadingCache<UUID, FetchResult<JwtBackendStatus>> jwtStatusCache;

  /**
   * Constructor.
   * 
   * @param jwtBackendHandler the sourcing {@link IJwtBackendHandler} this caching
   *                          instance encapsulates
   * @param minutesTolive     the number of minutes a JWT status object shall be
   *                          cached in-memory
   * @param maxCacheSize      the max number of JWT status instances to hold in
   *                          cache at any one time
   */
  public CachingJwtBackendHandler(final IJwtBackendHandler jwtBackendHandler, int minutesTolive, int maxCacheSize) {
    this.targetHandler = jwtBackendHandler;
    this.jwtStatusCache = Caffeine.newBuilder().expireAfterWrite(minutesTolive, TimeUnit.MINUTES)
        .maximumSize(maxCacheSize).build(key -> {
          log.info("Fetching backend JWT status for {}.", key);
          return this.targetHandler.getBackendJwtStatus(key);
        });
    log.info("Caching JWT backend status provider created with Time-to-Live: {} minutes, Max-Cache-Size: {}.",
        minutesTolive, maxCacheSize);
  }

  @Override
  public FetchResult<JwtBackendStatus> getBackendJwtStatus(UUID jwtId) {
    return jwtStatusCache.get(jwtId);
  }

  @Override
  public FetchResult<Integer> getNumActiveJwtLogins(UUID jwtUserId) {
    return targetHandler.getNumActiveJwtLogins(jwtUserId);
  }

  @Override
  public FetchResult<IJwtUser> jwtBackendLogin(String username, String pswd, UUID pendingJwtId,
      String clientOriginToken, Instant requestInstant, Instant jwtExpiration) {
    return targetHandler.jwtBackendLogin(username, pswd, pendingJwtId, clientOriginToken, requestInstant,
        jwtExpiration);
  }

  @Override
  public FetchResult<Boolean> jwtBackendLogout(UUID jwtUserId, UUID jwtId, String clientOriginToken,
      Instant requestInstant) {
    return targetHandler.jwtBackendLogout(jwtUserId, jwtId, clientOriginToken, requestInstant);
  }

  @Override
  public FetchResult<Boolean> jwtInvalidateAllForUser(UUID jwtUserId, String clientOriginToken,
      Instant requestInstant) {
    final FetchResult<Boolean> fr = targetHandler.jwtInvalidateAllForUser(jwtUserId, clientOriginToken, requestInstant);
    if(isNotNull(fr) && fr.isSuccess()) {
      jwtStatusCache.invalidate(jwtUserId);
    }
    return fr;
  }

}