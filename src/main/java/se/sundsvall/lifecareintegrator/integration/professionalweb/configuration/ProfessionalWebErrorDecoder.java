package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.exception.ServerProblem;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;

class ProfessionalWebErrorDecoder implements ErrorDecoder {

	/** Lifecare's own status for a refusal its rules make, answered with a text for the caseworker. */
	private static final int LIFECARE_REFUSAL_STATUS = 461;

	private static final List<String> TEXT_KEYS = List.of("exceptionMessage", "ExceptionMessage", "message", "Message");
	private static final String STANDARD_TEXT = "Lifecare refused the request";
	private static final JsonMapper JSON = JsonMapper.builder().build();
	private static final Logger LOG = LoggerFactory.getLogger(ProfessionalWebErrorDecoder.class);

	@Override
	public Exception decode(final String methodKey, final Response response) {
		LOG.info("Lifecare answered {} {} with status {}", response.request().httpMethod(), URI.create(response.request().url()).getPath(), response.status());
		return switch (response.status()) {
			case 400 -> new ClientProblem(BAD_REQUEST, findLifecareText(response).orElse(STANDARD_TEXT));
			case 403 -> new ClientProblem(FORBIDDEN, "Lifecare does not let the integration account do this");
			case 404 -> new ClientProblem(NOT_FOUND, "Lifecare found nothing there");
			case LIFECARE_REFUSAL_STATUS -> new ClientProblem(UNPROCESSABLE_CONTENT, findLifecareText(response).orElse(STANDARD_TEXT));
			default -> new ServerProblem(BAD_GATEWAY, "Lifecare answered with status " + response.status());
		};
	}

	private static Optional<String> findLifecareText(final Response response) {
		return Optional.ofNullable(response.body())
			.flatMap(ProfessionalWebErrorDecoder::readBody)
			.flatMap(ProfessionalWebErrorDecoder::parse)
			.flatMap(node -> TEXT_KEYS.stream()
				.map(node::get)
				.filter(value -> value != null && value.isString() && !value.stringValue().isBlank())
				.map(JsonNode::stringValue)
				.findFirst());
	}

	private static Optional<String> readBody(final Response.Body body) {
		try (InputStream stream = body.asInputStream()) {
			return Optional.of(new String(stream.readAllBytes(), UTF_8));
		} catch (final IOException _) {
			return Optional.empty();
		}
	}

	private static Optional<JsonNode> parse(final String body) {
		try {
			return Optional.ofNullable(JSON.readTree(body));
		} catch (final JacksonException _) {
			return Optional.empty();
		}
	}
}
