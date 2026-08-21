package se.sundsvall.lifecareintegrator.integration;

import feign.Response;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

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

		LOG.warn("{} responded {} to {} {}", integration, buffered.status(),
			buffered.request().httpMethod(), redact(buffered.request().url()));

		return super.decode(methodKey, buffered);
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
