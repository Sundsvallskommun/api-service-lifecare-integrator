package se.sundsvall.lifecareintegrator.integration.lifecareec;

import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1LssDecision;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

import static java.util.Collections.emptyList;

/**
 * Wrapper around {@link LifecareEcClient}, querying the EC list resources per person and normalizing a dismissed 404
 * to an empty list, or to an empty {@link Optional} for the single-decision reads.
 *
 * <p>
 * EC's {@code q} grammar is {@code Field='Value'}: the single quotes are mandatory, and the unquoted form is rejected
 * with {@code 400 "Invalid query format"}.
 *
 * <p>
 * The single-decision reads are NOT scoped to a person — EC serves any decision by id. The caller must check the
 * returned decision's {@code PersonId} against the person it is acting for before publishing it.
 */
@Component
public class LifecareEcIntegration {

	static final String PERSON_ID_QUERY = "PersonId='%s'";
	static final int FETCH_LIMIT = 1000;

	private final LifecareEcClient lifecareEcClient;

	public LifecareEcIntegration(final LifecareEcClient lifecareEcClient) {
		this.lifecareEcClient = lifecareEcClient;
	}

	public List<WEECIntegrationContractsDecisionV1Decision> getSolDecisions(final String personNumber) {
		return Optional.ofNullable(lifecareEcClient.getSolDecisions(PERSON_ID_QUERY.formatted(personNumber), FETCH_LIMIT))
			.orElse(emptyList());
	}

	public List<WEECIntegrationContractsDecisionV1LssDecision> getLssDecisions(final String personNumber) {
		return Optional.ofNullable(lifecareEcClient.getLssDecisions(PERSON_ID_QUERY.formatted(personNumber), FETCH_LIMIT))
			.orElse(emptyList());
	}

	/** A single SoL decision by its Lifecare id, or empty when EC has no decision with that id. */
	public Optional<WEECIntegrationContractsDecisionV1Decision> getSolDecision(final String decisionId) {
		return Optional.ofNullable(lifecareEcClient.getSolDecision(decisionId));
	}

	/** A single LSS (or SFB) decision by its Lifecare id, or empty when EC has no decision with that id. */
	public Optional<WEECIntegrationContractsDecisionV1LssDecision> getLssDecision(final String decisionId) {
		return Optional.ofNullable(lifecareEcClient.getLssDecision(decisionId));
	}
}
