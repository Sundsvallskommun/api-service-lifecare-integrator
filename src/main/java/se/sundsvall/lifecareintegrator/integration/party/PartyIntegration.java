package se.sundsvall.lifecareintegrator.integration.party;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.lifecareintegrator.integration.party.configuration.PartyProperties;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;

/**
 * Wrapper around {@link PartyClient} that resolves partyId(s) to person numbers. The person number is an internal
 * concern only — it is forwarded to the Lifecare APIs but never exposed in this service's public API.
 *
 * <p>
 * Every person-scoped operation starts with a partyId → person number lookup, and every read that names people in its
 * answer ends with the reverse, so one caseworker action touches the same person several times within seconds. Both
 * directions are therefore cached in memory, per municipality, for {@link PartyProperties#cacheTimeToLive()}: a hit in
 * either direction fills the other too, and only the misses go to Party — in one batch per call, capped by the
 * per-call limits. A partyId and its person number belong together for the life of the person, so a short time to
 * live is all the invalidation needed. A lookup Party cannot answer is never cached, so a person Party does not know
 * yet is asked for again next time. The cache lives in the heap only: nothing in it is logged or persisted.
 */
@Component
public class PartyIntegration {

	static final String PERSON_NUMBER_NOT_FOUND = "No person number found for partyId '%s'";
	static final String PERSON_NUMBERS_NOT_FOUND = "No person number found for partyIds %s";

	private final PartyClient partyClient;
	private final int maxPartyIdsPerCall;
	private final int maxLegalIdsPerCall;
	private final Cache<CacheKey, String> personNumbersByPartyId;
	private final Cache<CacheKey, String> partyIdsByPersonNumber;

	/** One identity in one municipality — a partyId or a person number, depending on the cache it keys. */
	private record CacheKey(String municipalityId, String id) {}

	@Autowired
	public PartyIntegration(final PartyClient partyClient, final PartyProperties properties) {
		this(partyClient, properties, Ticker.systemTicker());
	}

	PartyIntegration(final PartyClient partyClient, final PartyProperties properties, final Ticker ticker) {
		this.partyClient = partyClient;
		this.maxPartyIdsPerCall = max(1, properties.maxPartyIdsPerCall());
		this.maxLegalIdsPerCall = max(1, properties.maxLegalIdsPerCall());
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

	/**
	 * Resolve a single partyId to a person number.
	 *
	 * @param  municipalityId the municipality id
	 * @param  partyId        the partyId to resolve
	 * @return                the person number (throws NOT_FOUND problem when the partyId cannot be resolved)
	 */
	public String getPersonNumber(final String municipalityId, final String partyId) {
		return Optional.ofNullable(resolvePersonNumbers(municipalityId, List.of(partyId)).get(partyId))
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

		final var result = resolvePersonNumbers(municipalityId, partyIds);
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
		return resolve(municipalityId, personNumbers, partyIdsByPersonNumber, maxLegalIdsPerCall, partyClient::getPartyIds, false);
	}

	private Map<String, String> resolvePersonNumbers(final String municipalityId, final List<String> partyIds) {
		return resolve(municipalityId, partyIds, personNumbersByPartyId, maxPartyIdsPerCall, partyClient::getPersonNumbers, true);
	}

	/**
	 * The cached answers for {@code ids}, with the misses fetched from Party in batches of at most {@code maxPerCall}.
	 * Each answer is remembered in both directions: {@code forward} says whether {@code ids} are partyIds (so an answer
	 * is a person number) or person numbers (so an answer is a partyId).
	 */
	private Map<String, String> resolve(final String municipalityId, final List<String> ids, final Cache<CacheKey, String> cache, final int maxPerCall,
		final BiFunction<String, List<String>, Map<String, String>> fetch, final boolean forward) {

		final var result = new HashMap<String, String>();
		final var misses = new ArrayList<String>();
		for (final var id : new LinkedHashSet<>(ids)) {
			Optional.ofNullable(cache.getIfPresent(new CacheKey(municipalityId, id)))
				.ifPresentOrElse(answer -> result.put(id, answer), () -> misses.add(id));
		}

		for (var from = 0; from < misses.size(); from += maxPerCall) {
			final var batch = List.copyOf(misses.subList(from, min(from + maxPerCall, misses.size())));
			Optional.ofNullable(fetch.apply(municipalityId, batch)).orElse(emptyMap()).forEach((id, answer) -> {
				if (hasText(id) && hasText(answer)) {
					result.put(id, answer);
					remember(municipalityId, id, answer, forward);
				}
			});
		}
		return result;
	}

	private void remember(final String municipalityId, final String id, final String answer, final boolean forward) {
		if (forward) {
			personNumbersByPartyId.put(new CacheKey(municipalityId, id), answer);
			partyIdsByPersonNumber.put(new CacheKey(municipalityId, answer), id);
			return;
		}
		partyIdsByPersonNumber.put(new CacheKey(municipalityId, id), answer);
		personNumbersByPartyId.put(new CacheKey(municipalityId, answer), id);
	}
}
