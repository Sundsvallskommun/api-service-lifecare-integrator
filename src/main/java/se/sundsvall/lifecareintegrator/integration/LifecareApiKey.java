package se.sundsvall.lifecareintegrator.integration;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Renders the Lifecare API key for the query string, the one and only place it is sent.
 *
 * <p>
 * Lifecare keys are base64, so they contain {@code +}, {@code /} and {@code =}, and they are handed out in both a
 * percent-encoded form ({@code …U%2beJwc…}, which is what a Postman collection stores) and a decoded one
 * ({@code …U+eJwc…}). Getting that wrong is silent: a raw {@code +} in a query string is decoded by the server as a
 * <em>space</em>, and Feign passes it through untouched (verified 2026-08-21), so the key arrives corrupted with
 * nothing to say so. Either configured form is accepted here and leaves as the same correctly encoded value.
 *
 * <p>
 * The key must not also travel in an {@code X-API-Key} or {@code Authorization} header. EC compares every carrier it
 * finds and rejects the request outright when they disagree — and they cannot agree, because the query copy is
 * URL-decoded on arrival and the header copy is not:
 * {@code 400 "Invalid argument: key. Ambiguous API key, different keys either in the HTTP header (Authorization) or in
 * the HTTP header (X-API-Key) or in the URL query string (key)."} (verified 2026-08-24). One carrier, no ambiguity.
 */
public final class LifecareApiKey {

	/**
	 * A percent-encoded triplet — how we tell an already-encoded key from a decoded one. Base64 never contains {@code %}.
	 */
	private static final Pattern PERCENT_ENCODED = Pattern.compile("%[0-9A-Fa-f]{2}");

	private LifecareApiKey() {}

	/**
	 * The key percent-encoded for carriage in a query string, so the server decodes it back to the real secret.
	 *
	 * @param  key the configured key, in either the encoded or the decoded form
	 * @return     the key safe to place in a query string
	 */
	public static String forQuery(final String key) {
		if (key == null) {
			return null;
		}
		// URLEncoder is form-encoding: it renders a space as '+', which is exactly the confusion being avoided here.
		return URLEncoder.encode(decoded(key), UTF_8).replace("+", "%20");
	}

	/** The real secret, whichever form was configured. */
	private static String decoded(final String key) {
		if (!PERCENT_ENCODED.matcher(key).find()) {
			return key;
		}
		// Guard the '+' first: URLDecoder would read it as a space.
		return URLDecoder.decode(key.replace("+", "%2B"), UTF_8);
	}
}
