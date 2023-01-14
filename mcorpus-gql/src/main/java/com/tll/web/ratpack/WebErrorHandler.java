package com.tll.web.ratpack;

import static com.tll.core.Util.clean;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.isNullOrEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.core.error.ClientErrorHandler;
import ratpack.core.error.ServerErrorHandler;
import ratpack.core.handling.Context;

/**
 * The global error handler for the mcorpus web app.
 *
 * @author jkirton
 */
public class WebErrorHandler implements ServerErrorHandler, ClientErrorHandler {

	private final Logger log = LoggerFactory.getLogger(WebErrorHandler.class);

	/**
	 * The client error handler.
	 */
	@Override
	public void error(Context ctx, int statusCode) throws Exception {
		switch(statusCode) {
		case 205: // reset content
			sendError(ctx, statusCode, "Reset Content (205)");
			break;
		case 400: // bad request
			sendError(ctx, statusCode, "Bad Request (400)");
			break;
		case 401: // unauthorized
			sendError(ctx, statusCode, "Unauthorized (401)");
			break;
		case 403: // forbidden
			sendError(ctx, statusCode, "Forbidden (403)");
			break;
		case 404: // not found
			sendError(ctx, statusCode, "Not Found (404)");
			break;
		default: // default client error response
			sendError(ctx, statusCode, "Bad Client");
			break;
		}
		log.error("Client error {} response sent for request: {} - {}",
			statusCode,
			ctx.getRequest().getPath(),
			ctx.get(RequestSnapshotFactory.class).getOrCreateRequestSnapshot(ctx)
		);
	}

	/**
	 * The server/exception error handler.
	 * <p>
	 * Uncaught exceptions that bubble up in the handler chain when processing a
	 * received request will be handled by this method.
	 */
	@Override
	public void error(Context ctx, Throwable error) throws Exception {
		sendError(ctx, 500, "Server error (500)");
		final String emsg = isNull(error) ? "UNKNOWN" :
			isNullOrEmpty(error.getMessage()) ? "UNKNOWN" : error.getMessage();
		log.error("Server error '{}' for request: {} - {}",
				emsg,
				ctx.getRequest().getPath(),
				ctx.get(RequestSnapshotFactory.class).getOrCreateRequestSnapshot(ctx)
		);
		if(ctx.getServerConfig().isDevelopment()) {
			log.error("StackTrace:\n\n{}\n", error);
		}
	}

	private void sendError(Context ctx, int httpStatusCode, String errorMsg) {
		final boolean sendAsJson = clean(ctx.getRequest().getPath()).startsWith("graphql");
		if(sendAsJson) {
			// json
			String jerror = String.format("{ \"httpStatusCode\": %d, \"error\": \"%s\" }", httpStatusCode, errorMsg);
			ctx.getResponse().status(httpStatusCode).send("application/json", jerror);
		} else {
			// text
			ctx.getResponse().status(httpStatusCode).send(errorMsg);
		}
	}

}
