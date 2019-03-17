package com.tll.mcorpus.gmodel;

import static com.tll.core.Util.isNotNullOrEmpty;
import static com.tll.core.Util.isNotNull;

import java.util.UUID;

import com.tll.gmodel.IKey;

public class MidAndAddressnameKey implements IKey {
  private final UUID mid;
  private final String addressname;

  public MidAndAddressnameKey(UUID mid, String addressname) {
    this.mid = mid;
    this.addressname = addressname;
  }

  public UUID getMid() { return mid; }

  public String getAddressname() { return addressname; }

  @Override
  public boolean isSet() { return isNotNull(mid) && isNotNullOrEmpty(addressname); }

  @Override
  public String refToken() {
    return String.format("MidAndAddressnameKey[mid: %s, addressname: %s]", mid, addressname);
  }
}