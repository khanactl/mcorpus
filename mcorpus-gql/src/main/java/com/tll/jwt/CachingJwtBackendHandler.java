package com.tll.jwt;

import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.not;

import java.net.InetAddress;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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

	private static class JwtCacheKey {
		public final UUID jwtId;
		public final UUID jwtUserId;

		public JwtCacheKey(UUID jwtId, UUID jwtUserId) {
			this.jwtId = jwtId;
			this.jwtUserId = jwtUserId;
		}

		@Override
		public int hashCode() {
			return Objects.hash(jwtId, jwtUserId);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JwtCacheKey other = (JwtCacheKey) obj;
			return Objects.equals(jwtId, other.jwtId) && Objects.equals(jwtUserId, other.jwtUserId);
		}

		@Override
		public String toString() {
			return String.format("JwtCacheKey[jwtId: %s, jwtUserId: %s]", jwtId, jwtUserId);
		}
	}

	private final Logger log = LoggerFactory.getLogger(CachingJwtBackendHandler.class);

	private final IJwtBackendHandler targetHandler;

	private final LoadingCache<JwtCacheKey, FetchResult<JwtBackendStatus>> jwtStatusCache;

	/**
	 * Constructor.
	 *
	 * @param jwtBackendHandler the sourcing {@link IJwtBackendHandler} this caching
	 *													instance encapsulates
	 * @param minutesTolive			the number of minutes a JWT status object shall be
	 *													cached in-memory
	 * @param maxCacheSize			the max number of JWT status instances to hold in
	 *													cache at any one time
	 */
	public CachingJwtBackendHandler(final IJwtBackendHandler jwtBackendHandler, int minutesTolive, int maxCacheSize) {
		this.targetHandler = jwtBackendHandler;
		this.jwtStatusCache = Caffeine.newBuilder().expireAfterWrite(minutesTolive, TimeUnit.MINUTES)
				.maximumSize(maxCacheSize).build(key -> {
					log.info("Fetching backend JWT status for {}.", key);
					final FetchResult<JwtBackendStatus> fr = this.targetHandler.getBackendJwtStatus(key.jwtId, key.jwtUserId);
					if(isNotNull(fr) && fr.isSuccess()) {
						return fr;
					}
					// default
					return null;
				});
		log.info("Caching JWT backend status provider created with Time-to-Live: {} minutes, Max-Cache-Size: {}.",
				minutesTolive, maxCacheSize);
	}

	@Override
	public FetchResult<JwtBackendStatus> getBackendJwtStatus(UUID jwtId, UUID jwtUserId) {
		return jwtStatusCache.get(new JwtCacheKey(jwtId, jwtUserId));
	}

	@Override
	public FetchResult<IJwtUser> getJwtUserInfo(UUID jwtUserId) {
		return targetHandler.getJwtUserInfo(jwtUserId);
	}

	@Override
	public FetchResult<List<IJwtInfo>> getActiveJwtLogins(UUID jwtUserId) {
		return targetHandler.getActiveJwtLogins(jwtUserId);
	}

	@Override
	public FetchResult<IJwtUser> jwtBackendLogin(String username, String pswd, UUID pendingJwtId,
			InetAddress requestOrigin, Instant requestInstant, Instant jwtExpiration) {
		return targetHandler.jwtBackendLogin(username, pswd, pendingJwtId, requestOrigin, requestInstant,
				jwtExpiration);
	}

	@Override
	public FetchResult<IJwtUser> jwtBackendLoginRefresh(UUID oldJwtId, UUID pendingJwtId, InetAddress requestOrigin,
			Instant requestInstant, Instant jwtExpiration) {
				return targetHandler.jwtBackendLoginRefresh(oldJwtId, pendingJwtId, requestOrigin, requestInstant,
					jwtExpiration);
	}

	@Override
	public FetchResult<Boolean> jwtBackendLogout(UUID jwtUserId, UUID jwtId, InetAddress requestOrigin,
			Instant requestInstant) {
		final FetchResult<Boolean> fr = targetHandler.jwtBackendLogout(jwtUserId, jwtId, requestOrigin, requestInstant);
		if(isNotNull(fr) && fr.isSuccess()) {
			jwtStatusCache.invalidate(new JwtCacheKey(jwtId, jwtUserId));
		}
		return fr;
	}

	@Override
	public FetchResult<Boolean> jwtInvalidateAllForUser(final UUID jwtUserId, final InetAddress requestOrigin, final Instant requestInstant) {
		final FetchResult<Boolean> fr = targetHandler.jwtInvalidateAllForUser(jwtUserId, requestOrigin, requestInstant);
		if(isNotNull(fr) && fr.isSuccess()) {
			final Map<JwtCacheKey, FetchResult<JwtBackendStatus>> cmap = jwtStatusCache.asMap();
			if(isNotNull(cmap) && not(cmap.isEmpty())) {
				final HashSet<JwtCacheKey> dset = new HashSet<>(cmap.size());
				for(Entry<JwtCacheKey, FetchResult<JwtBackendStatus>> entry : cmap.entrySet()) {
					if(entry.getKey().jwtUserId.equals(jwtUserId)) {
						dset.add(entry.getKey());
					}
				}
				jwtStatusCache.invalidateAll(dset);
			}
		}
		return fr;
	}
}