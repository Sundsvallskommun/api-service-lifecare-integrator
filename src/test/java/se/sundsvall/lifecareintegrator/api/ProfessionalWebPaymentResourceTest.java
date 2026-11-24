package se.sundsvall.lifecareintegrator.api;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebService;

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
class ProfessionalWebPaymentResourceTest {

	private static final String LIFECARE_JSON = "{\"payment\":{\"amount\":1250.50},\"personnummer\":\"191212121212\"}";
	private static final String BODY = "{ \"amount\": 1250.50,\n \"text\": \"Utbetalning åäö\", \"unknown\": {\"a\": [1]} }";

	@MockitoBean
	private ProfessionalWebService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getPaymentProposal() {
		when(serviceMock.getPaymentProposal(42)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/services/42/payments/proposal")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).getPaymentProposal(42);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getLatestPayments() {
		when(serviceMock.getLatestPayments(42)).thenReturn("[]".getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/services/42/payments/latest")).isEqualTo("[]");
		verify(serviceMock).getLatestPayments(42);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void hasHouseholdReturnsLifecaresBoolean() {
		when(serviceMock.hasHousehold(42, LocalDate.of(2026, 11, 1))).thenReturn("true".getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/services/42/household?date=2026-11-01")).isEqualTo("true");
		verify(serviceMock).hasHousehold(42, LocalDate.of(2026, 11, 1));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createPaymentAnswersCreatedWithLifecaresBody() {
		when(serviceMock.createPayment(42, BODY.getBytes(UTF_8))).thenReturn(Optional.of("{\"paymentId\":5}".getBytes(UTF_8)));

		final var body = webTestClient.post().uri("/2281/professional-web/services/42/payments")
			.contentType(APPLICATION_JSON)
			.bodyValue(BODY.getBytes(UTF_8))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo("{\"paymentId\":5}");
		verify(serviceMock).createPayment(42, BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createPaymentWithoutLifecareAnswerIsNoContent() {
		when(serviceMock.createPayment(42, "{}".getBytes(UTF_8))).thenReturn(Optional.empty());

		webTestClient.post().uri("/2281/professional-web/services/42/payments")
			.contentType(APPLICATION_JSON)
			.bodyValue("{}")
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(serviceMock).createPayment(42, "{}".getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createPayeeAnswersCreatedWithLifecaresBody() {
		when(serviceMock.createPayee(BODY.getBytes(UTF_8))).thenReturn(Optional.of("{\"payeeId\":3}".getBytes(UTF_8)));

		final var body = webTestClient.post().uri("/2281/professional-web/payees")
			.contentType(APPLICATION_JSON)
			.bodyValue(BODY.getBytes(UTF_8))
			.exchange()
			.expectStatus().isCreated()
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo("{\"payeeId\":3}");
		verify(serviceMock).createPayee(BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	private String getJson(final String uri) {
		return webTestClient.get().uri(uri)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();
	}
}
