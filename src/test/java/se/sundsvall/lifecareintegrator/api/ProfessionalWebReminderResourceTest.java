package se.sundsvall.lifecareintegrator.api;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebReminderService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebReminderResourceTest {

	private static final String LIFECARE_JSON = "{\"id\":42,\"unknownField\":{\"nested\":[1,2]},\"personnummer\":\"191212121212\"}";
	private static final String CREATE_BODY = "{ \"serviceId\": 42,\n  \"text\": \"Påminnelse åäö\", \"personnummer\": \"19121212-1212\", \"unknown\": {\"a\": [1, 2.50]} }";
	private static final String UPDATE_BODY = "{\"reminderId\": 7, \"text\": \"Påminnelse åäö\", \"personnummer\": \"191212121212\", \"unknown\": {\"a\": [1]}}";

	@MockitoBean
	private ProfessionalWebReminderService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void listReminders() {
		when(serviceMock.listReminders(42)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		final var body = webTestClient.get().uri("/2281/professional-web/services/42/reminders")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).listReminders(42);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void listRemindersAcceptsServiceIdOne() {
		when(serviceMock.listReminders(1)).thenReturn("[]".getBytes(UTF_8));

		final var body = webTestClient.get().uri("/2281/professional-web/services/1/reminders")
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo("[]");
		verify(serviceMock).listReminders(1);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getReminderProposal() {
		when(serviceMock.getReminderProposal(42)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		final var body = webTestClient.get().uri("/2281/professional-web/services/42/reminders/proposal")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).getReminderProposal(42);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getReminder() {
		when(serviceMock.getReminder(7)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		final var body = webTestClient.get().uri("/2281/professional-web/reminders/7")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).getReminder(7);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createReminder() {
		when(serviceMock.createReminder(CREATE_BODY.getBytes(UTF_8))).thenReturn(Optional.of("{\"id\":9}".getBytes(UTF_8)));

		final var body = webTestClient.post().uri("/2281/professional-web/reminders")
			.contentType(APPLICATION_JSON)
			.bodyValue(CREATE_BODY.getBytes(UTF_8))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo("{\"id\":9}");
		verify(serviceMock).createReminder(CREATE_BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createReminderWithoutLifecareAnswer() {
		when(serviceMock.createReminder("{}".getBytes(UTF_8))).thenReturn(Optional.empty());

		webTestClient.post().uri("/2281/professional-web/reminders")
			.contentType(APPLICATION_JSON)
			.bodyValue("{}")
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().doesNotExist("Content-Type")
			.expectBody().isEmpty();

		verify(serviceMock).createReminder("{}".getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateReminder() {
		webTestClient.put().uri("/2281/professional-web/reminders/7")
			.contentType(APPLICATION_JSON)
			.bodyValue(UPDATE_BODY.getBytes(UTF_8))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(serviceMock).updateReminder(7, UPDATE_BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void removeReminder() {
		webTestClient.delete().uri("/2281/professional-web/reminders/7")
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(serviceMock).removeReminder(7);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateReminderAcceptingJson() {
		webTestClient.put().uri("/2281/professional-web/reminders/7")
			.contentType(APPLICATION_JSON)
			.accept(APPLICATION_JSON)
			.bodyValue(UPDATE_BODY.getBytes(UTF_8))
			.exchange()
			.expectStatus().isNoContent();

		verify(serviceMock).updateReminder(7, UPDATE_BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void removeReminderAcceptingJson() {
		webTestClient.delete().uri("/2281/professional-web/reminders/7")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isNoContent();

		verify(serviceMock).removeReminder(7);
		verifyNoMoreInteractions(serviceMock);
	}
}
