package se.sundsvall.lifecareintegrator.service;

import java.util.Optional;
import se.sundsvall.dept44.problem.Problem;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

final class ProfessionalWebBodies {

	static final String BODY_NOT_A_JSON_OBJECT = "The body must be a JSON object";

	private static final JsonMapper JSON = JsonMapper.builder().build();

	private ProfessionalWebBodies() {}

	static JsonNode requireJsonObject(final byte[] body) {
		return parse(body)
			.filter(JsonNode::isObject)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT));
	}

	static Optional<byte[]> toAnswer(final byte[] answer) {
		return Optional.ofNullable(answer)
			.filter(bytes -> bytes.length > 0);
	}

	static Optional<JsonNode> parse(final byte[] body) {
		try {
			return Optional.ofNullable(JSON.readTree(body));
		} catch (final JacksonException _) {
			return Optional.empty();
		}
	}
}
