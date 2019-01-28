package com.tll.mcorpus.gql;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tll.mcorpus.repo.MCorpusRepo;
import com.tll.mcorpus.repo.MCorpusUserRepo;

public class MCorpusGraphQLModule extends AbstractModule {

  @Override
  protected void configure() {}

  @Provides
  @Singleton
  MCorpusGraphQL mCorpusGraphQL(MCorpusUserRepo mcuserRepo, MCorpusRepo mcorpusRepo) {
    return new MCorpusGraphQL(mcuserRepo, mcorpusRepo);
  }
}
