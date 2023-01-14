package com.tll.validate;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNotNullOrEmpty;

/**
 * A single validation error.
 * 
 * @author jpk
 */
public class VldtnErr {

	public static VldtnErr verr(String vmsg) {
		return verr(vmsg, null, null);
	}

	public static VldtnErr verr(String vmsg, String fname) {
		return verr(vmsg, fname, null);
	}

	public static VldtnErr verr(String vmsg, String fname, String etype) {
		return verr(vmsg, fname, etype, null);
	}

	/**
	 * Nested level case - specify the parent path.
	 */
	public static VldtnErr verr(String vmsg, String fname, String etype, String ppath) {
		return new VldtnErr(vmsg, fname, etype, ppath);
	}

	private final String vmsg;
	private final String fname;
	private final String etype;
	private final String ppath;

	/**
	 * Constructor.
	 * 
	 * @param vmsg the UI display-ready validation error message
	 * @param fname the name of the invalid entity field
	 * @param fval optional field value
	 * @param etype optional parent entity type
	 * @param ppath optional parent path
	 */
	private VldtnErr(final String vmsg, final String fname, final String etype, final String ppath) {
		this.vmsg = clean(vmsg);
		this.fname = clean(fname);
		this.etype = clean(etype);
		this.ppath = clean(ppath);
	}

	/**
	 * @return the validation error message
	 */
	public String getVldtnErrMsg() { return vmsg; }

	/**
	 * @return the entity field name in error which may be null.
	 */
	public String getFieldName() { return fname; }

	/**
	 * The parent entity type name of the field in error.
	 */
	public String getParentType() { return etype; }

	/**
	 * @return the parent path.
	 * <p>
	 * Root-level entity validation implies an empty parent path 
	 * whereas nested entity validation implies a non-empty parent path.
	 */
	public String getParentPath() { return ppath; }

	/**
	 * @return the full <em>property path</em> identifying the field in error 
	 *				 relative to the root entity under validation.
	 *				 <p>
	 *				 [{ppath}.]{fname} never null.
	 */
	public String getFieldPath() { 
		final boolean hasPPath = isNotNullOrEmpty(ppath);
		final boolean hasField = isNotNullOrEmpty(fname);
		if(hasPPath && hasField) 
			return String.format("%s.%s", ppath, fname);
		else if(hasField) 
			return fname;
		return "";
	}

	@Override
	public String toString() {
		return String.format("VldtnErr[vmsg: %s, fname: %s, etype: %s, ppath: %s]",	 vmsg, fname, etype, ppath);
	}
}