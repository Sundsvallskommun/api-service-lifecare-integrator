package se.sundsvall.lifecareintegrator.service;

import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1LssDecision;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionDTO;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.api.model.common.Decision;
import se.sundsvall.lifecareintegrator.api.model.common.SourceStatus;
import se.sundsvall.lifecareintegrator.integration.lifecareec.LifecareEcIntegration;
import se.sundsvall.lifecareintegrator.integration.lifecarefc.LifecareFcIntegration;
import se.sundsvall.lifecareintegrator.integration.party.PartyIntegration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String PERSON_NUMBER = "199001011234";

	@Mock
	private PartyIntegration partyIntegrationMock;

	@Mock
	private LifecareEcIntegration lifecareEcIntegrationMock;

	@Mock
	private LifecareFcIntegration lifecareFcIntegrationMock;

	@InjectMocks
	private DecisionService decisionService;

	@Test
	void getDecisions() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getSolDecisions(PERSON_NUMBER)).thenReturn(List.of(
			new WEECIntegrationContractsDecisionV1Decision().id(1).date(OffsetDateTime.parse("2026-01-01T00:00:00Z"))));
		when(lifecareEcIntegrationMock.getLssDecisions(PERSON_NUMBER)).thenReturn(List.of(
			new WEECIntegrationContractsDecisionV1LssDecision().id(2).date(OffsetDateTime.parse("2026-03-01T00:00:00Z"))));
		when(lifecareFcIntegrationMock.getAllDecisions(any(), any(), any())).thenReturn(List.of(
			new PersonBasedDecisionDTO().id(3).date("2026-02-01")));

		// Act
		final var result = decisionService.getDecisions(MUNICIPALITY_ID, PARTY_ID, null, null);

		// Verify: merged and sorted by decision date, most recent first
		assertThat(result.getDecisions())
			.extracting(Decision::getSource, Decision::getLaw, Decision::getDecisionId)
			.containsExactly(
				tuple("ELDERLY_CARE", "LSS", "2"),
				tuple("FAMILY_CARE", null, "3"),
				tuple("ELDERLY_CARE", "SOL", "1"));
		assertThat(result.getSources())
			.extracting(SourceStatus::getSource, SourceStatus::getLaw, SourceStatus::getStatus)
			.containsExactly(
				tuple("ELDERLY_CARE", "SOL", "OK"),
				tuple("ELDERLY_CARE", "LSS", "OK"),
				tuple("FAMILY_CARE", null, "OK"));

		// Default FC window: 10 years back until today
		final var today = LocalDate.now(ZoneId.of("Europe/Stockholm"));
		verify(lifecareFcIntegrationMock).getAllDecisions(PERSON_NUMBER, today.minusYears(10), today);
	}

	@Test
	void getDecisionsWithWindowFiltersElderlyCareOnValidityOverlap() {
		// Parameter values
		final var from = LocalDate.parse("2026-01-01");
		final var to = LocalDate.parse("2026-06-30");

		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getSolDecisions(PERSON_NUMBER)).thenReturn(List.of(
			// Ends before the window — filtered out
			new WEECIntegrationContractsDecisionV1Decision().id(1)
				.fromDate(OffsetDateTime.parse("2020-01-01T00:00:00Z"))
				.toDate(OffsetDateTime.parse("2020-12-31T00:00:00Z")),
			// Overlaps the window — kept
			new WEECIntegrationContractsDecisionV1Decision().id(2)
				.fromDate(OffsetDateTime.parse("2025-06-01T00:00:00Z"))
				.toDate(OffsetDateTime.parse("2026-01-15T00:00:00Z")),
			// Open-ended — kept
			new WEECIntegrationContractsDecisionV1Decision().id(3)
				.fromDate(OffsetDateTime.parse("2026-02-01T00:00:00Z")),
			// Starts after the window — filtered out
			new WEECIntegrationContractsDecisionV1Decision().id(4)
				.fromDate(OffsetDateTime.parse("2026-08-01T00:00:00Z"))));
		when(lifecareEcIntegrationMock.getLssDecisions(PERSON_NUMBER)).thenReturn(List.of());
		when(lifecareFcIntegrationMock.getAllDecisions(PERSON_NUMBER, from, to)).thenReturn(List.of());

		// Act
		final var result = decisionService.getDecisions(MUNICIPALITY_ID, PARTY_ID, from, to);

		// Verify
		assertThat(result.getDecisions())
			.extracting(Decision::getDecisionId)
			.containsExactlyInAnyOrder("2", "3");
		verify(lifecareFcIntegrationMock).getAllDecisions(PERSON_NUMBER, from, to);
	}

	@Test
	void getDecisionsWithFailingSourceReturnsPartialResult() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getSolDecisions(PERSON_NUMBER)).thenReturn(List.of(
			new WEECIntegrationContractsDecisionV1Decision().id(1)));
		when(lifecareEcIntegrationMock.getLssDecisions(PERSON_NUMBER)).thenReturn(List.of());
		when(lifecareFcIntegrationMock.getAllDecisions(any(), any(), any())).thenThrow(new RuntimeException("FC is down"));

		// Act
		final var result = decisionService.getDecisions(MUNICIPALITY_ID, PARTY_ID, null, null);

		// Verify: the reachable sources are returned, the failing one is flagged
		assertThat(result.getDecisions())
			.extracting(Decision::getDecisionId)
			.containsExactly("1");
		assertThat(result.getSources())
			.extracting(SourceStatus::getSource, SourceStatus::getLaw, SourceStatus::getStatus)
			.containsExactly(
				tuple("ELDERLY_CARE", "SOL", "OK"),
				tuple("ELDERLY_CARE", "LSS", "OK"),
				tuple("FAMILY_CARE", null, "UNAVAILABLE"));
	}

	@Test
	void getDecisionsWithAllSourcesFailing() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getSolDecisions(PERSON_NUMBER)).thenThrow(new RuntimeException("EC is down"));
		when(lifecareEcIntegrationMock.getLssDecisions(PERSON_NUMBER)).thenThrow(new RuntimeException("EC is down"));
		when(lifecareFcIntegrationMock.getAllDecisions(any(), any(), any())).thenThrow(new RuntimeException("FC is down"));

		// Act
		final var result = decisionService.getDecisions(MUNICIPALITY_ID, PARTY_ID, null, null);

		// Verify: still a 200-shaped response, all sources flagged
		assertThat(result.getDecisions()).isEmpty();
		assertThat(result.getSources())
			.extracting(SourceStatus::getStatus)
			.containsOnly("UNAVAILABLE");
	}

	@Test
	void getDecisionsWithUnknownPartyId() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenThrow(Problem.valueOf(NOT_FOUND, "No person number found"));

		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> decisionService.getDecisions(MUNICIPALITY_ID, PARTY_ID, null, null));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
		verifyNoInteractions(lifecareEcIntegrationMock, lifecareFcIntegrationMock);
	}

	@Test
	void getDecisionsWithInvertedDateWindow() {
		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> decisionService.getDecisions(MUNICIPALITY_ID, PARTY_ID, LocalDate.parse("2026-06-30"), LocalDate.parse("2026-01-01")));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(exception.getDetail()).isEqualTo("'from' must be on or before 'to'");
		verifyNoInteractions(partyIntegrationMock, lifecareEcIntegrationMock, lifecareFcIntegrationMock);
	}

	@Test
	void getDecisionsSortsNullDecisionDatesLast() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getSolDecisions(PERSON_NUMBER)).thenReturn(List.of(
			new WEECIntegrationContractsDecisionV1Decision().id(1)));
		when(lifecareEcIntegrationMock.getLssDecisions(PERSON_NUMBER)).thenReturn(List.of());
		when(lifecareFcIntegrationMock.getAllDecisions(any(), any(), any())).thenReturn(List.of(
			new PersonBasedDecisionDTO().id(2).date("2026-02-01")));

		// Act
		final var result = decisionService.getDecisions(MUNICIPALITY_ID, PARTY_ID, null, null);

		// Verify
		assertThat(result.getDecisions())
			.extracting(Decision::getDecisionId)
			.containsExactly("2", "1");
	}
}
