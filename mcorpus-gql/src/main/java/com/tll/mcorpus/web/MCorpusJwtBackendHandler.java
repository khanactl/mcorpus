package com.tll.mcorpus.web;

import static com.tll.core.Util.isNull;

import java.sql.Timestamp;
import java.util.UUID;

import com.tll.jwt.IJwtBackendHandler;
import com.tll.jwt.IJwtUser;
import com.tll.mcorpus.db.enums.JwtStatus;
import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.mcorpus.transform.McuserXfrm;
import com.tll.repo.FetchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dedicated mcorpus JWT backend handler.
 * 
 * @author jpk
 */
public class MCorpusJwtBackendHandler implements IJwtBackendHandler {

  private static JwtBackendStatus map(final com.tll.mcorpus.db.enums.JwtStatus repoJwtStatus) {
    switch (repoJwtStatus) {
    case BLACKLISTED:
      return JwtBackendStatus.BLACKLISTED;
    case EXPIRED:
      return JwtBackendStatus.EXPIRED;
    case MCUSER_INACTIVE:
      return JwtBackendStatus.BAD_USER;
    case NOT_PRESENT:
      return JwtBackendStatus.NOT_PRESENT;
    case PRESENT_BAD_STATE:
      return JwtBackendStatus.PRESENT_BAD_STATE;
    case VALID:
      return JwtBackendStatus.VALID;
    default:
      return JwtBackendStatus.ERROR;
    }
  }

  private Logger log = LoggerFactory.getLogger(MCorpusJwtBackendHandler.class);

  private final MCorpusUserRepo mcuserRepo;

  public MCorpusJwtBackendHandler(final MCorpusUserRepo mcuserRepo) {
    this.mcuserRepo = mcuserRepo;
  }

  @Override
  public FetchResult<JwtBackendStatus> getBackendJwtStatus(UUID jwtId) {
    final FetchResult<JwtStatus> fr = mcuserRepo.getBackendJwtStatus(jwtId);
    final JwtBackendStatus jstat = isNull(fr.get()) ? null : map(fr.get());
    return new FetchResult<>(jstat, fr.getErrorMsg());
  }
 
  @Override
  public FetchResult<IJwtUser> jwtBackendLogin(String username, String pswd, UUID pendingJwtId, String clientOriginToken, long requestInstantMillis, long jwtExpirationMillis) {
    final McuserLogin mcuserLogin = new McuserLogin();
    mcuserLogin.setMcuserUsername(username);
    mcuserLogin.setMcuserPassword(pswd);
    mcuserLogin.setInJwtId(pendingJwtId);
    mcuserLogin.setInLoginExpiration(new Timestamp(jwtExpirationMillis));
    mcuserLogin.setInRequestOrigin(clientOriginToken);
    mcuserLogin.setInRequestTimestamp(new Timestamp(requestInstantMillis));

    // call db login
    log.debug("Authenticating mcuser '{}'..", username);
    final FetchResult<Mcuser> loginResult = mcuserRepo.login(mcuserLogin);
    if(loginResult.isSuccess()) {
      final McuserXfrm xfrm = new McuserXfrm();
      return new FetchResult<>(xfrm.fromBackend(loginResult.get()), loginResult.getErrorMsg());
    } else {
      return new FetchResult<>(null, loginResult.getErrorMsg());
    }    
  }

  @Override
  public FetchResult<Boolean> jwtBackendLogout(UUID jwtUserId, UUID jwtId, String clientOriginToken, long requestInstantMillis) {
    final McuserLogout mcuserLogout = new McuserLogout();
    mcuserLogout.setMcuserUid(jwtUserId);
    mcuserLogout.setJwtId(jwtId);
    mcuserLogout.setRequestTimestamp(new Timestamp(requestInstantMillis));
    mcuserLogout.setRequestOrigin(clientOriginToken);

    FetchResult<Boolean> fr = mcuserRepo.logout(mcuserLogout);
    return fr;
  }

  @Override
  public FetchResult<Integer> getNumActiveJwtLogins(UUID jwtUserId) {
    FetchResult<Integer> fr = mcuserRepo.getNumActiveLogins(jwtUserId);
    return fr;
  }
}