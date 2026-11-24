package se.sundsvall.lifecareintegrator.integration.party;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.lifecareintegrator.integration.party.configuration.PartyProperties;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;

@Component
public class PartyIntegration {

	static final String PERSON_NUMBER_NOT_FOUND = "No person number found for partyId '%s'";
	static final String PERSON_NUMBERS_NOT_FOUND = "No person number found for partyIds %s";

	private final PartyClient partyClient;
	private final Cache<CacheKey, String> personNumbersByPartyId;
	private final Cache<CacheKey, String> partyIdsByPersonNumber;

	private record CacheKey(String municipalityId, String id) {}

	public PartyIntegration(final PartyClient partyClient, final PartyProperties properties, final Ticker ticker) {
		this.partyClient = partyClient;
		this.personNumbersByPartyId = newCache(properties, ticker);
		this.partyIdsByPersonNumber = newCache(properties, ticker);
	}

	private static Cache<CacheKey, String> newCache(final PartyProperties properties, final Ticker ticker) {
		return Caffeine.newBuilder()
			.expireAfterWrite(properties.cacheTimeToLive())
			.maximumSize(properties.cacheMaximumSize())
			.ticker(ticker)
			.build();
	}

	public String getPersonNumber(final String municipalityId, final String partyId) {
		return Optional.ofNullable(lookUpPersonNumbers(municipalityId, List.of(partyId)).get(partyId))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, PERSON_NUMBER_NOT_FOUND.formatted(partyId)));
	}

	/** Throws NOT_FOUND naming the partyIds Party cannot resolve. */
	public Map<String, String> getPersonNumbers(final String municipalityId, final List<String> partyIds) {
		if (partyIds.isEmpty()) {
			return emptyMap();
		}

		final var result = lookUpPersonNumbers(municipalityId, partyIds);
		final var missing = partyIds.stream()
			.filter(partyId -> !result.containsKey(partyId))
			.toList();

		if (!missing.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, PERSON_NUMBERS_NOT_FOUND.formatted(missing));
		}
		return result;
	}

	/** Leaves out the person numbers Party cannot resolve, so one unknown person does not fail a response. */
	public Map<String, String> getPartyIds(final String municipalityId, final List<String> personNumbers) {
		if (personNumbers.isEmpty()) {
			return emptyMap();
		}
		return lookUpPartyIds(municipalityId, personNumbers);
	}

	private Map<String, String> lookUpPersonNumbers(final String municipalityId, final List<String> partyIds) {
		final var personNumbers = findCached(municipalityId, partyIds, personNumbersByPartyId);
		final var uncachedPartyIds = findUncached(partyIds, personNumbers);
		if (uncachedPartyIds.isEmpty()) {
			return personNumbers;
		}
		final var fetched = dropUnanswered(partyClient.getPersonNumbers(municipalityId, uncachedPartyIds));
		fetched.forEach((partyId, personNumber) -> cachePair(municipalityId, partyId, personNumber));
		personNumbers.putAll(fetched);
		return personNumbers;
	}

	private Map<String, String> lookUpPartyIds(final String municipalityId, final List<String> personNumbers) {
		final var partyIds = findCached(municipalityId, personNumbers, partyIdsByPersonNumber);
		final var uncachedPersonNumbers = findUncached(personNumbers, partyIds);
		if (uncachedPersonNumbers.isEmpty()) {
			return partyIds;
		}
		final var fetched = dropUnanswered(partyClient.getPartyIds(municipalityId, uncachedPersonNumbers));
		fetched.forEach((personNumber, partyId) -> cachePair(municipalityId, partyId, personNumber));
		partyIds.putAll(fetched);
		return partyIds;
	}

	private static Map<String, String> findCached(final String municipalityId, final List<String> ids, final Cache<CacheKey, String> cache) {
		final var cached = new HashMap<String, String>();
		cache.getAllPresent(ids.stream().map(id -> new CacheKey(municipalityId, id)).toList())
			.forEach((key, answer) -> cached.put(key.id(), answer));
		return cached;
	}

	private static List<String> findUncached(final List<String> ids, final Map<String, String> cached) {
		return ids.stream()
			.distinct()
			.filter(id -> !cached.containsKey(id))
			.toList();
	}

	private static Map<String, String> dropUnanswered(final Map<String, String> answers) {
		return Optional.ofNullable(answers).orElse(emptyMap()).entrySet().stream()
			.filter(entry -> hasText(entry.getKey()) && hasText(entry.getValue()))
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private void cachePair(final String municipalityId, final String partyId, final String personNumber) {
		personNumbersByPartyId.put(new CacheKey(municipalityId, partyId), personNumber);
		partyIdsByPersonNumber.put(new CacheKey(municipalityId, personNumber), partyId);
	}
}
