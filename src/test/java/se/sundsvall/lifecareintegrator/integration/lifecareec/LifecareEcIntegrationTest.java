package se.sundsvall.lifecareintegrator.integration.lifecareec;

import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1LssDecision;
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
class LifecareEcIntegrationTest {

	private static final String PERSON_NUMBER = "199001011234";
	private static final String EXPECTED_QUERY = "PersonId:" + PERSON_NUMBER;
	private static final int EXPECTED_LIMIT = 1000;

	@Mock
	private LifecareEcClient lifecareEcClientMock;

	@InjectMocks
	private LifecareEcIntegration lifecareEcIntegration;

	@Test
	void getSolDecisions() {
		// Parameter values
		final var decision = new WEECIntegrationContractsDecisionV1Decision().id(123);

		// Mock
		when(lifecareEcClientMock.getSolDecisions(EXPECTED_QUERY, EXPECTED_LIMIT)).thenReturn(List.of(decision));

		// Act
		final var result = lifecareEcIntegration.getSolDecisions(PERSON_NUMBER);

		// Verify
		assertThat(result).containsExactly(decision);
		verify(lifecareEcClientMock).getSolDecisions(EXPECTED_QUERY, EXPECTED_LIMIT);
		verifyNoMoreInteractions(lifecareEcClientMock);
	}

	@Test
	void getSolDecisionsWithNullResponse() {
		// Mock
		when(lifecareEcClientMock.getSolDecisions(EXPECTED_QUERY, EXPECTED_LIMIT)).thenReturn(null);

		// Act
		final var result = lifecareEcIntegration.getSolDecisions(PERSON_NUMBER);

		// Verify
		assertThat(result).isEmpty();
	}

	@Test
	void getLssDecisions() {
		// Parameter values
		final var decision = new WEECIntegrationContractsDecisionV1LssDecision().id(456);

		// Mock
		when(lifecareEcClientMock.getLssDecisions(EXPECTED_QUERY, EXPECTED_LIMIT)).thenReturn(List.of(decision));

		// Act
		final var result = lifecareEcIntegration.getLssDecisions(PERSON_NUMBER);

		// Verify
		assertThat(result).containsExactly(decision);
		verify(lifecareEcClientMock).getLssDecisions(EXPECTED_QUERY, EXPECTED_LIMIT);
		verifyNoMoreInteractions(lifecareEcClientMock);
	}

	@Test
	void getLssDecisionsWithNullResponse() {
		// Mock
		when(lifecareEcClientMock.getLssDecisions(EXPECTED_QUERY, EXPECTED_LIMIT)).thenReturn(null);

		// Act
		final var result = lifecareEcIntegration.getLssDecisions(PERSON_NUMBER);

		// Verify
		assertThat(result).isEmpty();
	}
}
