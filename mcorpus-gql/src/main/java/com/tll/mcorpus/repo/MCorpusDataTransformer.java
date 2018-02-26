package com.tll.mcorpus.repo;

import static com.tll.mcorpus.Util.asSqlDate;
import static com.tll.mcorpus.Util.digits;
import static com.tll.mcorpus.db.tables.Maddress.MADDRESS;
import static com.tll.mcorpus.db.tables.Mauth.MAUTH;
import static com.tll.mcorpus.db.tables.Member.MEMBER;
import static com.tll.mcorpus.repo.RepoUtil.fval;
import static com.tll.mcorpus.repo.RepoUtil.hasField;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.jooq.Field;

import com.tll.mcorpus.Util;
import com.tll.mcorpus.db.enums.Addressname;

/**
 * Responsible for transforming datastore-bound (about to be persisted) member data to a 'datastore-ready' state.
 */
public class MCorpusDataTransformer {

  /**
   * Copy the given field from the source map to the target map only when present in the source map.
   *
   * @param f the field ref
   * @param source the source map
   * @param target the target map
   */
  static void mcopy(Field<?> f, Map<String, Object> source, Map<String, Object> target) {
    if(hasField(f, source)) target.put(f.getName(), fval(f, source));
  }

  /**
   * Copy the given <em>transformed</em> field value from the source map to the target map only when present in the source map.
   *
   * @param f the field ref
   * @param source the source map
   * @param target the target map
   * @param fieldTransform the field transformation function that is applied before the field is put into the target map.
   */
  static void mcopy(Field<?> f, Map<String, Object> source, Map<String, Object> target, Function<Object, String> fieldTransform) {
    if(hasField(f, source)) target.put(f.getName(), fieldTransform.apply(fval(f, source)));
  }

  /**
   * Same as {@link #mcopy(Field, Map, Map)} but with a specific output type of java.sql.Date.
   * 
   * @param f the <em>date</em> field ref
   * @param source the input object
   * @param target the target map
   * @param fieldTransform the java.sql.Date conversion function
   */
  static void mcopySqlDate(Field<?> f, Map<String, Object> source, Map<String, Object> target) {
    if(hasField(f, source)) target.put(f.getName(), asSqlDate(fval(f, source)));
  }

  static void mcopyDigits(Field<String> f, Map<String, Object> source, Map<String, Object> target) {
    if(hasField(f, source)) target.put(f.getName(), digits((fval(f, source))));
  }

  /**
   * Given a map of member properties, create a new map with the same property names
   * but with cleansed / transformed values.
   *
   * <p>This is a way to accommodate database field/column constraints.</p>
   *
   * @param memberMap map of member properties keyed by the associated Jooq com.tll.mcorpus.db table/column field ref
   * @return newly created list of two maps:<br>
   *          index 0: member table fields<br>
   *          index 1: mauth table fields
   */
  public static List<Map<String, Object>> transformMember(final Map<String, Object> memberMap) {
    final Map<String, Object> cmapMember = new HashMap<>(6);
    final Map<String, Object> cmapMauth = new HashMap<>(9);

    // member
    mcopy(MEMBER.EMP_ID, memberMap, cmapMember);
    mcopy(MEMBER.LOCATION, memberMap, cmapMember);
    mcopy(MEMBER.NAME_FIRST, memberMap, cmapMember, Util::upper);
    mcopy(MEMBER.NAME_MIDDLE, memberMap, cmapMember, Util::upper);
    mcopy(MEMBER.NAME_LAST, memberMap, cmapMember, Util::upper);
    mcopy(MEMBER.DISPLAY_NAME, memberMap, cmapMember, Util::asStringAndClean);
    mcopy(MEMBER.STATUS, memberMap, cmapMember);

    // mauth
    mcopySqlDate(MAUTH.DOB, memberMap, cmapMauth);
    mcopyDigits(MAUTH.SSN, memberMap, cmapMauth);
    mcopy(MAUTH.EMAIL_PERSONAL, memberMap, cmapMauth, Util::asStringAndClean);
    mcopy(MAUTH.EMAIL_WORK, memberMap, cmapMauth, Util::asStringAndClean);
    mcopyDigits(MAUTH.MOBILE_PHONE, memberMap, cmapMauth);
    mcopyDigits(MAUTH.HOME_PHONE, memberMap, cmapMauth);
    mcopyDigits(MAUTH.WORK_PHONE, memberMap, cmapMauth);
    mcopy(MAUTH.USERNAME, memberMap, cmapMauth, Util::asStringAndClean);
    mcopy(MAUTH.PSWD, memberMap, cmapMauth, Util::asStringAndClean);

    final List<Map<String, Object>> cmaps = new ArrayList<>(2);
    cmaps.add(cmapMember);
    cmaps.add(cmapMauth);

    return cmaps;
  }

  /**
   * Member address transform op for updating.
   *
   * <p>This is a way to accommodate database field/column constraints.</p>
   *
   *
   * @param maddressMap map of member address properties
   * @return map of cleansed addresses properties
   */
  public static Map<String, Object> transformMemberAddressForAdd(final Map<String, Object> maddressMap) {
    final Map<String, Object> cmapMaddress = new HashMap<>(8);

    mcopy(MADDRESS.MID, maddressMap, cmapMaddress);
    mcopy(MADDRESS.ADDRESS_NAME, maddressMap, cmapMaddress);

    mcopy(MADDRESS.ATTN, maddressMap, cmapMaddress, Util::asStringAndClean);
    mcopy(MADDRESS.STREET1, maddressMap, cmapMaddress, Util::asStringAndClean);
    mcopy(MADDRESS.STREET2, maddressMap, cmapMaddress, Util::asStringAndClean);
    mcopy(MADDRESS.CITY, maddressMap, cmapMaddress, Util::asStringAndClean);
    mcopy(MADDRESS.STATE, maddressMap, cmapMaddress, Util::asStringAndClean);
    mcopy(MADDRESS.POSTAL_CODE, maddressMap, cmapMaddress, Util::asStringAndClean);
    mcopy(MADDRESS.COUNTRY, maddressMap, cmapMaddress, Util::asStringAndClean);

    return cmapMaddress;
  }

  /**
   * Member address transform op for updating.
   *
   * <p>This is a way to accommodate database field/column constraints.</p>
   *
   *
   * @param maddressMap map of member address properties
   * @return immutable list of 3 ordered elements:
   *
   *         [0]    -> mid
   *         [1]    -> addressName
   *         [2]    -> map of cleansed member address properties that are com.tll.mcorpus.db persist ready
   */
  public static List<Object> transformMemberAddressForUpdate(final Map<String, Object> maddressMap) {
    final Map<String, Object> cmapMaddress = new HashMap<>(8);

    final UUID mid = fval(MADDRESS.MID, maddressMap);
    final Addressname addressname = fval(MADDRESS.ADDRESS_NAME, maddressMap);

    mcopy(MADDRESS.ATTN, maddressMap, cmapMaddress, Util::asStringAndClean);
    mcopy(MADDRESS.STREET1, maddressMap, cmapMaddress, Util::asStringAndClean);
    mcopy(MADDRESS.STREET2, maddressMap, cmapMaddress, Util::asStringAndClean);
    mcopy(MADDRESS.CITY, maddressMap, cmapMaddress, Util::asStringAndClean);
    mcopy(MADDRESS.STATE, maddressMap, cmapMaddress, Util::asStringAndClean);
    mcopy(MADDRESS.POSTAL_CODE, maddressMap, cmapMaddress, Util::asStringAndClean);
    mcopy(MADDRESS.COUNTRY, maddressMap, cmapMaddress, Util::asStringAndClean);

    return asList(mid, addressname, cmapMaddress);
  }
}
