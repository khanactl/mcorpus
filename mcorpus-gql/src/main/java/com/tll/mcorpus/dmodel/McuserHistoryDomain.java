package com.tll.mcorpus.dmodel;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class McuserHistoryDomain {

  public static class LoginEventDomain {
    public final UUID jwtId;
    public final OffsetDateTime timestamp;
    public final InetAddress requestOrigin;

    public LoginEventDomain(UUID jwtId, OffsetDateTime timestamp, InetAddress requestOrigin) {
      this.jwtId = jwtId;
      this.timestamp = timestamp;
      this.requestOrigin = requestOrigin;
    }
  }

  public static class LogoutEventDomain {
    public final UUID jwtId;
    public final OffsetDateTime timestamp;
    public final InetAddress requestOrigin;

    public LogoutEventDomain(UUID jwtId, OffsetDateTime timestamp, InetAddress requestOrigin) {
      this.jwtId = jwtId;
      this.timestamp = timestamp;
      this.requestOrigin = requestOrigin;
    }
  }

  public final UUID uid;
  public final List<LoginEventDomain> logins;
  public final List<LogoutEventDomain> logouts;

  /**
   * Constructor.
   *
   * @param uid
   * @param logins
   * @param logouts
   */
  public McuserHistoryDomain(UUID uid, List<LoginEventDomain> logins, List<LogoutEventDomain> logouts) {
    this.uid = uid;
    this.logins = logins;
    this.logouts = logouts;
  }

  /**
   * Constructor - When no mcuser history exists.
   *
   * @param uid
   */
  public McuserHistoryDomain(UUID uid) {
    this.uid = uid;
    this.logins = Collections.emptyList();
    this.logouts = Collections.emptyList();
  }
}