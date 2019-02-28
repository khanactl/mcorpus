package com.tll.mcorpus.gmodel;

/**
 * GraphQL entity definition.
 * <p>
 * All declared entity fields are expected to be of the same type 
 * of declared in the GraphQL schema this entity backs.
 * 
 * @param <E> the concrete entity type
 * @param <PK> the primary key type
 * 
 * @author jpk
 */
public interface IGEntity<E extends IGEntity<E, PK>, PK extends IKey> {

  /**
   * @return Never-null entity primary key which may or may not be set.
   */
  PK getPk();

}