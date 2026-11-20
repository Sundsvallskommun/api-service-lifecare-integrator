package se.sundsvall.lifecareintegrator.integration.professionalweb;

import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import se.sundsvall.dept44.problem.Problem;
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
	static final int LIFECARE_REFUSAL_STATUS = 461;

	private static final List<String> TEXT_KEYS = List.of("exceptionMessage", "ExceptionMessage", "message", "Message");
	private static final String STANDARD_TEXT = "Lifecare refused the request";
	private static final JsonMapper JSON = JsonMapper.builder().build();

	@Override
	public Exception decode(final String methodKey, final Response response) {
		return switch (response.status()) {
			case 400 -> Problem.valueOf(BAD_REQUEST, lifecareText(response).orElse(STANDARD_TEXT));
			case 403 -> Problem.valueOf(FORBIDDEN, "Lifecare does not let the integration account do this");
			case 404 -> Problem.valueOf(NOT_FOUND, "Lifecare found nothing there");
			case LIFECARE_REFUSAL_STATUS -> Problem.valueOf(UNPROCESSABLE_CONTENT, lifecareText(response).orElse(STANDARD_TEXT));
			default -> Problem.valueOf(BAD_GATEWAY, "Lifecare answered with status " + response.status());
		};
	}

	private static Optional<String> lifecareText(final Response response) {
		return readBody(response)
			.flatMap(ProfessionalWebErrorDecoder::parse)
			.flatMap(node -> TEXT_KEYS.stream()
				.map(node::get)
				.filter(value -> value != null && value.isString() && !value.stringValue().isBlank())
				.map(JsonNode::stringValue)
				.findFirst());
	}

	private static Optional<String> readBody(final Response response) {
		if (response.body() == null) {
			return Optional.empty();
		}
		try (InputStream body = response.body().asInputStream()) {
			return Optional.of(new String(body.readAllBytes(), UTF_8));
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
