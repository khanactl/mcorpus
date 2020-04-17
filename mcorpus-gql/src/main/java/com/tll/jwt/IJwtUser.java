package com.tll.jwt;

import java.util.UUID;

/**
 * Defines the attributes of a JWT user for use in the web layer.
 *
 * @author jpk
 */
public interface IJwtUser {

  /**
   * @return the JWT user id.
   */
  UUID getJwtUserId();

  /**
   * @return <code>true</code> when the JWT user has administrative privileges.
   */
  boolean isAdministrator();

  /**
   * @return the JWT user roles.
   */
  String[] getJwtUserRoles();
}