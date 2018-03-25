package com.tll.mcorpus.repo;

import java.util.UUID;

import javax.sql.DataSource;

import com.tll.mcorpus.db.udt.pojos.Mref;
import com.tll.mcorpus.repo.model.FetchResult;

import ratpack.exec.Blocking;
import ratpack.exec.Promise;

public class MCorpusRepoAsync extends MCorpusRepo {

  /**
   * Constructor.
   *
   * @param ds the data source
   */
  public MCorpusRepoAsync(final DataSource ds) {
    super(ds);
  }

  public Promise<FetchResult<Mref>> fetchMRefByMidAsync(final UUID mid) {
    return Blocking.get(() -> { return fetchMRefByMid(mid); });
  }
  
}
