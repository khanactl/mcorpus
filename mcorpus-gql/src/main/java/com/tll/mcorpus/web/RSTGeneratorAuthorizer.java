package com.tll.mcorpus.web;

import java.util.List;

import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RST token generator disguised as an {@link Authorizer}.
 *  
 * @author jkirton
 */
public class RSTGeneratorAuthorizer implements Authorizer<CommonProfile> {
  
  private static final Logger log = LoggerFactory.getLogger(RSTGeneratorAuthorizer.class);
  
  private final RSTGenerator rstGenerator;

  private String domain;

  private String path = "/";

  private Boolean httpOnly;

  private Boolean secure;

  /**
   * Constructor.
   */
  public RSTGeneratorAuthorizer() {
    this.rstGenerator = new RSTGenerator();
  }
  
  /**
   * Get the <em>current</em> RST token held in session.
   * 
   * @param context
   * @return RST or null if not present
   */
  public String currentRst(final WebContext context) {
    return rstGenerator.isSet(context) ? rstGenerator.get(context) : null;
  }
  
  @Override
  public boolean isAuthorized(final WebContext context, final List<CommonProfile> profiles) throws HttpAction {
    final String tokenCurrent = rstGenerator.get(context);
    context.setRequestAttribute(RSTGenerator.RST_TOKEN_NAME, tokenCurrent);
    
    // now reset for next request
    // downstream handling shall verify against the rst request attribute set above
    rstGenerator.reset(context);
    final String tokenNext = rstGenerator.get(context);
    
    log.debug("next: {}, current: {}", tokenNext, tokenCurrent);
    
    context.setResponseHeader(RSTGenerator.RST_TOKEN_NAME, tokenNext);
    final Cookie cookie = new Cookie(RSTGenerator.RST_TOKEN_NAME, tokenNext);
    if (domain != null) {
        cookie.setDomain(domain);
    } else {
        cookie.setDomain(context.getServerName());
    }
    if (path != null) {
        cookie.setPath(path);
    }
    if (httpOnly != null) {
        cookie.setHttpOnly(httpOnly.booleanValue());
    }
    if (secure != null) {
        cookie.setSecure(secure.booleanValue());
    }
    context.addResponseCookie(cookie);
    
    return true;
  }

  public RSTGenerator getRSTGenerator() {
      return rstGenerator;
  }

  public String getDomain() {
      return domain;
  }

  public void setDomain(final String domain) {
      this.domain = domain;
  }

  public String getPath() {
      return path;
  }

  public void setPath(final String path) {
      this.path = path;
  }

  public Boolean getHttpOnly() {
      return httpOnly;
  }

  public void setHttpOnly(final Boolean httpOnly) {
      this.httpOnly = httpOnly;
  }

  public Boolean getSecure() {
      return secure;
  }

  public void setSecure(final Boolean secure) {
      this.secure = secure;
  }

  @Override
  public String toString() {
      return CommonHelper.toString(this.getClass(), 
          "rstGenerator", rstGenerator, 
          "domain", domain, 
          "path", path, 
          "httpOnly", httpOnly, 
          "secure", secure);
  }
}
