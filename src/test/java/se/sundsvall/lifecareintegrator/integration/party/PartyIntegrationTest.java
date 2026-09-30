package se.sundsvall.lifecareintegrator.integration.party;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.party.configuration.PartyProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
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

	private static final String OTHER_MUNICIPALITY_ID = "2260";
	private static final Duration TIME_TO_LIVE = Duration.ofMinutes(15);

	@Mock
	private PartyClient partyClientMock;

	private final AtomicLong nanos = new AtomicLong();

	private PartyIntegration partyIntegration;

	@BeforeEach
	void setUp() {
		partyIntegration = integration(1000, 1000);
	}

	private PartyIntegration integration(final int maxPartyIdsPerCall, final int maxLegalIdsPerCall) {
		final var properties = new PartyProperties(5, 30, maxPartyIdsPerCall, maxLegalIdsPerCall, TIME_TO_LIVE, 100);
		return new PartyIntegration(partyClientMock, properties, nanos::get);
	}

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

	// ---- Cache -------------------------------------------------------------------------------------------------------

	@Test
	void aRepeatedLookupIsServedFromTheCache() {
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID))).thenReturn(Map.of(PARTY_ID, PERSON_NUMBER));

		assertThat(partyIntegration.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).isEqualTo(PERSON_NUMBER);
		assertThat(partyIntegration.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).isEqualTo(PERSON_NUMBER);
		assertThat(partyIntegration.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID))).containsOnly(Map.entry(PARTY_ID, PERSON_NUMBER));

		verify(partyClientMock).getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID));
		verifyNoMoreInteractions(partyClientMock);
	}

	/** A forward answer also answers the reverse question: a read's own applicant needs no second lookup. */
	@Test
	void aForwardLookupAnswersTheReverseToo() {
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID))).thenReturn(Map.of(PARTY_ID, PERSON_NUMBER));

		partyIntegration.getPersonNumber(MUNICIPALITY_ID, PARTY_ID);

		assertThat(partyIntegration.getPartyIds(MUNICIPALITY_ID, List.of(PERSON_NUMBER))).containsOnly(Map.entry(PERSON_NUMBER, PARTY_ID));
		verify(partyClientMock).getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID));
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void aReverseLookupAnswersTheForwardToo() {
		when(partyClientMock.getPartyIds(MUNICIPALITY_ID, List.of(PERSON_NUMBER))).thenReturn(Map.of(PERSON_NUMBER, PARTY_ID));

		partyIntegration.getPartyIds(MUNICIPALITY_ID, List.of(PERSON_NUMBER));

		assertThat(partyIntegration.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).isEqualTo(PERSON_NUMBER);
		verify(partyClientMock).getPartyIds(MUNICIPALITY_ID, List.of(PERSON_NUMBER));
		verifyNoMoreInteractions(partyClientMock);
	}

	/** Only the misses go to Party, in one batch. */
	@Test
	void onlyTheMissesAreFetched() {
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID))).thenReturn(Map.of(PARTY_ID, PERSON_NUMBER));
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, List.of(OTHER_PARTY_ID))).thenReturn(Map.of(OTHER_PARTY_ID, OTHER_PERSON_NUMBER));
		partyIntegration.getPersonNumber(MUNICIPALITY_ID, PARTY_ID);

		final var result = partyIntegration.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID, OTHER_PARTY_ID));

		assertThat(result).containsOnly(Map.entry(PARTY_ID, PERSON_NUMBER), Map.entry(OTHER_PARTY_ID, OTHER_PERSON_NUMBER));
		verify(partyClientMock).getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID));
		verify(partyClientMock).getPersonNumbers(MUNICIPALITY_ID, List.of(OTHER_PARTY_ID));
		verifyNoMoreInteractions(partyClientMock);
	}

	/** A person Party does not know yet is asked for again rather than remembered as unknown. */
	@Test
	void anUnansweredLookupIsNotCached() {
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID))).thenReturn(Map.of()).thenReturn(Map.of(PARTY_ID, PERSON_NUMBER));

		assertThrows(ThrowableProblem.class, () -> partyIntegration.getPersonNumber(MUNICIPALITY_ID, PARTY_ID));
		assertThat(partyIntegration.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).isEqualTo(PERSON_NUMBER);

		verify(partyClientMock, times(2)).getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID));
	}

	@Test
	void anEntryExpiresAfterItsTimeToLive() {
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID))).thenReturn(Map.of(PARTY_ID, PERSON_NUMBER));
		partyIntegration.getPersonNumber(MUNICIPALITY_ID, PARTY_ID);

		nanos.addAndGet(TIME_TO_LIVE.plusSeconds(1).toNanos());
		partyIntegration.getPersonNumber(MUNICIPALITY_ID, PARTY_ID);

		verify(partyClientMock, times(2)).getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID));
	}

	@Test
	void theCacheIsPerMunicipality() {
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID))).thenReturn(Map.of(PARTY_ID, PERSON_NUMBER));
		when(partyClientMock.getPersonNumbers(OTHER_MUNICIPALITY_ID, List.of(PARTY_ID))).thenReturn(Map.of(PARTY_ID, PERSON_NUMBER));

		partyIntegration.getPersonNumber(MUNICIPALITY_ID, PARTY_ID);
		partyIntegration.getPersonNumber(OTHER_MUNICIPALITY_ID, PARTY_ID);

		verify(partyClientMock).getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID));
		verify(partyClientMock).getPersonNumbers(OTHER_MUNICIPALITY_ID, List.of(PARTY_ID));
	}

	@Test
	void missesAreFetchedInBatchesOfTheConfiguredSize() {
		final var integration = integration(1, 1);
		when(partyClientMock.getPartyIds(MUNICIPALITY_ID, List.of(PERSON_NUMBER))).thenReturn(Map.of(PERSON_NUMBER, PARTY_ID));
		when(partyClientMock.getPartyIds(MUNICIPALITY_ID, List.of(OTHER_PERSON_NUMBER))).thenReturn(Map.of(OTHER_PERSON_NUMBER, OTHER_PARTY_ID));

		final var result = integration.getPartyIds(MUNICIPALITY_ID, List.of(PERSON_NUMBER, OTHER_PERSON_NUMBER));

		assertThat(result).containsOnly(Map.entry(PERSON_NUMBER, PARTY_ID), Map.entry(OTHER_PERSON_NUMBER, OTHER_PARTY_ID));
		verify(partyClientMock).getPartyIds(MUNICIPALITY_ID, List.of(PERSON_NUMBER));
		verify(partyClientMock).getPartyIds(MUNICIPALITY_ID, List.of(OTHER_PERSON_NUMBER));
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void duplicatesInABatchAreAskedForOnce() {
		when(partyClientMock.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID))).thenReturn(Map.of(PARTY_ID, PERSON_NUMBER));

		assertThat(partyIntegration.getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID, PARTY_ID))).containsOnly(Map.entry(PARTY_ID, PERSON_NUMBER));
		verify(partyClientMock).getPersonNumbers(MUNICIPALITY_ID, List.of(PARTY_ID));
	}
}
