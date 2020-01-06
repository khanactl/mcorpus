package com.tll.core;

/**
 * Indicates the ability to provide a 'reference token'.
 *
 * @author jpk
 */
public interface IHasRefToken {

  /**
   * @return A short string conveying the state of some entity
   *         or to convey a short human readable message.
   */
  String refToken();
}