package com.tll.mcorpus.web;

import java.util.List;

import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.context.ContextHelper;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authorizer that verifies the incoming request's PRT token.
 * 
 * @author jkirton
 */
public class RSTAuthorizer implements Authorizer<CommonProfile> {
  
  private static final Logger log = LoggerFactory.getLogger(RSTAuthorizer.class);

  private final String parameterName = RSTGenerator.RST_TOKEN_NAME;

  private final String headerName = RSTGenerator.RST_TOKEN_NAME;

  private final boolean onlyCheckPostRequest;
  
  public RSTAuthorizer(boolean onlyCheckPostRequest) {
    this.onlyCheckPostRequest = onlyCheckPostRequest;
  }

  @Override
  public boolean isAuthorized(WebContext context, List<CommonProfile> profiles) throws HttpAction {
    final boolean checkRequest = !onlyCheckPostRequest || ContextHelper.isPost(context);
    if (checkRequest) {
      final String parameterToken = context.getRequestParameter(parameterName);
      final String headerToken = context.getRequestHeader(headerName);
      final String serverToken = (String) context.getRequestAttribute(RSTGenerator.RST_TOKEN_NAME);
      // NOTE: the serverPrtToken is that from the server session previously gotten upstream
      log.debug("server: {}, param: {}, header: {}", serverToken, parameterToken, headerToken);
      return serverToken != null && (serverToken.equals(parameterToken) || serverToken.equals(headerToken));
    } else {
      return true;
    }
  }

}
