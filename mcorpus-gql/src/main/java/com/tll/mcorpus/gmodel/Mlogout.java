package com.tll.mcorpus.gmodel;

import java.time.Instant;
import java.util.UUID;

public class Mlogout {
  private final UUID mid;
  private final Instant requestInstant;
  private final String requestOrigin;

  public Mlogout(UUID mid, Instant requestInstant, String requestOrigin) {
    this.mid = mid;
    this.requestInstant = requestInstant;
    this.requestOrigin = requestOrigin;
  }

  public UUID getMid() { return mid; }

  public Instant getRequestInstant() { return requestInstant; }

  public String getRequestOrigin() { return requestOrigin; }
}