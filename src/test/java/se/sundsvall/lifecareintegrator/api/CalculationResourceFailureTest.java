package se.sundsvall.lifecareintegrator.api;

import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateCalculationRequest;
import se.sundsvall.lifecareintegrator.service.FamilyCareService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class CalculationResourceFailureTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String INVALID_MUNICIPALITY_ID = "bad-municipality-id";
	private static final String INVALID_UUID = "not-a-valid-uuid";
	private static final String PATH = "/{municipalityId}/calculations";

	@MockitoBean
	private FamilyCareService familyCareServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getCalculationsWithInvalidMunicipalityId() {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", INVALID_MUNICIPALITY_ID)))
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
			.containsExactly(tuple("getCalculations.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(familyCareServiceMock);
	}

	@Test
	void createCalculationWithMissingRequiredFields() {
		// Missing normId and the calculation dates
		final var request = CreateCalculationRequest.create().withPartyId(PARTY_ID);

		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.contains(
				tuple("normId", "must not be null"),
				tuple("calculationDate", "must not be null"),
				tuple("calculationFromDate", "must not be null"),
				tuple("calculationToDate", "must not be null"));

		verifyNoInteractions(familyCareServiceMock);
	}

	@Test
	void createCalculationWithInvalidPartyId() {
		final var request = CreateCalculationRequest.create()
			.withPartyId(INVALID_UUID)
			.withNormId(1)
			.withCalculationDate(LocalDate.parse("2026-05-01"))
			.withCalculationFromDate(LocalDate.parse("2026-05-01"))
			.withCalculationToDate(LocalDate.parse("2026-05-31"));

		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.contains(tuple("partyId", "not a valid UUID"));

		verifyNoInteractions(familyCareServiceMock);
	}
}
