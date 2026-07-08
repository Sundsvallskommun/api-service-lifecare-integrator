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
import se.sundsvall.lifecareintegrator.service.DecisionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class DecisionResourceFailureTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String INVALID_MUNICIPALITY_ID = "bad-municipality-id";
	private static final String INVALID_PARTY_ID = "not-a-valid-uuid";
	private static final String PATH = "/{municipalityId}/decisions";

	@MockitoBean
	private DecisionService decisionServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getDecisionsWithInvalidMunicipalityId() {
		// Call
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", INVALID_MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getDecisions.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(decisionServiceMock);
	}

	@Test
	void getDecisionsWithInvalidPartyId() {
		// Call
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).queryParam("partyId", INVALID_PARTY_ID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getDecisions.partyId", "not a valid UUID"));

		verifyNoInteractions(decisionServiceMock);
	}

	@Test
	void getDecisionsWithMissingPartyId() {
		// Call
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);

		verifyNoInteractions(decisionServiceMock);
	}

	@Test
	void getDecisionsWithInvalidDate() {
		// Call
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH)
				.queryParam("partyId", PARTY_ID)
				.queryParam("from", "not-a-date")
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);

		verifyNoInteractions(decisionServiceMock);
	}
}
