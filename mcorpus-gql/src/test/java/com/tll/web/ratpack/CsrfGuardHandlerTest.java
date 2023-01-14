package com.tll.web.ratpack;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import com.tll.UnitTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

@Category(UnitTest.class)
public class CsrfGuardHandlerTest {
	public static final Pattern PTRN_GEN_RST = Pattern.compile("^(graphql\\/index|graphql)\\/?$");

	/**
	 * We only want two paths to satisfy the check here with optional trailing slash:
	 * <code>graphql[/] and graphql/index[/]</code>
	 */
	@Test
	public void testNoRstPattern() {
		assertTrue(PTRN_GEN_RST.matcher("graphql").matches());
		assertTrue(PTRN_GEN_RST.matcher("graphql/").matches());
		assertTrue(PTRN_GEN_RST.matcher("graphql/index").matches());
		assertTrue(PTRN_GEN_RST.matcher("graphql/index/").matches());

		assertFalse(PTRN_GEN_RST.matcher("sub/graphql/index").matches());
		assertFalse(PTRN_GEN_RST.matcher("graphql/index/sub").matches());
		assertFalse(PTRN_GEN_RST.matcher("graphql/index.html").matches());
		assertFalse(PTRN_GEN_RST.matcher("index.js").matches());
		assertFalse(PTRN_GEN_RST.matcher("index.css").matches());
		assertFalse(PTRN_GEN_RST.matcher("favicon.ico").matches());
	}
}