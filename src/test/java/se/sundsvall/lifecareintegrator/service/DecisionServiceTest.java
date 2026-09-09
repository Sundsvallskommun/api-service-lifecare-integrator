package se.sundsvall.lifecareintegrator.service;

import generated.se.sundsvall.lifecareec.WEECIntegrationContractsCommonV1Caseworker;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1LssDecision;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.api.model.common.Decision;
import se.sundsvall.lifecareintegrator.api.model.common.SourceStatus;
import se.sundsvall.lifecareintegrator.integration.employee.EmployeeIntegration;
import se.sundsvall.lifecareintegrator.integration.lifecareec.LifecareEcIntegration;
import se.sundsvall.lifecareintegrator.integration.lifecarefc.LifecareFcIntegration;
import se.sundsvall.lifecareintegrator.integration.party.PartyIntegration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String PERSON_NUMBER = "199001011234";
	private static final String OTHER_PERSON_NUMBER = "198001011234";
	private static final String DECISION_ID = "1001";

	@Mock
	private PartyIntegration partyIntegrationMock;

	@Mock
	private LifecareEcIntegration lifecareEcIntegrationMock;

	@Mock
	private LifecareFcIntegration lifecareFcIntegrationMock;

	@Mock
	private EmployeeIntegration employeeIntegrationMock;

	@InjectMocks
	private DecisionService decisionService;

	@Test
	void getDecisions() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getSolDecisions(PERSON_NUMBER)).thenReturn(List.of(
			new WEECIntegrationContractsDecisionV1Decision().id(1).date(LocalDateTime.parse("2026-01-01T00:00:00"))));
		when(lifecareEcIntegrationMock.getLssDecisions(PERSON_NUMBER)).thenReturn(List.of(
			new WEECIntegrationContractsDecisionV1LssDecision().id(2).date(LocalDateTime.parse("2026-03-01T00:00:00"))));
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
				.fromDate(LocalDateTime.parse("2020-01-01T00:00:00"))
				.toDate(LocalDateTime.parse("2020-12-31T00:00:00")),
			// Overlaps the window — kept
			new WEECIntegrationContractsDecisionV1Decision().id(2)
				.fromDate(LocalDateTime.parse("2025-06-01T00:00:00"))
				.toDate(LocalDateTime.parse("2026-01-15T00:00:00")),
			// Open-ended — kept
			new WEECIntegrationContractsDecisionV1Decision().id(3)
				.fromDate(LocalDateTime.parse("2026-02-01T00:00:00")),
			// Starts after the window — filtered out
			new WEECIntegrationContractsDecisionV1Decision().id(4)
				.fromDate(LocalDateTime.parse("2026-08-01T00:00:00"))));
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

	@Test
	void aCaseworkerWithoutANameIsResolvedOnceAndReusedAcrossDecisions() {
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getSolDecisions(PERSON_NUMBER)).thenReturn(List.of());
		when(lifecareEcIntegrationMock.getLssDecisions(PERSON_NUMBER)).thenReturn(List.of(
			lssDecisionWithBlankSfbCaseworker(1), lssDecisionWithBlankSfbCaseworker(2)));
		when(lifecareFcIntegrationMock.getAllDecisions(any(), any(), any())).thenReturn(List.of());
		when(employeeIntegrationMock.getFullName(MUNICIPALITY_ID, "LOHE")).thenReturn(Optional.of("Lotta Helsinger"));

		final var response = decisionService.getDecisions(MUNICIPALITY_ID, PARTY_ID, null, null);

		assertThat(response.getDecisions())
			.extracting(decision -> decision.getElderlyCareDetails().getSfbCaseworker())
			.containsExactly("Lotta Helsinger", "Lotta Helsinger");

		// Two decisions, one lookup: Lifecare repeats the same caseworker across a person's decisions.
		verify(employeeIntegrationMock, times(1)).getFullName(MUNICIPALITY_ID, "LOHE");
	}

	@Test
	void anUnresolvableCaseworkerLeavesTheNameOutRatherThanBlank() {
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getSolDecisions(PERSON_NUMBER)).thenReturn(List.of());
		when(lifecareEcIntegrationMock.getLssDecisions(PERSON_NUMBER)).thenReturn(List.of(lssDecisionWithBlankSfbCaseworker(1)));
		when(lifecareFcIntegrationMock.getAllDecisions(any(), any(), any())).thenReturn(List.of());
		when(employeeIntegrationMock.getFullName(MUNICIPALITY_ID, "LOHE")).thenReturn(Optional.empty());

		final var response = decisionService.getDecisions(MUNICIPALITY_ID, PARTY_ID, null, null);

		assertThat(response.getDecisions()).singleElement()
			.satisfies(decision -> assertThat(decision.getElderlyCareDetails().getSfbCaseworker()).isNull());
		// The source stays OK — a missing caseworker name is not a failed decision read.
		assertThat(response.getSources()).extracting(SourceStatus::getStatus).contains("OK");
	}

	@Test
	void theLawComesFromTheDecisionRatherThanTheEndpoint() {
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getSolDecisions(PERSON_NUMBER)).thenReturn(List.of());
		when(lifecareEcIntegrationMock.getLssDecisions(PERSON_NUMBER)).thenReturn(List.of(
			new WEECIntegrationContractsDecisionV1LssDecision().id(1).law(3),
			new WEECIntegrationContractsDecisionV1LssDecision().id(2).law(7)));
		when(lifecareFcIntegrationMock.getAllDecisions(any(), any(), any())).thenReturn(List.of());

		final var response = decisionService.getDecisions(MUNICIPALITY_ID, PARTY_ID, null, null);

		assertThat(response.getDecisions()).extracting(Decision::getDecisionId, Decision::getLaw)
			.containsExactlyInAnyOrder(tuple("1", "LSS"), tuple("2", "SFB"));

		// The source still reports the fetch it was: SFB decisions arrive through the LSS read.
		assertThat(response.getSources()).extracting(SourceStatus::getLaw).containsExactly("SOL", "LSS", null);
	}

	private static WEECIntegrationContractsDecisionV1LssDecision lssDecisionWithBlankSfbCaseworker(final int id) {
		return new WEECIntegrationContractsDecisionV1LssDecision()
			.id(id)
			.law(7)
			.sfbCaseworker(new WEECIntegrationContractsCommonV1Caseworker().id("LOHE").fullName(""));
	}

	@Test
	void getDecisionFromElderlyCareSol() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getSolDecision(DECISION_ID)).thenReturn(Optional.of(
			new WEECIntegrationContractsDecisionV1Decision().id(1001).personId(PERSON_NUMBER).date(LocalDateTime.parse("2026-01-01T00:00:00"))));

		// Act
		final var result = decisionService.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "ELDERLY_CARE", "SOL");

		// Verify
		assertThat(result.getSource()).isEqualTo("ELDERLY_CARE");
		assertThat(result.getLaw()).isEqualTo("SOL");
		assertThat(result.getDecisionId()).isEqualTo(DECISION_ID);
		assertThat(result.getDecided()).isEqualTo(LocalDate.parse("2026-01-01"));
		verify(lifecareEcIntegrationMock).getSolDecision(DECISION_ID);
		verifyNoMoreInteractions(lifecareEcIntegrationMock);
		verifyNoInteractions(lifecareFcIntegrationMock);
	}

	@Test
	void getDecisionFromElderlyCareLss() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getLssDecision(DECISION_ID)).thenReturn(Optional.of(
			new WEECIntegrationContractsDecisionV1LssDecision().id(1001).personId(PERSON_NUMBER).law(3)));

		// Act
		final var result = decisionService.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "ELDERLY_CARE", "LSS");

		// Verify
		assertThat(result.getSource()).isEqualTo("ELDERLY_CARE");
		assertThat(result.getLaw()).isEqualTo("LSS");
		assertThat(result.getDecisionId()).isEqualTo(DECISION_ID);
		verify(lifecareEcIntegrationMock).getLssDecision(DECISION_ID);
		verifyNoMoreInteractions(lifecareEcIntegrationMock);
		verifyNoInteractions(lifecareFcIntegrationMock);
	}

	@Test
	void getDecisionWithLawSfbIsServedByTheLssSource() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getLssDecision(DECISION_ID)).thenReturn(Optional.of(
			new WEECIntegrationContractsDecisionV1LssDecision().id(1001).personId(PERSON_NUMBER).law(7)));

		// Act
		final var result = decisionService.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "ELDERLY_CARE", "SFB");

		// Verify: the law is the decision's own, as on the list
		assertThat(result.getLaw()).isEqualTo("SFB");
		verify(lifecareEcIntegrationMock).getLssDecision(DECISION_ID);
		verifyNoMoreInteractions(lifecareEcIntegrationMock);
	}

	@Test
	void getDecisionFromElderlyCareResolvesTheCaseworkerName() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getLssDecision(DECISION_ID)).thenReturn(Optional.of(
			lssDecisionWithBlankSfbCaseworker(1001).personId(PERSON_NUMBER)));
		when(employeeIntegrationMock.getFullName(MUNICIPALITY_ID, "LOHE")).thenReturn(Optional.of("Lotta Helsinger"));

		// Act
		final var result = decisionService.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "ELDERLY_CARE", "SFB");

		// Verify
		assertThat(result.getElderlyCareDetails().getSfbCaseworker()).isEqualTo("Lotta Helsinger");
		verify(employeeIntegrationMock).getFullName(MUNICIPALITY_ID, "LOHE");
	}

	@Test
	void getDecisionFromFamilyCare() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareFcIntegrationMock.getAllDecisions(any(), any(), any())).thenReturn(List.of(
			new PersonBasedDecisionDTO().id(1000).date("2026-01-01"),
			new PersonBasedDecisionDTO().id(1001).date("2026-02-01")));

		// Act
		final var result = decisionService.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "FAMILY_CARE", null);

		// Verify: the party's decisions are listed over the default window and the one with the id picked out
		assertThat(result.getSource()).isEqualTo("FAMILY_CARE");
		assertThat(result.getLaw()).isNull();
		assertThat(result.getDecisionId()).isEqualTo(DECISION_ID);
		assertThat(result.getDecided()).isEqualTo(LocalDate.parse("2026-02-01"));
		final var today = LocalDate.now(ZoneId.of("Europe/Stockholm"));
		verify(lifecareFcIntegrationMock).getAllDecisions(PERSON_NUMBER, today.minusYears(10), today);
		verifyNoInteractions(lifecareEcIntegrationMock, employeeIntegrationMock);
	}

	@Test
	void getDecisionFromFamilyCareNotFound() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareFcIntegrationMock.getAllDecisions(any(), any(), any())).thenReturn(List.of(
			new PersonBasedDecisionDTO().id(1000)));

		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> decisionService.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "FAMILY_CARE", null));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(exception.getDetail()).isEqualTo("No decision with id '1001' found in source FAMILY_CARE for partyId '" + PARTY_ID + "'");
	}

	@Test
	void getDecisionFromElderlyCareNotFound() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getSolDecision(DECISION_ID)).thenReturn(Optional.empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> decisionService.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "ELDERLY_CARE", "SOL"));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(exception.getDetail()).isEqualTo("No decision with id '1001' found in source ELDERLY_CARE (SOL) for partyId '" + PARTY_ID + "'");
	}

	@Test
	void getDecisionBelongingToAnotherPersonIsNotFound() {
		// Mock: EC serves any decision by id — one that belongs to someone else must not be published
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getSolDecision(DECISION_ID)).thenReturn(Optional.of(
			new WEECIntegrationContractsDecisionV1Decision().id(1001).personId(OTHER_PERSON_NUMBER)));

		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> decisionService.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "ELDERLY_CARE", "SOL"));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
	}

	@Test
	void getDecisionWithoutPersonOnTheDecisionIsNotFound() {
		// Mock: no PersonId on the decision means ownership cannot be established — never publish on a guess
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareEcIntegrationMock.getLssDecision(DECISION_ID)).thenReturn(Optional.of(
			new WEECIntegrationContractsDecisionV1LssDecision().id(1001)));

		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> decisionService.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "ELDERLY_CARE", "LSS"));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
	}

	@Test
	void getDecisionFromElderlyCareWithoutLaw() {
		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> decisionService.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "ELDERLY_CARE", null));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(exception.getDetail()).isEqualTo("'law' is required when 'source' is ELDERLY_CARE");
		verifyNoInteractions(partyIntegrationMock, lifecareEcIntegrationMock, lifecareFcIntegrationMock);
	}

	@Test
	void getDecisionFromFamilyCareWithLaw() {
		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> decisionService.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "FAMILY_CARE", "SOL"));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(exception.getDetail()).isEqualTo("'law' is not applicable when 'source' is FAMILY_CARE");
		verifyNoInteractions(partyIntegrationMock, lifecareEcIntegrationMock, lifecareFcIntegrationMock);
	}

	@Test
	void getDecisionWithUnknownPartyId() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenThrow(Problem.valueOf(NOT_FOUND, "No person number found"));

		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> decisionService.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "FAMILY_CARE", null));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
		verifyNoInteractions(lifecareEcIntegrationMock, lifecareFcIntegrationMock);
	}

	@Test
	void getDecisionWithFailingSourceFailsTheRequest() {
		// Mock: unlike the list, there is no partial result to fall back on
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareFcIntegrationMock.getAllDecisions(any(), any(), any())).thenThrow(new RuntimeException("FC is down"));

		// Act
		final var exception = assertThrows(RuntimeException.class,
			() -> decisionService.getDecision(MUNICIPALITY_ID, PARTY_ID, DECISION_ID, "FAMILY_CARE", null));

		// Verify
		assertThat(exception.getMessage()).isEqualTo("FC is down");
	}
}
