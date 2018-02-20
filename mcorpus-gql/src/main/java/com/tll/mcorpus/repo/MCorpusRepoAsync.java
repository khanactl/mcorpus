package com.tll.mcorpus.repo;

import com.tll.mcorpus.repo.model.FetchResult;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;

import javax.sql.DataSource;
import java.util.Map;
import java.util.UUID;

public class MCorpusRepoAsync extends MCorpusRepo {

  /**
   * Constructor.
   *
   * @param ds the data source
   */
  public MCorpusRepoAsync(final DataSource ds) {
    super(ds);
  }

  public Promise<FetchResult<Map<String, Object>>> fetchMRefByMidAsync(final UUID mid) {
    return Blocking.get(() -> { return fetchMRefByMid(mid); });
  }

}
