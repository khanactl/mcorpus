package com.tll.web.ratpack;

import static com.tll.core.Util.isBlank;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.not;
import static ratpack.core.jackson.Jackson.fromJson;
import static ratpack.core.jackson.Jackson.json;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.reflect.TypeToken;
import com.tll.gql.GraphQLDataFetchError;
import com.tll.jwt.IJwtHttpRequestProvider;
import com.tll.jwt.IJwtHttpResponseAction;
import com.tll.jwt.JWT;
import com.tll.jwt.JWTHttpRequestStatus;
import com.tll.web.JWTUserGraphQLWebContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.ErrorType;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.GraphqlErrorBuilder;
import graphql.execution.ExecutionId;
import ratpack.core.handling.Context;
import ratpack.core.handling.Handler;

/**
 * Graphql request handler Ratpack style.
 *
 * @author jkirton
 */
public class GraphQLHandler implements Handler {

  private final Logger log = LoggerFactory.getLogger(GraphQLHandler.class);

  private final GraphQL graphQL;

  private final String jwtUserLoginQueryMethodName;

  private final String jwtUserLoginRefreshQueryMethodName;

  /**
   * Constructor.
   *
   * @param graphQL the app scoped {@link GraphQL} instance.
   * @param jwtUserLoginQueryMethodName the graphql query method name for JWT login.
   */
  public GraphQLHandler(final GraphQL graphQL, String jwtUserLoginQueryMethodName, String jwtUserLoginRefreshQueryMethodName) {
    this.graphQL = graphQL;
    this.jwtUserLoginQueryMethodName = jwtUserLoginQueryMethodName;
    this.jwtUserLoginRefreshQueryMethodName = jwtUserLoginRefreshQueryMethodName;
  }

  private static final TypeToken<Map<String, Object>> strObjMapTypeRef = new TypeToken<Map<String, Object>>() { };

  @Override
  public void handle(Context ctx) throws Exception {
    ctx.parse(fromJson(strObjMapTypeRef)).then(qmap -> {

      final JWT jwtbiz = ctx.get(JWT.class);
      final JWTHttpRequestStatus jwtRequestStatus = ctx.getRequest().get(JWTHttpRequestStatus.class);
      final IJwtHttpRequestProvider jwtRequestProvider = ctx.getRequest().get(IJwtHttpRequestProvider.class);
      final IJwtHttpResponseAction jwtResponseAction = ctx.getRequest().get(IJwtHttpResponseAction.class);

      // grab the http request info
      final String query = ((String) qmap.get("query"));
      log.debug("Received gql query:\n\n{}\n", query);

      @SuppressWarnings("unchecked")
      final Map<String, Object> vmap = (Map<String, Object>) qmap.get("variables");

      final JWTUserGraphQLWebContext gqlWebCtx = new JWTUserGraphQLWebContext(
        query,
        vmap,
        jwtRequestProvider,
        jwtRequestStatus,
        jwtbiz,
        jwtResponseAction,
        jwtUserLoginQueryMethodName,
        jwtUserLoginRefreshQueryMethodName
      );
      log.info("graphql query pending: {}.", gqlWebCtx);

      // validate graphql query request
      if(not(gqlWebCtx.isValid())) {
        log.error("graphql query {} is INVALID.", gqlWebCtx.getExecutionId());
        ctx.clientError(403);
        return;
      }

      switch(jwtRequestStatus.status()) {
      case NOT_PRESENT_IN_REQUEST:
      case EXPIRED: {
        // check refresh token status
        boolean requestOk = false;
        switch(jwtRequestStatus.refreshTokenStatus()) {
          case UNKNOWN:
          case NOT_PRESENT_IN_REQUEST:
          case REFRESH_TOKEN_CLAIM_EXPIRED:
            // login only allowed
            // only jwtLogin and introspection queries are allowed when no valid JWT present
            if(gqlWebCtx.isJwtUserLoginOrIntrospectionQuery()) {
              // allowed - you may proceed
              requestOk = true;
            }
            break;
          case VALID:
            // refresh login allowed
            if(gqlWebCtx.isJwtUserLoginRefreshOrIntrospectionQuery()) {
              // allowed - you may proceed
              requestOk = true;
            }
            break;
          default:
          case REFRESH_TOKEN_CLAIM_MISMATCH:
            // not cool
            break;
        }
        if(!requestOk) {
          ctx.clientError(401); // unauthorized
          return;
        }
      }
      case VALID:
        // mcuser logged in by jwt - you may proceed
        break;

      case BLOCKED:
        ctx.clientError(401); // unauthorized
        return;

      case ERROR:
        ctx.clientError(500); // server error
        return;

      default:
        ctx.clientError(403); // forbidden
        return;
      }

      // execute the query
      final ExecutionInput executionInput =
          ExecutionInput.newExecutionInput()
                        .query(gqlWebCtx.getQuery())
                        .variables(gqlWebCtx.getVariables())
                        .operationName(gqlWebCtx.getOpName())
                        .executionId(ExecutionId.from(gqlWebCtx.getExecutionId()))
                        .graphQLContext(builder -> builder.of(JWTUserGraphQLWebContext.class, gqlWebCtx))
                        .build();
      graphQL.executeAsync(executionInput).thenAccept(executionResult -> {
        if (executionResult.getErrors().isEmpty()) {
          ctx.render(json(executionResult.toSpecification()));
          log.info("graphql request {} handled successfully.", gqlWebCtx.getExecutionId());
        } else {
          log.error("graphql request {} execution error(s):\n\n{}\n", gqlWebCtx.getExecutionId(), executionResult.getErrors());
          ctx.render(json(executionResult.getErrors().stream().map(err -> {
            if(err.getErrorType() == ErrorType.DataFetchingException && err instanceof GraphQLDataFetchError) {
              // native type graphql error (no need to sanitize)
              return err;
            }
            else if(err.getErrorType() == ErrorType.ValidationError) {
              return GraphqlErrorBuilder.newError()
                .errorType(err.getErrorType())
                .locations(isNull(err.getLocations()) ? Collections.emptyList() : err.getLocations())
                .message("Query validation error.  Check field name spelling, type-mismatching or missing fields.")
                .build();
            }
            else if(err.getErrorType() == ErrorType.InvalidSyntax) {
              return GraphqlErrorBuilder.newError()
                .errorType(err.getErrorType())
                .locations(isNull(err.getLocations()) ? Collections.emptyList() : err.getLocations())
                .message("Invalid query syntax.")
                .build();
            }
            // default (sanitize!)
            return GraphqlErrorBuilder.newError()
              .errorType(err.getErrorType())
              .locations(isNull(err.getLocations()) ? Collections.emptyList() : err.getLocations())
              .message(isBlank(err.getMessage()) ? "An unspecified error happened." : err.getMessage())
              .build();
          }).collect(Collectors.toList())));
          log.warn("graphql request {} handled with errors.", gqlWebCtx.getExecutionId());
        }
      });
    });
  }
}
