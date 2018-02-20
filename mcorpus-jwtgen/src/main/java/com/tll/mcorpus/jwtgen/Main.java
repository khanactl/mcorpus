package com.tll.mcorpus.jwtgen;

import static com.tll.mcorpus.jwtgen.JwtGen.processInput;

/**
 * The jwtgen entry point.
 * <p>
 * This shall be packaged as an uber-jar for easy JWT token generation
 * thus precluding the need to login through the mcorpus web ui.
 * <p>
 * Now we have a separate, behind a trust-wall JWT token generator that
 * grants access to the mcorpus data api.
 *
 * @author jkirton
 */
public class Main {

  /**
   * mcorpus jwtgen command-line entry point.
   *
   * @param args input arguments
   */
  public static void main(final String... args) {
    System.out.print(processInput(args));
  }
}
