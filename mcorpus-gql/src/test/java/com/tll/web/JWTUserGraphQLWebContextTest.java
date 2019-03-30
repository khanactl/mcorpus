package com.tll.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tll.UnitTest;
import com.tll.web.JWTUserGraphQLWebContext;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit test methods for verifying the public methods in {@link GraphQLWebContext}.
 */
@Category(UnitTest.class)
public class JWTUserGraphQLWebContextTest {

  static JWTUserGraphQLWebContext create(String query) {
    return new JWTUserGraphQLWebContext(query, null, null, null, null, null, null, "jwtLogin");
  }

  @Test
  public void testIsMcuserLoginQuery() throws Exception {
    JWTUserGraphQLWebContext ctx;

    ctx = create("{ mrefByMid");
    assertFalse(ctx.isJwtUserLoginQuery());
  
    ctx = create("{ jwtLogin");
    assertTrue(ctx.isJwtUserLoginQuery());
    
    ctx = create("login { jwtLogin");
    assertTrue(ctx.isJwtUserLoginQuery());
    
    ctx = create("mutation login { jwtLogin");
    assertTrue(ctx.isJwtUserLoginQuery());

    ctx = create("query IntrospectionQuery {");
    assertFalse(ctx.isJwtUserLoginQuery());
    
    ctx = create("IntrospectionQuery {");
    assertFalse(ctx.isJwtUserLoginQuery());
  }
}