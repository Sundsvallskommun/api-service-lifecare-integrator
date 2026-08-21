package se.sundsvall.lifecareintegrator.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.lifecareintegrator.api.model.common.Decision;
import se.sundsvall.lifecareintegrator.api.model.common.DecisionsResponse;
import se.sundsvall.lifecareintegrator.api.model.common.SourceStatus;
import se.sundsvall.lifecareintegrator.integration.lifecareec.LifecareEcIntegration;
import se.sundsvall.lifecareintegrator.integration.lifecarefc.LifecareFcIntegration;
import se.sundsvall.lifecareintegrator.integration.party.PartyIntegration;
import se.sundsvall.lifecareintegrator.service.mapper.DecisionMapper;

import static se.sundsvall.lifecareintegrator.api.model.common.SourceStatus.STATUS_OK;
import static se.sundsvall.lifecareintegrator.api.model.common.SourceStatus.STATUS_UNAVAILABLE;
import static se.sundsvall.lifecareintegrator.service.mapper.DecisionMapper.LAW_LSS;
import static se.sundsvall.lifecareintegrator.service.mapper.DecisionMapper.LAW_SOL;
import static se.sundsvall.lifecareintegrator.service.mapper.DecisionMapper.SOURCE_ELDERLY_CARE;
import static se.sundsvall.lifecareintegrator.service.mapper.DecisionMapper.SOURCE_FAMILY_CARE;
import static se.sundsvall.lifecareintegrator.util.LogSanitizer.describe;

/**
 * The unified decision read: gathers decisions from the three Lifecare decision sources (EC SoL, EC LSS and FC) and
 * merges them. A failing source does not fail the request — its decisions are omitted and the source is reported as
 * UNAVAILABLE in the response.
 */
@Service
public class DecisionService {

	private static final Logger LOG = LoggerFactory.getLogger(DecisionService.class);

	private final PartyIntegration partyIntegration;
	private final LifecareEcIntegration lifecareEcIntegration;
	private final LifecareFcIntegration lifecareFcIntegration;

	public DecisionService(
		final PartyIntegration partyIntegration,
		final LifecareEcIntegration lifecareEcIntegration,
		final LifecareFcIntegration lifecareFcIntegration) {
		this.partyIntegration = partyIntegration;
		this.lifecareEcIntegration = lifecareEcIntegration;
		this.lifecareFcIntegration = lifecareFcIntegration;
	}

	public DecisionsResponse getDecisions(final String municipalityId, final String partyId, final LocalDate from, final LocalDate to) {
		// FC requires a date window — fall back to a wide default. EC has no decision-date filter (its gt/lt track
		// modification time), so EC is fetched unfiltered and filtered on validity overlap below.
		final var window = DateWindow.of(from, to);

		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);

		final var results = List.of(
			fetchSource(SOURCE_ELDERLY_CARE, LAW_SOL,
				() -> lifecareEcIntegration.getSolDecisions(personNumber).stream()
					.map(DecisionMapper::toDecision)
					.filter(decision -> overlapsWindow(decision, from, to))
					.toList()),
			fetchSource(SOURCE_ELDERLY_CARE, LAW_LSS,
				() -> lifecareEcIntegration.getLssDecisions(personNumber).stream()
					.map(DecisionMapper::toDecision)
					.filter(decision -> overlapsWindow(decision, from, to))
					.toList()),
			fetchSource(SOURCE_FAMILY_CARE, null,
				() -> lifecareFcIntegration.getAllDecisions(personNumber, window.start(), window.end()).stream()
					.map(DecisionMapper::toDecision)
					.toList()));

		return DecisionsResponse.create()
			.withDecisions(results.stream()
				.flatMap(result -> result.decisions().stream())
				.sorted(Comparator.comparing(Decision::getDecided, Comparator.nullsLast(Comparator.reverseOrder())))
				.toList())
			.withSources(results.stream()
				.map(SourceResult::toSourceStatus)
				.toList());
	}

	private SourceResult fetchSource(final String source, final String law, final Supplier<List<Decision>> fetcher) {
		try {
			return new SourceResult(source, law, fetcher.get(), true);
		} catch (final Exception e) {
			// Described rather than logged as a throwable: a Feign failure carries the request URL in its message, and
			// that URL holds the API key and the personnummer. See LogSanitizer. The stack trace is dropped with it —
			// it is Feign plumbing, and the cause chain in the description is what identifies the failure.
			LOG.warn("Decision source {} ({}) is unavailable: {}", source, law, describe(e));
			return new SourceResult(source, law, List.of(), false);
		}
	}

	private static boolean overlapsWindow(final Decision decision, final LocalDate from, final LocalDate to) {
		if (from == null && to == null) {
			return true;
		}
		final var validFrom = decision.getValidFrom();
		final var validTo = decision.getValidTo();
		final var startsBeforeWindowEnd = to == null || validFrom == null || !validFrom.isAfter(to);
		final var endsAfterWindowStart = from == null || validTo == null || !validTo.isBefore(from);
		return startsBeforeWindowEnd && endsAfterWindowStart;
	}

	private record SourceResult(String source, String law, List<Decision> decisions, boolean ok) {

		private SourceResult {
			Objects.requireNonNull(decisions);
		}

		private SourceStatus toSourceStatus() {
			return SourceStatus.create()
				.withSource(source)
				.withLaw(law)
				.withStatus(status());
		}

		private String status() {
			if (ok) {
				return STATUS_OK;
			}
			return STATUS_UNAVAILABLE;
		}
	}
}
