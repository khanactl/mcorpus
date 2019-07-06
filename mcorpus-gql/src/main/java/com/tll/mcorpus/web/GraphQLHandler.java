package com.tll.mcorpus.web;

import static com.tll.core.Util.not;
import static com.tll.mcorpus.web.RequestUtil.getOrCreateRequestSnapshot;
import static ratpack.jackson.Jackson.fromJson;
import static ratpack.jackson.Jackson.json;

import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.reflect.TypeToken;
import com.tll.jwt.IJwtBackendHandler;
import com.tll.jwt.JWT;
import com.tll.jwt.JWTHttpRequestStatus;
import com.tll.web.JWTUserGraphQLWebContext;
import com.tll.web.RequestSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.ErrorType;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.GraphqlErrorBuilder;
import graphql.execution.ExecutionId;
import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Handles graphql query requests.
 * 
 * @author jkirton
 */
public class GraphQLHandler implements Handler {

  private final Logger log = LoggerFactory.getLogger(GraphQLHandler.class);

  private final GraphQL graphQL;

  /**
   * Constructor.
   * 
   * @param graphQL the app scoped {@link GraphQL} instance.
   */
  public GraphQLHandler( final GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @SuppressWarnings("serial")
  private static final TypeToken<Map<String, Object>> strObjMapTypeRef = new TypeToken<Map<String, Object>>() { };
  
  @Override
  public void handle(Context ctx) throws Exception {
    ctx.parse(fromJson(strObjMapTypeRef)).then(qmap -> {
      
      final RequestSnapshot rsnap = getOrCreateRequestSnapshot(ctx);
      final JWTHttpRequestStatus jwtRequestStatus = ctx.getRequest().get(JWTHttpRequestStatus.class);
      
      // grab the http request info
      final String query = ((String) qmap.get("query"));
      log.debug("Received gql query:\n\n{}\n", query);
      
      @SuppressWarnings("unchecked")
      final Map<String, Object> vmap = (Map<String, Object>) qmap.get("variables");
      
      final JWTUserGraphQLWebContext gqlWebCtx = new JWTUserGraphQLWebContext(
        query, 
        vmap, 
        MCorpusJwtRequestProvider.fromRequestSnapshot(rsnap), 
        jwtRequestStatus, 
        ctx.get(JWT.class), 
        ctx.get(IJwtBackendHandler.class), 
        MCorpusJwtHttpResponseAction.fromRatpackContext(ctx), 
        "mclogin"
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
      case EXPIRED:
        // only mclogin and introspection queries are allowed when no valid JWT present
        if(gqlWebCtx.isJwtUserLoginOrIntrospectionQuery()) {
          // allowed - you may proceed
          break;
        }
        ctx.clientError(401); // unauthorized
        return;

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
                        .context(gqlWebCtx)
                        .build();
      graphQL.executeAsync(executionInput).thenAccept(executionResult -> {
        if (executionResult.getErrors().isEmpty()) {
          ctx.render(json(executionResult.toSpecification()));
          log.info("graphql request {} handled successfully.", gqlWebCtx.getExecutionId());
        } else {
          ctx.render(json(executionResult.getErrors().stream().map(err -> {
            if(err.getErrorType() == ErrorType.ValidationError) {
              return GraphqlErrorBuilder.newError()
                .errorType(err.getErrorType())
                .locations(err.getLocations())
                .message("Query validation error.  Check field name spelling, type-mismatching or missing fields.")
                .build();
            }
            else if(err.getErrorType() == ErrorType.InvalidSyntax) {
              return GraphqlErrorBuilder.newError()
                .errorType(err.getErrorType())
                .locations(err.getLocations())
                .message("Invalid query syntax.")
                .build();
            }
            // default
            return err;
          }).collect(Collectors.toList())));
          log.warn("graphql request {} handled with errors.", gqlWebCtx.getExecutionId());
        }
      });
    });
  }
}
