package se.sundsvall.lifecareintegrator.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebCalculationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebCalculationResourceFailureTest {

	private static final String INVALID_MUNICIPALITY_ID = "bad-municipality";
	private static final String NOT_A_MUNICIPALITY_ID = "not a valid municipality ID";
	private static final String NOT_POSITIVE = "must be greater than 0";

	@MockitoBean
	private ProfessionalWebCalculationService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@CsvSource({
		"GET, /services/%s/calculations/proposal, getCalculationProposal.serviceId",
		"GET, /services/%s/calculations/latest, getLatestCalculation.serviceId",
		"GET, /calculations/%s, getCalculation.calculationId",
		"GET, /calculations/%s/edit, getCalculationForEdit.calculationId",
		"GET, /services/%s/job-stimulus, getJobStimulus.serviceId",
		"POST, /services/%s/calculations, createCalculation.serviceId",
		"PUT, /calculations/%s, updateCalculation.calculationId",
		"DELETE, /calculations/%s, deleteCalculation.calculationId"
	})
	void idThatIsNotPositiveIsRefused(final HttpMethod method, final String path, final String field) {
		assertViolation(readViolations(exchange(method, "/2281/professional-web" + path.formatted("0"))), field, NOT_POSITIVE);
		assertViolation(readViolations(exchange(method, "/2281/professional-web" + path.formatted("-1"))), field, NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"abc", "99999999999999999999"
	})
	void idThatIsNotANumberIsRefused(final String id) {
		assertBadRequest(exchange(GET, "/2281/professional-web/services/" + id + "/calculations/proposal"));
		assertBadRequest(exchange(GET, "/2281/professional-web/services/" + id + "/calculations/latest"));
		assertBadRequest(exchange(GET, "/2281/professional-web/calculations/" + id));
		assertBadRequest(exchange(GET, "/2281/professional-web/calculations/" + id + "/edit"));
		assertBadRequest(exchange(GET, "/2281/professional-web/services/" + id + "/job-stimulus"));
		assertBadRequest(exchange(POST, "/2281/professional-web/services/" + id + "/calculations"));
		assertBadRequest(exchange(PUT, "/2281/professional-web/calculations/" + id));
		assertBadRequest(exchange(DELETE, "/2281/professional-web/calculations/" + id));

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@CsvSource({
		"POST, /calculations/place-persons", "POST, /calculations/amount", "POST, /calculations/shared-cost", "POST, /calculations/job-stimuli",
		"POST, /services/42/calculations", "PUT, /calculations/7"
	})
	void writeWithoutBodyIsRefused(final HttpMethod method, final String path) {
		assertBadRequest(webTestClient.method(method).uri("/2281/professional-web" + path).contentType(APPLICATION_JSON).exchange());

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@CsvSource({
		"GET, /services/42/calculations/proposal, getCalculationProposal",
		"GET, /services/42/calculations/latest, getLatestCalculation",
		"GET, /calculations/7, getCalculation",
		"GET, /calculations/7/edit, getCalculationForEdit",
		"GET, /services/42/job-stimulus, getJobStimulus",
		"POST, /calculations/place-persons, placePersons",
		"POST, /calculations/amount, getAmount",
		"POST, /calculations/shared-cost, getSharedCost",
		"POST, /calculations/job-stimuli, getJobStimuli",
		"POST, /services/42/calculations, createCalculation",
		"PUT, /calculations/7, updateCalculation",
		"DELETE, /calculations/7, deleteCalculation"
	})
	void invalidMunicipalityIdIsRefused(final HttpMethod method, final String path, final String operation) {
		assertViolation(readViolations(exchange(method, "/" + INVALID_MUNICIPALITY_ID + "/professional-web" + path)), operation + ".municipalityId", NOT_A_MUNICIPALITY_ID);

		verifyNoInteractions(serviceMock);
	}

	@Test
	void deleteCalculationRefusedByLifecareIsUnprocessable() {
		doThrow(Problem.valueOf(UNPROCESSABLE_CONTENT, "Beräkningen är slutlig")).when(serviceMock).deleteCalculation(7);

		webTestClient.delete().uri("/2281/professional-web/calculations/7")
			.exchange()
			.expectStatus().isEqualTo(422)
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Beräkningen är slutlig");

		verify(serviceMock).deleteCalculation(7);
		verifyNoMoreInteractions(serviceMock);
	}

	private WebTestClient.ResponseSpec exchange(final HttpMethod method, final String uri) {
		if (GET.equals(method) || DELETE.equals(method)) {
			return webTestClient.method(method).uri(uri).exchange();
		}
		return webTestClient.method(method).uri(uri).contentType(APPLICATION_JSON).bodyValue("{}").exchange();
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
