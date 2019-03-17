package com.tll.gql;

import static com.tll.core.Util.isNull;

import java.util.function.Function;
import java.util.function.Supplier;

import com.tll.repo.FetchResult;
import com.tll.validate.VldtnResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import graphql.validation.ValidationError;
import graphql.validation.ValidationErrorType;

/**
 * Processes GraphQL requests by CRUD operation type.
 * <p>
 * All public methods herein are expted to handle any exceptions that may occur.
 * The calling context shall NOT have to handle exceptions!
 * <p>
 * By routing all GraphQL requests through this processor, 
 * we now have the ability to consolidate the way these requests are handled
 * including consistent error handling in a project-agnostic manner.
 * <p>
 * Also, this processing construct affords us a way to separate mutating 
 * operations from fetch-only operations in a straight-forward way.
 * <p>
 * This class depends on the graphql-java library:
 * https://github.com/graphql-java/graphql-java.
 * 
 * @author jpk
 */
public class GraphQLRequestProcessor {

  private final Logger log = LoggerFactory.getLogger(GraphQLRequestProcessor.class);

  /**
   * Maps validation errors back to the calling GraphQL context.
   * 
   * @param  env the calling GraphQL context
   * @param vresult the validation result to map back to the GraphQL context
   */
  static void processInvalid(final DataFetchingEnvironment env, final VldtnResult vresult) {
    vresult.getErrors().stream().forEach(cv -> {
      env.getExecutionContext().addError(
        new ValidationError(
          ValidationErrorType.InvalidSyntax, 
          (SourceLocation) null, 
          cv.getVldtnErrMsg()));
    });
  }

  /**
   * Maps backend repo fetch errors back to the calling GraphQL context .
   * 
   * @param env the calling GraphQL context
   * @param emsg the fetch error message
   */
  static void processFetchError(final DataFetchingEnvironment env, final String emsg) {
    env.getExecutionContext().addError(new GraphQLDataFetchError(emsg));
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
  public <G, D> G handleMutation(
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
        if(fr.hasErrorMsg()) {
          processFetchError(env, fr.getErrorMsg());
        }
        return gpost;
      } else {
        processInvalid(env, vresult);
      }
    } catch(Exception e) {
      // mutation processing error
      log.error("Mutation processing error: {}", e.getMessage());
    }
    // default
    return null;
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
  public <G, D, GR> GR handleMutation(
    final DataFetchingEnvironment env, 
    final Supplier<G> gextractor, 
    final Function<G, FetchResult<D>> persistOp, 
    final Function<D, GR> trfmToFront
  ) {
    try {
      final G g = gextractor.get();
      final FetchResult<D> fr = persistOp.apply(g);
      final GR gpost = isNull(fr.get()) ? null : trfmToFront.apply(fr.get());
      if(fr.hasErrorMsg()) {
        processFetchError(env, fr.getErrorMsg());
      }
      return gpost;
    } catch(Exception e) {
      // mutation processing error
      log.error("Mutation processing error: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Process a GraphQL mutation request where the persist operation return 
   * type is not a {@link FetchResult}.
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
  public <G, D, GR> GR handleSimpleMutation(
    final DataFetchingEnvironment env, 
    final Supplier<G> gextractor, 
    final Function<G, D> persistOp, 
    final Function<D, GR> trfmToFront
  ) {
    try {
      final G g = gextractor.get();
      final D d = persistOp.apply(g);
      final GR gpost = isNull(d) ? null : trfmToFront.apply(d);
      return gpost;
    } catch(Exception e) {
      // mutation processing error
      log.error("Mutation [simple] processing error: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Process a GraphQL mutation request whose persist operation returns a simple boolean.
   * 
   * @param env
   * @param persisOp the persist op that returns {@link Boolean}.
   * @return true upon success, false otherwise.
   */
  public boolean handleSimpleMutation(
    final DataFetchingEnvironment env, 
    final Supplier<Boolean> persistOp 
  ) {
    try {
      final Boolean rval = persistOp.get();
      return isNull(rval) ? false : rval.booleanValue();
    } catch(Exception e) {
      // mutation processing error
      log.error("Mutation by supplier processing error: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Do a backend entity deletion for the case of a signle key 
   * input argument and a boolean return value.
   * 
   * @param <KG> the frontend key type
   * @param <KD> the backend domain key type
   * 
   * @param env the GraphQL data fetching environment ref
   * @param extractor function to extract the frontend key type
   * @param xfrmToBack transforms frontend key type to backend key type
   * @param deleteOp the {@link FetchResult} provider that performs the backend deletion
   * @return true when the delete op was run without error, false otherwise.
   */
  public <KG, KD> boolean handleDeletion(
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
        return fr.get().booleanValue();
      } 
      if(fr.hasErrorMsg()) {
        processFetchError(env, fr.getErrorMsg());
      }
    } catch(Exception e) {
      log.error("Deletion by simple PK processing error: {}", e.getMessage());
    }
    // delete error
    return false;
  }

  /**
   * Do a fetch op with a single simple input argument.
   * 
   * @param env the GraphQL data fetching environment ref
   * @param argExtractor function that extracts the sole input argument 
   *                     from the GraphQL <code>env</code>
   * @param fetchOp the fetch operation function
   * @param toFrontXfrm the backend to frontend transform function
   * @return the transformed backend result type
   */
  public <A, G, D> G fetch(
    final DataFetchingEnvironment env, 
    final Supplier<A> argExtractor, 
    final Function<A, FetchResult<D>> fetchOp, 
    final Function<D, G> toFrontXfrm 
  ) {
    try {
      final A key = argExtractor.get();
      final FetchResult<D> fr = fetchOp.apply(key);
      if(fr.hasErrorMsg()) {
        processFetchError(env, fr.getErrorMsg());
      }
      if(fr.isSuccess()) {
        final G g = toFrontXfrm.apply(fr.get());
        return g;
      } 
    } catch(Exception e) {
      log.error("Fetch processing error: {}", e.getMessage());
    }
    // default
    return null;
  }

  /**
   * Do a fetch op for a single object/entity input argument 
   * that validates the input before persisting.
   * 
   * @param env the GraphQL data fetching environment ref
   * @param argExtractor function that extracts the sole input argument 
   *                     from the GraphQL <code>env</code>
   * @param argVldtn optional input arg validator
   * @param argToBackXfrm transform input argument from frontend type to backend type
   * @param fetchOp the fetch operation function
   * @param toFrontXfrm the backend to frontend transform function
   * @return the transformed backend result type
   */
  public <AG, AD, G, D> G fetch(
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
        if(fr.hasErrorMsg()) {
          processFetchError(env, fr.getErrorMsg());
        }
        if(fr.isSuccess()) {
          final G g = toFrontXfrm.apply(fr.get());
          return g;
        } 
      } else {
        processInvalid(env, vresult);
      }
    } catch(Exception e) {
      log.error("Fetch processing error: {}", e.getMessage());
    }
    // default
    return null;
  }

  /**
   * Process a simple GraphQL op.
   * 
   * @param <G> the GraphQL type
   * 
   * @param env the GraphQL context
   * @param op provides the G type return value
   * @return the target frontend GraphQL type
   */
  public <G> G process(
    final DataFetchingEnvironment env, 
    final Supplier<G> op 
  ) {
    try {
      return op.get();
    } catch(Exception e) {
      log.error("Process error: {}", e.getMessage());
      return null;
    }
  }
}