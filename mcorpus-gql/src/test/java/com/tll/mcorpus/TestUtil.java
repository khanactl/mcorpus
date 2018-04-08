package com.tll.mcorpus;

import static com.tll.mcorpus.db.Tables.MADDRESS;
import static com.tll.mcorpus.db.Tables.MAUTH;
import static com.tll.mcorpus.db.Tables.MCUSER_AUDIT;
import static com.tll.mcorpus.db.Tables.MEMBER;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.postgresql.ds.PGSimpleDataSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.enums.JwtIdStatus;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.McuserAuditType;
import com.tll.mcorpus.db.enums.MemberStatus;
import com.tll.mcorpus.db.routines.McuserLogin;
import com.tll.mcorpus.db.routines.McuserLogout;
import com.tll.mcorpus.db.routines.MemberLogin;
import com.tll.mcorpus.db.routines.MemberLogout;
import com.tll.mcorpus.db.tables.pojos.McuserAudit;

public class TestUtil {
  
  public static final UUID testMcuserUid = UUID.fromString("d712f2d3-5494-472d-bdcc-4a1722a8c818");
  
  public static final String testRequestOrigin = "https://test-app.com ";

  public static final String testServerPublicAddress = "https://mcorpus.d2d:5150";

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final TypeReference<Map<String, Object>> strObjMapTypeRef = new TypeReference<Map<String, Object>>() { };

  private static final DateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd");

  private static final Random rand = new Random();
  
  private static DSLContext dslMcweb = null;
  
  private static DSLContext dslMcwebtest = null;
  
  /**
   * @return A newly created {@link DataSource} to the test database intended for
   *         testing.
   */
  public static DataSource ds_mcweb() {
    Properties dbprops = new Properties();
    InputStream istream = null;
    try {
      istream = Thread.currentThread().getContextClassLoader().getResourceAsStream("app.properties");
      dbprops.load(istream);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load test db props.");
    } finally {
      if(istream != null) try {
        istream.close();
      } catch (IOException e) {
      }
    }
    PGSimpleDataSource ds = new PGSimpleDataSource();
    ds.setDatabaseName(dbprops.getProperty("dbName"));
    ds.setServerName(dbprops.getProperty("dbServerName"));
    ds.setPortNumber(Integer.parseInt(dbprops.getProperty("dbPortNumber")));
    ds.setUser(dbprops.getProperty("dbUsername"));
    ds.setPassword(dbprops.getProperty("dbPassword"));
    ds.setCurrentSchema(dbprops.getProperty("dbSchema"));
    return ds;
  }
  
  /**
   * @return A newly created {@link DataSource} to the test database intended for
   *         testing.
   */
  public static DataSource ds_mcwebtest() {
    Properties dbprops = new Properties();
    InputStream istream = null;
    try {
      istream = Thread.currentThread().getContextClassLoader().getResourceAsStream("testdb.properties");
      dbprops.load(istream);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load test db props.");
    } finally {
      if(istream != null) try {
        istream.close();
      } catch (IOException e) {
      }
    }
    PGSimpleDataSource ds = new PGSimpleDataSource();
    ds.setUrl(dbprops.getProperty("testDbUrl"));
    ds.setUser(dbprops.getProperty("testDbUsername"));
    ds.setPassword(dbprops.getProperty("testDbPassword"));
    ds.setCurrentSchema(dbprops.getProperty("testDbSchema"));
    return ds;
  }
  
  /**
   * @return true if the internally managed test dsl context has been loaded.
   */
  public static synchronized boolean isTestDslMcwebLoaded() {
    return dslMcweb != null;
  }

  /**
   * @return a JooQ {@link DSLContext} intended for testing with <em>mcweb</em> db
   *         credentials.
   */
  public static synchronized DSLContext testDslMcweb() {
    if(dslMcweb == null) {
      Settings s = new Settings();
      s.setRenderSchema(false);
      s.setRenderNameStyle(RenderNameStyle.LOWER);
      s.setRenderKeywordStyle(RenderKeywordStyle.UPPER);
      dslMcweb = DSL.using(ds_mcweb(), SQLDialect.POSTGRES, s);
    }
    return dslMcweb;
  }

  /**
   * @return true if the internally managed mcwebtest dsl context has been loaded.
   */
  public static synchronized boolean isTestDslMcwebTestLoaded() {
    return dslMcwebtest != null;
  }

  /**
   * @return a JooQ {@link DSLContext} intended for testing with <em>mcwebtest</em> db
   *         credentials.
   */
  public static synchronized DSLContext testDslMcwebTest() {
    if(dslMcwebtest == null) {
      Settings s = new Settings();
      s.setRenderSchema(false);
      s.setRenderNameStyle(RenderNameStyle.LOWER);
      s.setRenderKeywordStyle(RenderKeywordStyle.UPPER);
      dslMcwebtest = DSL.using(ds_mcwebtest(), SQLDialect.POSTGRES, s);
    }
    return dslMcwebtest;
  }

  /**
   * Converts a JSON string to a map.
   *
   * <p>Requirement: the given json string is assumed to have a
   * root object ref with enclosing name and object key/value pairs.</p>
   *
   * <p>Example:
   * <pre>
   *   "{ \"query\": \"...\", \"variables\":\"...\", ... }"
   * </pre>
   * </p>
   *
   * @param json the JSON string
   * @return newly created {@link Map} with parsed name/values from the JSON string
   * @throws Exception when the json to map conversion fails for some reason.
   */
  public static Map<String, Object> jsonStringToMap(final String json) throws Exception {
    try {
      // convert JSON string to Map
      return mapper.readValue(json, strObjMapTypeRef);
    }
    catch(Throwable t) {
      throw new Exception("JSON to map failed: " + t.getMessage());
    }
  }

  /**
   * Class path resource to string.
   *
   * @param path the string-wise path to the test resource to load into a string
   * @return the loaded classpath resource as a UTF-8 string
   */
  public static String cpr(String path) {
    try {
      Path p = Paths.get(Thread.currentThread().getContextClassLoader().getResource(path).toURI());
      byte[] bytes = Objects.requireNonNull(Files.readAllBytes(p));
      return new String(bytes, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Return a string of repeated characters: <code><fillChar/code> of length <code>n</code>.
   *
   * @param n the number of characters to return
   * @return String of <code>'c'</code> characters repeated n times.
   */
  public static String strN(int n) {
    if(n > 0) {
     final char[] carr = new char[n];
     Arrays.fill(carr, 'c');
     return new String(carr);
    }
    return "";
  }

  /**
   * Convert a string date token of format: "yyyy-MM-dd" to a {@link Date}.
   *
   * @param s the date token
   * @return {@link Date} instance
   * @throws RuntimeException when the date parsing fails
   */
  public static Date toDate(final String s) {
    try {
      return dfmt.parse(s);
    }
    catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static String randomEmpId() {
    String s = String.format("%02d-%07d",
      Integer.valueOf(rand.nextInt(100) + 1),
      Integer.valueOf(rand.nextInt(10000000) + 1)
    );
    return s;
  }

  public static Location randomLocation() {
    return Location.values()[rand.nextInt(Location.values().length)];
  }

  public static void addMemberTableProperties(final Map<String, Object> memberMap) {
    memberMap.put(MEMBER.EMP_ID.getName(), randomEmpId());
    memberMap.put(MEMBER.LOCATION.getName(), randomLocation());
    memberMap.put(MEMBER.NAME_FIRST.getName(), "Jake");
    memberMap.put(MEMBER.NAME_MIDDLE.getName(), "Arthur");
    memberMap.put(MEMBER.NAME_LAST.getName(), "McGidrich");
    memberMap.put(MEMBER.DISPLAY_NAME.getName(), "JAMFMan");
    memberMap.put(MEMBER.STATUS.getName(), MemberStatus.ACTIVE);
  }

  public static void addMauthTableProperties(final Map<String, Object> memberMap) {
    memberMap.put(MAUTH.DOB.getName(), toDate("1977-07-04"));
    memberMap.put(MAUTH.SSN.getName(), "101-01-0101");
    memberMap.put(MAUTH.EMAIL_PERSONAL.getName(), "jam@ggl.com");
    memberMap.put(MAUTH.EMAIL_WORK.getName(), "jam-work@ggl.com");
    memberMap.put(MAUTH.MOBILE_PHONE.getName(), "(415) 674-7832");
    memberMap.put(MAUTH.HOME_PHONE.getName(), "415 474-1080");
    memberMap.put(MAUTH.WORK_PHONE.getName(), "415.374.2231");
    memberMap.put(MAUTH.USERNAME.getName(), "jmac");
    memberMap.put(MAUTH.PSWD.getName(), "33#4%%^22");
  }

  public static void addMaddressTableProperties(final Map<String, Object> maddressMap, UUID mid, Addressname addressname) {
    maddressMap.put(MADDRESS.MID.getName(), mid);
    maddressMap.put(MADDRESS.ADDRESS_NAME.getName(), addressname);
    maddressMap.put(MADDRESS.ATTN.getName(), "attn");
    maddressMap.put(MADDRESS.STREET1.getName(), "88 bway");
    maddressMap.put(MADDRESS.STREET2.getName(), "#3");
    maddressMap.put(MADDRESS.CITY.getName(), "city");
    maddressMap.put(MADDRESS.STATE.getName(), "MS");
    maddressMap.put(MADDRESS.POSTAL_CODE.getName(), "99876");
    maddressMap.put(MADDRESS.COUNTRY.getName(), "USA");
  }

  public static Map<String, Object> generateMemberToAddPropertyMap() {
    Map<String, Object> memberMap = new HashMap<>();
    addMemberTableProperties(memberMap);
    addMauthTableProperties(memberMap);
    return memberMap;
  }

  public static Map<String, Object> generateMemberToUpdatePropertyMap() {
    Map<String, Object> memberMap = new HashMap<>();
    addMemberTableProperties(memberMap);
    memberMap.remove(MEMBER.EMP_ID.getName());
    memberMap.remove(MEMBER.LOCATION.getName());
    addMauthTableProperties(memberMap);
    memberMap.remove(MAUTH.USERNAME.getName());
    memberMap.remove(MAUTH.PSWD.getName());
    return memberMap;
  }

  public static Map<String, Object> generateMaddressToAddPropertyMap(UUID mid, Addressname addressname) {
    Map<String, Object> maddressMap = new HashMap<>();
    addMaddressTableProperties(maddressMap, mid, addressname);
    return maddressMap;
  }

  public static Map<String, Object> generateMaddressToUpdatePropertyMap(UUID mid, Addressname addressname) {
    Map<String, Object> maddressMap = new HashMap<>();
    addMaddressTableProperties(maddressMap, mid, addressname);
    return maddressMap;
  }
  
  /**
   * @return newly created {@link McuserLogin} instance for an mcuser for testing purposes.
   */
  public static McuserLogin testMcuserLoginInput() {
    final long lnow = System.currentTimeMillis();
    final long expiry = lnow + Duration.ofMinutes(30).toMillis();
    McuserLogin mcuserLogin = new McuserLogin();
    mcuserLogin.setMcuserUsername("test");
    mcuserLogin.setMcuserPassword("jackson");
    mcuserLogin.setInJwtId(UUID.randomUUID());
    mcuserLogin.setInRequestTimestamp(new Timestamp(lnow));
    mcuserLogin.setInRequestOrigin(testRequestOrigin);
    mcuserLogin.setInLoginExpiration(new Timestamp(expiry));
    return mcuserLogin;
  }

  /**
   * @return newly created {@link McuserLogout} instance for an mcuser for testing purposes.
   */
  public static McuserLogout testMcuserLogoutInput() {
    final long lnow = System.currentTimeMillis();
    McuserLogout mcuserLogout = new McuserLogout();
    mcuserLogout.setJwtId((UUID)null);
    mcuserLogout.setMcuserUid((UUID)null);
    mcuserLogout.setRequestTimestamp(new Timestamp(lnow));
    mcuserLogout.setRequestOrigin(testRequestOrigin);
    return mcuserLogout;
  }

  /**
   * @return newly created {@link MemberLogin} instance for a member for testing purposes.
   */
  public static MemberLogin testMemberLoginInput() {
    final long lnow = System.currentTimeMillis();
    MemberLogin memberLogin = new MemberLogin();
    memberLogin.setMemberUsername("dhookes3f");
    memberLogin.setMemberPassword("8testItOut9");
    memberLogin.setInRequestTimestamp(new Timestamp(lnow));
    memberLogin.setInRequestOrigin(testRequestOrigin);
    return memberLogin;
  }

  /**
   * @return newly created {@link MemberLogout} instance for a member for testing purposes.
   */
  public static MemberLogout testMemberLogoutInput() {
    final long lnow = System.currentTimeMillis();
    MemberLogout memberLogout = new MemberLogout();
    memberLogout.setMid(UUID.fromString("394b6d00-cf1e-40c8-ac44-0e4e49f956ba"));
    memberLogout.setInRequestTimestamp(new Timestamp(lnow));
    memberLogout.setInRequestOrigin(testRequestOrigin);
    return memberLogout;
  }
  
  /**
   * Add a test mcuser_audit record.
   * 
   * @return Newly created McuserAudit pojo corresponding to the added
   *         MCUSER_AUDIT test record.
   * @throws Exception upon test record insert failure
   */
  public static McuserAudit addTestMcuserAuditRecord() throws Exception {
    long lnow = Instant.now().toEpochMilli();
    final long expiry = lnow + Duration.ofMinutes(30).toMillis();
    
    McuserAudit e = new McuserAudit(
        UUID.randomUUID(),
        null,
        McuserAuditType.LOGIN,
        new Timestamp(lnow),
        testRequestOrigin,
        new Timestamp(expiry),
        UUID.randomUUID(),
        JwtIdStatus.OK);
    
    final int numInserted = testDslMcweb().insertInto(MCUSER_AUDIT,
        MCUSER_AUDIT.TYPE,
        MCUSER_AUDIT.JWT_ID, 
        MCUSER_AUDIT.JWT_ID_STATUS,
        MCUSER_AUDIT.UID,
        MCUSER_AUDIT.REQUEST_TIMESTAMP,
        MCUSER_AUDIT.REQUEST_ORIGIN,
        MCUSER_AUDIT.LOGIN_EXPIRATION
      ).values(
          McuserAuditType.LOGIN,
          e.getJwtId(),
          e.getJwtIdStatus(),
          testMcuserUid,
          e.getRequestTimestamp(),
          e.getRequestOrigin(),
          e.getLoginExpiration()
      ).execute();
    if(numInserted != 1) throw new Exception("Num inserted MCUSER_AUDIT records: " + numInserted);
    
    return e;
  }
  
  private TestUtil() {}
}
