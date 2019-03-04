package com.tll.mcorpus.web;

import static com.tll.core.Util.not;
import static com.tll.mcorpus.web.RequestUtil.getOrCreateRequestSnapshot;
import static ratpack.jackson.Jackson.fromJson;
import static ratpack.jackson.Jackson.json;

import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.tll.jwt.JWTStatusInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.ExecutionInput;
import graphql.GraphQL;
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
      
      final JWTStatusInstance jwtStatusInst = ctx.getRequest().get(JWTStatusInstance.class);
      
      // grab the http request info
      final String query = ((String) qmap.get("query"));
      @SuppressWarnings("unchecked")
      final Map<String, Object> vmap = (Map<String, Object>) qmap.get("variables");
      
      final MCorpusGraphQLWebContext gqlWebCtx = new MCorpusGraphQLWebContext(query, vmap, getOrCreateRequestSnapshot(ctx), jwtStatusInst, ctx);
      log.info("{}", gqlWebCtx);
      
      // validate graphql query request
      if(not(gqlWebCtx.isValid())) {
        log.error("Invalid graphql query.");
        ctx.clientError(403);
        return;
      }
      
      switch(jwtStatusInst.status()) {
      case NOT_PRESENT_IN_REQUEST:
      case EXPIRED:
        // only mclogin and introspection queries are allowed when no valid JWT present
        if(gqlWebCtx.isMcuserLoginOrIntrospectionQuery()) {
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
                        .operationName(gqlWebCtx.getOperationName())
                        .context(gqlWebCtx)
                        .build();
      graphQL.executeAsync(executionInput).thenAccept(executionResult -> {
        if (executionResult.getErrors().isEmpty()) {
          ctx.render(json(executionResult.toSpecification()));
          log.info("graphql request handled successfully.");
        } else {
          ctx.render(json(executionResult.getErrors()));
          log.info("graphql request handled with errors.");
        }
      });
    });
  }
}
