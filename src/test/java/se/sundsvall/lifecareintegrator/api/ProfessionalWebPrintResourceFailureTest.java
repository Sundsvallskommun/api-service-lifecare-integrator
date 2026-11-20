package se.sundsvall.lifecareintegrator.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import se.sundsvall.lifecareintegrator.service.ProfessionalWebPrintService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_PDF;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebPrintResourceFailureTest {

	private static final String INVALID_MUNICIPALITY_ID = "bad-municipality";
	private static final String NOT_A_MUNICIPALITY_ID = "not a valid municipality ID";
	private static final String NOT_POSITIVE = "must be greater than 0";

	@MockitoBean
	private ProfessionalWebPrintService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@CsvSource({
		"/calculations/%s/pdf, getCalculationPdf.calculationId",
		"/decisions/%s/pdf, getDecisionPdf.decisionId",
		"/documents/%s/pdf, getDocumentPdf.id"
	})
	void idThatIsNotPositiveIsRefused(final String path, final String field) {
		assertViolation(readViolations(getPdf("/2281/professional-web" + path.formatted("0"))), field, NOT_POSITIVE);
		assertViolation(readViolations(getPdf("/2281/professional-web" + path.formatted("-1"))), field, NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"abc", "99999999999999999999"
	})
	void idThatIsNotANumberIsRefused(final String id) {
		assertBadRequest(getPdf("/2281/professional-web/calculations/" + id + "/pdf"));
		assertBadRequest(getPdf("/2281/professional-web/decisions/" + id + "/pdf"));
		assertBadRequest(getPdf("/2281/professional-web/documents/" + id + "/pdf"));

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@CsvSource({
		"/calculations/7/pdf, getCalculationPdf",
		"/decisions/9/pdf, getDecisionPdf",
		"/documents/1/pdf, getDocumentPdf"
	})
	void invalidMunicipalityIdIsRefused(final String path, final String operation) {
		assertViolation(readViolations(getPdf("/" + INVALID_MUNICIPALITY_ID + "/professional-web" + path)), operation + ".municipalityId", NOT_A_MUNICIPALITY_ID);

		verifyNoInteractions(serviceMock);
	}

	@Test
	void printThatIsNotAPdfIsBadGateway() {
		when(serviceMock.getDecisionPdf(9)).thenThrow(Problem.valueOf(BAD_GATEWAY, "Lifecare answered the print with something that is not a PDF"));

		getPdf("/2281/professional-web/decisions/9/pdf")
			.expectStatus().isEqualTo(502)
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Lifecare answered the print with something that is not a PDF");

		verify(serviceMock).getDecisionPdf(9);
		verifyNoMoreInteractions(serviceMock);
	}

	private WebTestClient.ResponseSpec getPdf(final String uri) {
		return webTestClient.get().uri(uri).accept(APPLICATION_PDF).exchange();
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
