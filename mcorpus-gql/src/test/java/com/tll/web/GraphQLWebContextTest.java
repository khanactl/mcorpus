package com.tll.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tll.UnitTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit test methods for verifying the public methods in {@link GraphQLWebContext}.
 */
@Category(UnitTest.class)
public class GraphQLWebContextTest {

  static GraphQLWebContext create(String query) {
    return new GraphQLWebContext(query, null, null);
  }

  @Test
  public void testOperationName() throws Exception {
    GraphQLWebContext ctx;

    ctx = create("query { opName");
    assertEquals("", ctx.getOperationName());
  
    ctx = create("query opname { opName");
    assertEquals("opname", ctx.getOperationName());

    ctx = create("  query opname { opName");
    assertEquals("opname", ctx.getOperationName());
    
    ctx = create("opname { opName");
    assertEquals("", ctx.getOperationName());
    
    ctx = create("  opname { opName");
    assertEquals("", ctx.getOperationName());
  }

  @Test
  public void testQueryMethodName() throws Exception {
    GraphQLWebContext ctx;
    
    ctx = create("{ gquery");
    assertEquals("gquery", ctx.getQueryMethodName());
  
    ctx = create("query { gquery");
    assertEquals("gquery", ctx.getQueryMethodName());
  
    ctx = create("  query  { gquery");
    assertEquals("gquery", ctx.getQueryMethodName());
  
    ctx = create("  query  { gquery(id: ) {}");
    assertEquals("gquery", ctx.getQueryMethodName());
  
    ctx = create("  query  { ");
    assertEquals("", ctx.getQueryMethodName());
  
    ctx = create(" ");
    assertEquals("", ctx.getQueryMethodName());
  }

  @Test
  public void testIsIntrospectionQuery() {
    GraphQLWebContext ctx;

    ctx = create("{ gquery");
    assertFalse(ctx.isIntrospectionQuery());
  
    ctx = create("{ gquery");
    assertFalse(ctx.isIntrospectionQuery());
    
    ctx = create("somelogin { loginmethod");
    assertFalse(ctx.isIntrospectionQuery());
    
    ctx = create("mutation login { loginmethod");
    assertFalse(ctx.isIntrospectionQuery());

    ctx = create("query IntrospectionQuery {");
    assertTrue(ctx.isIntrospectionQuery());

    ctx = create("IntrospectionQuery {");
    assertTrue(ctx.isIntrospectionQuery());
    
    ctx = create(" IntrospectionQuery {");
    assertTrue(ctx.isIntrospectionQuery());

    ctx = create("query IntrospectionQuery {    __schema { ");
    assertTrue(ctx.isIntrospectionQuery());
  }
}