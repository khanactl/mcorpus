package com.tll.mcorpus.web;

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
    return new GraphQLWebContext(query, null, null, null, null);
  }

  @Test
  public void testOperationName() throws Exception {
    GraphQLWebContext ctx;

    ctx = create("query { mrefByMid");
    assertEquals("", ctx.getOperationName());
  
    ctx = create("query opname { mrefByMid");
    assertEquals("opname", ctx.getOperationName());

    ctx = create("  query opname { mrefByMid");
    assertEquals("opname", ctx.getOperationName());
    
    ctx = create("opname { mrefByMid");
    assertEquals("", ctx.getOperationName());
    
    ctx = create("  opname { mrefByMid");
    assertEquals("", ctx.getOperationName());
  }

  @Test
  public void testQueryMethodName() throws Exception {
    GraphQLWebContext ctx;
    
    ctx = create("{ mrefByMid");
    assertEquals("mrefByMid", ctx.getQueryMethodName());
  
    ctx = create("query { mrefByMid");
    assertEquals("mrefByMid", ctx.getQueryMethodName());
  
    ctx = create("  query  { mrefByMid");
    assertEquals("mrefByMid", ctx.getQueryMethodName());
  
    ctx = create("  query  { mrefByMid(mid: ) {}");
    assertEquals("mrefByMid", ctx.getQueryMethodName());
  
    ctx = create("  query  { ");
    assertEquals("", ctx.getQueryMethodName());
  
    ctx = create(" ");
    assertEquals("", ctx.getQueryMethodName());
  }

  @Test
  public void testIsIntrospectionQuery() {
    GraphQLWebContext ctx;

    ctx = create("{ mrefByMid");
    assertFalse(ctx.isIntrospectionQuery());
  
    ctx = create("{ mclogin");
    assertFalse(ctx.isIntrospectionQuery());
    
    ctx = create("login { mclogin");
    assertFalse(ctx.isIntrospectionQuery());
    
    ctx = create("mutation login { mclogin");
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

  @Test
  public void testIsMcuserLoginQuery() throws Exception {
    GraphQLWebContext ctx;

    ctx = create("{ mrefByMid");
    assertFalse(ctx.isMcuserLoginQuery());
  
    ctx = create("{ mclogin");
    assertTrue(ctx.isMcuserLoginQuery());
    
    ctx = create("login { mclogin");
    assertTrue(ctx.isMcuserLoginQuery());
    
    ctx = create("mutation login { mclogin");
    assertTrue(ctx.isMcuserLoginQuery());

    ctx = create("query IntrospectionQuery {");
    assertFalse(ctx.isMcuserLoginQuery());
    
    ctx = create("IntrospectionQuery {");
    assertFalse(ctx.isMcuserLoginQuery());
  }
}