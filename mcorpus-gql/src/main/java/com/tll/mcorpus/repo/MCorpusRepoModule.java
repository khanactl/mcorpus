package com.tll.mcorpus.repo;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tll.mcorpus.repo.MCorpusRepoAsync;
import com.tll.mcorpus.repo.MCorpusUserRepoAsync;

import javax.sql.DataSource;

/**
 * MCorpusRepoModule module.
 *
 * Created by jpk on 11/22/17.
 */
public class MCorpusRepoModule extends AbstractModule {

  @Override
  protected void configure() {}

  @Provides
  @Singleton
  MCorpusUserRepoAsync mcorpusUserRepo(DataSource ds) {
    return new MCorpusUserRepoAsync(ds);
  }

  @Provides
  @Singleton
  MCorpusRepoAsync mcorpusRepo(DataSource ds) {
    return new MCorpusRepoAsync(ds);
  }
}
