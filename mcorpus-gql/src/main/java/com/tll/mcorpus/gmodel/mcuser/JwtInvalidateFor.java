package com.tll.mcorpus.gmodel.mcuser;

import java.time.Instant;
import java.util.UUID;

public class JwtInvalidateFor {
  private final UUID uid;
  private final Instant requestInstant;
  private final String requestOrigin;

  public JwtInvalidateFor(UUID uid, Instant requestInstant, String requestOrigin) {
    this.uid = uid;
    this.requestInstant = requestInstant;
    this.requestOrigin = requestOrigin;
  }

  public UUID getUid() { return uid; }

  public Instant getRequestInstant() { return requestInstant; }

  public String getRequestOrigin() { return requestOrigin; }
}