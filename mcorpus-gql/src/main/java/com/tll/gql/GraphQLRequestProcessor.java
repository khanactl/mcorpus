package com.tll.gql;

import static com.tll.core.Util.isNull;

import java.util.Map;
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
 * This class depends on the graphql-java library:
 * https://github.com/graphql-java/graphql-java.
 * 
 * @author jpk
 */
public class GraphQLRequestProcessor {

  private final Logger log = LoggerFactory.getLogger(GraphQLRequestProcessor.class);

  /**
   * Process a GraphQL mutation request.
   * <p>
   * Steps:
   * <ul>
   * <li>Transform GraphQL field map to fronend entity type
   * <li>Validate
   * <li>Transform to backend domain type
   * <li>Do the persist operation
   * <li>Transform the returned persisted entity to a frontend entity type
   * </ul>
   * 
   * @param <G> the GraphQl entity type
   * @param <D> the backend domain entity type
   * 
   * @param arg0 the [first] GraphQL mutation method input argument name 
   *             as defined in the loaded GraphQL schema
   * @param env the GraphQL data fetching env object
   * @param tfrmFromGqlMap constitutes a frontend GraphQL type from the 
   *                       GraphQL data fetching environment (param <code>env</code>).
   * @param vldtn the optional validation function to use to validate the frontend object <code>e</code>
   * @param tfrmToBack the frontend to backend transform function
   * @param persistOp the backend repository persist operation function
   * @param tfrmToFront the backend to frontend transform function
   * @return the returned entity from the backend persist operation
   *         which should be considered to be the post-mutation 
   *         current state of the entity.
   */
  public <G, D> G handleMutation(
    final String arg0, 
    final DataFetchingEnvironment env, 
    final Function<Map<String, Object>, G> tfrmFromGqlMap, 
    final Function<G, VldtnResult> vldtn, 
    final Function<G, D> tfrmToBack, 
    final Function<D, FetchResult<D>> persistOp, 
    final Function<D, G> trfmToFront
  ) {
    try {
      final G g = tfrmFromGqlMap.apply(env.getArgument(arg0));
      final VldtnResult vresult = isNull(vldtn) ? VldtnResult.VALID : vldtn.apply(g);
      if(vresult.isValid()) {
        final D d = tfrmToBack.apply(g);
        final FetchResult<D> fr = persistOp.apply(d);
        if(fr.isSuccess()) {
          final G gpost = trfmToFront.apply(fr.get());
          return gpost;
        } 
        if(fr.hasErrorMsg()) {
          final String emsg = fr.getErrorMsg();
          env.getExecutionContext().addError(
            new ValidationError(
                ValidationErrorType.InvalidSyntax, 
                (SourceLocation) null, 
                emsg));
        }
      } else {
        vresult.getErrors().stream().forEach(cv -> {
          env.getExecutionContext().addError(
            new ValidationError(
              ValidationErrorType.InvalidSyntax, 
              (SourceLocation) null, 
              cv.getVldtnErrMsg()));
        });
      }
    } catch(Exception e) {
      // mutation processing error
      log.error("Mutation processing error: {}", e.getMessage());
    }
    // default
    return null;
  }

  /**
   * Generalized backend entity deletion routine.
   * <p>
   * Use when the backend primary key is not a simple type.
   * 
   * @param <G> the GraphQl entity type
   * 
   * @param env the GraphQL data fetching environment ref
   * @param deleteOp the {@link FetchResult} provider that performs the backend deletion
   * @return true when the delete op was run without error, false otherwise.
   */
  public <G> boolean handleDeletion(
    final DataFetchingEnvironment env, 
    final Supplier<FetchResult<Boolean>> deleteOp 
  ) {
    try {
      final FetchResult<Boolean> fr = deleteOp.get();
      if(fr.isSuccess()) {
        return true;
      } 
      if(fr.hasErrorMsg()) {
        final String emsg = fr.getErrorMsg();
        env.getExecutionContext().addError(
          new ValidationError(
              ValidationErrorType.InvalidSyntax, 
              (SourceLocation) null, 
              emsg));
      }
    } catch(Exception e) {
      log.error("Deletion by op processing error: {}", e.getMessage());
    }
    // default
    return false;
  }

  /**
   * Do a backend entity deletion for the case of a simple primary key input argument.
   * 
   * @param <PK> the primary key type
   * @param <G> the GraphQl entity type
   * @param <D> the backend domain entity type
   * 
   * @param env the GraphQL data fetching environment ref
   * @param pk the entity primary key value
   * @param deleteOp the {@link FetchResult} provider that performs the backend deletion
   * @return true when the delete op was run without error, false otherwise.
   */
  public <PK, G, B> boolean handleDeletion(
    final DataFetchingEnvironment env, 
    final PK pk,
    final Function<PK, FetchResult<Boolean>> deleteOp 
  ) {
    try {
      final FetchResult<Boolean> fr = deleteOp.apply(pk);
      if(fr.isSuccess()) {
        return true;
      } 
      if(fr.hasErrorMsg()) {
        final String emsg = fr.getErrorMsg();
        env.getExecutionContext().addError(
          new ValidationError(
              ValidationErrorType.InvalidSyntax, 
              (SourceLocation) null, 
              emsg));
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
   *        from the GraphQL <code>env</code>
   * @param fetchOp the fetch operation function
   * @param toFrontXfrm the backend to frontend transform function
   */
  public <A, G, D> G fetch(
    final DataFetchingEnvironment env, 
    final Function<DataFetchingEnvironment, A> argExtractor, 
    final Function<A, FetchResult<D>> fetchOp, 
    final Function<D, G> toFrontXfrm 
  ) {
    final A key = argExtractor.apply(env);
    final FetchResult<D> fr = fetchOp.apply(key);
    return processFetchResult(fr, env, toFrontXfrm);
  }

  /**
   * Do a fetch op with a single object/entity input argument.
   * 
   * @param env the GraphQL data fetching environment ref
   * @param argExtractor function that extracts the sole input argument 
   *        from the GraphQL <code>env</code>
   * @param argToBackXfrm transform input argument from frontend type to backend type
   * @param fetchOp the fetch operation function
   * @param toFrontXfrm the backend to frontend transform function
   */
  public <AG, AD, G, D> G fetch(
    final DataFetchingEnvironment env, 
    final Function<DataFetchingEnvironment, AG> argExtractor, 
    final Function<AG, AD> argToBackXfrm, 
    final Function<AD, FetchResult<D>> fetchOp, 
    final Function<D, G> toFrontXfrm 
  ) {
    final AG gin = argExtractor.apply(env);
    final AD din = argToBackXfrm.apply(gin);
    final FetchResult<D> fr = fetchOp.apply(din);
    return processFetchResult(fr, env, toFrontXfrm);
  }

  /**
   * Processes a {@link FetchResult} that wraps a simple (non-entity) type.
   * <p>
   * Any backend errors will be translated then added to the GraphQL 
   * fetching environment and <code>null</code> is returned.
   * 
   * @param <T> the simple type in the given fetch result (usually {@link Boolean})
   * @param fr the fetch result
   * @param env the GraphQL data fetching environment ref
   * @return the extracted fetch result value
   */
  public <T> T processFetchResult(
    final FetchResult<T> fr, 
    final DataFetchingEnvironment env
  ) {
    try {
      if(fr.isSuccess()) {
        return fr.get();
      } 
      if(fr.hasErrorMsg()) {
        final String emsg = fr.getErrorMsg();
        env.getExecutionContext().addError(
          new ValidationError(
              ValidationErrorType.InvalidSyntax, 
              (SourceLocation) null, 
              emsg));
      }
    } catch(Exception e) {
      log.error("Fetch result processing error: {}", e.getMessage());
    }
    // default
    return null;
  }

  /**
   * Processes a {@link FetchResult} 
   * transforming the backend result type to the frontend GraphQL type.
   * <p>
   * Any backend errors will be translated then added to the GraphQL 
   * fetching environment and <code>null</code> is returned.
   * 
   * @param <G> the GraphQl entity type
   * @param <D> the backend domain type
   * 
   * @param fr the fetch result
   * @param env the GraphQL data fetching environment ref
   * @return the transformed fetch result value when no errors are present
   *         or null when errors are present
   */
  public <G, D> G processFetchResult(
    final FetchResult<D> fr, 
    final DataFetchingEnvironment env, 
    final Function<D, G> transform
  ) {
    try {
      if(fr.isSuccess()) {
        final G g = transform.apply(fr.get());
        return g;
      } 
      if(fr.hasErrorMsg()) {
        final String emsg = fr.getErrorMsg();
        env.getExecutionContext().addError(
          new ValidationError(
              ValidationErrorType.InvalidSyntax, 
              (SourceLocation) null, 
              emsg));
      }
    } catch(Exception e) {
      log.error("Fetch result (transforming) processing error: {}", e.getMessage());
    }
    // default
    return null;
  }

}