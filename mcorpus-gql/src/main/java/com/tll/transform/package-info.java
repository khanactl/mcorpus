/**
 * Generalized Transformation API intended for transforming frontend GraphQL types 
 * to backend domain types and vice versa.
 * <p>
 * Transform implementations (packages) have awareness of both frontend types and backend types.
 * In fact, the transformation package serves to provide the physical and logical 
 * boundary between the two. This promotes good separation of concerns, single responsibility 
 * and code modularity so as to be less brittle when either the frontend or backend changes 
 * over time.
 * 
 * @author jpk
 */
package com.tll.transform;
