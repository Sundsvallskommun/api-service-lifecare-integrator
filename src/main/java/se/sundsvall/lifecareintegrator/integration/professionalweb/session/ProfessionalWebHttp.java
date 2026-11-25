package se.sundsvall.lifecareintegrator.integration.professionalweb.session;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebForms;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

/**
 * The raw HTTP layer that sign-in, the module bootstrap and the exchange pass-through go through. The api2 Feign
 * clients use the same OkHttp setup through their own configuration.
 *
 * <p>
 * OkHttp used directly rather than through Feign: sign-in walks HTML forms and redirects across hosts whose addresses
 * come from the pages themselves, collecting cookies on the way. None of that fits a declarative client, so statuses
 * are read here rather than thrown, and redirects are never followed automatically.
 * </p>
 *
 * <p>
 * Trust comes from the dept44 truststore, where Lifecare's internal CA is installed. No body is ever logged here:
 * everything that crosses this connection may hold personal data or credentials.
 * </p>
 */
@Component
public class ProfessionalWebHttp {

	/**
	 * Lifecare is an application that has only ever been spoken to by browsers. Presenting the browser the flow was
	 * captured from removes a variable that would otherwise have to be ruled out later.
	 */
	public static final Map<String, String> BROWSER_HEADERS = Map.of(
		"User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/153.0.0.0 Safari/537.36",
		"Accept-Language", "sv-SE,sv;q=0.9,en-US;q=0.8,en;q=0.7");

	/**
	 * What makes api2 answer an automated caller the way it answers Lifecare's own web client. Without
	 * X-Requested-With a dead session is answered with the login page as HTML; ajax-no-cross-domain-redirect turns that
	 * into the bare 360 a caller can act on. For data calls only: sent while following the bootstrap chain, they would
	 * make Lifecare signal instead of redirect and the session would never be established.
	 */
	public static final Map<String, String> AJAX_HEADERS = Map.of(
		"Accept", "application/json, text/javascript, */*; q=0.01",
		"X-Requested-With", "XMLHttpRequest",
		"ajax-no-cross-domain-redirect", "true");

	public static final Map<String, String> NAVIGATION_HEADERS = Map.of(
		"Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

	/** Lifecare's own answer for this call needs a session. Not an HTTP status, a number its client agreed on. */
	public static final int SESSION_REQUIRED_STATUS = 360;

	public static final String IDENTITY_PORTAL_PATH = "IdentityPortalWeb";
	private static final int MAX_REDIRECT_HOPS = 10;

	private final OkHttpClient client;

	public ProfessionalWebHttp(@Qualifier("professionalWebHttpClient") final OkHttpClient client) {
		this.client = client;
	}

	/**
	 * Sends one request and returns whatever came back, whatever the status.
	 *
	 * @param  method  the HTTP method
	 * @param  uri     the absolute URI
	 * @param  headers the request headers
	 * @param  body    the body, or null for none
	 * @return         the response
	 */
	public ProfessionalWebResponse send(final String method, final URI uri, final Map<String, String> headers, final byte[] body) {
		final var builder = new Request.Builder().url(uri.toString());
		headers.forEach(builder::header);
		builder.method(method, toRequestBody(body));

		try (var response = client.newCall(builder.build()).execute()) {
			final var responseHeaders = HttpHeaders.of(response.headers().toMultimap(), (name, value) -> true);
			return new ProfessionalWebResponse(response.code(), responseHeaders, response.body().bytes(), uri);
		} catch (final IOException e) {
			// The exception type is the whole diagnosis (a refused connection says something else than a timeout), and it
			// holds nothing about the person being looked up.
			throw Problem.valueOf(BAD_GATEWAY, "Lifecare could not be reached (" + e.getClass().getSimpleName() + ")");
		}
	}

	private static RequestBody toRequestBody(final byte[] body) {
		return Optional.ofNullable(body)
			.map(bytes -> RequestBody.create(bytes))
			.orElse(null);
	}

	/**
	 * Walks a redirect chain by hand, carrying the cookies from each hop into the next.
	 *
	 * <p>
	 * Followed manually because the cookies are the point: the bootstrap only works if what the identity portal sets on
	 * one hop is presented on the next. Passing form fields submits a form as the first hop; everything after it is a
	 * GET, which is what a browser does with a 302 answering a POST.
	 * </p>
	 *
	 * @param  startUrl   where the chain starts
	 * @param  cookies    the session cookies, updated along the way
	 * @param  formFields form fields to post as the first hop, or null for a plain navigation
	 * @return            the first response in the chain that is not a redirect
	 */
	public ProfessionalWebResponse followRedirects(final String startUrl, final ProfessionalWebCookies cookies, final Map<String, String> formFields) {
		var uri = URI.create(startUrl);
		var remainingFormFields = formFields;

		for (var hop = 0; hop < MAX_REDIRECT_HOPS; hop++) {
			final var headers = new LinkedHashMap<String, String>();
			headers.putAll(BROWSER_HEADERS);
			headers.putAll(NAVIGATION_HEADERS);
			final var cookieHeader = cookies.toHeader();
			if (!cookieHeader.isEmpty()) {
				headers.put("Cookie", cookieHeader);
			}
			var method = "GET";
			byte[] body = null;
			if (remainingFormFields != null) {
				method = "POST";
				headers.put("Content-Type", "application/x-www-form-urlencoded");
				body = ProfessionalWebForms.encode(List.copyOf(remainingFormFields.entrySet()));
			}

			final var response = send(method, uri, headers, body);
			remainingFormFields = null;
			cookies.absorbSetCookie(response.setCookies());

			final var location = response.location();
			if (location.isEmpty()) {
				return response;
			}
			uri = uri.resolve(location.get());
		}

		throw Problem.valueOf(BAD_GATEWAY, "Lifecare kept redirecting while a session was being established");
	}
}
