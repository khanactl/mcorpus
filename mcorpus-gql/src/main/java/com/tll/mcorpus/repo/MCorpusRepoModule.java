package com.tll.mcorpus.repo;

import javax.sql.DataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * MCorpusRepoModule module.
 * <p>
 * Created on 11/22/17.
 * 
 * @author jpk
 */
public class MCorpusRepoModule extends AbstractModule {

	@Override
	protected void configure() {}

	@Provides
	@Singleton
	MCorpusUserRepo mcorpusUserRepo(DataSource ds) {
		return new MCorpusUserRepo(ds);
	}

	@Provides
	@Singleton
	MCorpusRepo mcorpusRepo(DataSource ds) {
		return new MCorpusRepo(ds);
	}
}
