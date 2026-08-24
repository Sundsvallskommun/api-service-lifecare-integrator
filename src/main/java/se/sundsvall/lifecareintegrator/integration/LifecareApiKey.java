package se.sundsvall.lifecareintegrator.integration;

import feign.RequestTemplate;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The Lifecare query-string authentication scheme: {@code domain} and {@code key}, and nothing else.
 *
 * <p>
 * The key must not travel in a header as well — Lifecare compares every carrier it finds and rejects the request when
 * they disagree, which they must, since a query value is URL-decoded on arrival and a header value is not.
 *
 * <p>
 * Keys are base64 and are handed out in both a percent-encoded and a decoded form. Either is accepted here and leaves
 * as the same encoded value: a raw {@code +} in a query string is decoded server-side as a <em>space</em>, and Feign
 * does not encode it, so the decoded form would otherwise arrive corrupted with nothing to say so.
 */
public final class LifecareApiKey {

	private static final String DOMAIN_PARAMETER = "domain";

	private static final String KEY_PARAMETER = "key";

	/**
	 * A percent-encoded triplet — how an already-encoded key is told from a decoded one. Base64 never contains {@code %}.
	 */
	private static final Pattern PERCENT_ENCODED = Pattern.compile("%[0-9A-Fa-f]{2}");

	private LifecareApiKey() {}

	/**
	 * Add the {@code domain} and {@code key} query parameters to a request, unless they are already there.
	 *
	 * <p>
	 * Feign re-applies request interceptors to the <em>same</em> template on every retry attempt and
	 * {@link RequestTemplate#query(String, String...)} appends, so without the guard a retried request would carry both
	 * parameters once per attempt.
	 *
	 * @param template the request being built
	 * @param domain   the Lifecare tenant id
	 * @param key      the API key, in either the encoded or the decoded form
	 */
	public static void applyTo(final RequestTemplate template, final String domain, final String key) {
		queryOnce(template, DOMAIN_PARAMETER, domain);
		queryOnce(template, KEY_PARAMETER, forQuery(key));
	}

	/**
	 * The key percent-encoded for carriage in a query string, so the server decodes it back to the real secret.
	 *
	 * @param  key the configured key, in either form
	 * @return     the key safe to place in a query string
	 */
	static String forQuery(final String key) {
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

	private static void queryOnce(final RequestTemplate template, final String name, final String value) {
		if (!template.queries().containsKey(name)) {
			template.query(name, value);
		}
	}
}
