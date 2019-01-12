package com.tll.mcorpus.web;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.tll.mcorpus.db.enums.JwtStatus;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.mcorpus.repo.model.FetchResult;
import com.tll.mcorpus.repo.model.IJwtStatusProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caching JWT backed status provider using a time-based cache policy.
 * 
 * @author jpk
 */
public class CachingJwtStatusProvider implements IJwtStatusProvider {

  private final Logger log = LoggerFactory.getLogger(CachingJwtStatusProvider.class);

  private final LoadingCache<UUID, FetchResult<JwtStatus>> cache;

  /**
   * Constructor.
   * 
   * @param repo the mcorpus user repo
   * @param minutesTolive the number of minutes a jwt status object shall live in-memory
   */
  public CachingJwtStatusProvider(final MCorpusUserRepo repo, int minutesTolive) {
    this.cache = Caffeine.newBuilder()
                            .expireAfterWrite(minutesTolive, TimeUnit.MINUTES)
                            .build(key -> {
                              log.info("Fetching JWT status from repo for {}.", key);
                              return repo.getJwtStatus(key);
                            });
  }
  
  @Override
  public FetchResult<JwtStatus> getJwtStatus(UUID jwtId) {
    return cache.get(jwtId);
  }
}