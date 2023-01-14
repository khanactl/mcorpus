package com.tll.web;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.UUID;

import org.junit.Test;

public class RequestSnapshotTest {

	static RequestSnapshot testRequestSnapshot_single() {
		return new RequestSnapshot(
				Instant.now(),
				"127.0.0.1",
				"path",
				"POST",
				"host",
				"origin",
				"https://mcorpus.d2d",
				"forwarded",
				"127.0.0.1",	// x-forwarded-for
				"localhost",	// x-forwarded-host
				"http",				// x-forwarded-proto
				null, // auth header
				null, // refresh token
				null, // rst cookie
				null, // rst header
				UUID.randomUUID().toString()
		);
	}

	static RequestSnapshot testRequestSnapshot_multi() {
		return new RequestSnapshot(
				Instant.now(),
				"127.0.0.1, 88.44.33.22, 1.1.1.1",
				"path",
				"POST",
				"host",
				"origin",
				"https://mcorpus.d2d",
				"forwarded",
				"127.0.0.1",	// x-forwarded-for
				"localhost",	// x-forwarded-host
				"http",				// x-forwarded-proto
				null, // auth header
				null, // refresh token
				null, // rst cookie
				null, // rst header
				UUID.randomUUID().toString()
		);
	}

	@Test
	public void stringQSTest() {
		assertEquals(null, RequestSnapshot.stripQS(null));
		assertEquals("", RequestSnapshot.stripQS(""));
		assertEquals(" ", RequestSnapshot.stripQS(" "));
		assertEquals("domain.com", RequestSnapshot.stripQS("domain.com"));
		assertEquals("domain.com", RequestSnapshot.stripQS("domain.com?"));
		assertEquals("domain.com", RequestSnapshot.stripQS("domain.com?a=b&c=d"));
	}

	@Test
	public void stripSchemeAndPortTest() {
		assertEquals("localhost", RequestSnapshot.stripSchemeAndPort("https://localhost:3000"));
		assertEquals("localhost", RequestSnapshot.stripSchemeAndPort("http://localhost:3000"));
		assertEquals("localhost", RequestSnapshot.stripSchemeAndPort("localhost:3000"));
		assertEquals("localhost", RequestSnapshot.stripSchemeAndPort("localhost"));
	}

	@Test
	public void testXForwardedForClientIp() {
		RequestSnapshot rs;
		rs = testRequestSnapshot_multi();
		assertEquals("127.0.0.1", rs.getXForwardedForClientIp());
		rs = testRequestSnapshot_single();
		assertEquals("127.0.0.1", rs.getXForwardedForClientIp());
	}
}