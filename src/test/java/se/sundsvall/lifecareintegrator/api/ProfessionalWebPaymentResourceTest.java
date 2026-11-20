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
import se.sundsvall.lifecareintegrator.service.ProfessionalWebPaymentService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebPaymentResourceTest {

	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String LIFECARE_JSON = "{\"payment\":{\"amount\":1250.50},\"personnummer\":\"191212121212\"}";
	private static final String BODY = "{ \"amount\": 1250.50,\n \"text\": \"Utbetalning åäö\", \"unknown\": {\"a\": [1]} }";

	@MockitoBean
	private ProfessionalWebPaymentService serviceMock;

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
		when(serviceMock.hasHousehold("2281", PARTY_ID, LocalDate.of(2026, 11, 1))).thenReturn("true".getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/persons/" + PARTY_ID + "/household?date=2026-11-01")).isEqualTo("true");
		verify(serviceMock).hasHousehold("2281", PARTY_ID, LocalDate.of(2026, 11, 1));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void hasHouseholdForAPartyIdPartyDoesNotKnowIsNotFound() {
		when(serviceMock.hasHousehold("2281", PARTY_ID, LocalDate.of(2026, 11, 1))).thenThrow(Problem.valueOf(NOT_FOUND, "No person number found for partyId '" + PARTY_ID + "'"));

		webTestClient.get().uri("/2281/professional-web/persons/" + PARTY_ID + "/household?date=2026-11-01")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.detail").isEqualTo("No person number found for partyId '" + PARTY_ID + "'");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"not-a-uuid", "12345", "81471222-5798-11e9-ae24-57fa13b361e"
	})
	void hasHouseholdWithAPartyIdThatIsNotAUuidIsRefused(final String partyId) {
		webTestClient.get().uri("/2281/professional-web/persons/" + partyId + "/household?date=2026-11-01")
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"?date=2026-02-30", "?date=2026/11/01", "?date=", ""
	})
	void hasHouseholdWithoutAValidDateIsRefused(final String query) {
		webTestClient.get().uri("/2281/professional-web/persons/" + PARTY_ID + "/household" + query)
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
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
	}

	@Test
	void createPaymentFailingInLifecareIsBadGateway() {
		when(serviceMock.createPayment(42, "{}".getBytes(UTF_8))).thenThrow(Problem.valueOf(BAD_GATEWAY, "Lifecare answered with status 500: Fel"));

		webTestClient.post().uri("/2281/professional-web/services/42/payments")
			.contentType(APPLICATION_JSON)
			.bodyValue("{}")
			.exchange()
			.expectStatus().isEqualTo(502)
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Lifecare answered with status 500: Fel");

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

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1", "abc", "99999999999999999999"
	})
	void serviceIdThatIsNotAPositiveNumberIsRefused(final String serviceId) {
		webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/payments/proposal").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/payments/latest").exchange().expectStatus().isBadRequest();
		webTestClient.post().uri("/2281/professional-web/services/" + serviceId + "/payments").contentType(APPLICATION_JSON).bodyValue("{}").exchange().expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	@Test
	void writeWithoutBodyIsRefused() {
		webTestClient.post().uri("/2281/professional-web/services/42/payments").contentType(APPLICATION_JSON).exchange().expectStatus().isBadRequest();
		webTestClient.post().uri("/2281/professional-web/payees").contentType(APPLICATION_JSON).exchange().expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	@Test
	void invalidMunicipalityIdIsRefused() {
		webTestClient.get().uri("/bad/professional-web/services/42/payments/proposal").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/bad/professional-web/services/42/payments/latest").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/bad/professional-web/persons/" + PARTY_ID + "/household?date=2026-11-01").exchange().expectStatus().isBadRequest();
		webTestClient.post().uri("/bad/professional-web/services/42/payments").contentType(APPLICATION_JSON).bodyValue("{}").exchange().expectStatus().isBadRequest();
		webTestClient.post().uri("/bad/professional-web/payees").contentType(APPLICATION_JSON).bodyValue("{}").exchange().expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	private String getJson(final String uri) {
		return webTestClient.get().uri(uri)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();
	}
}
