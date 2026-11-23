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
import se.sundsvall.lifecareintegrator.service.ProfessionalWebDecisionService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebDecisionResourceFailureTest {

	private static final String INVALID_MUNICIPALITY_ID = "bad-municipality";
	private static final String NOT_A_MUNICIPALITY_ID = "not a valid municipality ID";
	private static final String NOT_POSITIVE = "must be greater than 0";

	@MockitoBean
	private ProfessionalWebDecisionService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@ValueSource(strings = {
		"abc", "99999999999999999999"
	})
	void getDecisionProposalWithACalculationIdThatIsNotANumberIsRefused(final String calculationId) {
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/services/42/decisions/proposal?amountType=TotalSum&calculationId=" + calculationId).exchange(), "calculationId");

		verifyNoInteractions(serviceMock);
	}

	@Test
	void getDecisionProposalWithANegativeCalculationIdIsRefused() {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/services/42/decisions/proposal?amountType=TotalSum&calculationId=-1").exchange()),
			"getDecisionProposal.calculationId", "must be greater than or equal to 0");

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void getDecisionProposalWithServiceIdThatIsNotPositive(final String serviceId) {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/decisions/proposal?amountType=TotalSum&calculationId=7").exchange()),
			"getDecisionProposal.serviceId", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void getDecisionReasonsWithDecisionCodeThatIsNotPositive(final String decisionCode) {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/decision-codes/" + decisionCode + "/reasons").exchange()),
			"getDecisionReasons.decisionCode", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void getDecisionWithDecisionIdThatIsNotPositive(final String decisionId) {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/decisions/" + decisionId).exchange()),
			"getDecision.decisionId", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void createDecisionWithServiceIdThatIsNotPositive(final String serviceId) {
		assertViolation(readViolations(webTestClient.post().uri("/2281/professional-web/services/" + serviceId + "/decisions")
			.contentType(APPLICATION_JSON).bodyValue("{}").exchange()), "createDecision.serviceId", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void updateDecisionWithDecisionIdThatIsNotPositive(final String decisionId) {
		assertViolation(readViolations(webTestClient.put().uri("/2281/professional-web/decisions/" + decisionId)
			.contentType(APPLICATION_JSON).bodyValue("{}").exchange()), "updateDecision.decisionId", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"abc", "99999999999999999999"
	})
	void idThatIsNotANumberIsRefused(final String id) {
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/services/" + id + "/decisions/proposal?amountType=TotalSum&calculationId=7").exchange(), "serviceId");
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/decision-codes/" + id + "/reasons").exchange(), "decisionCode");
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/decisions/" + id).exchange(), "decisionId");
		assertBadRequest(webTestClient.post().uri("/2281/professional-web/services/" + id + "/decisions").contentType(APPLICATION_JSON).bodyValue("{}").exchange(), "serviceId");
		assertBadRequest(webTestClient.put().uri("/2281/professional-web/decisions/" + id).contentType(APPLICATION_JSON).bodyValue("{}").exchange(), "decisionId");

		verifyNoInteractions(serviceMock);
	}

	@Test
	void writeWithoutBodyIsRefused() {
		assertBadRequest(webTestClient.post().uri("/2281/professional-web/services/42/decisions").contentType(APPLICATION_JSON).exchange());
		assertBadRequest(webTestClient.put().uri("/2281/professional-web/decisions/9").contentType(APPLICATION_JSON).exchange());

		verifyNoInteractions(serviceMock);
	}

	@Test
	void invalidMunicipalityIdIsRefused() {
		assertViolation(readViolations(webTestClient.get().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/services/42/decisions/proposal?amountType=TotalSum&calculationId=7").exchange()),
			"getDecisionProposal.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.get().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/decision-codes/5/reasons").exchange()),
			"getDecisionReasons.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.get().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/decisions/9").exchange()),
			"getDecision.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.post().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/services/42/decisions")
			.contentType(APPLICATION_JSON).bodyValue("{}").exchange()), "createDecision.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.put().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/decisions/9")
			.contentType(APPLICATION_JSON).bodyValue("{}").exchange()), "updateDecision.municipalityId", NOT_A_MUNICIPALITY_ID);

		verifyNoInteractions(serviceMock);
	}

	@Test
	void updateDecisionRefusedByLifecareIsUnprocessable() {
		when(serviceMock.updateDecision(9, "{}".getBytes(UTF_8))).thenThrow(Problem.valueOf(UNPROCESSABLE_CONTENT, "Beslutet är verkställt"));

		webTestClient.put().uri("/2281/professional-web/decisions/9")
			.contentType(APPLICATION_JSON)
			.bodyValue("{}")
			.exchange()
			.expectStatus().isEqualTo(422)
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Beslutet är verkställt");

		verify(serviceMock).updateDecision(9, "{}".getBytes(UTF_8));
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
