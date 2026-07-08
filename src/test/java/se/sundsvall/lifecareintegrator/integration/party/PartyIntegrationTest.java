package se.sundsvall.lifecareintegrator.integration.party;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class PartyIntegrationTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String OTHER_PARTY_ID = "b3a1b9c2-5798-11e9-ae24-57fa13b361e2";
	private static final String PERSON_NUMBER = "199001011234";
	private static final String OTHER_PERSON_NUMBER = "199212312345";

	@Mock
	private PartyClient partyClientMock;

	@InjectMocks
	private PartyIntegration partyIntegration;

	@Test
	void getPersonNumber() {
		// Mock
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID))).thenReturn(Map.of(PARTY_ID, PERSON_NUMBER));

		// Act
		final var result = partyIntegration.getPersonNumber(MUNICIPALITY_ID, PARTY_ID);

		// Verify
		assertThat(result).isEqualTo(PERSON_NUMBER);
		verify(partyClientMock).getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID));
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getPersonNumberNotFound() {
		// Mock
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID))).thenReturn(Map.of());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> partyIntegration.getPersonNumber(MUNICIPALITY_ID, PARTY_ID));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(exception.getDetail()).isEqualTo("No person number found for partyId '%s'".formatted(PARTY_ID));
	}

	@Test
	void getPersonNumberNullResponse() {
		// Mock
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID))).thenReturn(null);

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> partyIntegration.getPersonNumber(MUNICIPALITY_ID, PARTY_ID));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
	}

	@Test
	void getPersonNumbers() {
		// Parameter values
		final var partyIds = List.of(PARTY_ID, OTHER_PARTY_ID);

		// Mock
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, partyIds)).thenReturn(Map.of(
			PARTY_ID, PERSON_NUMBER,
			OTHER_PARTY_ID, OTHER_PERSON_NUMBER));

		// Act
		final var result = partyIntegration.getPersonNumbers(MUNICIPALITY_ID, partyIds);

		// Verify
		assertThat(result).containsOnly(
			Map.entry(PARTY_ID, PERSON_NUMBER),
			Map.entry(OTHER_PARTY_ID, OTHER_PERSON_NUMBER));
		verify(partyClientMock).getPersonNumbers(MUNICIPALITY_ID, partyIds);
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getPersonNumbersWithMissingResolutions() {
		// Parameter values
		final var partyIds = List.of(PARTY_ID, OTHER_PARTY_ID);

		// Mock
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, partyIds)).thenReturn(Map.of(PARTY_ID, PERSON_NUMBER));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> partyIntegration.getPersonNumbers(MUNICIPALITY_ID, partyIds));

		// Verify: only the unresolved partyId is named, not the whole batch
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(exception.getDetail()).isEqualTo("No person number found for partyIds %s".formatted(List.of(OTHER_PARTY_ID)));
	}

	@Test
	void getPartyIds() {
		// Parameter values
		final var personNumbers = List.of(PERSON_NUMBER, OTHER_PERSON_NUMBER);

		// Mock: lenient resolution — one of two resolves
		when(partyClientMock.getPartyIds(MUNICIPALITY_ID, personNumbers)).thenReturn(Map.of(PERSON_NUMBER, PARTY_ID));

		// Act
		final var result = partyIntegration.getPartyIds(MUNICIPALITY_ID, personNumbers);

		// Verify: no exception for the unresolved person number
		assertThat(result).containsOnly(Map.entry(PERSON_NUMBER, PARTY_ID));
		verify(partyClientMock).getPartyIds(MUNICIPALITY_ID, personNumbers);
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getPartyIdsWithEmptyInput() {
		// Act
		final var result = partyIntegration.getPartyIds(MUNICIPALITY_ID, List.of());

		// Verify
		assertThat(result).isEmpty();
		verifyNoInteractions(partyClientMock);
	}

	@Test
	void getPartyIdsWithNullResponse() {
		// Mock
		when(partyClientMock.getPartyIds(MUNICIPALITY_ID, List.of(PERSON_NUMBER))).thenReturn(null);

		// Act
		final var result = partyIntegration.getPartyIds(MUNICIPALITY_ID, List.of(PERSON_NUMBER));

		// Verify
		assertThat(result).isEmpty();
	}

	@Test
	void getPersonNumbersNullResponse() {
		// Mock
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID))).thenReturn(null);

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> partyIntegration.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID)));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(exception.getDetail()).isEqualTo("No person number found for partyIds %s".formatted(List.of(PARTY_ID)));
	}
}
