package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.dflt;
import static com.tll.mcorpus.Util.isBlank;

import java.util.Date;

import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.jwt.JwtClaims;
import org.pac4j.jwt.profile.JwtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.db.tables.pojos.Mcuser;
import com.tll.mcorpus.repo.MCorpusUserRepoAsync;
import com.tll.mcorpus.repo.model.FetchResult;
import com.tll.mcorpus.repo.model.LoginInput;

/**
 * The authenticator for entry into the mcorpus graphql api.
 * <p>
 * A  username and password is provided.  A db fetch is then made for a matching user.
 * When a sigile user match is successful, a JWT is then generated and
 * set in the user profile object.
 */
public class MCorpusAuthenticator implements Authenticator<UsernamePasswordCredentials> {

  private static final Logger log = LoggerFactory.getLogger(MCorpusAuthenticator.class);

  private final MCorpusUserRepoAsync repo;

  private final JwtGenerator<CommonProfile> jwtGenerator;

  private final long jwtCookieTtlInMillis;
  
  private final int jwtCookieTtlInSeconds;

  private final String jwtCookieDomainName;

  /**
   * Constructor.
   *
   * @param repo the mcorpus data access object
   * @param jwtGenerator required
   * @param jwtCookieTtlInMillis the desired JWT Time to Live in milli-seconds when generating the JWT auth cookie.<br>
   *                      If null, a default value is set
   * @param jwtCookieDomainName the desired JWT cookie domain name to use when generating the JWT auth cookie.<br>
   *                            If null, a default value of "" is used.
   */
  public MCorpusAuthenticator(final MCorpusUserRepoAsync repo, final JwtGenerator<CommonProfile> jwtGenerator, final Long jwtCookieTtlInMillis, final String jwtCookieDomainName) {
    this.repo = repo;
    this.jwtGenerator = jwtGenerator;
    this.jwtCookieTtlInMillis = dflt(jwtCookieTtlInMillis, (1000L * 60L * 60L * 48)); // default: 2 days
    this.jwtCookieTtlInSeconds = Math.toIntExact(Math.floorDiv(this.jwtCookieTtlInMillis, 1000L));
    this.jwtCookieDomainName = dflt(jwtCookieDomainName, "");
  }

  @Override
  public void validate(UsernamePasswordCredentials credentials, WebContext context) throws CredentialsException {

    if (credentials == null)
      throw new CredentialsException("No credentials provided");

    final String username = credentials.getUsername();
    final String password = credentials.getPassword();

    if (isBlank(username))
      throw new CredentialsException("No username provided");
    else if (isBlank(password))
      throw new CredentialsException("No password provided");

    final String remoteAddr = context.getRemoteAddr();
    final String httpHost = context.getRequestHeader("Host");
    final String httpOrigin = context.getRequestHeader("Origin");
    final String httpReferer = context.getRequestHeader("Referer");
    final String httpForwarded = context.getRequestHeader("Forwarded");
    final String webSessionid = context.getSessionIdentifier();
    final LoginInput loginInput = new LoginInput(username, password, webSessionid, remoteAddr, httpHost, httpOrigin, httpReferer, httpForwarded);

    log.info("Authenticating: '{}'..", loginInput);
    final FetchResult<Mcuser> loginResult = repo.login(loginInput);
    assert loginResult != null : "loginResult should never be null";
    if(loginResult.hasErrorMsg())
      throw new CredentialsException(loginResult.getErrorMsg());
    assert loginResult.get() != null;

    // at this point, we're authenticated
    // now generate a JWT and set as a cookie to go back to user
    // the user is now expected to provide this jwt on every subsequent request at /graphql
    final long tstamp = System.currentTimeMillis();
    final long expires = tstamp + jwtCookieTtlInMillis;

    final CommonProfile profile = new CommonProfile();
    profile.setId(loginResult.get().getUid().toString());
    profile.addAttribute(Pac4jConstants.USERNAME, loginResult.get().getUsername());
    profile.addAttribute(JwtClaims.ISSUER, "mcweb");
    profile.addAttribute(JwtClaims.ISSUED_AT, new Date(tstamp));
    profile.addAttribute(JwtClaims.EXPIRATION_TIME, new Date(expires));
    profile.addAttribute(JwtClaims.AUDIENCE,
      loginResult.get().getAdmin() ? "mcorpus-admin" : "mcorpus-graphql-api");
    credentials.setUserProfile(profile);

    // jwt token
    final String jwt = jwtGenerator.generate(profile);

    // jwt cookie
    final Cookie cookie = new Cookie("JWT", jwt);
    cookie.setDomain(jwtCookieDomainName);
    cookie.setMaxAge(jwtCookieTtlInSeconds); // cookie max age is in seconds
    // CRITICAL: do NOT allow this cookie to be accessible from js on the clients!
    // HTTP ONLY please!
    cookie.setHttpOnly(true);
    // by setting the cookie path, client browsers will only send the cookie back when this path is requested,
    // this is desirable in that we're limiting its network exposure
    // (maybe this is incomplete?
    //  ..but I think this is a good thing to do since we're only checking for it when the path matches anyways.)
    // cookie.setPath("/graphql");
    log.info("JWT mcorpus cookie generated: {}", cookie.toString());

    // set response cookie
    context.addResponseCookie(cookie);

    log.info("'{}' authenticated.", username);
  }
}
