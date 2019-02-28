package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.clean;
import static com.tll.mcorpus.Util.isBlank;
import static com.tll.mcorpus.Util.isNull;
import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;
import static com.tll.mcorpus.web.RequestUtil.addJwtCookieToResponse;
import static com.tll.mcorpus.web.RequestUtil.expireAllCookies;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.gmodel.mcuser.Mcstatus;
import com.tll.mcorpus.repo.MCorpusUserRepo;
import com.tll.mcorpus.repo.FetchResult;
import com.tll.mcorpus.web.JWT.JWTStatusInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.handling.Context;

/**
 * Immutable encapsulation of a GraphQL query request for use in the app web
 * layer.
 * <p>
 * This is the GraphQL context object for http requests.
 * 
 * @author jkirton
 */
public class GraphQLWebContext {

  private static final Pattern gqlOperationName = 
    Pattern.compile("^[\\s|\"]*(mutation|query)\\s+(\\w+)\\s*\\{.*", 
      Pattern.CASE_INSENSITIVE);

  private static final Pattern gqlMethodName = 
    Pattern.compile("^.*?\\{.*?(\\w+).*", 
      Pattern.CASE_INSENSITIVE);

  private static final Pattern gqlIntrospectQuery = 
    Pattern.compile("^\\s*(query)?\\s*(IntrospectionQuery)\\s*\\{.*", 
        Pattern.CASE_INSENSITIVE);

  private final Logger log = LoggerFactory.getLogger(GraphQLWebContext.class);

  private final String query;
  private final String queryCleaned;
  private final Map<String, Object> vmap;
  private final RequestSnapshot requestSnapshot;
  private final JWTStatusInstance jwtStatus;
  private final Context ctx;

  /**
   * Constructor.
   *
   * @param query           the GraphQL query string
   * @param vmap            optional query variables expressed as a name/value map
   * @param requestSnapshot snapshot of the sourcing http request
   * @param jwtStatus       the status of the JWT of the sourcing http request
   * @param ctx             the Ratpack request handling context
   */
  public GraphQLWebContext(String query, Map<String, Object> vmap, RequestSnapshot requestSnapshot, JWTStatusInstance jwtStatus, Context ctx) {
    super();
    this.query = query;
    this.queryCleaned = clean(query).replaceAll("\\n", "").replaceAll("\n", "");
    this.vmap = vmap;
    this.requestSnapshot = requestSnapshot;
    this.jwtStatus = jwtStatus;
    this.ctx = ctx;
  }
  
  /**
   * Is this a valid GraphQL query ready to be handed off to further processing?
   * 
   * @return true/false
   */
  public boolean isValid() { 
    return 
        not(isNullOrEmpty(query))
        && not(isNull(requestSnapshot))
        && not(isNull(jwtStatus))
        && not(isNull(ctx))
        ;
  }
  
  /**
   * @return true when this GraphQL query has variables, false otherwise.
   */
  public boolean hasQueryVariables() { return vmap != null && !vmap.isEmpty(); }
  
  /**
   * @return the GraphQL query string.
   */
  public String getQuery() { return query; }
  
  /**
   * @return map of name/value pairs representing 
   *          the GraphQL variables associated with this query instance.
   */
  public Map<String, Object> getVariables() { return vmap; }

  /**
   * @return the never-null GraphQL query/mutation <em>operation</em> name.
   *         <p>
   *         The operation name is not required and when not present, 
   *         a zero-length string is returned.
   */
  public String getOperationName() {
    final Matcher matcher = gqlOperationName.matcher(queryCleaned);
    final String s = matcher.matches() ? matcher.group(2) : "";
    return s;
  }

  /**
   * @return the never-null GraphQL query/mutation <em>method</em> name.
   *         <p>
   *         The method name is expected to always be present 
   *         in a <em>valid</em> qraphql query string.
   */
  public String getQueryMethodName() {
    final Matcher matcher = gqlMethodName.matcher(queryCleaned);
    final String s = matcher.matches() ? matcher.group(1) : "";
    return s;
  }

  /**
   * @return true when the graphql query is an Introspection query.
   */
  public boolean isIntrospectionQuery() {
    final Matcher matcher = gqlIntrospectQuery.matcher(queryCleaned);
    boolean b = matcher.matches();
    return b;
  }

  /**
   * @return true when the GraphQL query is for mcuser login, false otherwise.
   */
  public boolean isMcuserLoginQuery() {
    return "mclogin".equals(getQueryMethodName());
  }

  /**
   * @return true when this GraphQL query is either an mcuser login mutation query
   *         or an introspection query, false otherwise.
   */
  public boolean isMcuserLoginOrIntrospectionQuery() { 
    return isMcuserLoginQuery() || isIntrospectionQuery();
  }

  /**
   * @return the snapshot of the sourcing http request.
   */
  public RequestSnapshot getRequestSnapshot() { return requestSnapshot; }
  
  /**
   * @return the JWT status instance of the sourcing http request.
   */
  public JWTStatusInstance getJwtStatus() { return jwtStatus; }

  /**
   * Get the mcuser login status.
   * <p>
   * Blocking - Db call is issued.
   * 
   * @return Newly created {@link Mcstatus} when an mcuser presents a valid and
   *         non-expired JWT -OR-<br>
   *         null when no JWT is present or is not valid.
   */
  public Mcstatus mcstatus() {
    final JWTStatusInstance jwtStatusInst = getJwtStatus();
    final UUID jwtId = jwtStatusInst.jwtId();
    if(jwtStatusInst.status().isValid()) {
      final UUID mcuserId = jwtStatusInst.mcuserId();
      FetchResult<Integer> fetchResult = ctx.get(MCorpusUserRepo.class).getNumActiveLogins(mcuserId);
      if(fetchResult.isSuccess()) {
        // success
        final Date since = jwtStatusInst.issued();
        final Date expires = jwtStatusInst.expires();
        final int numActiveJWTs = fetchResult.get().intValue();
        return new Mcstatus(mcuserId, since, expires, numActiveJWTs);
      } else {
        final String emsg = fetchResult.getErrorMsg();
        log.error("Invalid mcuser login status fetch result for JWT of id: {}: {}", jwtId, emsg);
      }
    } else {
      log.warn("Invalid JWT ({}) presented for mcuser status.", jwtId);
    }

    // default
    return null;
  }

  /**
   * Log an mcuser in and issue a JWT back to client when the login was successful.
   * <p>
   * Blocking - Db call is issued.
   * 
   * @param username the posted mcuser username
   * @param pswd the posted mcuser passwrod
   * @return true when the mcuser was successfully logged in, false otherwise
   */
  public boolean mcuserLogin(final String username, final String pswd) {
    
    // verify the JWT status is either not present or expired
    if(not(jwtStatus.isJWTStatusExpiredOrNotPresent())) {
      return false;
    }

    // validate login input
    if(isBlank(username) || isBlank(pswd)) {
      return false;
    }
    
    final RequestSnapshot requestSnapshot = ctx.getRequest().get(RequestSnapshot.class);
    final JWT jwtbiz = ctx.get(JWT.class);
    
    final UUID pendingJwtID = UUID.randomUUID();
    final long requestInstantMillis = requestSnapshot.getRequestInstant().toEpochMilli();
    final long jwtExpiresMillis = requestInstantMillis + jwtbiz.jwtCookieTtlInMillis();    

    final McuserLogin mcuserLogin = new McuserLogin();
    mcuserLogin.setMcuserUsername(username);
    mcuserLogin.setMcuserPassword(pswd);
    mcuserLogin.setInJwtId(pendingJwtID);
    mcuserLogin.setInLoginExpiration(new Timestamp(jwtExpiresMillis));
    mcuserLogin.setInRequestOrigin(requestSnapshot.getClientOrigin());
    mcuserLogin.setInRequestTimestamp(new Timestamp(requestInstantMillis));

    // call db login
    log.debug("Authenticating mcuser '{}'..", username);
    final FetchResult<Mcuser> loginResult = ctx.get(MCorpusUserRepo.class).login(mcuserLogin);
    if(not(loginResult.isSuccess())) {
      log.error("Mcuser login failed: {}", loginResult.getErrorMsg());
      return false;
    }
    log.info("Mcuser '{}' authenticated.", username);
    // at this point, we're authenticated
    
    log.debug("Generating JWT for mcuser '{}'..", username);
    final Mcuser mcuser = loginResult.get();
    try {
      // create the JWT - and set as a cookie to go back to user
      // the user is now expected to provide this JWT for subsequent mcorpus api requests
      final String issuer = ctx.getServerConfig().getPublicAddress().toString();
      final String audience = requestSnapshot.getClientOrigin();
      final String jwt = jwtbiz.generate(
          requestSnapshot.getRequestInstant(), 
          mcuser.getUid(), 
          pendingJwtID,
          issuer,
          audience,
          isNullOrEmpty(mcuser.getRoles()) ? "" : 
            Arrays.stream(mcuser.getRoles())
            .map(role -> { return role.getLiteral(); })
            .collect(Collectors.joining(","))
      );
      
      // jwt cookie
      addJwtCookieToResponse(ctx, jwt, jwtbiz.jwtCookieTtlInSeconds());
      
      log.info("Mcuser {} logged in.  JWT {} generated from server (issuer): '{}' to client origin (audience:) '{}'.", 
          mcuser.getUid(), pendingJwtID, issuer, audience);
      return true;
    }
    catch(Exception e) {
      log.error("Mcuser {} login error: {}", mcuser.getUid(), e.getMessage());
    }
    
    // default
    return false;
  }

  /**
   * Log an mcuser out.
   * <p>
   * Blocking - Db call is issued.
   * 
   * @return true when the mcuser bound to the presenting JWT in incoming request
   *         is successfully logged out, false otherwise.
   */
  public boolean mcuserLogout() {
    // call db-level logout routine to invalidate the jwt id 
    // and capture the logout event as an mcuser audit record
    final JWTStatusInstance jwtStatus = ctx.getRequest().get(JWTStatusInstance.class);
    final RequestSnapshot requestSnapshot = ctx.getRequest().get(RequestSnapshot.class);
    
    final McuserLogout mcuserLogout = new McuserLogout();
    mcuserLogout.setMcuserUid(jwtStatus.mcuserId());
    mcuserLogout.setJwtId(jwtStatus.jwtId());
    mcuserLogout.setRequestTimestamp(new Timestamp(requestSnapshot.getRequestInstant().toEpochMilli()));
    mcuserLogout.setRequestOrigin(requestSnapshot.getClientOrigin());
    FetchResult<Boolean> fetchResult = ctx.get(MCorpusUserRepo.class).logout(mcuserLogout);
    if(fetchResult.isSuccess()) {
      // logout success - nix all mcorpus cookies clientside
      expireAllCookies(ctx);
      log.info("mcuser '{}' logged out.", jwtStatus.mcuserId());
      return true;
    }

    // default - logout failed
    log.error("mcuser '{}' logout failed.", jwtStatus.mcuserId());
    return false;
  }

  @Override
  public String toString() {
    return String.format("op: %s qry: %s - %n%s%n", getOperationName(), getQueryMethodName(), query);
  }
}
