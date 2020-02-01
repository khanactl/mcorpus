package com.tll.mcorpus.gmodel;

import static com.tll.core.Util.copy;
import static com.tll.core.Util.isNotNullOrEmpty;

import java.util.Date;
import java.util.UUID;

import com.tll.gmodel.BaseEntity;
import com.tll.gmodel.UUIDKey;

public class MemberAddress extends BaseEntity<MemberAddress, MemberAddress.MidAndAddressNameKey> {

  public static class MidAndAddressNameKey extends UUIDKey {

    private final String addressName;

    public MidAndAddressNameKey(final UUID mid, final String addressName) {
      super(mid, "maddress");
      this.addressName = addressName;
    }

    public UUID getMid() { return uuid; }

    public String getAddressName() { return addressName; }

    @Override
    public boolean isSet() { return super.isSet() && isNotNullOrEmpty(addressName); }

    @Override
    public String refToken() { return String.format("Maddress[mid: %s, AddressName: %s]", uuid, addressName); }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (!super.equals(obj))
        return false;
      if (getClass() != obj.getClass())
        return false;
      MidAndAddressNameKey other = (MidAndAddressNameKey) obj;
      if (addressName == null) {
        if (other.addressName != null)
          return false;
      } else if (!addressName.equals(other.addressName))
        return false;
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((addressName == null) ? 0 : addressName.hashCode());
      return result;
    }

  } // MidAndAddressNameKey

  private final MidAndAddressNameKey pk;
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
    this.pk = new MidAndAddressNameKey(mid, addressName);
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
  public MemberAddress.MidAndAddressNameKey getPk() { return pk; }

  public UUID getMid() {
    return pk.getUUID();
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