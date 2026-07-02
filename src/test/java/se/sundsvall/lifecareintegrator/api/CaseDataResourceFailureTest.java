package se.sundsvall.lifecareintegrator.api;

import java.util.Map;
import org.junit.jupiter.api.Test;
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
import se.sundsvall.lifecareintegrator.service.FamilyCareService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class CaseDataResourceFailureTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String INVALID_MUNICIPALITY_ID = "bad-municipality-id";
	private static final String INVALID_UUID = "not-a-valid-uuid";
	private static final String PAYMENTS_PATH = "/{municipalityId}/payments";
	private static final String INVESTIGATIONS_PATH = "/{municipalityId}/investigations";

	@MockitoBean
	private FamilyCareService familyCareServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getPaymentsWithInvalidMunicipalityId() {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PAYMENTS_PATH).queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", INVALID_MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getPayments.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(familyCareServiceMock);
	}

	@Test
	void getPaymentsWithInvalidPartyId() {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PAYMENTS_PATH).queryParam("partyId", INVALID_UUID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getPayments.partyId", "not a valid UUID"));

		verifyNoInteractions(familyCareServiceMock);
	}

	@Test
	void getPaymentsWithMissingPartyId() {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PAYMENTS_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);

		verifyNoInteractions(familyCareServiceMock);
	}

	@Test
	void getPaymentsWithNegativePage() {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PAYMENTS_PATH).queryParam("partyId", PARTY_ID).queryParam("page", "-1").build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getPayments.page", "must be greater than 0"));

		verifyNoInteractions(familyCareServiceMock);
	}

	@Test
	void getInvestigationsWithInvalidMunicipalityId() {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(INVESTIGATIONS_PATH).queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", INVALID_MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getInvestigations.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(familyCareServiceMock);
	}

	@Test
	void getInvestigationsWithNegativePageSize() {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(INVESTIGATIONS_PATH).queryParam("partyId", PARTY_ID).queryParam("pageSize", "-5").build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getInvestigations.pageSize", "must be greater than 0"));

		verifyNoInteractions(familyCareServiceMock);
	}
}
