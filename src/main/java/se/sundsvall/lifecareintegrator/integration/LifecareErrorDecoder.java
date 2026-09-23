package se.sundsvall.lifecareintegrator.integration;

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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static se.sundsvall.lifecareintegrator.util.LogSanitizer.redact;

/**
 * The dept44 {@link ProblemErrorDecoder} with the error body buffered first, and the failed request logged.
 *
 * <p>
 * dept44's {@code AbstractErrorDecoder} reads the response body twice, and an OkHttp body is not repeatable, so the
 * second read comes back empty and Lifecare's explanation is replaced by {@code title=Unknown error}. Buffering over a
 * {@code byte[]} makes both reads see the same content; this class can go once dept44 reads the body once.
 *
 * <p>
 * Lifecare does not answer in RFC 9457 either. Its errors are ASP.NET's {@code {"Message": "…"}}, sometimes a bare
 * string, and dept44 finds no {@code title}/{@code detail} in them — the explanation FamilyCare sends ("Saknar norm
 * för angiven hushållsstorlek") would be reduced to {@code {status=400 Bad Request}} or {@code title=Unknown error}.
 * {@link #extractErrorMessage(Response)} reads that shape and carries the message on as the problem's {@code detail},
 * redacted and capped, so the caller sees why Lifecare refused. A body that is RFC 9457 after all is left to dept44.
 *
 * <p>
 * The request line is logged with the status because the body alone does not say what was sent. It passes through
 * {@link se.sundsvall.lifecareintegrator.util.LogSanitizer} first — the URL carries the API key and a personnummer.
 */
public class LifecareErrorDecoder extends ProblemErrorDecoder {

	private static final Logger LOG = LoggerFactory.getLogger(LifecareErrorDecoder.class);

	/** Enough for any Lifecare error payload; a cap in case a gateway answers with an HTML page instead. */
	private static final int MAX_BODY_CHARACTERS = 2000;

	private static final String TRUNCATION_MARKER = "…(truncated)";

	private static final String NO_BODY = "<none>";

	private static final String EMPTY_BODY = "<empty>";

	private static final String UNBUFFERED_BODY = "<unbuffered>";

	/** A sentence or two of explanation is all a Lifecare message is; anything longer is not one. */
	private static final int MAX_MESSAGE_CHARACTERS = 500;

	private static final String ERROR_TEMPLATE = "%s error: %s";

	private static final String KEY_DETAIL = "detail";
	private static final String KEY_STATUS = "status";
	private static final String KEY_TITLE = "title";

	/** The RFC 9457 fields dept44 reads. A body carrying any of them is dept44's to decode, not ours. */
	private static final List<String> PROBLEM_FIELDS = List.of("title", "detail", "violations");

	private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

	private final String integration;
	private final List<Integer> expectedStatuses;

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
		this.integration = integrationName;
		this.expectedStatuses = List.copyOf(bypassResponseCodes);
	}

	@Override
	public Exception decode(final String methodKey, final Response response) {
		final var buffered = withRepeatableBody(response);

		logFailure(buffered);

		return super.decode(methodKey, buffered);
	}

	/**
	 * The error message, with Lifecare's own explanation as its {@code detail} when the body carries one, in the same
	 * {@code "<integration> error: {detail=…, status=…, title=…}"} form dept44 produces.
	 *
	 * <p>
	 * Only the message is carried on, never the body: it is Lifecare's explanation to the API caller, whereas the rest
	 * of the body is vendor-controlled and stays at DEBUG (see {@link #logFailure(Response)}). {@code ExceptionMessage}
	 * and {@code StackTrace} are left out on the same grounds — they describe Lifecare's internals, not our request.
	 */
	@Override
	public String extractErrorMessage(final Response response) throws IOException {
		final var body = bodyAsString(response);
		if (isProblem(body)) {
			return super.extractErrorMessage(response);
		}

		final var status = HttpStatus.valueOf(response.status());
		final var errorInfo = new TreeMap<String, Object>();
		errorInfo.put(KEY_STATUS, status.value() + " " + status.getReasonPhrase());
		errorInfo.put(KEY_TITLE, status.getReasonPhrase());
		lifecareMessage(body)
			.map(LogSanitizer::redact)
			.map(LifecareErrorDecoder::cappedMessage)
			.ifPresent(message -> errorInfo.put(KEY_DETAIL, message));

		return ERROR_TEMPLATE.formatted(integration, errorInfo);
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

	private static String cappedMessage(final String message) {
		if (message.length() <= MAX_MESSAGE_CHARACTERS) {
			return message;
		}
		return message.substring(0, MAX_MESSAGE_CHARACTERS) + TRUNCATION_MARKER;
	}

	/**
	 * Logs the failed request, and the body only at DEBUG.
	 *
	 * <p>
	 * The request line is safe to log at WARN — every parameter in it is ours, and the two sensitive ones are redacted.
	 * The body is not: it is vendor-controlled and may carry personal data beyond what redaction anticipates, so it is
	 * written only when someone turns diagnostics on. A bypassed status is an outcome the caller handles rather than a
	 * failure, so it does not warn at all.
	 */
	private void logFailure(final Response response) {
		final var request = "%s %s".formatted(response.request().httpMethod(), redact(response.request().url()));

		if (expectedStatuses.contains(response.status())) {
			LOG.debug("{} responded {} to {}", integration, response.status(), request);
			return;
		}

		LOG.warn("{} responded {} to {}", integration, response.status(), request);
		// Deferred, not just unwritten: building the snippet reads the whole body and runs it through several regex
		// passes, which is exactly the cost moving it to DEBUG was meant to stop paying on every error in production.
		LOG.atDebug()
			.addArgument(integration)
			.addArgument(() -> bodySnippet(response))
			.log("{} response body was: {}");
	}

	private static String capped(final String content) {
		if (content.length() <= MAX_BODY_CHARACTERS) {
			return content;
		}
		return content.substring(0, MAX_BODY_CHARACTERS) + TRUNCATION_MARKER;
	}

	/**
	 * The error body as text, redacted and capped.
	 *
	 * <p>
	 * Only Lifecare's message reaches the exception (see {@link #extractErrorMessage(Response)}); the body verbatim is
	 * what to turn on when the message is not enough, or when Lifecare answered in a shape the decoder does not read.
	 *
	 * <p>
	 * Only a buffered body is read. Reading an unbuffered one here would consume the single available pass and leave
	 * nothing for the decoder that follows.
	 */
	static String bodySnippet(final Response response) {
		final var body = response.body();
		if (body == null) {
			return NO_BODY;
		}
		if (!body.isRepeatable()) {
			return UNBUFFERED_BODY;
		}

		try {
			final var content = new String(body.asInputStream().readAllBytes(), UTF_8);
			if (content.isBlank()) {
				return EMPTY_BODY;
			}
			return capped(redact(content));
		} catch (final IOException e) {
			return "<unreadable: %s>".formatted(redact(e.getMessage()));
		}
	}

	/**
	 * The same response, with its body replaced by an equivalent repeatable one. Returns the response untouched when
	 * there is nothing to buffer, or when reading it fails — a failed read must not mask the error being decoded.
	 */
	static Response withRepeatableBody(final Response response) {
		final var body = response.body();
		if (body == null || body.isRepeatable()) {
			return response;
		}

		try (body) {
			return response.toBuilder()
				.body(body.asInputStream().readAllBytes())
				.build();
		} catch (final IOException e) {
			LOG.warn("Could not buffer the error response body: {}", redact(e.getMessage()));
			return response;
		}
	}
}
