package com.tll.mcorpus.repo;

import javax.sql.DataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

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
