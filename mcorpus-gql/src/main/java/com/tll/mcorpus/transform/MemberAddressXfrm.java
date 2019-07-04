package com.tll.mcorpus.transform;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.lower;
import static com.tll.core.Util.neclean;
import static com.tll.core.Util.upper;
import static com.tll.transform.TransformUtil.fval;
import static com.tll.transform.TransformUtil.odtFromDate;
import static com.tll.transform.TransformUtil.odtToDate;
import static com.tll.transform.TransformUtil.uuidFromToken;

import java.util.Map;

import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.tables.pojos.Maddress;
import com.tll.mcorpus.gmodel.MemberAddress;

public class MemberAddressXfrm extends BaseMcorpusTransformer<MemberAddress, Maddress> {

  public static Addressname addressnameFromString(final String s) {
    try {
      return Addressname.valueOf(lower(clean(s)));
    } catch(Exception e) {
      return null;
    }
  }
  
  public static String addressnameToString(final Addressname enm) {
    return isNull(enm) ? null : enm.getLiteral();
  }

  @Override
  protected MemberAddress fromNotEmptyGraphQLMapForAdd(final Map<String, Object> gqlMap) {
    return new MemberAddress(
      uuidFromToken(fval("mid", gqlMap)),
      fval("addressName", gqlMap),
      fval("modified", gqlMap),
      fval("attn", gqlMap),
      fval("street1", gqlMap),
      fval("street2", gqlMap),
      fval("city", gqlMap),
      fval("state", gqlMap),
      fval("postalCode", gqlMap),
      fval("country", gqlMap)
    );
  }

  @Override
  public MemberAddress fromNotEmptyGraphQLMapForUpdate(final Map<String, Object> gqlMap) {
    return fromNotEmptyGraphQLMapForAdd(gqlMap);
  }

  @Override
  protected MemberAddress fromNonNullBackend(final Maddress d) {
    return new MemberAddress(
      d.getMid(),
      upper(addressnameToString(d.getAddressName())),
      odtToDate(d.getModified()),
      d.getAttn(),
      d.getStreet1(),
      d.getStreet2(),
      d.getCity(),
      d.getState(),
      d.getPostalCode(),
      d.getCountry()
    );
  }

  @Override
  protected Maddress toBackendFromNonNull(final MemberAddress g) {
    return new Maddress(
      g.getMid(),
      addressnameFromString(g.getAddressName()),
      odtFromDate(g.getModified()),
      neclean(g.getAttn()),
      neclean(g.getStreet1()),
      neclean(g.getStreet2()),
      neclean(g.getCity()),
      neclean(g.getState()),
      neclean(g.getPostalCode()),
      neclean(g.getCountry())
    );
  }

}