package com.tll.mcorpus.repo.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class McuserHistory {

  public static class LoginEvent {
    public final UUID jwtId;
    public final Date timestamp;

    public LoginEvent(UUID jwtId, Date timestamp) {
      this.jwtId = jwtId;
      this.timestamp = timestamp;
    }
  }

  public static class LogoutEvent {
    public final UUID jwtId;
    public final Date timestamp;

    public LogoutEvent(UUID jwtId, Date timestamp) {
      this.jwtId = jwtId;
      this.timestamp = timestamp;
    }
  }

  public final UUID uid;
  public final List<LoginEvent> logins;
  public final List<LogoutEvent> logouts;

  /**
   * Constructor.
   * 
   * @param uid
   * @param logins
   * @param logouts
   */
  public McuserHistory(UUID uid, List<LoginEvent> logins, List<LogoutEvent> logouts) {
    this.uid = uid;
    this.logins = logins;
    this.logouts = logouts;
  }

  /**
   * Constructor - When no mcuser history exists.
   * 
   * @param uid
   */
  public McuserHistory(UUID uid) {
    this.uid = uid;
    this.logins = Collections.emptyList();
    this.logouts = Collections.emptyList();
  }
}