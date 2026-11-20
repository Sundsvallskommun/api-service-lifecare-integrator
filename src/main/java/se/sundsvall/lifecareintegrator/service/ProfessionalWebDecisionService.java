package se.sundsvall.lifecareintegrator.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebDecisionClient;

import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.requireJsonObject;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.toAnswer;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBusinessTypes.DECISION;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBusinessTypes.SERVICE;

@Service
public class ProfessionalWebDecisionService {

	/** Lifecare's web app always asks for a new proposal, never for a stored one. */
	private static final int NEW_PROPOSAL = 0;

	private final ProfessionalWebDecisionClient client;

	public ProfessionalWebDecisionService(final ProfessionalWebDecisionClient client) {
		this.client = client;
	}

	public byte[] getDecisionProposal(final long serviceId, final String amountType, final long calculationId) {
		return client.getProposalForService(SERVICE, serviceId, amountType, calculationId, NEW_PROPOSAL);
	}

	public byte[] getDecisionReasons(final long decisionCode) {
		return client.getMappedDecisionReasons(decisionCode);
	}

	public byte[] getDecision(final long decisionId) {
		return client.getDecision(DECISION, decisionId);
	}

	public Optional<byte[]> createDecision(final long serviceId, final byte[] body) {
		requireJsonObject(body);
		return toAnswer(client.create(SERVICE, serviceId, body));
	}

	public Optional<byte[]> updateDecision(final long decisionId, final byte[] body) {
		requireJsonObject(body);
		return toAnswer(client.update(DECISION, decisionId, body));
	}
}
