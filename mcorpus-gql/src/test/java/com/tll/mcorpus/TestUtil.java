package com.tll.mcorpus;

import static com.tll.mcorpus.db.Tables.MADDRESS;
import static com.tll.mcorpus.db.Tables.MAUTH;
import static com.tll.mcorpus.db.Tables.MEMBER;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.enums.Location;
import com.tll.mcorpus.db.enums.MemberStatus;

public class TestUtil {

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final TypeReference<Map<String, Object>> strObjMapTypeRef = new TypeReference<Map<String, Object>>() { };

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

  private static final DateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd");

  private static final Random rand = new Random();

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
    return String.format("%s-%s",
      Integer.toString(rand.nextInt(99) + 10),
      Integer.toString(rand.nextInt(9999999) + 1000000));
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

  private TestUtil() {}
}
