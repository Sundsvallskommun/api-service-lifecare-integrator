package se.sundsvall.lifecareintegrator.integration.party;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import se.sundsvall.dept44.problem.Problem;

import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;

/**
 * Wrapper around {@link PartyClient} that resolves partyId(s) to person numbers. The person number is an internal
 * concern only — it is forwarded to the Lifecare APIs but never exposed in this service's public API.
 */
@Component
public class PartyIntegration {

	static final String PERSON_NUMBER_NOT_FOUND = "No person number found for partyId '%s'";
	static final String PERSON_NUMBERS_NOT_FOUND = "No person number found for partyIds %s";

	private final PartyClient partyClient;

	public PartyIntegration(final PartyClient partyClient) {
		this.partyClient = partyClient;
	}

	/**
	 * Resolve a single partyId to a person number.
	 *
	 * @param  municipalityId the municipality id
	 * @param  partyId        the partyId to resolve
	 * @return                the person number (throws NOT_FOUND problem when the partyId cannot be resolved)
	 */
	public String getPersonNumber(final String municipalityId, final String partyId) {
		return Optional.ofNullable(partyClient.getPersonNumbers(municipalityId, List.of(partyId)))
			.map(result -> result.get(partyId))
			.filter(StringUtils::hasText)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, PERSON_NUMBER_NOT_FOUND.formatted(partyId)));
	}

	/**
	 * Resolve a batch of partyIds to person numbers. All partyIds must resolve — the thrown problem names the specific
	 * partyIds that could not be resolved.
	 *
	 * @param  municipalityId the municipality id
	 * @param  partyIds       the partyIds to resolve
	 * @return                map of partyId to person number
	 */
	public Map<String, String> getPersonNumbers(final String municipalityId, final List<String> partyIds) {
		if (partyIds.isEmpty()) {
			return emptyMap();
		}
		final var result = Optional.ofNullable(partyClient.getPersonNumbers(municipalityId, partyIds))
			.orElse(emptyMap());

		final var missing = partyIds.stream()
			.filter(partyId -> !hasText(result.get(partyId)))
			.toList();

		if (!missing.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, PERSON_NUMBERS_NOT_FOUND.formatted(missing));
		}
		return result;
	}

	/**
	 * Resolve a batch of person numbers to partyIds. Lenient (best effort) — person numbers that cannot be resolved are
	 * simply absent from the returned map. Used to swap person numbers for partyIds in responses, where an unresolvable
	 * person should not fail the whole request.
	 *
	 * @param  municipalityId the municipality id
	 * @param  personNumbers  the person numbers to resolve
	 * @return                map of person number to partyId, containing only the resolvable entries
	 */
	public Map<String, String> getPartyIds(final String municipalityId, final List<String> personNumbers) {
		if (personNumbers.isEmpty()) {
			return emptyMap();
		}
		return Optional.ofNullable(partyClient.getPartyIds(municipalityId, personNumbers))
			.orElse(emptyMap());
	}
}
