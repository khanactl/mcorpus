package com.tll.mcorpus.gql;

import static com.tll.mcorpus.Util.upper;

import com.tll.mcorpus.web.GraphQLWebQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;

/**
 * AuthorizationDirective.
 * <p>
 * Defines and enforces a simple role-based 
 * access control mechanism at the graphql query exectution level
 * for mcorpus.
 * 
 * @author jpk
 */
class AuthorizationDirective implements SchemaDirectiveWiring {

  private final Logger log = LoggerFactory.getLogger(AuthorizationDirective.class);

  /**
   * The GraphQL query/mutation field defined roles for the mcorpus graphql schema.
   */
  static enum Role {
    PUBLIC,
    MEMBER,
    MCORPUS;

    /**
     * Authorize a 'requesting' role against this (the ascribed or target) role.
     * 
     * @param requestingRole the role requesting access 
     * @return true when authorized and false when not authorized.
     */
    public boolean isAuthorized(final Role requestingRole) {
      if(requestingRole == null) return false;
      switch(this) {
        case MCORPUS:
          switch(requestingRole) {
            case MCORPUS:
              return true;
            default:
              return false;
          }
        case MEMBER:
          switch(requestingRole) {
            case MCORPUS:
            case MEMBER:
              return true;
            default:
              return false;
          }
        case PUBLIC:
          switch(requestingRole) {
            case MCORPUS:
            case MEMBER:
            case PUBLIC:
              return true;
            default:
              return false;
          }
        default: 
          return false; // deny by default
      }
    }

    public static Role fromString(final String s) {
      for(Role r : Role.values()) if(r.name().equals(s)) return r;
      return null;
    }

  } // enum Role

  @Override
  public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> env) {
    final String roletok = (String) env.getDirective().getArgument("role").getValue();
    final Role targetRole = Role.fromString(upper(roletok));
    final GraphQLFieldDefinition f = env.getElement();
    return f.transform(builder -> builder.dataFetcher(new DataFetcher() {
      @Override
      public Object get(final DataFetchingEnvironment dataFetchingEnvironment) {
        try {
          final GraphQLWebQuery webContext = dataFetchingEnvironment.getContext();
          final Role requestingRole = Role.fromString(webContext.getJwtStatus().role().getLiteral());
          log.debug("Authorizing access to {} requiring role {} for requesting role {}..", f.getName(), targetRole, requestingRole);
          if(targetRole.isAuthorized(requestingRole)) {
            // authorized
            return f.getDataFetcher().get(dataFetchingEnvironment);
          }
          // not authorized
          log.warn("Role {} not authorized for {}", requestingRole, f.getName());
          return null;
        } catch(Exception e) {
          log.error("Role check for {} error: {}", f.getName(), e.getMessage());
          return null;
        }
      }
    }));
  }
}