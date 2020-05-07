package com.tll.mcorpus.web;

import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.mcorpus.transform.McuserXfrm.mcuserRolesArrayToStringArray;
import static com.tll.repo.FetchResult.fetchrslt;
import static com.tll.transform.TransformUtil.odtToDate;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tll.jwt.IJwtBackendHandler;
import com.tll.jwt.IJwtInfo;
import com.tll.jwt.IJwtUser;
import com.tll.mcorpus.db.enums.JwtStatus;
import com.tll.mcorpus.db.enums.McuserRole;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.dmodel.ActiveLoginDomain;
import com.tll.mcorpus.repo.MCorpusUserRepo;
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
    case MCUSER_NOTACTIVE:
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

  private static IJwtInfo map(final ActiveLoginDomain al) {
    return new IJwtInfo() {

      @Override
      public UUID getJwtId() {
        return al.jwtId;
      }

      @Override
      public Date expires() {
        return odtToDate(al.expires);
      }

      @Override
      public Date created() {
        return odtToDate(al.requestTimestamp);
      }

      @Override
      public String clientOrigin() {
        return al.requestOrigin.getHostAddress();
      }
    };
  }

  private static IJwtUser map(final Mcuser mc) {
    return new IJwtUser() {

      @Override
      public UUID getJwtUserId() {
        return mc.getUid();
      }

      @Override
      public String getJwtUserName() {
        return mc.getName();
      }

      @Override
      public String getJwtUserUsername() {
        return mc.getUsername();
      }

      @Override
      public String getJwtUserEmail() {
        return mc.getEmail();
      }

      @Override
      public boolean isAdministrator() {
        return isNullOrEmpty(mc.getRoles()) ?
          false : Arrays.stream(mc.getRoles()).anyMatch(McuserRole.ADMIN::equals);
      }

      @Override
      public String[] getJwtUserRoles() {
        return mcuserRolesArrayToStringArray(mc.getRoles());
      }
    };
  }

  private final Logger log = LoggerFactory.getLogger(MCorpusJwtBackendHandler.class);

  private final MCorpusUserRepo mcuserRepo;

  public MCorpusJwtBackendHandler(final MCorpusUserRepo mcuserRepo) {
    this.mcuserRepo = mcuserRepo;
  }

  @Override
  public FetchResult<JwtBackendStatus> getBackendJwtStatus(UUID jwtId) {
    final FetchResult<JwtStatus> fr = mcuserRepo.getBackendJwtStatus(jwtId);
    final JwtBackendStatus jstat = isNull(fr.get()) ? null : map(fr.get());
    return fetchrslt(jstat, fr.getErrorMsg());
  }

  @Override
  public FetchResult<IJwtUser> getJwtUserInfo(UUID jwtUserId) {
    FetchResult<Mcuser> fr = mcuserRepo.fetchMcuser(jwtUserId);
    if (fr.isSuccess()) {
      final IJwtUser jwtUser = map(fr.get());
      return fetchrslt(jwtUser);
    } else {
      return fetchrslt(fr.getErrorMsg());
    }
  }

  @Override
  public FetchResult<List<IJwtInfo>> getActiveJwtLogins(UUID jwtUserId) {
    FetchResult<List<ActiveLoginDomain>> fr = mcuserRepo.getActiveLogins(jwtUserId);
    if (fr.isSuccess()) {
      final List<IJwtInfo> jlist = fr.get().stream()
        .map(al -> map(al))
        .collect(Collectors.toList());
        return fetchrslt(jlist, null);
    } else {
      return fetchrslt(fr.getErrorMsg());
    }
  }

  @Override
  public FetchResult<IJwtUser> jwtBackendLogin(String username, String pswd, UUID pendingJwtId,
      InetAddress requestOrigin, Instant requestInstant, Instant jwtExpiration) {
    log.debug("Authenticating mcuser '{}'..", username);
    final FetchResult<Mcuser> loginResult = mcuserRepo.login(username, pswd, pendingJwtId, jwtExpiration,
        requestInstant, requestOrigin);
    if (loginResult.isSuccess()) {
      return fetchrslt(map(loginResult.get()), loginResult.getErrorMsg());
    } else {
      return fetchrslt(loginResult.getErrorMsg());
    }
  }

  @Override
  public FetchResult<Boolean> jwtBackendLogout(UUID jwtUserId, UUID jwtId, InetAddress requestOrigin,
      Instant requestInstant) {
    FetchResult<Boolean> fr = mcuserRepo.logout(jwtUserId, jwtId, requestInstant, requestOrigin);
    return fr;
  }

  @Override
  public FetchResult<Boolean> jwtInvalidateAllForUser(UUID jwtUserId, InetAddress requestOrigin, Instant requestInstant) {
    return mcuserRepo.invalidateJwtsFor(jwtUserId, requestInstant, requestOrigin);
  }

}