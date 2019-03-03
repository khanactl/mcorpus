package com.tll.mcorpus.web;

import java.util.UUID;

import com.tll.mcorpus.db.enums.JwtStatus;
import com.tll.jwt.IJwtBackendStatusProvider;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.repo.FetchResult;

/**
 * Dedicated mcorpus JWT backend status provider.
 * 
 * @author jpk
 */
public class MCorpusJwtBackendStatusProvider implements IJwtBackendStatusProvider {

  private static JwtBackendStatus.Status map(final com.tll.mcorpus.db.enums.JwtStatus repoJwtStatus) {
    switch (repoJwtStatus) {
    case BLACKLISTED:
      return JwtBackendStatus.Status.BLACKLISTED;
    case EXPIRED:
      return JwtBackendStatus.Status.EXPIRED;
    case MCUSER_INACTIVE:
      return JwtBackendStatus.Status.BAD_USER;
    case NOT_PRESENT:
      return JwtBackendStatus.Status.NOT_PRESENT;
    case PRESENT_BAD_STATE:
      return JwtBackendStatus.Status.PRESENT_BAD_STATE;
    case VALID:
      return JwtBackendStatus.Status.VALID;
    default:
      return JwtBackendStatus.Status.ERROR;
    }
  }

  private final MCorpusUserRepo mcuserRepo;

  public MCorpusJwtBackendStatusProvider(final MCorpusUserRepo mcuserRepo) {
    this.mcuserRepo = mcuserRepo;
  }

  @Override
  public JwtBackendStatus getBackendJwtStatus(UUID jwtId) {
    final FetchResult<JwtStatus> fr = mcuserRepo.getBackendJwtStatus(jwtId);
    if(fr.isSuccess()) {
      JwtBackendStatus.Status jstat = map(fr.get());
      return new JwtBackendStatus(jstat);
    }
    // default
    return new JwtBackendStatus(fr.getErrorMsg());
 }
 
}