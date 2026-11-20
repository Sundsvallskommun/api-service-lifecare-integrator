package se.sundsvall.lifecareintegrator.api;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebJobStimulusService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebJobStimulusResourceFailureTest {

	private static final String INVALID_MUNICIPALITY_ID = "bad-municipality";
	private static final String NOT_A_MUNICIPALITY_ID = "not a valid municipality ID";

	@MockitoBean
	private ProfessionalWebJobStimulusService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@ValueSource(strings = {
		"?fromDate=2026-13-01", "?fromDate=20261101", "?fromDate=2026-11-1", "?fromDate=01-11-2026", "?fromDate=", ""
	})
	void getJobStimulusEndDateWithoutAValidDateIsRefused(final String query) {
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/job-stimulus/end-date" + query).exchange());

		verifyNoInteractions(serviceMock);
	}

	@Test
	void saveJobStimulusWithoutBodyIsRefused() {
		assertBadRequest(webTestClient.post().uri("/2281/professional-web/job-stimulus").contentType(APPLICATION_JSON).exchange());

		verifyNoInteractions(serviceMock);
	}

	@Test
	void invalidMunicipalityIdIsRefused() {
		assertViolation(readViolations(webTestClient.get().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/job-stimulus/end-date?fromDate=2026-11-01").exchange()),
			"getJobStimulusEndDate.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.post().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/job-stimulus")
			.contentType(APPLICATION_JSON).bodyValue("{}").exchange()), "saveJobStimulus.municipalityId", NOT_A_MUNICIPALITY_ID);

		verifyNoInteractions(serviceMock);
	}

	@Test
	void saveJobStimulusWithABodyTheServiceRefuses() {
		when(serviceMock.saveJobStimulus("[]".getBytes(UTF_8))).thenThrow(Problem.valueOf(BAD_REQUEST, "The body must be a JSON object"));

		webTestClient.post().uri("/2281/professional-web/job-stimulus")
			.contentType(APPLICATION_JSON)
			.bodyValue("[]")
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.detail").isEqualTo("The body must be a JSON object");

		verify(serviceMock).saveJobStimulus("[]".getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void saveJobStimulusRefusedByLifecareIsUnprocessable() {
		when(serviceMock.saveJobStimulus("{}".getBytes(UTF_8))).thenThrow(Problem.valueOf(UNPROCESSABLE_CONTENT, "Perioden överlappar"));

		webTestClient.post().uri("/2281/professional-web/job-stimulus")
			.contentType(APPLICATION_JSON)
			.bodyValue("{}")
			.exchange()
			.expectStatus().isEqualTo(422)
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Perioden överlappar");

		verify(serviceMock).saveJobStimulus("{}".getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getJobStimulusEndDateFailingInLifecareIsBadGateway() {
		when(serviceMock.getJobStimulusEndDate(LocalDate.of(2026, 11, 1))).thenThrow(Problem.valueOf(BAD_GATEWAY, "Lifecare answered with status 500: Fel"));

		webTestClient.get().uri("/2281/professional-web/job-stimulus/end-date?fromDate=2026-11-01")
			.exchange()
			.expectStatus().isEqualTo(502)
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Lifecare answered with status 500: Fel");

		verify(serviceMock).getJobStimulusEndDate(LocalDate.of(2026, 11, 1));
		verifyNoMoreInteractions(serviceMock);
	}

	private static ConstraintViolationProblem readViolations(final WebTestClient.ResponseSpec response) {
		return response
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();
	}

	private static void assertViolation(final ConstraintViolationProblem response, final String field, final String message) {
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple(field, message));
	}

	private static void assertBadRequest(final WebTestClient.ResponseSpec response) {
		response.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Bad Request");
	}
}
