package com.tll.gmodel;

/**
 * Represents either a primary key or business key that uniquely
 * identifies some business entity (usually an RDBMS record).
 * <P>
 * Required IKey attribues:<br>
 * <ol>
 * <li>Is it set?  Are all the 'fields' that comprise the key non-null
 *    and valid guaranteeing that it uniquely identifies some remote entity?
 *
 * <li>Ability to provide a reference token (string representation) of the key
 *    which itself is a unique identifier.
 *
 * <li>Optionally, field accessor methods in {@link IKey} implementations.
 * </ol>
 *
 * @author jpk
 */
public interface IKey {

  /**
   * @return true when the constituent key fields are set, false otherwise.
   */
  boolean isSet();

  /**
   * @return presentation-worthy text conveying the type and state of this key.
   */
  String refToken();
}