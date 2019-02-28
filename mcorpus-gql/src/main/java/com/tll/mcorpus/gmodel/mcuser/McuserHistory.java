package com.tll.mcorpus.gmodel.mcuser;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.tll.mcorpus.gmodel.BaseEntity;
import com.tll.mcorpus.gmodel.IKey;

public class McuserHistory extends BaseEntity<McuserHistory, IKey> {

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

  public final IKey pk; // really the related one pk
  public final UUID uid;
  public final List<LoginEvent> logins;
  public final List<LogoutEvent> logouts;

  /**
   * Constructor - When no mcuser history exists.
   * 
   * @param uid
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
    this.pk = IKey.uuid("McuserHistory", uid);
    this.uid = uid;
    this.logins = logins == null ? Collections.emptyList() : logins;
    this.logouts = logouts == null ? Collections.emptyList() : logouts;
  }

  @Override
  public IKey getPk() { return  pk; }
}
