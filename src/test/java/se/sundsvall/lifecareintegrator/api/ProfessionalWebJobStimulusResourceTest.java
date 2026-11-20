package se.sundsvall.lifecareintegrator.api;

import java.time.LocalDate;
import java.util.Optional;
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
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebJobStimulusService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
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
class ProfessionalWebJobStimulusResourceTest {

	private static final String BODY = "{ \"personId\": \"19121212-1212\",\n \"text\": \"åäö\", \"unknown\": {\"a\": [1, 2.50]} }";

	@MockitoBean
	private ProfessionalWebJobStimulusService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getJobStimulusEndDateReturnsLifecaresBareString() {
		when(serviceMock.getJobStimulusEndDate(LocalDate.of(2026, 11, 1))).thenReturn("\"2028-10-31\"".getBytes(UTF_8));

		final var body = webTestClient.get().uri("/2281/professional-web/job-stimulus/end-date?fromDate=2026-11-01")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo("\"2028-10-31\"");
		verify(serviceMock).getJobStimulusEndDate(LocalDate.of(2026, 11, 1));
		verifyNoMoreInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"?fromDate=2026-13-01", "?fromDate=20261101", "?fromDate=2026-11-1", "?fromDate=01-11-2026", "?fromDate=", ""
	})
	void getJobStimulusEndDateWithoutAValidDateIsRefused(final String query) {
		webTestClient.get().uri("/2281/professional-web/job-stimulus/end-date" + query)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400);

		verifyNoInteractions(serviceMock);
	}

	@Test
	void saveJobStimulusAnswersCreatedWithLifecaresBody() {
		when(serviceMock.saveJobStimulus(BODY.getBytes(UTF_8))).thenReturn(Optional.of("{\"saved\":true}".getBytes(UTF_8)));

		final var body = webTestClient.post().uri("/2281/professional-web/job-stimulus")
			.contentType(APPLICATION_JSON)
			.bodyValue(BODY.getBytes(UTF_8))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo("{\"saved\":true}");
		verify(serviceMock).saveJobStimulus(BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void saveJobStimulusWithoutLifecareAnswerIsNoContent() {
		when(serviceMock.saveJobStimulus("{}".getBytes(UTF_8))).thenReturn(Optional.empty());

		webTestClient.post().uri("/2281/professional-web/job-stimulus")
			.contentType(APPLICATION_JSON)
			.bodyValue("{}")
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(serviceMock).saveJobStimulus("{}".getBytes(UTF_8));
	}

	@Test
	void saveJobStimulusWithoutBodyIsRefused() {
		webTestClient.post().uri("/2281/professional-web/job-stimulus")
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest();

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
			.jsonPath("$.detail").isEqualTo("The body must be a JSON object");
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
	}

	@Test
	void getJobStimulusEndDateFailingInLifecareIsBadGateway() {
		when(serviceMock.getJobStimulusEndDate(LocalDate.of(2026, 11, 1))).thenThrow(Problem.valueOf(BAD_GATEWAY, "Lifecare answered with status 500: Fel"));

		webTestClient.get().uri("/2281/professional-web/job-stimulus/end-date?fromDate=2026-11-01")
			.exchange()
			.expectStatus().isEqualTo(502)
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Lifecare answered with status 500: Fel");
	}

	@Test
	void invalidMunicipalityIdIsRefused() {
		webTestClient.get().uri("/bad-municipality/professional-web/job-stimulus/end-date?fromDate=2026-11-01")
			.exchange()
			.expectStatus().isBadRequest();
		webTestClient.post().uri("/bad-municipality/professional-web/job-stimulus")
			.contentType(APPLICATION_JSON)
			.bodyValue("{}")
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}
}
