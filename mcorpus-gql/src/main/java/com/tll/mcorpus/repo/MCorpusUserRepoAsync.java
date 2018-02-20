package com.tll.mcorpus.repo;

import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.repo.model.FetchResult;
import com.tll.mcorpus.repo.model.LoginInput;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;

import javax.sql.DataSource;

/**
 * MCorpus Repository (data access).
 * <p>
 * All public methods are <em>blocking</em>!
 *
 * @author jpk
 */
public class MCorpusUserRepoAsync extends MCorpusUserRepo {

  /**
   * Constructor.
   *
   * @param ds the data source
   */
  public MCorpusUserRepoAsync(final DataSource ds) {
    super(ds);
  }

  public Promise<FetchResult<Mcuser>> loginAsync(final LoginInput loginInput) {
    return Blocking.get(() -> { return login(loginInput); });
  }

}
