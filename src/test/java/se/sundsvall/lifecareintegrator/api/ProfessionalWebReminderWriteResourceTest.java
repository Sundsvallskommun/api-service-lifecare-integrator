package se.sundsvall.lifecareintegrator.api;

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
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebApi2Client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebReminderWriteResourceTest {

	private static final String CREATE_BODY = "{ \"serviceId\": 42,\n  \"personnummer\": \"19121212-1212\", \"unknown\": {\"a\": [1, 2.50]} }";
	private static final String UPDATE_BODY = "{\"reminderId\": 7, \"personnummer\": \"191212121212\", \"unknown\": {\"a\": [1]}}";

	@MockitoBean
	private ProfessionalWebApi2Client clientMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createSendsTheBodyUnchangedAndAnswersCreatedWithLifecaresBody() {
		when(clientMock.createReminder(CREATE_BODY.getBytes(UTF_8))).thenReturn("{\"id\":9}".getBytes(UTF_8));

		final var body = webTestClient.post().uri("/2281/professional-web/reminders")
			.contentType(APPLICATION_JSON)
			.bodyValue(CREATE_BODY)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo("{\"id\":9}");
		verify(clientMock).createReminder(CREATE_BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createWithAnEmptyLifecareAnswerAnswersCreatedWithoutBody() {
		when(clientMock.createReminder(any())).thenReturn(new byte[0]);

		webTestClient.post().uri("/2281/professional-web/reminders")
			.contentType(APPLICATION_JSON)
			.bodyValue("{}")
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().doesNotExist("Content-Type")
			.expectBody().isEmpty();
	}

	@Test
	void createWithANullLifecareAnswerAnswersCreatedWithoutBody() {
		when(clientMock.createReminder(any())).thenReturn(null);

		webTestClient.post().uri("/2281/professional-web/reminders")
			.contentType(APPLICATION_JSON)
			.bodyValue("{}")
			.exchange()
			.expectStatus().isCreated()
			.expectBody().isEmpty();
	}

	@Test
	void updateSendsTheBodyUnchangedAndAnswersNoContent() {
		webTestClient.put().uri("/2281/professional-web/reminders/7")
			.contentType(APPLICATION_JSON)
			.bodyValue(UPDATE_BODY)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(clientMock).updateReminder(UPDATE_BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"{\"reminderId\": 8}", "{\"text\": \"x\"}", "{\"reminderId\": \"7\"}", "{\"reminderId\": null}", "{\"reminderId\": 7.5}", "{\"nested\": {\"reminderId\": 7}}"
	})
	void updateWhoseBodyDoesNotNameTheSameReminderIsRefused(final String body) {
		webTestClient.put().uri("/2281/professional-web/reminders/7")
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(clientMock);
	}

	@Test
	void removeSendsTheIdAndAnswersNoContent() {
		webTestClient.delete().uri("/2281/professional-web/reminders/7")
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(clientMock).removeReminder("{\"id\":7}".getBytes(UTF_8));
		verifyNoMoreInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"reminderId\": 7}]", "\"text\"", "42", "null"
	})
	void bodyThatIsNotAJsonObjectIsRefused(final String body) {
		webTestClient.post().uri("/2281/professional-web/reminders").contentType(APPLICATION_JSON).bodyValue(body).exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/2281/professional-web/reminders/7").contentType(APPLICATION_JSON).bodyValue(body).exchange().expectStatus().isBadRequest();

		verifyNoInteractions(clientMock);
	}

	@Test
	void bodyThatIsNotJsonIsRefused() {
		webTestClient.post().uri("/2281/professional-web/reminders").contentType(APPLICATION_JSON).bodyValue("{not json").exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/2281/professional-web/reminders/7").contentType(APPLICATION_JSON).bodyValue("{not json").exchange().expectStatus().isBadRequest();

		verifyNoInteractions(clientMock);
	}

	@Test
	void emptyBodyIsRefused() {
		webTestClient.post().uri("/2281/professional-web/reminders").contentType(APPLICATION_JSON).exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/2281/professional-web/reminders/7").contentType(APPLICATION_JSON).exchange().expectStatus().isBadRequest();

		verifyNoInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1", "abc"
	})
	void invalidReminderIdIsRefused(final String reminderId) {
		webTestClient.put().uri("/2281/professional-web/reminders/" + reminderId).contentType(APPLICATION_JSON).bodyValue("{\"reminderId\": 0}").exchange().expectStatus().isBadRequest();
		webTestClient.delete().uri("/2281/professional-web/reminders/" + reminderId).exchange().expectStatus().isBadRequest();

		verifyNoInteractions(clientMock);
	}

	@Test
	void invalidMunicipalityIdIsRefused() {
		webTestClient.post().uri("/bad-municipality/professional-web/reminders").contentType(APPLICATION_JSON).bodyValue("{}").exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/bad-municipality/professional-web/reminders/7").contentType(APPLICATION_JSON).bodyValue("{\"reminderId\": 7}").exchange().expectStatus().isBadRequest();
		webTestClient.delete().uri("/bad-municipality/professional-web/reminders/7").exchange().expectStatus().isBadRequest();

		verifyNoInteractions(clientMock);
	}

	@Test
	void lifecareRefusalOfAnUpdateIsUnprocessableWithLifecaresText() {
		doThrow(Problem.valueOf(UNPROCESSABLE_CONTENT, "Påminnelsen är klar")).when(clientMock).updateReminder(any());

		webTestClient.put().uri("/2281/professional-web/reminders/7")
			.contentType(APPLICATION_JSON)
			.bodyValue("{\"reminderId\": 7}")
			.exchange()
			.expectStatus().isEqualTo(422)
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Påminnelsen är klar");
	}
}
