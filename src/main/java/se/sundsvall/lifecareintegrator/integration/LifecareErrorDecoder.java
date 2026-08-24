package se.sundsvall.lifecareintegrator.integration;

import feign.Response;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;
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
	 * dept44 maps the body onto RFC 9457's {@code title}/{@code detail}. Lifecare does not answer in RFC 9457: its error
	 * JSON parses cleanly but into all-null fields, so the explanation is silently dropped and the message degrades to
	 * bare {@code {status=400 Bad Request}}. Logging the body verbatim is the only way to see what Lifecare actually
	 * said.
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
