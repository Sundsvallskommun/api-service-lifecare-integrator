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
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebService;

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
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebPaymentResourceFailureTest {

	private static final String INVALID_MUNICIPALITY_ID = "bad-municipality";
	private static final String NOT_A_MUNICIPALITY_ID = "not a valid municipality ID";
	private static final String NOT_POSITIVE = "must be greater than 0";

	@MockitoBean
	private ProfessionalWebService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void getPaymentProposalWithServiceIdThatIsNotPositive(final String serviceId) {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/payments/proposal").exchange()),
			"getPaymentProposal.serviceId", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void getLatestPaymentsWithServiceIdThatIsNotPositive(final String serviceId) {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/payments/latest").exchange()),
			"getLatestPayments.serviceId", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void createPaymentWithServiceIdThatIsNotPositive(final String serviceId) {
		assertViolation(readViolations(webTestClient.post().uri("/2281/professional-web/services/" + serviceId + "/payments")
			.contentType(APPLICATION_JSON).bodyValue("{}").exchange()), "createPayment.serviceId", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"abc", "99999999999999999999"
	})
	void serviceIdThatIsNotANumberIsRefused(final String serviceId) {
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/payments/proposal").exchange(), "serviceId");
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/payments/latest").exchange(), "serviceId");
		assertBadRequest(webTestClient.post().uri("/2281/professional-web/services/" + serviceId + "/payments").contentType(APPLICATION_JSON).bodyValue("{}").exchange(), "serviceId");
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/household?date=2026-11-01").exchange(), "serviceId");

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void hasHouseholdWithServiceIdThatIsNotPositive(final String serviceId) {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/household?date=2026-11-01").exchange()),
			"hasHousehold.serviceId", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"?date=2026-02-30", "?date=2026/11/01", "?date=", ""
	})
	void hasHouseholdWithoutAValidDateIsRefused(final String query) {
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/services/42/household" + query).exchange(), "date");

		verifyNoInteractions(serviceMock);
	}

	@Test
	void writeWithoutBodyIsRefused() {
		assertBadRequest(webTestClient.post().uri("/2281/professional-web/services/42/payments").contentType(APPLICATION_JSON).exchange());
		assertBadRequest(webTestClient.post().uri("/2281/professional-web/payees").contentType(APPLICATION_JSON).exchange());

		verifyNoInteractions(serviceMock);
	}

	@Test
	void invalidMunicipalityIdIsRefused() {
		assertViolation(readViolations(webTestClient.get().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/services/42/payments/proposal").exchange()),
			"getPaymentProposal.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.get().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/services/42/payments/latest").exchange()),
			"getLatestPayments.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.get().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/services/42/household?date=2026-11-01").exchange()),
			"hasHousehold.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.post().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/services/42/payments")
			.contentType(APPLICATION_JSON).bodyValue("{}").exchange()), "createPayment.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.post().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/payees")
			.contentType(APPLICATION_JSON).bodyValue("{}").exchange()), "createPayee.municipalityId", NOT_A_MUNICIPALITY_ID);

		verifyNoInteractions(serviceMock);
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

	private static void assertBadRequest(final WebTestClient.ResponseSpec response, final String parameter) {
		response.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Bad Request")
			.jsonPath("$.detail").value(String.class, detail -> assertThat(detail).contains("'" + parameter + "'"));
	}
}
