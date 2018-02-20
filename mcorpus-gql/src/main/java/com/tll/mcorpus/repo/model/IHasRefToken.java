package com.tll.mcorpus.repo.model;

/**
 * Indicates the ability to provide a "reference token".
 * <p>
 * <b>Revision History</b><br>
 * <ul>
 * </ul>
 * </p>
 * @author jkirton
 * @since May 31, 2016
 */
public interface IHasRefToken {

	/**
	 * @return A terse token containing the key fields and their values intended
	 *         to serve as a human readable message for logging and/or UI display.
	 */
	String refToken();
}
