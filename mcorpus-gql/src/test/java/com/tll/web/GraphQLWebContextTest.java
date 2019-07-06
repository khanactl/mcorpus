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
    return new GraphQLWebContext(query, null);
  }

  @Test
  public void testRemoveBackslashN() throws Exception {
    String tstr = "testing \\n test";
    String pstr = tstr.replaceAll("\\\\n", "");
    assertEquals("testing  test", pstr);
  }

  @Test
  public void testReduceWhitespace() throws Exception {
    String tstr = "    testing   \n \n     test  \n  \n  ";
    String pstr = tstr.replaceAll("\\s+", " ");
    assertEquals(" testing test ", pstr);
  }

  @Test
  public void testQueryType() throws Exception {
    GraphQLWebContext ctx;

    ctx = create("query");
    assertEquals("", ctx.getQueryType());
  
    ctx = create("query {");
    assertEquals("query", ctx.getQueryType());
  
    ctx = create(" query {");
    assertEquals("query", ctx.getQueryType());
  
    ctx = create(" query {  ");
    assertEquals("query", ctx.getQueryType());
  
    ctx = create("query { methodName");
    assertEquals("query", ctx.getQueryType());
  
    ctx = create("query qname { methodName");
    assertEquals("query", ctx.getQueryType());

    ctx = create("  query qname { methodName  ");
    assertEquals("query", ctx.getQueryType());
    
    ctx = create("mutation");
    assertEquals("", ctx.getQueryType());
  
    ctx = create("mutation {");
    assertEquals("mutation", ctx.getQueryType());
  
    ctx = create(" mutation {");
    assertEquals("mutation", ctx.getQueryType());
  
    ctx = create("mutation { methodName");
    assertEquals("mutation", ctx.getQueryType());
  
    ctx = create("mutation qname { methodName");
    assertEquals("mutation", ctx.getQueryType());

    ctx = create("  mutation qname { methodName  ");
    assertEquals("mutation", ctx.getQueryType());
  }

  @Test
  public void testOperationName() throws Exception {
    GraphQLWebContext ctx;

    ctx = create("query");
    assertEquals("", ctx.getOpName());
  
    ctx = create("query {}");
    assertEquals("", ctx.getOpName());
  
    ctx = create("query { method }");
    assertEquals("", ctx.getOpName());
  
    ctx = create("query opname { method }");
    assertEquals("opname", ctx.getOpName());
    
    ctx = create("query opname($mid : ID!) { method(mid: $mid) }");
    assertEquals("opname", ctx.getOpName());
  }

  @Test
  public void testFirstMethodName() throws Exception {
    GraphQLWebContext ctx;
    
    ctx = create("query qname($mid: ID) { methodName {} }");
    assertEquals("methodName", ctx.getFirstMethodName());
  
    ctx = create("query { methodName }");
    assertEquals("methodName", ctx.getFirstMethodName());
  
    ctx = create("{ methodName {");
    assertEquals("", ctx.getFirstMethodName());
  
    ctx = create("{ methodName }");
    assertEquals("methodName", ctx.getFirstMethodName());
  
    ctx = create("{ methodName }");
    assertEquals("methodName", ctx.getFirstMethodName());
  
    ctx = create("  { methodName }  ");
    assertEquals("methodName", ctx.getFirstMethodName());
  
    ctx = create("query { methodName");
    assertEquals("", ctx.getFirstMethodName());
  
    ctx = create("  query  { methodName");
    assertEquals("", ctx.getFirstMethodName());
  
    ctx = create("  query  { methodName(id: \"tank\") {} } ");
    assertEquals("methodName", ctx.getFirstMethodName());
  
    ctx = create("  query  { ");
    assertEquals("", ctx.getFirstMethodName());
  
    ctx = create(" ");
    assertEquals("", ctx.getFirstMethodName());
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

    ctx = create("IntrospectionQuery {");
    assertFalse(ctx.isIntrospectionQuery());
    
    ctx = create(" IntrospectionQuery {");
    assertFalse(ctx.isIntrospectionQuery());

    ctx = create("query IntrospectionQuery {");
    assertTrue(ctx.isIntrospectionQuery());

    ctx = create("query IntrospectionQuery {    __schema { ");
    assertTrue(ctx.isIntrospectionQuery());
  }
}