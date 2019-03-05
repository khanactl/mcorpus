package com.tll.mcorpus.gmodel;

import static com.tll.core.Util.copy;
import static com.tll.core.Util.isNotNullOrEmpty;

import java.util.Date;
import java.util.UUID;

import com.tll.gmodel.BaseEntity;
import com.tll.gmodel.IKey;

public class MemberAddress extends BaseEntity<MemberAddress, MemberAddress.MidAndAddressName> {

  public static class MidAndAddressName implements IKey {

    private final UUID mid;
    private final String addressName;

    public MidAndAddressName(final UUID mid, final String addressName) {
      this.mid = mid;
      this.addressName = addressName;
    }

    public UUID getMid() { return mid; }

    public String getAddressName() { return addressName; }

    @Override
    public boolean isSet() { return mid != null && isNotNullOrEmpty(addressName); }

    @Override
    public String refToken() { return String.format("Maddress[mid: %s, AddressName: %s]", mid, addressName); }
  }

  private final MidAndAddressName pk;
  private final UUID        mid;
  private final String      addressName;
  private final Date        modified;
  private final String      attn;
  private final String      street1;
  private final String      street2;
  private final String      city;
  private final String      state;
  private final String      postalCode;
  private final String      country;

  public MemberAddress(UUID mid, String addressName, Date modified, String attn, String street1, String street2, String city, String state, String postalCode, String country) {
    this.pk = new MidAndAddressName(mid, addressName);
    this.mid = mid;
    this.addressName = addressName;
    this.modified = copy(modified);
    this.attn = attn;
    this.street1 = street1;
    this.street2 = street2;
    this.city = city;
    this.state = state;
    this.postalCode = postalCode;
    this.country = country;
  }

  @Override
  public MemberAddress.MidAndAddressName getPk() { return pk; }
  
  public UUID getMid() {
    return mid;
  }

  public String getAddressName() {
    return addressName;
  }

  public Date getModified() {
    return copy(modified);
  }

  public String getAttn() {
    return attn;
  }

  public String getStreet1() {
    return street1;
  }

  public String getStreet2() {
    return street2;
  }

  public String getCity() {
    return city;
  }

  public String getState() {
    return state;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public String getCountry() {
    return country;
  }
}