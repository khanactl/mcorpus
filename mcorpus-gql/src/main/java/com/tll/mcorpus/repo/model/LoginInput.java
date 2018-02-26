package com.tll.mcorpus.repo.model;

import com.google.common.base.MoreObjects;

/**
 * Encapsulates input for web based login.
 * 
 * @author d2d
 */
public class LoginInput {
  private final String username;
  private final String password;

  private final String webSessionId;
  private final String ip; // remote addr i.e. host
  private final String httpHost;
  private final String httpOrigin;
  private final String httpReferer;
  private final String httpForwarded;

  /**
   * Constructor.
   *
   * @param username
   * @param password
   * @param webSessionId
   * @param ip
   * @param httpHost
   * @param httpOrigin
   * @param httpReferer
   * @param httpForwarded
   */
  public LoginInput(String username, String password, String webSessionId, String ip, String httpHost, String httpOrigin, String httpReferer, String httpForwarded) {
    this.username = username;
    this.password = password;
    this.webSessionId = webSessionId;
    this.ip = ip;
    this.httpHost = httpHost;
    this.httpOrigin = httpOrigin;
    this.httpReferer = httpReferer;
    this.httpForwarded = httpForwarded;
  }

  /**
   * @return true if this login input is valid, false otherwise.
   */
  public boolean isValid() {
    return
      username != null
      && password != null
      && webSessionId != null
      && ip != null;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getWebSessionId() {
    return webSessionId;
  }

  /**
   * @return the IP address of the incoming request gotten from the remoteAddr http header.
   */
  public String getIp() {
    return ip;
  }

  public String getHttpHost() {
    return httpHost;
  }

  public String getHttpOrigin() {
    return httpOrigin;
  }

  public String getHttpReferer() {
    return httpReferer;
  }

  public String getHttpForwarded() {
    return httpForwarded;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("username", username)
      .add("ip", ip)
      .add("webSessionId", webSessionId)
      .add("httpHost", httpHost)
      .add("httpOrigin", httpOrigin)
      .add("httpReferer", httpReferer)
      .add("httpForwarded", httpForwarded)
      .toString();
  }
}
