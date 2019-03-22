package com.tll.jwt;

import java.util.UUID;

/**
 * Defines the attributes for a JWT user for use in the web layer.
 * 
 * @author jpk
 */
public interface IJwtUser {

  /**
   * @return the JWT user id.
   */
  UUID getJwtUserId();

  /**
   * @return the JWT user roles.
   */
  String[] getJwtUserRoles();
}