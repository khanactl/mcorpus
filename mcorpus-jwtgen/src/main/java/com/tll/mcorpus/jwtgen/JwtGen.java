package com.tll.mcorpus.jwtgen;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.jwt.JwtClaims;
import org.pac4j.jwt.config.encryption.EncryptionConfiguration;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.config.signature.SignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.postgresql.ds.PGSimpleDataSource;

/**
 * Generates JWTs for use against the mcorpus api.
 * 
 * Expected input:<br>
 * <pre>
 * username  the mcorpus user's username
 * password  the mcorpus user's password
 * duration  the temporal duration the generated jwt shall be valid
 *
 * The expected duration format:
 *
 * "{N}{d}"
 *
 * where:
 *
 *   N    is the numeric quantity of the time duration
 *        valid N values: 1 - 99
 *   d    is the unit of time which can be one of:
 *          'h' - hours
 *          'd' - days
 *    
 * examples:
 * 
 *   "1d"  - 1 day
 *   "99h" - 99 hours
 * 
 * </pre>
 * 
 * @author jkirton
 */
public class JwtGen {
  
  /**
   * The cached properties needed to generate JWT tokens for mcorpus.
   */
  private static final Properties jwtGenProps;
  
  static {
    // load and cache the jwtgen properties for repeated access
    jwtGenProps = new Properties();
    InputStream inputStream = null;
    try {
      inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jwtgen.properties");
      jwtGenProps.load(inputStream);
    }
    catch(Exception e) {
      throw new Error("jwtgen property load error.");
    }
    finally {
      try { if(inputStream != null) inputStream.close(); } catch(Exception e) { }
    }

  }

  static boolean isNullOrEmpty(String s) { return s == null || s.isEmpty(); }
  
  /**
   * The type for holding needed input for 
   * generating a jwt token for use against the mcorpus api.
   */
  public static final class Input {
    
    /**
     * @return Command-line helper text for this input.
     */
    public static String helpText() {
      return 
            "\n=====\n"
          + "mcorpus JWT generator\n"
          + "\n"
          + "INPUT args:\n"
          + "  {username} {password} {duration}\n"
          + "\n"
          + "where duration has format:\n"
          + "  '{N}d' -or- '{N}h' and N is 1 - 99\n"
          + "\n"
          + "examples:\n"
          + "  '3d' - 3 days\n"
          + "  '7h' - 7 hours\n"
          + "=====\n";
    }

    /**
     * Parse the user provided input and return a newly created Input instance.
     * <pre>
     * String[] args legend:
     * 
     * args[0]  username  the mcorpus user's username
     * args[1]  password  the mcorpus user's password
     * args[2]  duration  the jwt duration (expiry time) as a string expression
     * </pre>
     *
     * @param username the provided username
     * @param password the provided password
     * @param durationToken the provided duration as a string expression
     * @return Never-null, newly created Input instance
     * @throws Exception upon invalid arguments or error of any kind
     */
    public static Input assembleInput(String username, String password, String durationToken) throws Exception {
      if(isNullOrEmpty(username) || isNullOrEmpty(password) || isNullOrEmpty(durationToken))
        throw new Exception("Invalid input.");

      final int dtl = durationToken.length();
      if(durationToken.length() < 2) throw new Exception("Invalid duration.");
      final char d = durationToken.charAt(dtl - 1);
      final int n;
      try {
        n = Integer.parseInt(durationToken.substring(0, dtl - 1));
      }
      catch(Throwable t) {
        throw new Exception("Malformed duration.");
      }
      // require n be 1 - 99 inclusive
      if(n < 1 || n > 99) throw new Exception("Invalid duration amount.");

      Duration duration;

      switch(d) {
        case 'h': // hours
          duration = Duration.of(n, ChronoUnit.HOURS);
          break;
        case 'd': // days
          duration = Duration.of(n, ChronoUnit.DAYS);
          break;
        default:
          throw new Exception("Unrecognized duration unit.");
      }

      return new Input(username, password, duration);
    }
    
    private final String username;
    private final String password;
    private final Duration duration;

    private Input(String username, String password, Duration duration) {
      this.username = username;
      this.password = password;
      this.duration = duration;
    }
    
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Duration getDuration() { return duration; }
    
  } // Input class
  
  /**
   * Holds the generated jwt token and associated meta-data 
   * pertaining to the generated jwt token.
   */
  public static final class Output {
    private final String jwt;
    private final Duration duration;
    private final UUID uid;
    private final String username;
    
    private Output(String jwt, Duration duration, UUID uid, String username) {
      this.jwt = jwt;
      this.duration = duration;
      this.uid = uid;
      this.username = username;
    }
    
    /**
     * @return the generated jwt token.
     */
    public String getJwt() { return jwt; }
    
    /**
     * @return the time to live of the generated jwt 
     *         before it shall be considered expired.
     */
    public Duration getDuration() { return duration; }
    
    /**
     * @return the mcorpus user id bound to the generated jwt token.
     */
    public UUID getUid() { return uid; }
    
    /**
     * @return the mcorpus username bound to the generated jwt token.
     */
    public String getUsername() { return username; }
    
    /**
     * Formatted output suitable for command-line output.
     * <p>
     * This descriptor will always contain "JWT" and the JWT token itself.
     * 
     * @return Formatted jwt output descriptor string.
     */
    public String descriptor() {
      String s = String.format("\n*****\nJWT of duration %s generated for user '%s' (uid: %s):\n-----\n%s\n-----\n*****\n\n",
          duration.toString(), username, uid.toString(), jwt);
      return s;
    } 
  } // Output class
  
  /**
   * Process the given user input.
   * 
   * @param args the user provided input
   * @return the string to output as a response
   */
  public static synchronized String processInput(final String... args) {
    if(args == null) return "No input arguments provided.";
    String sout = null;
    switch(args.length) {
    case 3:
      try {
        sout = generateJwt(Input.assembleInput(args[0], args[1], args[2])).descriptor();
      }
      catch(Exception e) {
        sout = e.getMessage();
      }
      break;
    default:
      sout = Input.helpText();
      break;
    }
    return sout;
  }
  
  /**
   * Generate a JWT for the given input.
   * <p>
   * If no exception is thrown, a jwt token is guaranteed to be generated 
   * and held in a containing {@link Output} instance.
   * 
   * @param input the needed input for generating a valid JWT for use with mcorpus.
   * @return Never-null, newly created Output instance hodling the generated jwt token 
   *          and associated meta-data.
   * @throws Exception Upon any error generating a jwt token.
   */
  public static synchronized Output generateJwt(final Input input) throws Exception {
    if(input == null) throw new Exception("No input provided.");
    
    final String jwtSessionId = String.format("jwtgen-%s", UUID.randomUUID().toString());
    UUID uid = null;
    boolean isAdmin = false;

    // call login stored proc
    Connection cnc = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      final PGSimpleDataSource ds = new PGSimpleDataSource();
      ds.setUrl(jwtGenProps.getProperty("dbUrl"));
      ds.setUser(jwtGenProps.getProperty("dbUsername"));
      ds.setPassword(jwtGenProps.getProperty("dbPassword"));
      cnc = ds.getConnection();
      statement = cnc.prepareStatement("select uid, admin from login(?, ?, ?, ?, ?, ?, ?, ?)");
      statement.setString(1, input.username);
      statement.setString(2, input.password);
      statement.setString(3, jwtSessionId);
      statement.setString(4, "");
      statement.setString(5, "");
      statement.setString(6, "");
      statement.setString(7, "");
      statement.setString(8, "");
      rs = statement.executeQuery();
      if(rs != null && rs.next()) {
        uid = (UUID) rs.getObject(1);
        isAdmin = rs.getBoolean(2);
      }
      if(uid == null) throw new Exception();
    }
    catch(Exception e) {
      throw new Exception("Mcorpus db communication error.");
    }
    finally {
      try {
        if (rs != null) rs.close();
        if(statement != null) statement.close();
        if (cnc != null) cnc.close();
      }
      catch(Exception e) { }
    }

    // jwt token
    final long tstamp = System.currentTimeMillis();
    final long expires = tstamp + input.duration.toMillis();
    final CommonProfile profile = new CommonProfile();
    profile.setId(uid.toString());
    profile.addAttribute(Pac4jConstants.USERNAME, input.username);
    profile.addAttribute(JwtClaims.ISSUER, "jwtgen");
    profile.addAttribute(JwtClaims.ISSUED_AT, new Date(tstamp));
    profile.addAttribute(JwtClaims.EXPIRATION_TIME, new Date(expires));
    profile.addAttribute(JwtClaims.AUDIENCE, isAdmin ? "mcorpus-admin" : "mcorpus-graphql-api");
    final String jwtSalt = jwtGenProps.getProperty("jwt.salt");
    final SignatureConfiguration signatureConfiguration = new SecretSignatureConfiguration(jwtSalt);
    final EncryptionConfiguration encryptionConfiguration = new SecretEncryptionConfiguration(jwtSalt);
    final JwtGenerator<CommonProfile> jwtGenerator = new JwtGenerator<>(signatureConfiguration, encryptionConfiguration);
    final String jwt = jwtGenerator.generate(profile);
    if(jwt == null) throw new Exception("jwt generator returned null token");

    // successful jwt generation
    return new Output(jwt, input.duration, uid, input.username);
  }
}
