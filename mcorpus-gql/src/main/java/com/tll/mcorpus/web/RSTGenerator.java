package com.tll.mcorpus.web;

import org.pac4j.core.context.WebContext;

/**
 * Request Synchronization Token (RST) generator.
 * <p>
 * Thread-safe.
 * 
 * @author jkirton
 */
public class RSTGenerator implements org.pac4j.core.authorization.authorizer.csrf.CsrfTokenGenerator {
  
  /**
   * The Request Synchronization Token (RST) name.
   */
  public static final String RST_TOKEN_NAME = "RST";
  
  /**
   * Does an RST token exist in the server-held user session?
   * 
   * @param context the jesssionid web context for this web session
   * @return true if an RST token is held in session cache and false if not
   */
  public synchronized boolean isSet(final WebContext context) {
    return context.getSessionAttribute(RST_TOKEN_NAME) != null;
  }
  
  /**
   * Set the PRT held in session to null.
   * 
   * @param context
   */
  public void reset(final WebContext context) {
    String token = (String) context.getSessionAttribute(RST_TOKEN_NAME);
    if (token != null) {
      synchronized (this) {
        token = (String) context.getSessionAttribute(RST_TOKEN_NAME);
        if (token != null) {
          context.setSessionAttribute(RST_TOKEN_NAME, null);
        }
      }
    }
  }

  /**
   * Get the RST for the current user session creating one if one doesn't currently exist.
   */
  @Override
  public String get(WebContext context) {
    String token = (String) context.getSessionAttribute(RST_TOKEN_NAME);
    if (token == null) {
      synchronized (this) {
        token = (String) context.getSessionAttribute(RST_TOKEN_NAME);
        if (token == null) {
          token = java.util.UUID.randomUUID().toString();
          context.setSessionAttribute(RST_TOKEN_NAME, token);
        }
      }
    }
    return token;
  }
}
