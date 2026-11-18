package se.sundsvall.lifecareintegrator.integration;

import feign.Request;
import feign.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;
import se.sundsvall.lifecareintegrator.util.LogSanitizer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static se.sundsvall.lifecareintegrator.util.LogSanitizer.redact;

/**
 * dept44's {@link ProblemErrorDecoder} for Lifecare, whose errors are ASP.NET {@code {"Message": "…"}} rather than
 * RFC 9457: the message is carried on as the problem's {@code detail}, and the failed request is logged.
 */
public class LifecareErrorDecoder extends ProblemErrorDecoder {

	private static final Logger LOG = LoggerFactory.getLogger(LifecareErrorDecoder.class);

	/** Enough for any Lifecare error payload; a cap in case a gateway answers with an HTML page instead. */
	private static final int MAX_BODY_CHARACTERS = 2000;

	private static final String TRUNCATION_MARKER = "…(truncated)";

	private static final String UNKNOWN_REQUEST = "<unknown request>";

	/** A sentence or two of explanation is all a Lifecare message is; anything longer is not one. */
	private static final int MAX_MESSAGE_CHARACTERS = 500;

	private static final String ERROR_TEMPLATE = "%s error: %s";

	private static final String KEY_DETAIL = "detail";
	private static final String KEY_STATUS = "status";
	private static final String KEY_TITLE = "title";
	private static final String UNKNOWN_REASON_PHRASE = "Unknown Status";

	/** The RFC 9457 fields dept44 reads. A body carrying any of them is dept44's to decode, not ours. */
	private static final List<String> PROBLEM_FIELDS = List.of(KEY_TITLE, KEY_DETAIL, "violations");

	private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

	public LifecareErrorDecoder(final String integrationName) {
		this(integrationName, List.of());
	}

	/**
	 * @param bypassResponseCodes statuses propagated as themselves rather than as BAD_GATEWAY. They are expected
	 *                            outcomes the caller handles — an FC document that does not exist, say — so they are
	 *                            logged at DEBUG rather than WARN.
	 */
	public LifecareErrorDecoder(final String integrationName, final List<Integer> bypassResponseCodes) {
		super(integrationName, bypassResponseCodes);
	}

	@Override
	public Exception decode(final String methodKey, final Response response) {
		logFailure(response);

		return super.decode(methodKey, response);
	}

	/**
	 * The error message, with Lifecare's own explanation as its {@code detail} when the body carries one, in the same
	 * {@code "<integration> error: {detail=…, status=…, title=…}"} form dept44 produces.
	 *
	 * <p>
	 * Only the message is carried on, never the body: it is Lifecare's explanation to the API caller, whereas the rest
	 * of the body is vendor-controlled and stays at DEBUG (see {@link #logBody(int, String)}). {@code ExceptionMessage}
	 * and {@code StackTrace} are left out on the same grounds — they describe Lifecare's internals, not our request.
	 */
	@Override
	public String extractErrorMessage(final Response response) throws IOException {
		final var status = response.status();
		final var body = bodyAsString(response);
		logBody(status, body);
		if (isProblem(body)) {
			return super.extractErrorMessage(response);
		}

		final var reasonPhrase = getReasonPhrase(status);
		final var errorInfo = new TreeMap<String, Object>();
		errorInfo.put(KEY_STATUS, status + " " + reasonPhrase);
		errorInfo.put(KEY_TITLE, reasonPhrase);
		lifecareMessage(body)
			.map(LogSanitizer::redact)
			.map(message -> cap(message, MAX_MESSAGE_CHARACTERS))
			.ifPresent(message -> errorInfo.put(KEY_DETAIL, message));

		return ERROR_TEMPLATE.formatted(integrationName, errorInfo);
	}

	private static String getReasonPhrase(final int status) {
		return ofNullable(HttpStatus.resolve(status))
			.map(HttpStatus::getReasonPhrase)
			.orElse(UNKNOWN_REASON_PHRASE);
	}

	private static boolean isProblem(final String body) {
		return parse(body)
			.filter(JsonNode::isObject)
			.filter(node -> PROBLEM_FIELDS.stream().anyMatch(node::has))
			.isPresent();
	}

	/**
	 * The explanation in a Lifecare error body: {@code Message} (with {@code MessageDetail} and any {@code ModelState}
	 * validation errors appended), a bare JSON string, or plain text. An HTML page — a gateway's, not Lifecare's — has no
	 * message worth carrying, and neither has JSON of any other shape.
	 */
	static Optional<String> lifecareMessage(final String body) {
		final var json = parse(body);
		if (json.isEmpty()) {
			return plainText(body);
		}

		final var node = json.get();
		if (node.isString()) {
			return nonBlank(node.stringValue());
		}
		if (!node.isObject()) {
			return empty();
		}

		return textField(node, "Message").map(message -> textField(node, "MessageDetail")
			.map(detail -> "%s (%s)".formatted(message, detail))
			.orElse(message))
			.map(message -> modelState(node)
				.map(errors -> "%s: %s".formatted(message, errors))
				.orElse(message));
	}

	/** ASP.NET's validation errors, as {@code field: error} pairs — the same shape dept44 gives constraint violations. */
	private static Optional<String> modelState(final JsonNode node) {
		return ofNullable(node.get("ModelState"))
			.filter(JsonNode::isObject)
			.map(state -> state.properties().stream()
				.flatMap(field -> field.getValue().valueStream()
					.filter(JsonNode::isString)
					.map(error -> "%s: %s".formatted(field.getKey(), error.stringValue())))
				.collect(Collectors.joining(", ")))
			.flatMap(LifecareErrorDecoder::nonBlank);
	}

	/** A text field, matched case-insensitively — FC and EC do not agree on casing. */
	private static Optional<String> textField(final JsonNode node, final String name) {
		return node.properties().stream()
			.filter(field -> field.getKey().equalsIgnoreCase(name))
			.map(Map.Entry::getValue)
			.filter(JsonNode::isString)
			.map(JsonNode::stringValue)
			.flatMap(value -> nonBlank(value).stream())
			.findFirst();
	}

	private static Optional<String> plainText(final String body) {
		return nonBlank(body)
			.map(String::strip)
			.filter(text -> !text.startsWith("<"));
	}

	private static Optional<String> nonBlank(final String text) {
		return ofNullable(text).filter(value -> !value.isBlank());
	}

	private static Optional<JsonNode> parse(final String body) {
		try {
			return ofNullable(JSON_MAPPER.readTree(body));
		} catch (final JacksonException _) {
			return empty();
		}
	}

	private static String cap(final String text, final int maxCharacters) {
		if (text.length() <= maxCharacters) {
			return text;
		}
		return text.substring(0, maxCharacters) + TRUNCATION_MARKER;
	}

	/**
	 * Logs the failed request. The request line is safe to log at WARN — every parameter in it is ours, and the two
	 * sensitive ones are redacted. A bypassed status is an outcome the caller handles rather than a failure, so it does
	 * not warn at all.
	 */
	private void logFailure(final Response response) {
		final var request = describeRequest(response.request());

		if (isBypassed(response.status())) {
			LOG.debug("{} responded {} to {}", integrationName, response.status(), request);
			return;
		}

		LOG.warn("{} responded {} to {}", integrationName, response.status(), request);
	}

	/**
	 * Logs the body at DEBUG only: it is vendor-controlled and may carry personal data beyond what redaction anticipates.
	 * Deferred, since redacting runs several regex passes over the body.
	 */
	private void logBody(final int status, final String body) {
		if (isBypassed(status)) {
			return;
		}

		LOG.atDebug()
			.addArgument(integrationName)
			.addArgument(() -> redactAndCap(body))
			.log("{} response body was: {}");
	}

	private boolean isBypassed(final int status) {
		return bypassResponseCodes.contains(status);
	}

	/**
	 * The request line to log, without ever dereferencing a null {@link Response#request()}. Feign's own builder
	 * enforces a non-null request on every {@code Response} it constructs, but nothing here should rely on that
	 * invariant holding for every {@code Response} implementation that could ever reach this decoder — a null request
	 * must degrade the log line, not throw out of {@link #logFailure(Response)}.
	 */
	static String describeRequest(final Request request) {
		if (request == null) {
			return UNKNOWN_REQUEST;
		}
		return "%s %s".formatted(request.httpMethod(), redact(request.url()));
	}

	/** Redaction runs before capping, so the cap cannot cut a personnummer into a fragment redaction no longer finds. */
	static String redactAndCap(final String body) {
		return cap(redact(body), MAX_BODY_CHARACTERS);
	}
}
