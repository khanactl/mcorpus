package com.tll.web.ratpack;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.lower;

import java.util.Objects;
import java.util.UUID;

import com.tll.web.RequestSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.core.handling.Context;
import ratpack.core.handling.RequestId;
import ratpack.core.http.Request;

/**
 * {@link RequestSnapshot} provider Ratpack style.
 *
 * @author jpk
 */
public class RequestSnapshotFactory {

	static boolean isNullwiseOrEmpty(final String s) {
		final String sc = lower(clean(s));
		return isNullOrEmpty(sc) || "null".equals(sc) || "undefined".equals(sc);
	}

	static String nullif(final String s) {
		return isNullwiseOrEmpty(s) ? null : s;
	}

	private final Logger log = LoggerFactory.getLogger("RequestSnapshot");

	private final String rstTokenName;
	private final String jwtRefreshTokenName;

	public RequestSnapshotFactory(String rstTokenName, String jwtRefreshTokenName) {
		this.rstTokenName = Objects.requireNonNull(rstTokenName);
		this.jwtRefreshTokenName = Objects.requireNonNull(jwtRefreshTokenName);
	}

	/**
	 * Interrogate the incoming request for the presence of a
	 * {@link RequestSnapshot} instance. If one isn't present, take a request
	 * snapshot and cache it in the request for downstream use.
	 * Finally, return the generated or accessed instance.
	 *
	 * @param ctx the ratpack {@link Context} instance encapsulating the inbound
	 *						http request from which a request 'snapshot' is generated.
	 * @return Never-null {@link RequestSnapshot} instance.
	 */
	public RequestSnapshot getOrCreateRequestSnapshot(final Context ctx) {
		return ctx.getRequest().maybeGet(RequestSnapshot.class).orElseGet(() -> {
			final Request req = ctx.getRequest();
			final RequestSnapshot rs = new RequestSnapshot(
				req.getTimestamp(),

				nullif(req.getRemoteAddress().getHost()),
				nullif(req.getPath()),
				nullif(req.getMethod().getName()),

				nullif(req.getHeaders().get("Host")),
				nullif(req.getHeaders().get("Origin")),
				nullif(req.getHeaders().get("Referer")),
				nullif(req.getHeaders().get("Forwarded")),
				nullif(req.getHeaders().get("X-Forwarded-For")),
				nullif(req.getHeaders().get("X-Forwarded-Host")),
				nullif(req.getHeaders().get("X-Forwarded-Proto")),

				// nullif(req.oneCookie(jwtTokenName)),
				nullif(req.getHeaders().get("Authorization")),
				nullif(req.oneCookie(jwtRefreshTokenName)),

				nullif(req.oneCookie(rstTokenName)),
				nullif(req.getHeaders().get(rstTokenName)),
				req.maybeGet(RequestId.class).orElse(RequestId.of(UUID.randomUUID().toString())).toString()
			);
			ctx.getRequest().add(rs);
			log.info("Request snapshot taken: {}", rs);
			return rs;
		});
	}
}