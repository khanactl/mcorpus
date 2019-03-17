package com.tll.mcorpus.gmodel;

import java.time.Instant;

public class Mlogin {
  private final String username;
  private final String pswd;
  private final Instant requestInstant;
  private final String requestOrigin;

  public Mlogin(String username, String pswd, Instant requestInstant, String requestOrigin) {
    this.username = username;
    this.pswd = pswd;
    this.requestInstant = requestInstant;
    this.requestOrigin = requestOrigin;
  }

  public String getUsername() {
    return username;
  }

  public String getPswd() {
    return pswd;
  }

  public Instant getRequestInstant() {
    return requestInstant;
  }

  public String getRequestOrigin() {
    return requestOrigin;
  }

}