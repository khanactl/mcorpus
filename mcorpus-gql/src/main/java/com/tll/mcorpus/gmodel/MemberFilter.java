package com.tll.mcorpus.gmodel;

import static com.tll.core.Util.copy;
import static com.tll.core.Util.isNotBlank;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNotNullOrEmpty;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.tll.mcorpus.db.enums.MemberStatus;

/**
 * Member search filter GraphQL entity type.
 * <p>
 * Encapsulates member search criteria backing the types
 * defined in the associated GraphQL schema.
 *
 * @author jpk
 */
public class MemberFilter {

	interface IFieldPredicate {
		/**
		 * @return true if a field value has been set,
		 *				 false otherwise.
		 */
		boolean isSet();
	}

	interface INullableFieldPredicate extends IFieldPredicate {
		/**
		 * @return true if the operation is a null or non-null check,
		 *				 false otherwise.
		 */
		boolean isNullCheck();
	}

	public static class StringPredicate implements INullableFieldPredicate {

		public enum Operation {
			IS_NULL,
			IS_NOT_NULL,
			EQUALS,
			LIKE,
		}

		private final Operation operation;
		private final String value;
		private final boolean ignoreCase;

		public StringPredicate(Operation operation, String value, boolean ignoreCase) {
			this.operation = operation;
			this.value = value;
			this.ignoreCase = ignoreCase;
		}

		@Override
		public boolean isNullCheck() {
			return operation == Operation.IS_NULL || operation == Operation.IS_NOT_NULL;
		}

		@Override
		public boolean isSet() {
			return isNullCheck() || isNotNull(value);
		}

		public Operation getOperation() { return operation; }

		public String getValue() { return value; }

		public boolean isIgnoreCase() { return ignoreCase; }

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			StringPredicate that = (StringPredicate) o;
			return ignoreCase == that.ignoreCase &&
				Objects.equals(value, that.value) &&
				operation == that.operation;
		}

		@Override
		public int hashCode() {
			return Objects.hash(value, ignoreCase, operation);
		}

		@Override
		public String toString() { return String.format("StringPredicate [value: '%s', ignoreCase? %s, operation: %s]", value, ignoreCase, operation); }
	}

	public static class DatePredicate implements INullableFieldPredicate {

		public enum DateOp {
			IS_NULL,
			IS_NOT_NULL,
			EQUAL_TO,
			NOT_EQUAL_TO,
			LESS_THAN,
			NOT_LESS_THAN,
			LESS_THAN_OR_EQUAL_TO,
			NOT_LESS_THAN_OR_EQUAL_TO,
			GREATER_THAN,
			NOT_GREATER_THAN,
			GREATER_THAN_OR_EQUAL_TO,
			NOT_GREATER_THAN_OR_EQUAL_TO,
			BETWEEN,
			NOT_BETWEEN;
		}

		private final DateOp dateOp;
		private final Date a;
		private final Date b;

		/**
		 * Constructor.
		 *
		 * @param dateOp the date op
		 * @param a the first instant argument
		 * @param b the second instant argument which may not be used (depends on the date op)
		 */
		public DatePredicate(DateOp dateOp, Date a, Date b) {
			this.dateOp = dateOp;
			this.a = copy(a);
			this.b = copy(b);
		}

		@Override
		public boolean isNullCheck() {
			return dateOp == DateOp.IS_NULL || dateOp == DateOp.IS_NOT_NULL;
		}

		@Override
		public boolean isSet() {
			if(isNotNull(dateOp)) {
				switch(dateOp) {
				case IS_NULL:
				case IS_NOT_NULL:
					return true;
				case BETWEEN:
				case NOT_BETWEEN:
					return a != null && b != null;
				default:
					return a != null;
				}
			}
			return false;
		}

		public DateOp getDateOp() {
			return dateOp;
		}

		public Date getA() { return copy(a); }

		public Date getB() { return copy(b); }

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			DatePredicate that = (DatePredicate) o;
			return dateOp == that.dateOp &&
				Objects.equals(a, that.a) &&
				Objects.equals(b, that.b);
		}

		@Override
		public int hashCode() {
			return Objects.hash(dateOp, a, b);
		}

		@Override
		public String toString() { return String.format("DatePredicate [Op: %s, argA: %s, argB: %s]", dateOp, a, b); }
	}

	public static class LocationPredicate implements IFieldPredicate {

		private final Set<String> locations;
		private final boolean negate;

		public LocationPredicate(final Collection<String> locations, boolean negate) {
			this.locations = isNullOrEmpty(locations) ? Collections.emptySet() : new HashSet<>(locations);
			this.negate = negate;
		}

		public boolean isSet() { return isNotNullOrEmpty(locations); }

		public Set<String> getLocations() { return locations; }

		/**
		 * @return true when the held set of Locations is to be considered as "not in",
		 *				 false -> "in"
		 */
		public boolean isNegate() { return negate; }

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			LocationPredicate that = (LocationPredicate) o;
			return negate == that.negate &&
				Objects.equals(locations, that.locations);
		}

		@Override
		public int hashCode() {
			return Objects.hash(locations, negate);
		}

		@Override
		public String toString() { return String.format("LocationPredicate[locations: %s, negate?: %s]", locations, negate); }
	}

	public static class OrderBy implements IFieldPredicate {

		public enum Dir { ASC, DESC; }

		private final String token;
		private final Dir dir;

		/**
		 * Constructor - token only with default dir asc.
		 */
		public OrderBy(String token) {
			this(token, Dir.ASC);
		}

		/**
		 * Constructor.
		 *
		 * @param token the order by token
		 * @param dir asc or desc?
		 */
		public OrderBy(String token, Dir dir) {
			this.token = token;
			this.dir = isNull(dir) ? Dir.ASC : dir;
		}

		public boolean isSet() { return isNotBlank(token); }

		public String getToken() { return token; }

		public Dir getDir() { return dir; }

		public boolean asc() { return dir == Dir.ASC; }

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			OrderBy orderBy = (OrderBy) o;
			return Objects.equals(token, orderBy.token) &&
				dir == orderBy.dir;
		}

		@Override
		public int hashCode() {
			return Objects.hash(token, dir);
		}

		@Override
		public String toString() { return String.format("OrderBy['%s' %s]", token, asc() ? "asc" : "desc"); }
	}

	/**
	 * The value used for the limit property when it is not explicitly specified.
	 */
	public static final int DEFAULT_LIMIT = 10;

	private int offset = 0;
	private int limit = DEFAULT_LIMIT;
	private DatePredicate created;
	private DatePredicate modified;
	private StringPredicate empId;
	private LocationPredicate location;
	private StringPredicate nameFirst;
	private StringPredicate nameMiddle;
	private StringPredicate nameLast;
	private StringPredicate displayName;
	private MemberStatus status;
	private DatePredicate dob;
	private StringPredicate username;

	private List<OrderBy> orderByList;

	/**
	 * Constructor.
	 */
	public MemberFilter() { }

	public int getOffset() { return offset; }

	public void setOffset(int offset) { this.offset = offset; }

	public int getLimit() { return limit == 0 ? DEFAULT_LIMIT : limit; }

	public void setLimit(int limit) { this.limit = limit; }

	public boolean hasCreated() { return isNotNull(created) && created.isSet(); }

	public DatePredicate getCreated() {
		return created;
	}

	public void setCreated(DatePredicate created) {
		this.created = created;
	}

	public boolean hasModified() { return isNotNull(modified) && modified.isSet(); }

	public DatePredicate getModified() {
		return modified;
	}

	public void setModified(DatePredicate modified) {
		this.modified = modified;
	}

	public boolean hasEmpId() { return isNotNull(empId) && empId.isSet(); }

	public StringPredicate getEmpId() {
		return empId;
	}

	public void setEmpId(StringPredicate empId) {
		this.empId = empId;
	}

	public boolean hasLocation() { return isNotNull(location) && location.isSet(); }

	public LocationPredicate getLocation() {
		return location;
	}

	public void setLocation(LocationPredicate location) {
		this.location = location;
	}

	public boolean hasNameFirst() { return isNotNull(nameFirst ) && nameFirst.isSet(); }

	public StringPredicate getNameFirst() {
		return nameFirst;
	}

	public void setNameFirst(StringPredicate nameFirst) {
		this.nameFirst = nameFirst;
	}

	public boolean hasNameMiddle() { return isNotNull(nameMiddle) && nameMiddle.isSet(); }

	public StringPredicate getNameMiddle() {
		return nameMiddle;
	}

	public void setNameMiddle(StringPredicate nameMiddle) {
		this.nameMiddle = nameMiddle;
	}

	public boolean hasNameLast() { return isNotNull(nameLast) && nameLast.isSet(); }

	public StringPredicate getNameLast() {
		return nameLast;
	}

	public void setNameLast(StringPredicate nameLast) {
		this.nameLast = nameLast;
	}

	public boolean hasDisplayName() { return isNotNull(displayName) && displayName.isSet(); }

	public StringPredicate getDisplayName() {
		return displayName;
	}

	public void setDisplayName(StringPredicate displayName) {
		this.displayName = displayName;
	}

	public boolean hasStatus() { return isNotNull(status); }

	public MemberStatus getStatus() {
		return status;
	}

	public void setStatus(MemberStatus status) {
		this.status = status;
	}

	public boolean hasDob() { return isNotNull(dob) && dob.isSet(); }

	public DatePredicate getDob() {
		return dob;
	}

	public void setDob(DatePredicate dob) {
		this.dob = dob;
	}

	public boolean hasUsername() { return isNotNull(username) && username.isSet(); }

	public StringPredicate getUsername() {
		return username;
	}

	public void setUsername(StringPredicate username) {
		this.username = username;
	}

	public List<OrderBy> getOrderByList() { return orderByList; }

	public void setOrderByList(final List<OrderBy> orderBys) {
		this.orderByList = isNullOrEmpty(orderBys) ? Collections.emptyList() : new ArrayList<>(orderBys);
	}

	/**
	 * @return true if at least one member filter constraint is set, false otherwise.
	 */
	public boolean isSet() {
		return hasCreated()
			|| hasModified()
			|| hasEmpId()
			|| hasLocation()
			|| hasNameFirst()
			|| hasNameMiddle()
			|| hasNameLast()
			|| hasDisplayName()
			|| hasStatus()
			|| hasDob()
			|| hasUsername()
			;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MemberFilter that = (MemberFilter) o;
		return Objects.equals(created, that.created) &&
			Objects.equals(modified, that.modified) &&
			Objects.equals(location, that.location) &&
			Objects.equals(nameFirst, that.nameFirst) &&
			Objects.equals(nameMiddle, that.nameMiddle) &&
			Objects.equals(nameLast, that.nameLast) &&
			Objects.equals(displayName, that.displayName) &&
			Objects.equals(status, that.status) &&
			Objects.equals(dob, that.dob) &&
			Objects.equals(username, that.username) &&
			Objects.equals(orderByList, that.orderByList)
			;
	}

	@Override
	public int hashCode() {
		return Objects.hash(created, modified, location, nameFirst, nameMiddle, nameLast, displayName, status, dob, username, orderByList);
	}

	/*
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("created", created)
			.add("modified", modified)
			.add("location", location)
			.add("nameFirst", nameFirst)
			.add("nameMiddle", nameMiddle)
			.add("nameLast", nameLast)
			.add("displayName", displayName)
			.toString();
	}
	*/
}
