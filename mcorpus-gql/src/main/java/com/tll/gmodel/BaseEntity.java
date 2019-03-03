package com.tll.gmodel;

/**
 * Base class for all {@link IGEntity} implementations.
 * 
 * @author jpk
 */
public abstract class BaseEntity<E extends IGEntity<E, PK>, PK extends IKey> implements IGEntity<E, PK> {

}