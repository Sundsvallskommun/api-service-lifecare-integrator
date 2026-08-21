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
 * The dept44 {@link ProblemErrorDecoder}, with the error response body buffered before it is handed over, and the
 * failed request logged.
 *
 * <p>
 * dept44's {@code AbstractErrorDecoder.extractMessage} reads the body twice — once to test it for blankness, then again
 * in {@code extractErrorMessage}. An OkHttp response body is not repeatable, so the second read returns nothing:
 * Jackson fails with {@code No content to map due to end-of-input}, a full stack trace is logged as "Something went
 * wrong when extracting error-message", and the real error is replaced with {@code title=Unknown error}. Every
 * explanation Lifecare sends with a 4xx is lost exactly when it is needed. Rebuilding the response over a
 * {@code byte[]}
 * body makes it repeatable, so both reads see the same content. Remove this once dept44 reads the body once.
 *
 * <p>
 * The request line is logged alongside the status because the response body alone does not say what we sent, and the
 * shape of the {@code q} filter is what most EC failures turn on. It goes through {@link
 * se.sundsvall.lifecareintegrator.util.LogSanitizer} first — the URL carries the API key and a personnummer.
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

	public LifecareErrorDecoder(final String integrationName) {
		super(integrationName);
		this.integration = integrationName;
	}

	public LifecareErrorDecoder(final String integrationName, final List<Integer> bypassResponseCodes) {
		super(integrationName, bypassResponseCodes);
		this.integration = integrationName;
	}

	@Override
	public Exception decode(final String methodKey, final Response response) {
		final var buffered = withRepeatableBody(response);

		LOG.warn("{} responded {} to {} {} with body: {}", integration, buffered.status(),
			buffered.request().httpMethod(), redact(buffered.request().url()), bodySnippet(buffered));

		return super.decode(methodKey, buffered);
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
			return redact(content.length() > MAX_BODY_CHARACTERS
				? content.substring(0, MAX_BODY_CHARACTERS) + TRUNCATION_MARKER
				: content);
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
