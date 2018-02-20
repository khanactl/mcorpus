package com.tll.mcorpus.repo.model;

import static com.tll.mcorpus.Util.asString;
import static org.jooq.impl.DSL.not;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.jooq.Condition;
import org.jooq.Field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.tll.mcorpus.db.enums.Location;

/**
 * Encapsulates search constraints for the member location field.
 */
public class LocationPredicate implements IFieldPredicate<Location> {

  public static LocationPredicate fromMap(final Map<String, Object> map) {
    LocationPredicate lp = null;
    if(map !=  null && !map.isEmpty()) {
      lp = new LocationPredicate();
      for(final Entry<String, Object> entry : map.entrySet()) {
        String key = entry.getKey();
        switch (key) {
          case "locations":
            @SuppressWarnings("unchecked") List<String> sloclist = (List<String>) entry.getValue();
            if(sloclist != null && !sloclist.isEmpty()) {
             for(final String sloc : sloclist) {
               if(sloc.startsWith("L")) {
                 String literalLoc = sloc.substring(1);
                 for(final Location loc : Location.values()) {
                   if(loc.getLiteral().equals(literalLoc)) {
                     lp.addLocation(loc);
                     break;
                   }
                 }
               }
             }
            }
            break;
          case "negate":
            lp.negate = Boolean.valueOf(asString(entry.getValue())).booleanValue();
            break;
        }
      }
    }
    return lp;
  }

  private final Set<Location> locations = new HashSet<>(5);

  private boolean negate;

  /**
   * Constructor.
   *
   * The <code>negate</code> defaults to <code>false</code>.
   */
  public LocationPredicate() {
    this(false);
  }

  /**
   * Constructor.
   *
   * @param locations
   * @param negate
   */
  @JsonCreator
  public LocationPredicate(List<Location> locations, boolean negate) {
    this.locations.addAll(locations);
    this.negate = negate;
  }

  /**
   * Constructor.
   *
   * @param negate when true, the constraint is taken as "not in"
   *                     whereas when false (default), the constraint is taken as "in".
   */
  public LocationPredicate(boolean negate) {
    this.negate = negate;
  }

  /**
   * Add a location to the set of locations in this predicate.
   *
   * <p>NOTE: if the location is already present, nothing happens. </p>
   *
   * @param loc the [unique] location to add
   */
  public void addLocation(Location loc) {
    locations.add(loc);
  }

  @Override
  public Condition asJooqCondition(final Field<Location> f) {
    return negate ? not(f.in(locations)) : f.in(locations);
  }

  /**
   * @return true when the held set of Locations is to be considered as "not in",
   *         false -> "in"
   */
  public boolean isNegate() {
    return negate;
  }

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
  public String toString() { return String.format("locations: %s, negate?: %s", locations, negate); }
}
