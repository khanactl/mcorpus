package com.tll.mcorpus.web;

import java.security.SecureRandom;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

/**
 * Generates a random hex-wise string to serve as a JWT shared secret (salt).
 * <p>
 * One may invoke this class' main method to generate a new shared secret 
 * for use in generating JWTs used by the system.
 *  
 * @author jkirton
 */
public class JwtSharedSecretGenerator {
  
  /**
   * @return cryptographically strong random bytes array of size 32 (256 bits).  
   */
  public static byte[] random32bytes() {
    final SecureRandom random = new SecureRandom();
    final byte[] sharedSecret = new byte[32]; // i.e. 256 bytes
    random.nextBytes(sharedSecret);
    return sharedSecret;
  }

  /**
   * Generate a hex-wise string from an arbitrary byte array.
   * 
   * @param bytes
   *          the bytes array
   * @return newly created string that represents the given byte array as a
   *         hex-based string
   */
  public static String serialize(final byte[] bytes) {
    return DatatypeConverter.printHexBinary(bytes);
  }
  
  /**
   * 
   * @param hexToken
   * @return
   */
  public static byte[] deserialize(final String hexToken) {
    return DatatypeConverter.parseHexBinary(hexToken);
  }
  
  private JwtSharedSecretGenerator() {}
  
  public static void main(String[] args) {
    final byte[] randarr = random32bytes();
    final String s = serialize(randarr);
    final byte[] derandarr = deserialize(s);
    if(!Arrays.equals(randarr, derandarr)) throw new Error("byte array mismatch.");
    System.out.println(String.format("Random 32-byte hex token: %s", s));
  }
}
