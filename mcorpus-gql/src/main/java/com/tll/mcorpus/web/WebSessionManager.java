package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.glog;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import net.jcip.annotations.ThreadSafe;

/**
 * In memory cache of server-side web sessions. 
 * 
 * @author jkirton
 */
@ThreadSafe
public final class WebSessionManager {
  
  /**
   *  The amount of time a newly created web session lasts by design.
   */
  public static final Duration webSessionDuration = Duration.ofMinutes(10);
  
  /**
   * Definition of the possible web sesssion status states.
   */
  public static enum WebSessionStatus {
    NO_SESSION_ID, 
    BAD_SESSION_ID,
    SESION_ID_EXPIRED, 
    VALID_SESSION_ID;
  }

  /**
   * Encapsulates the web sesssion status for a given http request.
   */
  public static class WebSessionInstance {
    private final WebSessionStatus status;
    private final WebSession webSession;
    
    /**
     * Constructor.
     *
     * @param status
     * @param webSession
     */
    private WebSessionInstance(WebSessionStatus status, WebSession webSession) {
      super();
      this.status = status;
      this.webSession = webSession;
    }

    /**
     * @return the status of this web session.
     */
    public WebSessionStatus getStatus() {
      return status;
    }

    /**
     * @return the web session object reference.
     */
    public WebSession getWebSession() {
      return webSession;
    }
  
  } // WebSessionInstance class
  
  /**
   * Encapsulates a single web session.
   */
  public static class WebSession {
    private final UUID sid;
    private final RequestSnapshot initiatingRequest;
    private transient UUID rst;

    /**
     * Constructor.
     *
     * @param sid the session id to ascribe
     * @param initiatingRequest snapshot of the http request that initiated this web session
     * @throws IllegalArgumentException when one or more required constructor arguments is null. 
     */
    public WebSession(UUID sid, RequestSnapshot initiatingRequest) {
      if(sid == null || initiatingRequest == null) throw new IllegalArgumentException();
      this.sid = sid;
      this.initiatingRequest = initiatingRequest;
      this.rst = null;
    }

    /**
     * @return the session id
     */
    public UUID sid() { return sid; }
    
    /**
     * @return the snapshot of the request that initiated this session.
     */
    public RequestSnapshot initiatingRequest() { return initiatingRequest; }
    
    /**
     * Is this web session expired based in the current request snapshot?
     * 
     * @param currentRequest the current request snapshot
     * @return true if this web session is expired
     */
    public boolean isExpired(RequestSnapshot currentRequest) {
      return currentRequest.getRequestInstant()
        .isAfter(initiatingRequest.getRequestInstant().plus(webSessionDuration));
    }
    
    /**
     * @return a physical copy of the currently held request sync token or null if
     *         it is not set.
     */
    public synchronized UUID currentRst() { 
      return rst == null ? null : 
        new UUID(rst.getMostSignificantBits(), rst.getLeastSignificantBits());
    }
    
    /**
     * Mutates the held request sync token to a new and strong random value
     * returning a memory distinct physical copy of the newly set value.
     * 
     * @return Never-null physical copy of the newly set request sync token value.
     */
    public synchronized UUID resetRst() {
      final UUID rand = UUID.randomUUID();
      this.rst = new UUID(rand.getMostSignificantBits(), rand.getLeastSignificantBits());
      glog().debug("rst reset to: {}", rand.toString());
      return rand;
    }
    
    @Override
    public String toString() { return String.format("sid: '%s', rst: '%s'", sid, rst); }
    
  } // WebSession class
  
  public static WebSessionInstance wsi(WebSessionStatus status, WebSession webSession) {
    return new WebSessionInstance(status, webSession);
  }
  
  private static final Cache<UUID, WebSession> cache;
  
  static {
    // we set the max web session size here
    // we allow only {maxSize} clients to have a session at a time
    // this is a good memery escalation constraint if we end up getting ddos'd
    final int maxSize = 20;
    cache = Caffeine.newBuilder()
      .maximumSize(20)
      .expireAfterWrite(webSessionDuration)
      // TODO add listener when sessions expire for logging
      .build();
    glog().info("Web session manager created (Max. size: {}, Session duration: {}).", maxSize, webSessionDuration.toString());
  }
  
  /**
   * Get the web session by the given session id.
   * <p>
   * NULL is returned if there is not web session by the given session id.
   * 
   * @param sid the web session id
   * @return the cached {@link WebSession} instance -OR- null if not present in the cache.
   */
  public static WebSession getSession(UUID sid) { return cache.getIfPresent(sid); }
  
  /**
   * Get the WebSession or call the given function when it isn't there initially.
   * 
   * @param sid the session id
   * @param whenNotPresent function that is called when not in cache
   * @return Never null {@link WebSession}.
   */
  public static WebSession getSession(UUID sid, Function<UUID, WebSession> whenNotPresent) {
    return cache.get(sid, whenNotPresent);
  }
  
  /**
   * Invalidte the {@link WebSession} bound to the given sesssion id (sid).
   * 
   * @param sid the id of the session to invalidate
   */
  public static void destroySession(final UUID sid) {
    cache.invalidate(sid);
  }

}
