package com.tll.mcorpus.web;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNullOrEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The GraphQL query/mutation field defined roles for the mcorpus graphql
 * schema.
 *
 * @author jpk
 */
public enum GraphQLRole {
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
