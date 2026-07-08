package se.sundsvall.lifecareintegrator.integration.lifecareec;

import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1LssDecision;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

import static java.util.Collections.emptyList;

/**
 * Wrapper around {@link LifecareEcClient}. The EC list resources are queried per person via
 * {@code q=PersonId:&lt;personNumber&gt;}. The client is configured with {@code dismiss404 = true}, so a 404 surfaces
 * as {@code null} and is normalized to an empty
 * list here.
 */
@Component
public class LifecareEcIntegration {

	static final String PERSON_ID_QUERY = "PersonId:%s";
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
}
