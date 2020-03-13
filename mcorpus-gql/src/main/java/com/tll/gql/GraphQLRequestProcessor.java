package com.tll.gql;

import static com.tll.core.Util.isNull;

import java.util.function.Function;
import java.util.function.Supplier;

import com.tll.repo.FetchResult;
import com.tll.validate.VldtnResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;

/**
 * Processes GraphQL requests by CRUD operation type.
 * <p>
 * All public methods herein are expected to handle any exceptions that may occur.
 * The calling context shall NOT have to handle exceptions.
 * <p>
 * By routing all GraphQL requests through this processor,
 * we now have the ability to consolidate the way these requests are handled
 * including consistent error handling in a project-agnostic manner.
 * <p>
 * Also, this processing construct affords us a way to separate mutating
 * operations from fetch-only operations in a straight-forward way.
 * <p>
 * This class depends only on the graphql-java library:
 * https://github.com/graphql-java/graphql-java.
 *
 * @author jpk
 */
public class GraphQLRequestProcessor {

  private final Logger log = LoggerFactory.getLogger(GraphQLRequestProcessor.class);

  /**
   * Domain Fetch Result - error free (success) case.
   *
   * @param <G> the graphql frontend type
   * @param data the target graphql data object
   * @return Newly created {@link DataFetcherResult}
   */
  @SuppressWarnings("unchecked")
  private static <G> DataFetcherResult<G> dfr(final G data) {
    return (DataFetcherResult<G>) DataFetcherResult.newResult().data(data).build();
  }

  /**
   * Domain Fetch Result - data *and* error msg case.
   *
   * @param <G> the graphql frontend type
   * @param ensg the data fetch error msg
   * @return Newly created {@link DataFetcherResult}
   */
  @SuppressWarnings("unchecked")
  private static <G> DataFetcherResult<G> dfr(final DataFetchingEnvironment env, final G data, final String emsg) {
    return (DataFetcherResult<G>) DataFetcherResult.newResult()
      .data(data)
      .error(GraphQLDataFetchError.inst(env.getExecutionStepInfo().getPath(), emsg))
      .build();
  }

  /**
   * Domain Fetch Result - error msg only case.
   *
   * @param <G> the graphql frontend type
   * @param emsg the data fetch error msg
   * @return Newly created {@link DataFetcherResult}
   */
  private static <G> DataFetcherResult<G> dfr(final DataFetchingEnvironment env, final String emsg) {
    return dfr(env, null, emsg);
  }

  /**
   * Domain Fetch Result - exception only case.
   *
   * @param <G> the graphql frontend type
   * @param ex the data fetch exception
   * @return Newly created {@link DataFetcherResult}
   */
  private static <G> DataFetcherResult<G> dfr(final DataFetchingEnvironment env, final Exception ex) {
    return dfr(env, null, ex.getMessage());
  }

  /**
   * Domain Fetch Result - validation errors only case.
   *
   * @param <G> the graphql frontend type
   * @param vresult the validation errors
   * @return Newly created {@link DataFetcherResult}
   */
  @SuppressWarnings("unchecked")
  private static <G> DataFetcherResult<G> dfr(final DataFetchingEnvironment env, final VldtnResult vresult) {
    return (DataFetcherResult<G>) DataFetcherResult.newResult()
      .error(GraphQLDataValidationError.inst(env.getExecutionStepInfo().getPath(), vresult))
      .build();
  }

  /**
   * Process a GraphQL mutation request.
   *
   * @param <G> the GraphQl entity type
   * @param <D> the backend domain entity type
   *
   * @param env the GraphQL data fetching env object
   * @param gextractor obtains the <G> entity from the GraphQL env
   * @param vldtn the optional validation function to use to validate the frontend g entity
   * @param tfrmToBack the frontend to backend transform function
   * @param persistOp the backend repository persist operation function
   * @param tfrmToFront the backend to frontend transform function
   * @return the returned entity from the backend persist operation
   *         which should be considered to be the post-mutation
   *         current state of the entity.
   */
  public <G, D> DataFetcherResult<G> handleMutation(
    final DataFetchingEnvironment env,
    final Supplier<G> gextractor,
    final Function<G, VldtnResult> vldtn,
    final Function<G, D> tfrmToBack,
    final Function<D, FetchResult<D>> persistOp,
    final Function<D, G> trfmToFront
  ) {
    try {
      final G g = gextractor.get();
      final VldtnResult vresult = isNull(vldtn) ? VldtnResult.VALID : vldtn.apply(g);
      if(vresult.isValid()) {
        final D d = tfrmToBack.apply(g);
        final FetchResult<D> fr = persistOp.apply(d);
        final G gpost = isNull(fr.get()) ? null : trfmToFront.apply(fr.get());
        if(fr.isSuccess()) {
          return dfr(gpost);
        }
        return dfr(env, gpost, fr.getErrorMsg());
      } else {
        return dfr(env, vresult);
      }
    } catch(Exception e) {
      // mutation processing error
      log.error("Mutation (extract, validate, transform, persist, transform) processing error: {}", e.getMessage());
      return dfr(env, e);
    }
  }

  /**
   * Process a GraphQL mutation request.
   *
   * @param <G> the GraphQl entity type
   * @param <D> the backend domain entity type
   * @param <GR> the frontend gql entity type to return
   *
   * @param env the GraphQL data fetching env object
   * @param gextractor obtains the <G> entity from the GraphQL env
   * @param persistOp the backend repository persist operation function
   * @param tfrmToFront the backend to frontend transform function
   * @return the returned entity from the backend persist operation
   *         which should be considered to be the post-mutation
   *         current state of the entity.
   */
  public <G, D, GR> DataFetcherResult<GR> mutate(
    final DataFetchingEnvironment env,
    final Supplier<G> gextractor,
    final Function<G, FetchResult<D>> persistOp,
    final Function<D, GR> trfmToFront
  ) {
    try {
      final G g = gextractor.get();
      final FetchResult<D> fr = persistOp.apply(g);
      final GR gpost = isNull(fr.get()) ? null : trfmToFront.apply(fr.get());
      if(fr.isSuccess()) {
        return dfr(gpost);
      }
      return dfr(env, gpost, fr.getErrorMsg());
    } catch(Exception e) {
      // mutation processing error
      log.error("Mutation (extract, persist, transform) processing error: {}", e.getMessage());
      return dfr(env, e);
    }
  }

  /**
   * Do a backend entity deletion for the case of a signle key
   * input argument and a boolean return value.
   *
   * @param <KG> the frontend key type
   * @param <KD> the backend domain key type
   *
   * @param env the GraphQL data fetching env object
   * @param extractor function to extract the frontend key type
   * @param xfrmToBack transforms frontend key type to backend key type
   * @param deleteOp the {@link FetchResult} provider that performs the backend deletion
   * @return true when the delete op was run without error, false otherwise.
   */
  public <KG, KD> DataFetcherResult<Boolean> delete(
    final DataFetchingEnvironment env,
    final Supplier<KG> extractor,
    final Function<KG, KD> xfrmToBack,
    final Function<KD, FetchResult<Boolean>> deleteOp
  ) {
    try {
      final KG key = extractor.get();
      final KD keyb = xfrmToBack.apply(key);
      final FetchResult<Boolean> fr = deleteOp.apply(keyb);
      if(fr.isSuccess()) {
        return dfr(fr.get());
      }
      return dfr(env, fr.get(), fr.getErrorMsg());
    } catch(Exception e) {
      log.error("Deletion by key (extract, transform, delete) processing error: {}", e.getMessage());
      return dfr(env, e);
    }
  }

  /**
   * Do a fetch op with a single simple input argument.
   *
   * @param env the GraphQL data fetching env object
   * @param argExtractor function that extracts the sole input argument
   *                     from the GraphQL <code>env</code>
   * @param fetchOp the fetch operation function
   * @param toFrontXfrm the backend to frontend transform function
   * @return the transformed backend result type
   */
  public <A, G, D> DataFetcherResult<G> fetch(
    final DataFetchingEnvironment env,
    final Supplier<A> argExtractor,
    final Function<A, FetchResult<D>> fetchOp,
    final Function<D, G> toFrontXfrm
  ) {
    try {
      final A key = argExtractor.get();
      final FetchResult<D> fr = fetchOp.apply(key);
      if(fr.isSuccess()) {
        final G g = toFrontXfrm.apply(fr.get());
        return dfr(g);
      }
      return dfr(env, fr.getErrorMsg());
    } catch(Exception e) {
      log.error("Fetch (extract, fetch, transform) processing error: {}", e.getMessage());
      return dfr(env, e);
    }
  }

  /**
   * Do a fetch op for a single object/entity input argument
   * that validates the input before fetching.
   *
   * @param env the GraphQL data fetching env object
   * @param argExtractor function that extracts the sole input argument
   *                     from the GraphQL <code>env</code>
   * @param argVldtn optional input arg validator
   * @param argToBackXfrm transform input argument from frontend type to backend type
   * @param fetchOp the fetch operation function
   * @param toFrontXfrm the backend to frontend transform function
   * @return the transformed backend result type
   */
  public <AG, AD, G, D> DataFetcherResult<G> fetch(
    final DataFetchingEnvironment env,
    final Supplier<AG> argExtractor,
    final Function<AG, VldtnResult> argVldtn,
    final Function<AG, AD> argToBackXfrm,
    final Function<AD, FetchResult<D>> fetchOp,
    final Function<D, G> toFrontXfrm
  ) {
    try {
      final AG gin = argExtractor.get();
      final VldtnResult vresult = isNull(argVldtn) ? VldtnResult.VALID : argVldtn.apply(gin);
      if(vresult.isValid()) {
        final AD din = argToBackXfrm.apply(gin);
        final FetchResult<D> fr = fetchOp.apply(din);
        if(fr.isSuccess()) {
          final G g = toFrontXfrm.apply(fr.get());
          return dfr(g);
        }
        return dfr(env, fr.getErrorMsg());
      } else {
        return dfr(env, vresult);
      }
    } catch(Exception e) {
      log.error("Fetch (extract, validate, transform, fetch, transform) processing error: {}", e.getMessage());
      return dfr(env, e);
    }
  }

  /**
   * Process a simple GraphQL op.
   *
   * @param <G> the GraphQL op return type
   *
   * @param env the GraphQL data fetching env object
   * @param op provides the G type return value
   * @return the returned op value
   */
  public <G> DataFetcherResult<G> process(
    final DataFetchingEnvironment env,
    final Supplier<G> op
  ) {
    try {
      return dfr(op.get());
    } catch(Exception e) {
      log.error("Process error: {}", e.getMessage());
      return dfr(env, e);
    }
  }
}