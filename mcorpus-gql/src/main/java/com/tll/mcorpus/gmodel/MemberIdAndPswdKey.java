package com.tll.mcorpus.gmodel;

import static com.tll.core.Util.isNotBlank;
import static com.tll.core.Util.isNotNull;

import java.util.UUID;

import com.tll.gmodel.IKey;

public class MemberIdAndPswdKey implements IKey {
  private final UUID mid;
  private final String pswd;

  public MemberIdAndPswdKey(UUID uid, String pswd) {
    this.mid = uid;
    this.pswd = pswd;
  }

  public UUID getMid() { return mid; }

  public String getPswd() { return pswd; }

  @Override
  public boolean isSet() { return isNotNull(mid) && isNotBlank(pswd); }

  @Override
  public String refToken() { return String.format("MemberIdAndPswdKey[uid: %s]", mid); }

  

}