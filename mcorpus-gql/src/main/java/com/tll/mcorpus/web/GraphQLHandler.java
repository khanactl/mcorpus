package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.not;
import static com.tll.mcorpus.web.RequestUtil.getOrCreateRequestSnapshot;
import static ratpack.jackson.Jackson.fromJson;
import static ratpack.jackson.Jackson.json;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
import com.tll.mcorpus.gql.MCorpusGraphQL;

import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Handles graphql query requests.
 * 
 * @author jkirton
 */
public class GraphQLHandler implements Handler {

  private static final Logger log = LoggerFactory.getLogger(GraphQLHandler.class);
  
  @SuppressWarnings("serial")
  private static final TypeToken<Map<String, Object>> strObjMapTypeRef = new TypeToken<Map<String, Object>>() { };
  
  @Override
  public void handle(Context ctx) throws Exception {
    ctx.parse(fromJson(strObjMapTypeRef)).then(qmap -> {
      // graphql (the api via http post)
      // grab the http request info
      final String query = ((String) qmap.get("query"));
      @SuppressWarnings("unchecked")
      final Map<String, Object> vmap = (Map<String, Object>) qmap.get("variables");
      final GraphQLWebQuery queryObject = new GraphQLWebQuery(query, vmap, getOrCreateRequestSnapshot(ctx));
      final GraphQLSchema schema = ctx.get(MCorpusGraphQL.class).getGraphQLSchema();
      final GraphQL graphQL = GraphQL.newGraphQL(schema).build();
      log.info("{}", queryObject);

      // validate graphql query request
      if (not(queryObject.isValid())) {
        log.error("Invalid graphql query.");
        ctx.clientError(403);
        return;
      }
      
      // execute the query
      final ExecutionInput executionInput =
          ExecutionInput.newExecutionInput()
                        .query(queryObject.getQuery())
                        .variables(queryObject.getVariables())
                        // the graphql context: GraphQLWebQuery type
                        .context(queryObject)
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
