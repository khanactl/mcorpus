package com.tll.jwt;

import java.util.Date;
import java.util.UUID;

public interface IJwtInfo {
  UUID getJwtId();
  Date created();
  Date expires();
  String clientOrigin();
}