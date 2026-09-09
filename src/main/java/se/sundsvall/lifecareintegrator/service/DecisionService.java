package se.sundsvall.lifecareintegrator.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.lifecareintegrator.api.model.common.Decision;
import se.sundsvall.lifecareintegrator.api.model.common.DecisionsResponse;
import se.sundsvall.lifecareintegrator.api.model.common.SourceStatus;
import se.sundsvall.lifecareintegrator.integration.employee.EmployeeIntegration;
import se.sundsvall.lifecareintegrator.integration.lifecareec.LifecareEcIntegration;
import se.sundsvall.lifecareintegrator.integration.lifecarefc.LifecareFcIntegration;
import se.sundsvall.lifecareintegrator.integration.party.PartyIntegration;
import se.sundsvall.lifecareintegrator.service.mapper.DecisionMapper;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
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
 *
 * <p>
 * The single-decision read addresses one source: a decision id is only unique within its source system, so the caller
 * names the source (and, for elderly care, the law) along with the id. EC serves a decision by id for any person, so
 * the decision is checked against the party before it is published; FC has no by-id read at all, so the party's
 * decisions are listed and the one with the id picked out. Either way a decision that does not belong to the party is
 * reported as not found — never as forbidden, which would confirm that the id exists.
 */
@Service
public class DecisionService {

	static final String LAW_REQUIRED = "'law' is required when 'source' is %s".formatted(SOURCE_ELDERLY_CARE);
	static final String LAW_NOT_APPLICABLE = "'law' is not applicable when 'source' is %s".formatted(SOURCE_FAMILY_CARE);
	static final String DECISION_NOT_FOUND = "No decision with id '%s' found in source %s for partyId '%s'";

	private static final Logger LOG = LoggerFactory.getLogger(DecisionService.class);

	private final PartyIntegration partyIntegration;
	private final LifecareEcIntegration lifecareEcIntegration;
	private final LifecareFcIntegration lifecareFcIntegration;
	private final EmployeeIntegration employeeIntegration;

	public DecisionService(
		final PartyIntegration partyIntegration,
		final LifecareEcIntegration lifecareEcIntegration,
		final LifecareFcIntegration lifecareFcIntegration,
		final EmployeeIntegration employeeIntegration) {
		this.partyIntegration = partyIntegration;
		this.lifecareEcIntegration = lifecareEcIntegration;
		this.lifecareFcIntegration = lifecareFcIntegration;
		this.employeeIntegration = employeeIntegration;
	}

	public DecisionsResponse getDecisions(final String municipalityId, final String partyId, final LocalDate from, final LocalDate to) {
		// FC requires a date window — fall back to a wide default. EC has no decision-date filter (its gt/lt track
		// modification time), so EC is fetched unfiltered and filtered on validity overlap below.
		final var window = DateWindow.of(from, to);

		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		final var caseworkerNames = caseworkerNameResolver(municipalityId);

		final var results = List.of(
			fetchSource(SOURCE_ELDERLY_CARE, LAW_SOL,
				() -> lifecareEcIntegration.getSolDecisions(personNumber).stream()
					.map(decision -> DecisionMapper.toDecision(decision, caseworkerNames))
					.filter(decision -> overlapsWindow(decision, from, to))
					.toList()),
			fetchSource(SOURCE_ELDERLY_CARE, LAW_LSS,
				() -> lifecareEcIntegration.getLssDecisions(personNumber).stream()
					.map(decision -> DecisionMapper.toDecision(decision, caseworkerNames))
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

	/**
	 * A single decision, addressed by (source, law, decisionId) as published on the decision list. Unlike the list, an
	 * unreachable source fails the request — there is no partial result to fall back on.
	 *
	 * @param law the law for an ELDERLY_CARE decision; SFB decisions are served by the LSS source, so LSS and SFB
	 *            address the same id space. Must be left out for FAMILY_CARE.
	 */
	public Decision getDecision(final String municipalityId, final String partyId, final String decisionId, final String source, final String law) {
		validateLaw(source, law);

		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);

		return findDecision(municipalityId, personNumber, decisionId, source, law)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, DECISION_NOT_FOUND.formatted(decisionId, describeSource(source, law), partyId)));
	}

	private Optional<Decision> findDecision(final String municipalityId, final String personNumber, final String decisionId, final String source, final String law) {
		if (SOURCE_FAMILY_CARE.equals(source)) {
			return findFamilyCareDecision(personNumber, decisionId);
		}

		final var caseworkerNames = caseworkerNameResolver(municipalityId);
		if (LAW_SOL.equals(law)) {
			return lifecareEcIntegration.getSolDecision(decisionId)
				.filter(decision -> personNumber.equals(decision.getPersonId()))
				.map(decision -> DecisionMapper.toDecision(decision, caseworkerNames));
		}
		return lifecareEcIntegration.getLssDecision(decisionId)
			.filter(decision -> personNumber.equals(decision.getPersonId()))
			.map(decision -> DecisionMapper.toDecision(decision, caseworkerNames));
	}

	/**
	 * FC has no by-id decision read, so the party's decisions are listed over the default window and the one with the
	 * id is picked out. The list is person-scoped by construction, so no ownership check is needed here.
	 */
	private Optional<Decision> findFamilyCareDecision(final String personNumber, final String decisionId) {
		final var window = DateWindow.of(null, null);

		return lifecareFcIntegration.getAllDecisions(personNumber, window.start(), window.end()).stream()
			.filter(decision -> decisionId.equals(String.valueOf(decision.getId())))
			.findFirst()
			.map(DecisionMapper::toDecision);
	}

	private static void validateLaw(final String source, final String law) {
		if (SOURCE_ELDERLY_CARE.equals(source) && law == null) {
			throw Problem.valueOf(BAD_REQUEST, LAW_REQUIRED);
		}
		if (SOURCE_FAMILY_CARE.equals(source) && law != null) {
			throw Problem.valueOf(BAD_REQUEST, LAW_NOT_APPLICABLE);
		}
	}

	private static String describeSource(final String source, final String law) {
		return Optional.ofNullable(law)
			.map(value -> "%s (%s)".formatted(source, value))
			.orElse(source);
	}

	/**
	 * A caseworker-id-to-name resolver for one request, memoizing every lookup — including the misses, which are the
	 * common case for ids that are not employees at all. Lifecare repeats the same handful of caseworkers across a
	 * person's decisions, so without this a name would be fetched once per decision. Confined to a single request
	 * thread, so the plain map is enough.
	 */
	private Function<String, Optional<String>> caseworkerNameResolver(final String municipalityId) {
		final Map<String, Optional<String>> resolved = new HashMap<>();

		return loginName -> resolved.computeIfAbsent(loginName, id -> employeeIntegration.getFullName(municipalityId, id));
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
