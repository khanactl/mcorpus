package com.tll.mcorpus.web;

import static com.tll.core.Util.upper;

import java.util.List;

import com.tll.web.JWTUserGraphQLWebContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;

/**
 * MCorpus-specific authorization directive GraphQL extension.
 * <p>
 * Enforces a role-based access control schema at the graphql query exectution
 * level based on declared authorization constraints in the mcorpus GraphQL
 * schema.
 *
 * @author jpk
 */
class AuthorizationDirective implements SchemaDirectiveWiring {

  private final Logger log = LoggerFactory.getLogger(AuthorizationDirective.class);

  /**
   * The GraphQL query/mutation field defined roles for the mcorpus graphql
   * schema.
   */
  @Override
  public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> env) {
    final String roletok = (String) env.getDirective().getArgument("role").getValue();
    final GraphQLRole targetRole = GraphQLRole.fromString(upper(roletok));

    final GraphQLFieldDefinition f = env.getElement();
    final GraphQLFieldsContainer parentType = env.getFieldsContainer();

    final DataFetcher<?> original = env.getCodeRegistry().getDataFetcher(parentType, f);
    final DataFetcher<?> auth = new DataFetcher<Object>() {

      @Override
      public Object get(DataFetchingEnvironment environment) throws Exception {
        final JWTUserGraphQLWebContext webContext = environment.getContext();
        final List<GraphQLRole> requestingRoles = GraphQLRole.fromCommaDelimitedString(webContext.getJwtStatus().roles());
        // log.debug("Authorizing access to {} requiring role {} for requesting role(s) {}..", f.getName(), targetRole, requestingRoles);
        if(targetRole.isAuthorized(requestingRoles)) {
          // authorized
          // log.debug("Role(s) {} authorized for {}", requestingRoles, f.getName());
          try {
            return original.get(environment);
          } catch(Exception e) {
            log.error("Data fetching error for {}: {}", f.getName(), e.getMessage());
          }
        } else {
          // not authorized
          log.debug("Role(s) {} NOT authorized for {}", requestingRoles, f.getName());
        }
        // default
        return null;
      }
    };
    // now change the field definition to have the new authorising data fetcher
    env.getCodeRegistry().dataFetcher(parentType, f, auth);

    return f;
  }
}