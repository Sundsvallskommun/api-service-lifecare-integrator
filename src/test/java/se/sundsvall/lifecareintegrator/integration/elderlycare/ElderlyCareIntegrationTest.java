package se.sundsvall.lifecareintegrator.integration.elderlycare;

import generated.se.sundsvall.elderlycare.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.elderlycare.WEECIntegrationContractsDecisionV1LssDecision;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElderlyCareIntegrationTest {

	private static final String PERSON_NUMBER = "199001011234";
	private static final String EXPECTED_QUERY = "PersonId='" + PERSON_NUMBER + "'";
	private static final int EXPECTED_LIMIT = 1000;

	@Mock
	private ElderlyCareClient elderlyCareClientMock;

	@InjectMocks
	private ElderlyCareIntegration elderlyCareIntegration;

	@Test
	void getSolDecisions() {
		// Parameter values
		final var decision = new WEECIntegrationContractsDecisionV1Decision().id(123);

		// Mock
		when(elderlyCareClientMock.getSolDecisions(EXPECTED_QUERY, EXPECTED_LIMIT)).thenReturn(List.of(decision));

		// Act
		final var result = elderlyCareIntegration.getSolDecisions(PERSON_NUMBER);

		// Verify
		assertThat(result).containsExactly(decision);
		verify(elderlyCareClientMock).getSolDecisions(EXPECTED_QUERY, EXPECTED_LIMIT);
		verifyNoMoreInteractions(elderlyCareClientMock);
	}

	@Test
	void getSolDecisionsWithNullResponse() {
		// Mock
		when(elderlyCareClientMock.getSolDecisions(EXPECTED_QUERY, EXPECTED_LIMIT)).thenReturn(null);

		// Act
		final var result = elderlyCareIntegration.getSolDecisions(PERSON_NUMBER);

		// Verify
		assertThat(result).isEmpty();
	}

	@Test
	void getLssDecisions() {
		// Parameter values
		final var decision = new WEECIntegrationContractsDecisionV1LssDecision().id(456);

		// Mock
		when(elderlyCareClientMock.getLssDecisions(EXPECTED_QUERY, EXPECTED_LIMIT)).thenReturn(List.of(decision));

		// Act
		final var result = elderlyCareIntegration.getLssDecisions(PERSON_NUMBER);

		// Verify
		assertThat(result).containsExactly(decision);
		verify(elderlyCareClientMock).getLssDecisions(EXPECTED_QUERY, EXPECTED_LIMIT);
		verifyNoMoreInteractions(elderlyCareClientMock);
	}

	@Test
	void getLssDecisionsWithNullResponse() {
		// Mock
		when(elderlyCareClientMock.getLssDecisions(EXPECTED_QUERY, EXPECTED_LIMIT)).thenReturn(null);

		// Act
		final var result = elderlyCareIntegration.getLssDecisions(PERSON_NUMBER);

		// Verify
		assertThat(result).isEmpty();
	}

	@Test
	void getSolDecision() {
		// Parameter values
		final var decision = new WEECIntegrationContractsDecisionV1Decision().id(123);

		// Mock
		when(elderlyCareClientMock.getSolDecision("123")).thenReturn(decision);

		// Act
		final var result = elderlyCareIntegration.getSolDecision("123");

		// Verify
		assertThat(result).contains(decision);
		verify(elderlyCareClientMock).getSolDecision("123");
		verifyNoMoreInteractions(elderlyCareClientMock);
	}

	@Test
	void getSolDecisionWithNullResponse() {
		// Mock
		when(elderlyCareClientMock.getSolDecision("123")).thenReturn(null);

		// Act
		final var result = elderlyCareIntegration.getSolDecision("123");

		// Verify
		assertThat(result).isEmpty();
	}

	@Test
	void getLssDecision() {
		// Parameter values
		final var decision = new WEECIntegrationContractsDecisionV1LssDecision().id(456);

		// Mock
		when(elderlyCareClientMock.getLssDecision("456")).thenReturn(decision);

		// Act
		final var result = elderlyCareIntegration.getLssDecision("456");

		// Verify
		assertThat(result).contains(decision);
		verify(elderlyCareClientMock).getLssDecision("456");
		verifyNoMoreInteractions(elderlyCareClientMock);
	}

	@Test
	void getLssDecisionWithNullResponse() {
		// Mock
		when(elderlyCareClientMock.getLssDecision("456")).thenReturn(null);

		// Act
		final var result = elderlyCareIntegration.getLssDecision("456");

		// Verify
		assertThat(result).isEmpty();
	}
}
