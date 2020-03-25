package com.tll.jwt;

import java.util.Arrays;

/**
 * Generates a random hex-wise string to serve as a JWT shared secret (salt).
 * <p>
 * One may invoke this class' main method to generate a new shared secret
 * for use in generating JWTs used by the system.
 *
 * @author jkirton
 */
public class JwtSharedSecretGenerator {

  private JwtSharedSecretGenerator() {}

  public static void main(String[] args) {
    final byte[] randarr = JWT.generateJwtSharedSecret();
    final String s = JWT.serialize(randarr);
    final byte[] derandarr = JWT.deserialize(s);
    if(!Arrays.equals(randarr, derandarr)) throw new Error("byte array mismatch.");
    System.out.println(String.format("Random 32-byte hex token: %s", s));
  }
}
