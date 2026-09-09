package se.sundsvall.lifecareintegrator.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.api.model.common.Decision;
import se.sundsvall.lifecareintegrator.api.model.common.DecisionsResponse;
import se.sundsvall.lifecareintegrator.api.model.common.SourceStatus;
import se.sundsvall.lifecareintegrator.service.DecisionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class DecisionResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String PATH = "/{municipalityId}/decisions";
	private static final String BY_ID_PATH = "/{municipalityId}/decisions/{decisionId}";
	private static final String DECISION_ID = "12345";

	@MockitoBean
	private DecisionService decisionServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getDecisions() {
		// Mock
		final var response = DecisionsResponse.create()
			.withDecisions(List.of(Decision.create().withSource("ELDERLY_CARE").withLaw("SOL").withDecisionId("1")))
			.withSources(List.of(SourceStatus.create().withSource("ELDERLY_CARE").withLaw("SOL").withStatus("OK")));
		when(decisionServiceMock.getDecisions(MUNICIPALITY_ID, PARTY_ID, null, null)).thenReturn(response);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(PATH).queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(DecisionsResponse.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(response);
		verify(decisionServiceMock).getDecisions(MUNICIPALITY_ID, PARTY_ID, null, null);
	}

	@Test
	void getDecisionsWithDateWindow() {
		// Parameter values
		final var from = LocalDate.parse("2026-01-01");
		final var to = LocalDate.parse("2026-06-30");

		// Mock
		final var response = DecisionsResponse.create().withDecisions(List.of()).withSources(List.of());
		when(decisionServiceMock.getDecisions(MUNICIPALITY_ID, PARTY_ID, from, to)).thenReturn(response);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(PATH)
				.queryParam("partyId", PARTY_ID)
				.queryParam("from", from.toString())
				.queryParam("to", to.toString())
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(DecisionsResponse.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(response);
		verify(decisionServiceMock).getDecisions(MUNICIPALITY_ID, PARTY_ID, from, to);
	}

	@Test
	void getDecisionFromElderlyCare() {
		// Mock
		final var decision = Decision.create().withSource("ELDERLY_CARE").withLaw("SOL").withDecisionId(DECISION_ID);
		when(decisionServiceMock.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "ELDERLY_CARE", "SOL")).thenReturn(decision);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(BY_ID_PATH)
				.queryParam("partyId", PARTY_ID)
				.queryParam("source", "ELDERLY_CARE")
				.queryParam("law", "SOL")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "decisionId", DECISION_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Decision.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(decision);
		verify(decisionServiceMock).getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "ELDERLY_CARE", "SOL");
	}

	@Test
	void getDecisionFromFamilyCare() {
		// Mock
		final var decision = Decision.create().withSource("FAMILY_CARE").withDecisionId(DECISION_ID);
		when(decisionServiceMock.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "FAMILY_CARE", null)).thenReturn(decision);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(BY_ID_PATH)
				.queryParam("partyId", PARTY_ID)
				.queryParam("source", "FAMILY_CARE")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "decisionId", DECISION_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Decision.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(decision);
		verify(decisionServiceMock).getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "FAMILY_CARE", null);
	}
}
