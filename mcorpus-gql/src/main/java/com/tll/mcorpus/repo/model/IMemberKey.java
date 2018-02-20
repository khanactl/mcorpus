package com.tll.mcorpus.repo.model;

import com.tll.mcorpus.db.enums.Location;

import java.util.UUID;

/**
 * Identifies a single member either by ssn or by employee id and location or
 * both.
 * <p>
 * <b>Revision History</b><br>
 * <ul>
 * </ul>
 * </p>
 * @author jkirton
 * @since May 2016
 */
public interface IMemberKey extends IKey {

  /**
   * @return the unique member id
   */
  UUID getMid();

	/**
	 * @return the employee id
	 */
	String getEmpId();

	/**
	 * @return the employee location
	 */
  Location getLocation();
}
