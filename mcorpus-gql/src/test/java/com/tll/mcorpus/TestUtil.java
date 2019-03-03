package com.tll.mcorpus;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.jwt.JWT;
import com.tll.mcorpus.jwt.JWTStatus;
import com.tll.mcorpus.jwt.JWTStatusInstance;
import com.tll.mcorpus.webapi.RequestSnapshot;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.postgresql.ds.PGSimpleDataSource;

public class TestUtil {
  
  public static final String testRequestOrigin = "localhost|localhost";

  public static final String testServerPublicAddress = "https://mcorpus.d2d";

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
    PGSimpleDataSource ds = new PGSimpleDataSource();
    ds.setUrl(System.getenv("MCORPUS_DB_URL"));
    return ds;
  }
  
  /**
   * @return A newly created {@link DataSource} to the test database intended for
   *         testing.
   */
  public static DataSource ds_mcwebtest() {
    PGSimpleDataSource ds = new PGSimpleDataSource();
    ds.setUrl(System.getenv("MCORPUS_TEST_DB_URL"));
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

  /**
   * Convert a string date token of format: "yyyy-MM-dd" to a {@link Timestamp}.
   *
   * @param s the date token
   * @return {@link Timestamp} instance
   * @throws RuntimeException when the date parsing fails
   */
  public static Timestamp toTimestamp(final String s) {
    final Date d = toDate(s);
    return new Timestamp(d.getTime());
  }

  /**
   * Convert a string date token of format: "yyyy-MM-dd" to a {@link java.sql.Date}.
   *
   * @param s the date token
   * @return {@link java.sql.Date} instance
   * @throws RuntimeException when the date parsing fails
   */
  public static java.sql.Date toSqlDate(final String s) {
    final Date d = toDate(s);
    return new java.sql.Date(d.getTime());
  }

  public static String randomEmpId() {
    String s = String.format("%02d-%07d",
      Integer.valueOf(rand.nextInt(100)),
      Integer.valueOf(rand.nextInt(10000000))
    );
    return s;
  }

  public static Location randomLocation() {
    return Location.values()[rand.nextInt(Location.values().length)];
  }

  public static RequestSnapshot testRequestSnapshot() {
    return new RequestSnapshot(
        Instant.now(),
        "127.0.0.1",
        "localhost",
        "origin",
        "https://mcorpus.d2d",
        "forwarded",
        "X-Forwarded-For",
        "X-Forwarded-Proto",
        "X-Forwarded-Port",
        null, // jwt cookie
        null, // rst cookie
        null // rst header
    );
  }

  public static JWTStatusInstance testJwtStatus(JWTStatus jwtStatus, String roles) {
    return JWT.jsi(
      jwtStatus,
      UUID.randomUUID(),
      UUID.randomUUID(),
      roles, 
      new Date(Instant.now().toEpochMilli()),
      new Date(Instant.now().toEpochMilli())
    );
  }

  private TestUtil() {}
}
