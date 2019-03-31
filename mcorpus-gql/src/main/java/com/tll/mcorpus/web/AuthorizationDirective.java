package com.tll.mcorpus.web;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.upper;

import java.util.ArrayList;
import java.util.List;

import com.tll.web.JWTUserGraphQLWebContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;

/**
 * MCorpus-specific authorization directive GraphQL extension.
 * <p>
 * Enforces a role-based access control schema at the graphql query exectution level
 * based on declared authorization constraints in the mcorpus GraphQL schema.
 * 
 * @author jpk
 */
class AuthorizationDirective implements SchemaDirectiveWiring {

  private final Logger log = LoggerFactory.getLogger(AuthorizationDirective.class);

  /**
   * The GraphQL query/mutation field defined roles for the mcorpus graphql schema.
   */
  static enum Role {
    MEMBER,
    MCORPUS,
    MPII,
    ADMIN;

    /**
     * Authorize a 'requesting' role against this - the ascribed or target role.
     * 
     * @param requestingRoles the role(s) requesting access 
     * @return true when authorized and false when not authorized.
     */
    public boolean isAuthorized(final Role[] requestingRoles) {
      if(requestingRoles == null || requestingRoles.length < 1) return false;
      for(final Role requestingRole : requestingRoles) {
        switch(this) {
          case MCORPUS:
            switch(requestingRole) {
              case MCORPUS:
              case ADMIN:
                return true;
              default:
                break;
            }
            break;
          case MEMBER:
            switch(requestingRole) {
              case MCORPUS:
              case MEMBER:
              case ADMIN:
                return true;
              default:
                break;
            }
            break;
          case MPII:
            switch(requestingRole) {
              case MPII:
              case ADMIN:
                return true;
              default:
                break;
            }
            break;
          case ADMIN:
            switch(requestingRole) {
              case ADMIN:
                return true;
              default:
                break;
            }
            break;
          default: 
            break;
        }
      }
      return false; // deny by default
    }

    public static Role fromString(final String s) {
      if(isNullOrEmpty(s)) return null;
      for(final Role r : Role.values()) if(r.name().equals(s)) return r;
      return null;
    }

    public static Role[] fromCommaDelimitedString(final String s) {
      if(isNullOrEmpty(s)) return new Role[0];
      String[] sarr = s.split(",");
      if(sarr.length < 1) return new Role[0];
      List<Role> rlist = new ArrayList<Role>(sarr.length);
      for(int i = 0; i < sarr.length; i++) {
        Role role = fromString(clean(sarr[i]));
        if(role != null) rlist.add(role);
      }
      return rlist.toArray(new Role[rlist.size()]);
    }
  } // enum Role

  @Override
  public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> env) {
    final String roletok = (String) env.getDirective().getArgument("role").getValue();
    final Role targetRole = Role.fromString(upper(roletok));
    final GraphQLFieldDefinition f = env.getElement();
    return f.transform(builder -> builder.dataFetcher(dataFetchingEnvironment -> {
      final JWTUserGraphQLWebContext webContext = dataFetchingEnvironment.getContext();
      final Role[] requestingRoles = Role.fromCommaDelimitedString(webContext.getJwtStatus().roles());
      // log.debug("Authorizing access to {} requiring role {} for requesting role(s) {}..", f.getName(), targetRole, requestingRoles);
      if(targetRole.isAuthorized(requestingRoles)) {
        // authorized
        // log.debug("Role(s) {} authorized for {}", requestingRoles, f.getName());
        try {
          return f.getDataFetcher().get(dataFetchingEnvironment);
        } catch(Exception e) {
          log.error("Data fetching error for {}: {}", f.getName(), e.getMessage());
        }
      } else {
        // not authorized
        log.warn("Role(s) {} NOT authorized for {}", requestingRoles, f.getName());
      }
      // default 
      return null;
    }));
  }
}