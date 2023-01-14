package com.tll.mcorpus.dmodel;

import com.tll.mcorpus.db.enums.Location;

/**
 * Emp Id and Location backend.
 * 
 * @author jpk
 */
public class EmpIdAndLocation {

	private final String empId;
	private final Location location;

	public EmpIdAndLocation(String empId, Location location) {
		this.empId = empId;
		this.location = location;
	}

	public String empId() { return empId; }

	public Location location() { return location; }	 
}