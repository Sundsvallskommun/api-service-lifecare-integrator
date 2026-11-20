package se.sundsvall.lifecareintegrator.integration.professionalweb.session;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.problem.Problem;

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
		builder.method(method, toRequestBody(method, body));

		try (var response = client.newCall(builder.build()).execute()) {
			final var responseHeaders = HttpHeaders.of(response.headers().toMultimap(), (name, value) -> true);
			return new ProfessionalWebResponse(response.code(), responseHeaders, response.body().bytes(), uri);
		} catch (final IOException e) {
			// The exception type is the whole diagnosis (a refused connection says something else than a timeout), and it
			// holds nothing about the person being looked up.
			throw Problem.valueOf(BAD_GATEWAY, "Lifecare could not be reached (" + e.getClass().getSimpleName() + ")");
		}
	}

	private static RequestBody toRequestBody(final String method, final byte[] body) {
		if (body != null) {
			return RequestBody.create(body);
		}
		// OkHttp refuses a POST or PUT without a body
		if ("POST".equals(method) || "PUT".equals(method)) {
			return RequestBody.create(new byte[0]);
		}
		return null;
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
				body = encodeForm(remainingFormFields);
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

	/**
	 * Whether a response means the session is gone and must be established again.
	 *
	 * <p>
	 * Lifecare says it three ways depending on the endpoint: the 360 signal, a redirect back to the identity portal, and
	 * the login page delivered as HTML where JSON was expected. A 401 joins them because an expired ASP.NET session
	 * surfaces that way too. A 403 deliberately does not: that is Lifecare saying the account may not see this, and a
	 * new session would only hide a real permission problem behind a second failure.
	 * </p>
	 *
	 * <p>
	 * One HTML answer is not the login page: the parameter query Lifecare puts in front of a print whose template asks a
	 * question (see {@code isParameterQuery}). It is a form the caller answers, so it is handed back like any other
	 * answer.
	 * </p>
	 *
	 * @param  response the response
	 * @return          true when a new session is needed
	 */
	public static boolean needsSession(final ProfessionalWebResponse response) {
		if (response.status() == SESSION_REQUIRED_STATUS || response.status() == 401) {
			return true;
		}
		if (response.location().filter(location -> location.contains(IDENTITY_PORTAL_PATH)).isPresent()) {
			return true;
		}
		if (!response.contentType().contains("text/html")) {
			return false;
		}
		return !isParameterQuery(response);
	}

	/**
	 * Whether an HTML answer is Lifecare's parameter query page: the "Parameterfrågor" form a print template shows when
	 * it asks something before it renders. Recognised by the form's id and by the hidden X-LEGACY-TOKEN field that a
	 * browser posts back with it, two things the sign-in pages do not have.
	 */
	private static boolean isParameterQuery(final ProfessionalWebResponse response) {
		final var html = response.bodyAsString();
		return html.contains("id=\"myForm\"") && html.contains("name=\"X-LEGACY-TOKEN\"");
	}

	static byte[] encodeForm(final Map<String, String> fields) {
		return encodeForm(List.copyOf(fields.entrySet()));
	}

	/**
	 * Encodes form fields in the order given, so a name may occur more than once as it does in a browser form.
	 *
	 * @param  fields the name and value of each field
	 * @return        the application/x-www-form-urlencoded body
	 */
	public static byte[] encodeForm(final List<? extends Map.Entry<String, String>> fields) {
		return fields.stream()
			.map(field -> URLEncoder.encode(field.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(field.getValue(), StandardCharsets.UTF_8))
			.collect(Collectors.joining("&"))
			.getBytes(StandardCharsets.UTF_8);
	}
}
