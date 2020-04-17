package com.tll.mcorpus.web;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.upper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
class MCorpusAuthorizationDirective implements SchemaDirectiveWiring {

  public static enum GraphQLRole {
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
    public boolean isAuthorized(final Collection<GraphQLRole> requestingRoles) {
      if (requestingRoles == null || requestingRoles.size() < 1)
        return false;
      for (final GraphQLRole requestingRole : requestingRoles) {
        switch (this) {
        case MCORPUS:
          switch (requestingRole) {
          case MCORPUS:
          case ADMIN:
            return true;
          default:
            break;
          }
          break;
        case MEMBER:
          switch (requestingRole) {
          case MCORPUS:
          case MEMBER:
          case ADMIN:
            return true;
          default:
            break;
          }
          break;
        case MPII:
          switch (requestingRole) {
          case MPII:
          case ADMIN:
            return true;
          default:
            break;
          }
          break;
        case ADMIN:
          switch (requestingRole) {
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

    public static GraphQLRole fromString(final String s) {
      if (isNullOrEmpty(s))
        return null;
      for (final GraphQLRole r : GraphQLRole.values())
        if (r.name().equals(s))
          return r;
      return null;
    }

    public static List<GraphQLRole> fromCommaDelimitedString(final String s) {
      if (isNullOrEmpty(s)) return Collections.emptyList();
      String[] sarr = s.split(",");
      if (sarr.length < 1) return Collections.emptyList();
      List<GraphQLRole> rlist = new ArrayList<GraphQLRole>(sarr.length);
      for (int i = 0; i < sarr.length; i++) {
        GraphQLRole role = fromString(clean(sarr[i]));
        if (role != null)
          rlist.add(role);
      }
      return rlist;
    }

    public static boolean hasAdminRole(final String roles) {
      return fromCommaDelimitedString(roles).contains(GraphQLRole.ADMIN);
    }

  }

  private final Logger log = LoggerFactory.getLogger(MCorpusAuthorizationDirective.class);

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