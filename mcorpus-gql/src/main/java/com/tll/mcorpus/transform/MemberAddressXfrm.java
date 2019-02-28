package com.tll.mcorpus.transform;

import static com.tll.mcorpus.Util.neclean;
import static com.tll.mcorpus.Util.clean;
import static com.tll.mcorpus.Util.isNull;
import static com.tll.mcorpus.Util.lower;
import static com.tll.mcorpus.Util.uuidFromToken;
import static com.tll.mcorpus.repo.RepoUtil.fval;

import java.util.Map;

import com.tll.mcorpus.db.enums.Addressname;
import com.tll.mcorpus.db.tables.pojos.Maddress;
import com.tll.mcorpus.gmodel.MemberAddress;
import com.tll.mcorpus.transformapi.BaseTransformer;

public class MemberAddressXfrm extends BaseTransformer<MemberAddress, Maddress> {

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
      addressnameToString(d.getAddressName()),
      d.getModified(),
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
      dateToTimestamp(g.getModified()),
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