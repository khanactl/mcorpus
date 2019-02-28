package com.tll.mcorpus.transformapi;

import java.util.Map;

/**
 * Contract for instantiating and transforming frontend GraphQL objects 
 * to and from backend domain objects.
 * <p>
 * IMPT: <code>null</code> input shoulld produce <code>null</code> output 
 * for these transform methods generally speaking.
 * 
 * @param <G> the frontend GraphQL type
 * @param <D> the backend domain type
 * 
 * @author jpk
 */
public interface IEntityTransformer<G, D> {

  /**
   * Responsible for instantiating a frontend entity 
   * from a GraphQL key value field map that is passed 
   * to the backend for a query operation.
   *
   * @param gqlMap the GraphQL field map
   * @return Newly created frontend entity or null if <code>gqlMap</code> is null or empty
   */
  G fromGraphQLMap(final Map<String, Object> gqlMap);

  /**
   * Responsible for instantiating a frontend entity 
   * from a GraphQL key value field map for a backend add/insert op.
   *
   * @param gqlMap the GraphQL field map
   * @return Newly created frontend entity or null if <code>gqlMap</code> is null or empty
   */
  G fromGraphQLMapForAdd(final Map<String, Object> gqlMap);

  /**
   * Responsible for instantiating a frontend entity 
   * from a GraphQL key value field map for a backend update op.
   *
   * @return Newly created frontend entity or null if <code>gqlMap</code> is null or empty
   */
  G fromGraphQLMapForUpdate(final Map<String, Object> gqlMap);

  /**
   * Responsible for transforming a frontend entity to a backend entity.
   * 
   * @param e the frontend entity
   * @return Newly created backend entity or null if <code>e</code> is null
   */
  D toBackend(final G e);

  /**
   * Responsible for transforming a backend entity to a frontend entity.
   * 
   * @param b the backend entity
   * @return Newly created frontend entity or null if <code>d</code> is null
   */
  G fromBackend(final D d);

}