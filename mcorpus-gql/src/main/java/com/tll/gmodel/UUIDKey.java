package com.tll.gmodel;

import java.util.UUID;

/**
 * {@link IKey} impl for UUIDs and an ascribed name.
 */
public class UUIDKey implements IKey {

  protected final UUID uuid;
  protected final String name;

  public UUIDKey(final UUID uuid, final String name) {
    this.uuid = uuid;
    this.name = name;
  }

  /**
   * @return the {@link UUID} key value.
   */
  public UUID getUUID() { return uuid; }

  /**
   * @return the ascribed key name.
   */
  public String getName() { return name; }

  @Override
  public String refToken() { return String.format("%s[%s]", name, uuid); }

  @Override
  public boolean isSet() { return uuid != null; }

  @Override
  public String toString() { return refToken(); }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UUIDKey other = (UUIDKey) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (uuid == null) {
      if (other.uuid != null)
        return false;
    } else if (!uuid.equals(other.uuid))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
    return result;
  }

}