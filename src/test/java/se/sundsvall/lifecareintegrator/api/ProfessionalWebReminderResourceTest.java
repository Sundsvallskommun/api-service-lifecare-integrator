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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebReminderResourceTest {

	private static final String LIFECARE_JSON = "{\"id\":42,\"unknownField\":{\"nested\":[1,2]},\"personnummer\":\"191212121212\"}";

	@MockitoBean
	private ProfessionalWebApi2Client clientMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void listRemindersByService() {
		when(clientMock.listRemindersByServiceId(42)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		final var body = webTestClient.get().uri("/2281/professional-web/services/42/reminders")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo(LIFECARE_JSON);
		verify(clientMock).listRemindersByServiceId(42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getProposalForService() {
		when(clientMock.getProposalForService(42)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		final var body = webTestClient.get().uri("/2281/professional-web/services/42/reminders/proposal")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo(LIFECARE_JSON);
		verify(clientMock).getProposalForService(42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getReminder() {
		when(clientMock.getReminderEditComposite(7)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		final var body = webTestClient.get().uri("/2281/professional-web/reminders/7")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo(LIFECARE_JSON);
		verify(clientMock).getReminderEditComposite(7);
		verifyNoMoreInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1", "abc", "1.5", "99999999999999999999"
	})
	void invalidServiceIdIsRefused(final String serviceId) {
		webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/reminders").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/reminders/proposal").exchange().expectStatus().isBadRequest();

		verifyNoInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1", "abc"
	})
	void invalidReminderIdIsRefused(final String reminderId) {
		webTestClient.get().uri("/2281/professional-web/reminders/" + reminderId).exchange().expectStatus().isBadRequest();

		verifyNoInteractions(clientMock);
	}

	@Test
	void serviceIdOneIsAccepted() {
		when(clientMock.listRemindersByServiceId(1)).thenReturn("[]".getBytes(UTF_8));

		webTestClient.get().uri("/2281/professional-web/services/1/reminders").exchange().expectStatus().isOk();

		verify(clientMock).listRemindersByServiceId(1);
	}

	@Test
	void invalidMunicipalityIdIsRefused() {
		webTestClient.get().uri("/bad-municipality/professional-web/services/42/reminders").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/bad-municipality/professional-web/services/42/reminders/proposal").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/bad-municipality/professional-web/reminders/7").exchange().expectStatus().isBadRequest();

		verifyNoInteractions(clientMock);
	}

	@Test
	void lifecareRefusalPassesThroughAsTheProblem() {
		when(clientMock.getReminderEditComposite(7)).thenThrow(Problem.valueOf(NOT_FOUND, "Lifecare found nothing there"));

		webTestClient.get().uri("/2281/professional-web/reminders/7")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.detail").isEqualTo("Lifecare found nothing there");
	}
}
