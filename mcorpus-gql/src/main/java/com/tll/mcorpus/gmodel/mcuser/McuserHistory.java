package com.tll.mcorpus.gmodel.mcuser;

import static com.tll.core.Util.copy;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.tll.gmodel.BaseEntity;
import com.tll.gmodel.UUIDKey;

public class McuserHistory extends BaseEntity<McuserHistory, UUIDKey> {

  public static class LoginEvent {
    public final UUID jwtId;
    public final Date timestamp;
    public final String clientOrigin;

    public LoginEvent(UUID jwtId, Date timestamp, String clientOrigin) {
      this.jwtId = jwtId;
      this.timestamp = copy(timestamp);
      this.clientOrigin = clientOrigin;
    }
  }

  public static class LogoutEvent {
    public final UUID jwtId;
    public final Date timestamp;
    public final String clientOrigin;

    public LogoutEvent(UUID jwtId, Date timestamp, String clientOrigin) {
      this.jwtId = jwtId;
      this.timestamp = copy(timestamp);
      this.clientOrigin = clientOrigin;
    }
  }

  public final UUIDKey mcuserKey;
  public final List<LoginEvent> logins;
  public final List<LogoutEvent> logouts;

  /**
   * Constructor - When no mcuser history exists.
   *
   * @param uid the parent mcuser id
   */
  public McuserHistory(UUID uid) {
    this(uid, null, null);
  }

  /**
   * Constructor.
   *
   * @param uid
   * @param logins
   * @param logouts
   */
  public McuserHistory(UUID uid, List<LoginEvent> logins, List<LogoutEvent> logouts) {
    this.mcuserKey = new UUIDKey(uid, "mcuser");
    this.logins = logins == null ? Collections.emptyList() : logins;
    this.logouts = logouts == null ? Collections.emptyList() : logouts;
  }

  @Override
  public UUIDKey getPk() { return  mcuserKey; }
}
