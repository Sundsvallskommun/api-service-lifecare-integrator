package se.sundsvall.lifecareintegrator.integration.professionalweb;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * One api2 call through the integration account's session, with Lifecare's own session escalation.
 *
 * <p>
 * When an answer says a session is needed: first bootstrap the module and ask again, then sign in afresh and ask
 * again, then give up with 502. Retrying a write is safe here, because a session refusal comes before Lifecare acts on
 * the request. Every other answer, success or refusal, is handed back as it is; interpreting it is the caller's job.
 * </p>
 *
 * <p>
 * Nothing that crosses this class is logged beyond the path and the status: answers carry personnummer, and request
 * bodies carry whatever the caseworker wrote.
 * </p>
 */
@Component
public class ProfessionalWebExchange {

	/** The one Lifecare module the pass-through talks to. */
	public static final String MODULE = "WESE.FC.ProfessionalWeb";

	private static final Logger LOG = LoggerFactory.getLogger(ProfessionalWebExchange.class);

	private final ProfessionalWebProperties properties;
	private final ProfessionalWebSession session;
	private final ProfessionalWebHttp http;

	public ProfessionalWebExchange(final ProfessionalWebProperties properties, final ProfessionalWebSession session, final ProfessionalWebHttp http) {
		this.properties = properties;
		this.session = session;
		this.http = http;
	}

	/**
	 * Sends one call and returns Lifecare's final answer, whatever its status.
	 *
	 * @param  method the HTTP method
	 * @param  path   the path below the module, e.g. {@code api2/Calculation/GetCalculation}
	 * @param  params query parameters, in order
	 * @param  body   the JSON body, or null for none
	 * @return        the answer
	 */
	public ProfessionalWebResponse exchange(final String method, final String path, final Map<String, String> params, final byte[] body) {
		return escalating(method, path, () -> send(method, path, params, body));
	}

	/**
	 * Posts a form the way a browser does and returns Lifecare's final answer, whatever its status. For the pages that
	 * answer a GET with a form to fill in, such as the parameter query in front of a decision print: the caller reads the
	 * form and posts it back to the same path and query.
	 *
	 * <p>
	 * Sent with the navigation headers a browser uses for a form (no ajax headers) and with the request's own address as
	 * Referer. The fields go out exactly as given, a token field included: it is Lifecare's own session token as the
	 * caller read it off the page, so it is stale if the session was replaced in between.
	 * </p>
	 *
	 * @param  path   the path below the module, e.g. {@code RenderPdf/PrintDecision}
	 * @param  params query parameters, in order
	 * @param  fields the name and value of each form field, in document order; a name may repeat
	 * @return        the answer
	 */
	public ProfessionalWebResponse exchangeForm(final String path, final Map<String, String> params, final List<? extends Map.Entry<String, String>> fields) {
		return escalating("POST", path, () -> sendForm(path, params, fields));
	}

	private ProfessionalWebResponse escalating(final String method, final String path, final Supplier<ProfessionalWebResponse> send) {
		var response = send.get();

		if (ProfessionalWebHttp.needsSession(response)) {
			LOG.atWarn().addArgument(MODULE).addArgument(response::describe).log("Lifecare wants a session for {} ({}) - bootstrapping it");
			session.bootstrapModule(MODULE);
			response = send.get();
		}
		if (ProfessionalWebHttp.needsSession(response)) {
			LOG.atWarn().addArgument(path).addArgument(response::describe).log("Lifecare still refuses {} ({}) - signing in again");
			session.reset();
			response = send.get();
		}
		if (ProfessionalWebHttp.needsSession(response)) {
			throw Problem.valueOf(BAD_GATEWAY, "Lifecare would not accept a freshly established session (" + response.describe() + ")");
		}
		if (!response.isSuccess()) {
			LOG.atInfo().addArgument(method).addArgument(path).addArgument(response::describe).log("Lifecare answered {} {} with {}");
		}
		return response;
	}

	private ProfessionalWebResponse send(final String method, final String path, final Map<String, String> params, final byte[] body) {
		final var headers = new LinkedHashMap<String, String>();
		headers.putAll(ProfessionalWebHttp.BROWSER_HEADERS);
		headers.putAll(ProfessionalWebHttp.AJAX_HEADERS);
		headers.put("Origin", origin());
		headers.put("Referer", properties.baseUrl() + "/WE.Flow.Html/");
		byte[] bytes = null;
		if (!"GET".equals(method)) {
			headers.put("Content-Type", "application/json; charset=UTF-8");
			bytes = body;
			if (bytes == null) {
				bytes = new byte[0];
			}
		}
		headers.putAll(session.prepare());

		final var response = http.send(method, uri(path, params), headers, bytes);
		session.absorb(response);
		return response;
	}

	private ProfessionalWebResponse sendForm(final String path, final Map<String, String> params, final List<? extends Map.Entry<String, String>> fields) {
		final var uri = uri(path, params);
		final var headers = new LinkedHashMap<String, String>();
		headers.putAll(ProfessionalWebHttp.BROWSER_HEADERS);
		headers.putAll(ProfessionalWebHttp.NAVIGATION_HEADERS);
		headers.put("Origin", origin());
		headers.put("Referer", uri.toString());
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.putAll(session.prepare());

		final var response = http.send("POST", uri, headers, ProfessionalWebHttp.encodeForm(fields));
		session.absorb(response);
		return response;
	}

	private URI uri(final String path, final Map<String, String> params) {
		final var base = properties.baseUrl() + "/" + MODULE + "/" + path.replaceAll("^/+", "");
		requireNoEscapeFromModule(base, path);
		if (params == null || params.isEmpty()) {
			return URI.create(base);
		}
		final var query = params.entrySet().stream()
			.map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
			.collect(Collectors.joining("&"));
		return URI.create(base + "?" + query);
	}

	/**
	 * Defense in depth alongside {@code ProfessionalWebExchangeRequest}'s own {@code @Pattern}: even if a caller reaches
	 * this class with a path the resource layer did not validate, a {@code ..} segment must never be allowed to walk the
	 * request out of {@link #MODULE} onto another Lifecare module such as the identity portal.
	 */
	private void requireNoEscapeFromModule(final String base, final String path) {
		final var normalized = URI.create(base).normalize().getPath();
		final var expectedPrefix = "/" + MODULE + "/";
		if (!normalized.startsWith(expectedPrefix)) {
			throw Problem.valueOf(BAD_REQUEST, "Refusing a ProfessionalWeb path that would leave " + MODULE + ": " + path);
		}
	}

	private String origin() {
		final var uri = URI.create(properties.baseUrl());
		return uri.getScheme() + "://" + uri.getRawAuthority();
	}

	private static String encode(final String value) {
		if (value == null) {
			return "";
		}
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
