package com.tll.mcorpus.dmodel;

import java.util.UUID;

import com.tll.mcorpus.db.enums.Addressname;

public class MidAndAddressname {
	private final UUID mid;
	private final Addressname addressname;

	public MidAndAddressname(UUID mid, Addressname addressname) {
		this.mid = mid;
		this.addressname = addressname;
	}

	public UUID getMid() { return mid; }

	public Addressname getAddressname() { return addressname; }
}