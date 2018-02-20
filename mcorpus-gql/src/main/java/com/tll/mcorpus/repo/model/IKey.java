package com.tll.mcorpus.repo.model;

/**
 * Simple contract for encapsulating one or more fields that together represent
 * a "key". Usually intended for representing either a primary or business key
 * for database access.
 * <p>
 * <b>Revision History</b><br>
 * <ul>
 * </ul>
 * </p>
 * @author jkirton
 * @since May 26, 2016
 */
public interface IKey extends IHasRefToken {

	/**
	 * @return <code>true</code> if this key has the necessary fields set to
	 *         obtain the object to which this key refers, <code>false</code>
	 *         otherwise.
	 */
	boolean isSet();
}
