package com.tll.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tll.UnitTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit test methods for verifying the public methods in {@link GraphQLWebContext}.
 */
@Category(UnitTest.class)
public class JWTUserGraphQLWebContextTest {

	static JWTUserGraphQLWebContext create(String query) {
		return new JWTUserGraphQLWebContext(query, null, null, null, null, null, "jwtLogin", "jwtRefresh");
	}

	@Test
	public void testIsJwtUserLoginQuery() throws Exception {
		JWTUserGraphQLWebContext ctx;

		ctx = create("{ fetchOp");
		assertFalse(ctx.isJwtUserLoginQuery());

		ctx = create("{ jwtLogin");
		assertFalse(ctx.isJwtUserLoginQuery());

		ctx = create("login { jwtLogin");
		assertFalse(ctx.isJwtUserLoginQuery());

		ctx = create("query IntrospectionQuery {");
		assertFalse(ctx.isJwtUserLoginQuery());

		ctx = create("IntrospectionQuery {");
		assertFalse(ctx.isJwtUserLoginQuery());

		ctx = create("mutation login { jwtLogin }");
		assertTrue(ctx.isJwtUserLoginQuery());

		ctx = create("mutation { jwtLogin }");
		assertTrue(ctx.isJwtUserLoginQuery());
	}
}