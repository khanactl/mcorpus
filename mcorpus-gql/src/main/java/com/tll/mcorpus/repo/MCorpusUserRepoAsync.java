package com.tll.mcorpus.repo;

import java.util.UUID;

import javax.sql.DataSource;

import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.repo.model.FetchResult;

import ratpack.exec.Blocking;
import ratpack.exec.Promise;

public class MCorpusUserRepoAsync extends MCorpusUserRepo {

  /**
   * Constructor.
   *
   * @param ds the data source
   */
  public MCorpusUserRepoAsync(final DataSource ds) {
    super(ds);
  }

  public Promise<FetchResult<Mcuser>> loginAsync(final McuserLogin mcuserLogin) {
    return Blocking.get(() -> { return login(mcuserLogin); });
  }

  public Promise<FetchResult<Boolean>> logoutAsync(final McuserLogout mcuserLogout) {
    return Blocking.get(() -> { return logout(mcuserLogout); });
  }

  public Promise<FetchResult<Boolean>> isJwtValidAsync(final UUID jwtId) {
    return Blocking.get(() -> { return isJwtIdValid(jwtId); });
  }
}
