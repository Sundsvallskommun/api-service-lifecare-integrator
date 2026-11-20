package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;

class ProfessionalWebErrorDecoderTest {

	private final ProfessionalWebErrorDecoder decoder = new ProfessionalWebErrorDecoder();

	@ParameterizedTest
	@ValueSource(strings = {
		"exceptionMessage", "ExceptionMessage", "message", "Message"
	})
	void badRequestCarriesLifecaresText(final String key) {
		final var problem = decode(400, "{\"" + key + "\":\"Datum saknas\"}");

		assertProblem(problem, BAD_REQUEST, "Datum saknas");
	}

	@Test
	void exceptionMessageIsPreferredOverMessage() {
		final var problem = decode(400, "{\"Message\":\"An error has occurred.\",\"ExceptionMessage\":\"Datum saknas\"}");

		assertProblem(problem, BAD_REQUEST, "Datum saknas");
	}

	@Test
	void badRequestWithoutBodyGetsAStandardText() {
		assertProblem(decode(400, null), BAD_REQUEST, "Lifecare refused the request");
	}

	@Test
	void badRequestWithABodyThatIsNotJsonGetsAStandardText() {
		assertProblem(decode(400, "<html>Bad Request</html>"), BAD_REQUEST, "Lifecare refused the request");
	}

	@Test
	void badRequestWithJsonWithoutTextGetsAStandardText() {
		assertProblem(decode(400, "{\"exceptionMessage\":\" \",\"other\":\"x\"}"), BAD_REQUEST, "Lifecare refused the request");
	}

	@Test
	void badRequestWithJsonThatIsNotAnObjectGetsAStandardText() {
		assertProblem(decode(400, "[\"Datum saknas\"]"), BAD_REQUEST, "Lifecare refused the request");
	}

	@Test
	void forbiddenIsForbidden() {
		assertProblem(decode(403, "{\"message\":\"Nej\"}"), FORBIDDEN, "Lifecare does not let the integration account do this");
	}

	@Test
	void notFoundIsNotFound() {
		assertProblem(decode(404, "{\"message\":\"Saknas\"}"), NOT_FOUND, "Lifecare found nothing there");
	}

	@Test
	void lifecaresOwnRefusalIsUnprocessableWithItsText() {
		assertProblem(decode(461, "{\"exceptionMessage\":\"Påminnelsen är redan klar\"}"), UNPROCESSABLE_CONTENT, "Påminnelsen är redan klar");
	}

	@Test
	void lifecaresOwnRefusalWithoutTextGetsAStandardText() {
		assertProblem(decode(461, ""), UNPROCESSABLE_CONTENT, "Lifecare refused the request");
	}

	@ParameterizedTest
	@ValueSource(ints = {
		401, 405, 409, 460, 462, 500, 503
	})
	void everyOtherStatusIsBadGateway(final int status) {
		assertProblem(decode(status, "{\"exceptionMessage\":\"intern detalj\"}"), BAD_GATEWAY, "Lifecare answered with status " + status);
	}

	private ThrowableProblem decode(final int status, final String body) {
		final var response = Response.builder()
			.status(status)
			.request(Request.create(Request.HttpMethod.GET, "http://x/api2/y", Map.of(), null, UTF_8, new RequestTemplate()))
			.headers(Map.of());
		if (body != null) {
			response.body(body, UTF_8);
		}
		final var exception = decoder.decode("method", response.build());
		assertThat(exception).isInstanceOf(ThrowableProblem.class);
		return (ThrowableProblem) exception;
	}

	private static void assertProblem(final ThrowableProblem problem, final HttpStatus status, final String detail) {
		assertThat(problem.getStatus().value()).isEqualTo(status.value());
		assertThat(problem.getDetail()).isEqualTo(detail);
	}
}
