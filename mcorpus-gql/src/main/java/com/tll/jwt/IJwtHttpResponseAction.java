package com.tll.jwt;

import java.time.Duration;

/**
 * Contract for setting <b>http response state</b> for JWT handling
 * in the web layer.
 *
 * @author jpk
 */
public interface IJwtHttpResponseAction {

	/**
	 * Set JWT http clientside state in an outgoing http response.
	 * <p>
	 * Call on JWT user login.
	 * <p>
	 * This method is expected to be called upon a successful JWT user login by
	 * username and password and sets a freshly minted JWT clientside.
	 *
	 * @param jwt the JWT string to set on the client browser
	 * @param refreshToken the jwt refresh token
	 * @param refreshTokenTimeToLive the amount of time the refresh token shall be considered valid
	 */
	void setJwtClientside(String jwt, String refreshToken, Duration refreshTokenTimeToLive);

	/**
	 * Clears all JWT related http state clientside.
	 * <p>
	 * Call on JWT user logout.
	 * <p>
	 * This method is responsible for clearing out all JWT related state clientside
	 * such that the JWT user must [re] login in order to obtain a freshly minted JWT.
	 */
	void expireJwtClientside();
}