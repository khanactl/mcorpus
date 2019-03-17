package com.tll.mcorpus.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tll.UnitTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit test methods for verifying the public methods in {@link GraphQLWebContext}.
 */
@Category(UnitTest.class)
public class MCorpusGraphQLWebContextTest {

  static MCorpusGraphQLWebContext create(String query) {
    return new MCorpusGraphQLWebContext(query, null, null, null, null);
  }

  @Test
  public void testIsMcuserLoginQuery() throws Exception {
    MCorpusGraphQLWebContext ctx;

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